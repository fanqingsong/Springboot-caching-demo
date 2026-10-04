# Redis Insight

[Redis Insight](https://redis.io/insight/) is Redis's graphical client. Compose starts it next to Redis so cache keys can be browsed in a browser instead of `redis-cli`.

Open [http://localhost:5540](http://localhost:5540) after `docker compose up`.

The `weather-cache` database is already configured. It connects to the Compose Redis service at `redis:6379` with no password. Settings live in the `redisinsight` volume, mounted at `/data` inside the container.

| Setting | Value |
|---|---|
| Image | `redis/redisinsight` (Huawei mirror prefix, same as the other images) |
| Host port | `5540` |
| Alias | `weather-cache` |
| Host / port | `redis` / `6379` |

Environment variables `RI_REDIS_HOST`, `RI_REDIS_PORT`, and `RI_REDIS_ALIAS` on the `redisinsight` service create that connection. Changing them takes effect after the container is recreated. Starting the container without those variables drops connections that were added only through them.

## What to look at

This application writes string keys. Query a city from the demo UI or the API, then open **Browser** in Redis Insight.

| Key | Meaning |
|---|---|
| `weatherCache::Bangalore` | One city, cached by `GET /weather/{city}` |
| `weatherCacheAll::SimpleKey []` | The full list from `GET /weather` |

Each value is the cached weather payload. **TTL** on a key is the remaining lifetime. A fresh entry is near 600 seconds. See [Redis lifetime and memory limits](redis-limits.md).

The same keys can be read from the shell. See [Redis CLI](redis-cli.md).
