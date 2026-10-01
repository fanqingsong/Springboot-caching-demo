const logEl = document.querySelector("#log");
const queryResult = document.querySelector("#query-result");

function log(message) {
  const item = document.createElement("li");
  item.textContent = `${new Date().toLocaleTimeString()}  ${message}`;
  logEl.prepend(item);
}

async function api(path, options = {}) {
  const started = performance.now();
  const response = await fetch(`/api${path}`, {
    headers: { "Content-Type": "application/json", ...(options.headers || {}) },
    ...options,
  });
  const elapsed = Math.round(performance.now() - started);
  const text = await response.text();
  let body = text;
  if (text) {
    try { body = JSON.parse(text); } catch { body = text; }
  } else {
    body = null;
  }
  if (!response.ok) {
    throw new Error(`${response.status} ${text}`);
  }
  return { body, elapsed, cache: response.headers.get("X-Cache") };
}

function showQuery(city, result) {
  queryResult.classList.remove("empty");
  queryResult.replaceChildren();
  if (result.cache) {
    const badge = document.createElement("div");
    badge.className = `badge ${result.cache === "HIT" ? "hit" : "miss"}`;
    badge.textContent = result.cache === "HIT" ? "命中缓存" : "未命中，已读数据库";
    queryResult.append(badge);
  }
  const body = document.createElement("div");
  body.textContent = `${city} · ${result.elapsed} ms\n${result.body ? JSON.stringify(result.body, null, 2) : "没有这条城市记录"}`;
  queryResult.append(body);
}

async function refreshCaches() {
  const [city, all] = await Promise.all([
    api("/cache/contents/weatherCache"),
    api("/cache/contents/weatherCacheAll"),
  ]);
  document.querySelector("#cache-city").textContent = JSON.stringify(city.body, null, 2);
  document.querySelector("#cache-all").textContent = JSON.stringify(all.body, null, 2);
}

async function queryCity(city) {
  const result = await api(`/weather/${encodeURIComponent(city)}`);
  showQuery(city, result);
  const verdict = result.cache === "HIT" ? "命中 weatherCache" : "未命中，回源 PostgreSQL 并写入 weatherCache";
  log(`GET /weather/${city} → ${verdict}（${result.elapsed} ms）`);
  await refreshCaches();
}

document.querySelector("#query-form").addEventListener("submit", async (event) => {
  event.preventDefault();
  await queryCity(document.querySelector("#city").value.trim());
});

document.querySelector("#query-again").addEventListener("click", async () => {
  await queryCity(document.querySelector("#city").value.trim());
});

function weatherPayload() {
  return {
    city: document.querySelector("#write-city").value.trim(),
    temperature: Number(document.querySelector("#temperature").value),
    humidity: Number(document.querySelector("#humidity").value),
    condition: document.querySelector("#condition").value.trim(),
  };
}

document.querySelector("#add-btn").addEventListener("click", async () => {
  const weather = weatherPayload();
  const result = await api("/weather", { method: "POST", body: JSON.stringify(weather) });
  log(`POST /weather ${weather.city} → @CachePut 写入 weatherCache（${result.elapsed} ms）`);
  await refreshCaches();
});

document.querySelector("#update-btn").addEventListener("click", async () => {
  const weather = weatherPayload();
  const result = await api(`/weather/${encodeURIComponent(weather.city)}`, {
    method: "PUT",
    body: JSON.stringify(weather),
  });
  log(`PUT /weather/${weather.city} → @CachePut 刷新 weatherCache（${result.elapsed} ms）`);
  await refreshCaches();
});

document.querySelector("#delete-btn").addEventListener("click", async () => {
  const city = document.querySelector("#write-city").value.trim();
  const result = await api(`/weather/${encodeURIComponent(city)}`, { method: "DELETE" });
  log(`DELETE /weather/${city} → @CacheEvict 移除 weatherCache::${city}（${result.elapsed} ms）`);
  await refreshCaches();
});

document.querySelector("#list-btn").addEventListener("click", async () => {
  const result = await api("/weather");
  const verdict = result.cache === "HIT" ? "命中 weatherCacheAll" : "未命中，回源数据库并写入 weatherCacheAll";
  log(`GET /weather → ${verdict}（${result.elapsed} ms），${Array.isArray(result.body) ? result.body.length : 0} 条`);
  queryResult.classList.remove("empty");
  queryResult.replaceChildren();
  const badge = document.createElement("div");
  badge.className = `badge ${result.cache === "HIT" ? "hit" : "miss"}`;
  badge.textContent = verdict;
  const body = document.createElement("div");
  body.textContent = `${result.elapsed} ms\n${JSON.stringify(result.body, null, 2)}`;
  queryResult.append(badge, body);
  await refreshCaches();
});

async function evict(name) {
  const result = await api(`/cache/evict/${name}`, { method: "DELETE" });
  log(`DELETE /cache/evict/${name} → ${result.body}`);
  await refreshCaches();
}

document.querySelector("#evict-btn").addEventListener("click", () => evict("weatherCache"));
document.querySelector("#evict-all-btn").addEventListener("click", () => evict("weatherCacheAll"));
document.querySelector("#refresh-cache").addEventListener("click", refreshCaches);

refreshCaches().catch((error) => {
  log(`后端尚未就绪：${error.message}`);
  document.querySelector("#cache-city").textContent = "等待后端…";
  document.querySelector("#cache-all").textContent = "等待后端…";
});
