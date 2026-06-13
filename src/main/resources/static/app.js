const AUTH_BASE = window.location.origin;
const API_BASE = `${AUTH_BASE}/movie-rank-api`;

let accessToken = localStorage.getItem("token") || "";
let refreshToken = localStorage.getItem("refresh_token") || "";

async function ensureBlueAlbumSession() {
    const gate = document.querySelector("#authGate");
    const root = document.querySelector("#appRoot");

    if (!accessToken) {
        showLoginPanel();
        return null;
    }

    gate.hidden = false;
    document.querySelector("#loginPanel").hidden = true;

    try {
        const user = await requestBlueAlbumUser(accessToken);
        showAppForUser(user);
        gate.hidden = true;
        root.hidden = false;
        return user;
    } catch (error) {
        if (refreshToken) {
            try {
                accessToken = await refreshBlueAlbumToken(refreshToken);
                const user = await requestBlueAlbumUser(accessToken);
                showAppForUser(user);
                gate.hidden = true;
                root.hidden = false;
                return user;
            } catch {
                clearAuthStorage();
            }
        }

        gate.innerHTML = `
            <div class="gate-mark is-blocked"></div>
            <p>需要先登录 Blue Album</p>
        `;
        showLoginPanel();
        return null;
    }
}

function showLoginPanel() {
    document.querySelector("#authGate").hidden = true;
    document.querySelector("#appRoot").hidden = true;
    document.querySelector("#loginPanel").hidden = false;
    document.querySelector("#username").focus();
}

function hideLoginPanel() {
    document.querySelector("#authGate").hidden = true;
    document.querySelector("#loginPanel").hidden = true;
    document.querySelector("#appRoot").hidden = false;
}

async function requestBlueAlbumUser(token) {
    for (const path of ["/api/auth/me", "/api/users/me"]) {
        const response = await fetch(`${AUTH_BASE}${path}`, {
            headers: {Authorization: `Bearer ${token}`}
        });
        if (response.ok) {
            return response.json();
        }
        if (response.status !== 404) {
            throw new Error(`HTTP ${response.status}`);
        }
    }
    throw new Error("Blue Album user endpoint not found");
}

async function refreshBlueAlbumToken(token) {
    const response = await fetch(`${AUTH_BASE}/api/auth/refresh`, {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({refresh_token: token})
    });
    if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
    }

    const payload = await response.json();
    localStorage.setItem("token", payload.access_token);
    if (payload.refresh_token) {
        localStorage.setItem("refresh_token", payload.refresh_token);
        refreshToken = payload.refresh_token;
    }
    if (payload.user) {
        localStorage.setItem("user", JSON.stringify(payload.user));
    }
    return payload.access_token;
}

async function submitBlueAlbumLogin(event) {
    event.preventDefault();
    const errorBox = document.querySelector("#loginError");
    const button = document.querySelector("#loginButton");
    const username = document.querySelector("#username").value.trim();
    const password = document.querySelector("#password").value;

    errorBox.textContent = "";
    button.disabled = true;
    button.textContent = "登录中...";

    try {
        const formData = new FormData();
        formData.append("username", username);
        formData.append("password", password);

        const response = await fetch(`${AUTH_BASE}/api/auth/login`, {
            method: "POST",
            body: formData
        });
        if (!response.ok) {
            throw new Error("账号或密码不正确");
        }

        const payload = await response.json();
        accessToken = payload.access_token;
        refreshToken = payload.refresh_token || "";
        localStorage.setItem("token", accessToken);
        if (refreshToken) {
            localStorage.setItem("refresh_token", refreshToken);
        } else {
            localStorage.removeItem("refresh_token");
        }
        if (payload.user) {
            localStorage.setItem("user", JSON.stringify(payload.user));
        }

        const user = payload.user || await requestBlueAlbumUser(accessToken);
        showAppForUser(user);
        hideLoginPanel();
        loadNowPlaying();
        loadMovieRanks();
        loadTspdtTop1000();
    } catch (error) {
        errorBox.textContent = error.message || "登录失败，请稍后再试";
    } finally {
        button.disabled = false;
        button.textContent = "登录";
    }
}

function showAppForUser(user) {
    const sessionUser = document.querySelector("#sessionUser");
    const name = user?.username || "Blue Album 用户";
    sessionUser.textContent = `${name} 已登录`;
}

