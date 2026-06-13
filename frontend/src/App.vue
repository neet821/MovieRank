<template>
  <main class="shell">
    <section v-if="!loggedIn" class="login-screen">
      <div class="login-panel">
        <p class="eyebrow">MovieRank</p>
        <h1>电影榜单系统</h1>
        <form autocomplete="off" @submit.prevent="login">
          <label>
            账号
            <input v-model="username" autocomplete="username" />
          </label>
          <label>
            密码
            <input v-model="password" type="password" autocomplete="off" />
          </label>
          <p v-if="loginMessage" class="error-text">{{ loginMessage }}</p>
          <button class="primary-button" type="submit">登录</button>
        </form>
      </div>
    </section>

    <template v-else>
      <header class="hero">
        <div>
          <p class="eyebrow">MovieRank</p>
          <h1>{{ selectedMovie ? '电影详情' : '五大电影榜单' }}</h1>
        </div>
        <button v-if="selectedMovie" class="ghost-button" @click="closeDetail">返回榜单</button>
      </header>

      <section v-if="!selectedMovie" class="section-stack">
        <section class="panel">
          <div class="panel-head">
            <div>
              <h2>豆瓣正在上映</h2>
              <p>{{ nowPlayingStatus }}</p>
            </div>
            <button class="ghost-button small" @click="loadNowPlaying">刷新</button>
          </div>
          <div class="now-grid">
            <a
              v-for="movie in nowPlaying"
              :key="movie.title"
              class="now-card"
              :href="movie.detailUrl"
              target="_blank"
              rel="noreferrer"
            >
              <img v-if="movie.posterUrl" :src="movie.posterUrl" :alt="movie.title" />
              <span v-else class="poster-empty">无海报</span>
              <strong>{{ movie.title }}</strong>
              <small>{{ movie.rating || '暂无评分' }}</small>
            </a>
          </div>
        </section>

        <section class="layout">
          <div class="panel rank-panel">
            <div class="toolbar in-panel">
              <button
                v-for="mode in modes"
                :key="mode.code"
                :class="['mode-button', { active: selectedMode === mode.code }]"
                @click="selectMode(mode.code)"
              >
                {{ mode.name }}
              </button>
            </div>
            <div class="panel-head">
              <div>
                <h2>{{ currentModeName }}</h2>
                <p>{{ rankingStatus }}</p>
              </div>
              <span class="pill">默认五榜各 20%</span>
            </div>

            <div class="rank-list compact-list">
              <button
                v-for="movie in combinedRanks"
                :key="movie.id"
                class="movie-row"
                @click="openDetail(movie.id)"
              >
                <span class="rank-number">{{ movie.finalRank }}</span>
                <span class="movie-main">
                  <strong>{{ movie.title }}</strong>
                  <small>{{ movie.year }} · {{ movie.presentSources.join(' / ') }}</small>
                </span>
                <span class="score">{{ movie.finalScore }}</span>
              </button>
            </div>
          </div>

          <aside class="panel compare-panel">
            <div class="panel-head compact">
              <div>
                <h2>横向对比</h2>
                <p>共同、独有和差距最大的电影</p>
              </div>
            </div>

            <div class="compare-selects">
              <select v-model="sourceA" @change="loadComparison">
                <option v-for="source in sources" :key="source.code" :value="source.code">
                  {{ source.name }}
                </option>
              </select>
              <select v-model="sourceB" @change="loadComparison">
                <option v-for="source in sources" :key="source.code" :value="source.code">
                  {{ source.name }}
                </option>
              </select>
            </div>

            <div v-if="comparison" class="compare-block">
              <div class="stat">
                <span>共同入榜数量</span>
                <strong>{{ comparison.sharedCount }}</strong>
              </div>
              <h3>共同入榜电影</h3>
              <ul>
                <li v-for="movie in comparison.sharedMovies.slice(0, 10)" :key="`shared-${movie.title}`">
                  <button class="mini-row" @click="openDetail(slugFrom(movie.title, movie.year))">
                    <span>{{ movie.title }}</span>
                    <span>#{{ movie.sourceARank }} / #{{ movie.sourceBRank }}</span>
                  </button>
                </li>
              </ul>
              <h3>排名差距最大</h3>
              <ul>
                <li v-for="movie in comparison.largestRankGaps.slice(0, 10)" :key="movie.title">
                  <button class="mini-row" @click="openDetail(slugFrom(movie.title, movie.year))">
                    <span>{{ movie.title }}</span>
                    <span>差 {{ movie.rankGap }}</span>
                  </button>
                </li>
              </ul>
              <h3>只在 {{ comparison.sourceA }} 出现</h3>
              <ul>
                <li v-for="movie in comparison.onlyInSourceA.slice(0, 10)" :key="movie.title">
                  <button class="mini-row" @click="openDetail(movie.id)">
                    <span>{{ movie.title }}</span>
                    <span>#{{ movie.sourceRank }}</span>
                  </button>
                </li>
              </ul>
              <h3>只在 {{ comparison.sourceB }} 出现</h3>
              <ul>
                <li v-for="movie in comparison.onlyInSourceB.slice(0, 10)" :key="movie.title">
                  <button class="mini-row" @click="openDetail(movie.id)">
                    <span>{{ movie.title }}</span>
                    <span>#{{ movie.sourceRank }}</span>
                  </button>
                </li>
              </ul>
            </div>
          </aside>
        </section>

        <section class="panel">
          <div class="panel-head">
            <div>
              <h2>五个榜单完整排名</h2>
              <p>{{ currentSourceName }} 已显示 {{ sourceRanks.length }} 部</p>
            </div>
            <select class="source-picker" v-model="selectedSource" @change="loadSourceRanks">
              <option v-for="source in sources" :key="source.code" :value="source.code">
                {{ source.name }}（{{ source.loadedSize }}）
              </option>
            </select>
          </div>
          <div class="source-table">
            <table>
              <thead>
                <tr>
                  <th>排名</th>
                  <th>电影</th>
                  <th>年份</th>
                  <th>榜单</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="movie in sourceRanks"
                  :key="`${movie.sourceName}-${movie.sourceRank}`"
                  class="source-row"
                  @click="openDetail(movie.id)"
                >
                  <td>{{ movie.sourceRank }}</td>
                  <td>{{ movie.title }}</td>
                  <td>{{ movie.year }}</td>
                  <td>{{ movie.sourceName }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </section>

      <section v-else class="panel detail-panel">
        <div class="poster">
          <img v-if="movieDetail.posterUrl" :src="movieDetail.posterUrl" :alt="movieDetail.title" />
          <span v-else>无海报</span>
        </div>
        <div class="detail-copy">
          <p class="eyebrow">电影详情</p>
          <h2>{{ movieDetail.title }}</h2>
          <p class="meta">{{ movieDetail.year }} · TMDB {{ movieDetail.tmdbRating || '暂无评分' }}</p>
          <p class="overview">{{ movieDetail.overview }}</p>
          <div class="source-grid">
            <div v-for="(rank, source) in movieDetail.sourceRanks" :key="source">
              <span>{{ source }}</span>
              <strong>#{{ rank }}</strong>
            </div>
          </div>
        </div>
      </section>
    </template>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'

const loggedIn = ref(localStorage.getItem('movierank-login') === 'yes')
const username = ref('admin')
const password = ref('')
const loginMessage = ref('')
const modes = ref([])
const sources = ref([])
const combinedRanks = ref([])
const sourceRanks = ref([])
const nowPlaying = ref([])
const comparison = ref(null)
const movieDetail = ref({})
const selectedMode = ref('BALANCED')
const selectedSource = ref('IMDB')
const selectedMovie = ref('')
const sourceA = ref('IMDB')
const sourceB = ref('DOUBAN')
const loading = ref(false)
const nowPlayingLoading = ref(false)

const currentModeName = computed(() => {
  return modes.value.find((mode) => mode.code === selectedMode.value)?.name || '均衡综合榜'
})

const currentSourceName = computed(() => {
  return sources.value.find((source) => source.code === selectedSource.value)?.name || 'IMDb'
})

const rankingStatus = computed(() => {
  if (loading.value) return '正在加载综合榜'
  return `已合并 ${combinedRanks.value.length} 部电影`
})

const nowPlayingStatus = computed(() => {
  if (nowPlayingLoading.value) return '正在爬取豆瓣正在上映'
  return nowPlaying.value.length ? `已加载 ${nowPlaying.value.length} 部` : '暂无数据'
})

async function api(path, options) {
  const response = await fetch(path, options)
  if (!response.ok) throw new Error(`请求失败：${response.status}`)
  return response.json()
}

async function login() {
  loginMessage.value = ''
  try {
    const result = await api('/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: username.value, password: password.value })
    })
    if (result.code === 200) {
      loggedIn.value = true
      localStorage.setItem('movierank-login', 'yes')
      await loadHome()
    } else {
      loginMessage.value = '账号或密码错误'
    }
  } catch {
    loginMessage.value = '登录请求失败，请确认后端已启动'
  }
}

