async function loadNowPlaying() {
  const grid = document.querySelector("#nowplayingGrid");
  const status = document.querySelector("#nowplayingStatus");
  try {
    const response = await fetch("/nowplaying");
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }
    const movies = await response.json();
    status.textContent = `已加载 ${movies.length} 部正在上映电影（武汉）`;
    grid.innerHTML = movies
      .map((item) => {
        const ratingText = escapeHtml(item.rating || "暂无评分");
        const isNa = !item.rating || item.rating === "暂无评分";
        const ratingClass = isNa ? "movie-rating na" : "movie-rating";
        const poster = item.posterUrl
          ? `<img src="${escapeHtml(item.posterUrl)}" alt="${escapeHtml(item.title)}" loading="lazy">`
          : `<div style="aspect-ratio:3/4;background:#e5e7eb;display:flex;align-items:center;justify-content:center;color:#9ca3af;font-size:13px">暂无海报</div>`;
        const detailLink = item.detailUrl
          ? `<a href="${escapeHtml(item.detailUrl)}" target="_blank" rel="noreferrer">${escapeHtml(item.title)}</a>`
          : escapeHtml(item.title);
        return `
                <div class="movie-card">
                    ${poster}
                    <div class="movie-card-body">
                        <h3>${detailLink}</h3>
                        <span class="${ratingClass}">${ratingText}</span>
                    </div>
                </div>
            `;
      })
      .join("");
  } catch (error) {
    status.textContent = "豆瓣正在上映加载失败";
    grid.innerHTML = `<p>豆瓣正在上映加载失败：${error.message}</p>`;
  }
}

async function loadTspdtTop1000() {
  const tbody = document.querySelector("#tspdtBody");
  const status = document.querySelector("#tspdtStatus");
  try {
    const response = await fetch("/tspdt/top1000");
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }
    const movies = await response.json();
    status.textContent = `已加载 ${movies.length} 部电影`;
    tbody.innerHTML = movies
      .map(
        (item) => `
            <tr>
                <td>${item.position}</td>
                <td>${item.previousRank || "-"}</td>
                <td>${escapeHtml(item.title)}</td>
                <td>${escapeHtml(item.director)}</td>
                <td>${escapeHtml(item.year)}</td>
                <td>${escapeHtml(item.country)}</td>
                <td>${item.minutes ? `${item.minutes} 分钟` : "-"}</td>
            </tr>
        `,
      )
      .join("");
  } catch (error) {
    status.textContent = "TSPDT Top 1000 加载失败";
    tbody.innerHTML = `<tr><td colspan="7">TSPDT Top 1000 加载失败：${error.message}</td></tr>`;
  }
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}
loadNowPlaying();
loadTspdtTop1000();