function clearAuthStorage() {
    localStorage.removeItem("token");
    localStorage.removeItem("refresh_token");
    localStorage.removeItem("user");
    accessToken = "";
    refreshToken = "";
}

async function apiFetch(path) {
    const response = await fetch(`${API_BASE}${path}`, {
        headers: {Authorization: `Bearer ${accessToken}`}
    });
    if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
    }
    return response.json();
}

async function loadNowPlaying() {
    const grid = document.querySelector("#nowplayingGrid");
    const status = document.querySelector("#nowplayingStatus");
    try {
        const movies = await apiFetch("/nowplaying");
        status.textContent = `已加载 ${movies.length} 部`;
        grid.innerHTML = movies.map((item) => {
            const ratingText = escapeHtml(item.rating || "暂无评分");
            const posterImage = item.posterUrl
                ? `<img src="${escapeHtml(item.posterUrl)}" alt="${escapeHtml(item.title)}" loading="lazy">`
                : `<div class="poster-empty">暂无海报</div>`;
            const metaItems = [
                item.releaseDate ? `上映 ${escapeHtml(item.releaseDate)}` : "",
                item.tmdbRating ? `TMDB ${formatScore(item.tmdbRating)}` : "",
                item.popularity ? `热度 ${formatScore(item.popularity)}` : ""
            ].filter(Boolean);

            return `
                <article class="movie-card">
                    ${posterImage}
                    <div class="movie-card-body">
                        <h3>${escapeHtml(item.title)}</h3>
                        <div class="movie-meta">${metaItems.join("<span></span>") || "暂无更多资料"}</div>
                        <p>${escapeHtml(item.overview || "暂无简介")}</p>
                        <span class="movie-rating">${ratingText}</span>
                    </div>
                </article>
            `;
        }).join("");
    } catch (error) {
        status.textContent = "加载失败";
        grid.innerHTML = `<p class="empty-state">正在上映加载失败：${escapeHtml(error.message)}</p>`;
    }
}

async function loadMovieRanks() {
    const tbody = document.querySelector("#rankBody");
    try {
        const ranks = await apiFetch("/movie-ranks");
        tbody.innerHTML = ranks.map((item) => `
            <tr>
                <td>${item.finalRank}</td>
                <td>${escapeHtml(item.title)}</td>
                <td>${escapeHtml(item.year)}</td>
                <td>${formatScore(item.doubanScore)}</td>
                <td>${formatScore(item.imdbScore)}</td>
                <td>${formatScore(item.maoyanScore)}</td>
                <td><strong>${formatScore(item.finalScore)}</strong></td>
                <td>${item.missingSources?.length ? escapeHtml(item.missingSources.join(", ")) : "无"}</td>
            </tr>
        `).join("");
    } catch (error) {
        tbody.innerHTML = `<tr><td colspan="8">电影排名加载失败：${escapeHtml(error.message)}</td></tr>`;
    }
}

async function loadTspdtTop1000() {
    const tbody = document.querySelector("#tspdtBody");
    const status = document.querySelector("#tspdtStatus");
    try {
        const movies = await apiFetch("/tspdt/top1000");
        status.textContent = `已加载 ${movies.length} 部`;
        tbody.innerHTML = movies.map((item) => `
            <tr>
                <td>${item.position}</td>
                <td>${item.previousRank || "-"}</td>
                <td>${escapeHtml(item.title)}</td>
                <td>${escapeHtml(item.director)}</td>
                <td>${escapeHtml(item.year)}</td>
                <td>${escapeHtml(item.country)}</td>
                <td>${item.minutes ? `${item.minutes} 分钟` : "-"}</td>
            </tr>
        `).join("");
    } catch (error) {
        status.textContent = "加载失败";
        tbody.innerHTML = `<tr><td colspan="7">TSPDT Top 1000 加载失败：${escapeHtml(error.message)}</td></tr>`;
    }
}

function formatScore(value) {
    return Number(value || 0).toFixed(2).replace(/\.00$/, "");
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

document.querySelector("#loginForm").addEventListener("submit", submitBlueAlbumLogin);

ensureBlueAlbumSession().then((user) => {
    if (!user) {
        return;
    }
    loadNowPlaying();
    loadMovieRanks();
    loadTspdtTop1000();
});