async function loadBasics() {
  const [modeRows, sourceRows] = await Promise.all([
    api('/api/ranking-modes'),
    api('/api/ranking-sources')
  ])
  modes.value = modeRows
  sources.value = sourceRows
}

async function loadRanks() {
  loading.value = true
  try {
    combinedRanks.value = await api(`/api/rankings/combined?mode=${selectedMode.value}`)
  } finally {
    loading.value = false
  }
}

async function loadSourceRanks() {
  sourceRanks.value = await api(`/api/source-rankings?source=${selectedSource.value}`)
}

async function loadNowPlaying() {
  nowPlayingLoading.value = true
  try {
    nowPlaying.value = await api('/nowplaying')
  } finally {
    nowPlayingLoading.value = false
  }
}

async function loadComparison() {
  comparison.value = await api(`/api/rankings/compare?sourceA=${sourceA.value}&sourceB=${sourceB.value}`)
}

async function selectMode(code) {
  selectedMode.value = code
  await loadRanks()
}

async function openDetail(id) {
  selectedMovie.value = id
  movieDetail.value = await api(`/api/movies/${encodeURIComponent(id)}?mode=${selectedMode.value}`)
}

function slugFrom(title, year) {
  return `${title || ''}`
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^\p{L}\p{N}]+/gu, ' ')
    .trim()
    .toLowerCase()
    .replaceAll(' ', '-') + `-${year}`
}

function closeDetail() {
  selectedMovie.value = ''
  movieDetail.value = {}
}

async function loadHome() {
  await loadBasics()
  await Promise.all([loadRanks(), loadSourceRanks(), loadNowPlaying(), loadComparison()])
}

onMounted(async () => {
  if (loggedIn.value) {
    await loadHome()
  }
})
</script>
