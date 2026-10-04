# Weather App with Spring Boot Caching

A Spring Boot API that stores weather data in PostgreSQL and caches it in Redis. `@Cacheable`, `@CachePut`, and `@CacheEvict` decide what is cached. Redis is the store.

```sh
docker compose up --build
```

- Demo UI: [http://localhost:8080](http://localhost:8080)
- API / Swagger: [http://localhost:8777/swagger-ui.html](http://localhost:8777/swagger-ui.html)
- Redis Insight: [http://localhost:5540](http://localhost:5540)

## Docs

| Topic | Document |
|---|---|
| Request paths, components, and layout | [Architecture](docs/architecture.md) |
| What caching is for | [Caching](docs/caching.md) |
| `simple`, Caffeine, and Redis | [Cache providers](docs/cache-providers.md) |
| 10-minute TTL and 64mb LRU | [Redis lifetime and memory limits](docs/redis-limits.md) |
| `@Cacheable`, `@CachePut`, `@CacheEvict` | [Cache annotations](docs/annotations.md) |
| `CacheConfig` beans | [Cache configuration beans](docs/cache-config.md) |
| Bean, `@Component`, and lifecycle | [Bean lifecycle](docs/bean-lifecycle.md) |
| `@Component`, `@Service`, `@RestController`, `@Bean` | [Bean stereotypes](docs/bean-stereotypes.md) |
| Startup callback that seeds weather rows | [ApplicationRunner](docs/application-runner.md) |
| Weather and cache HTTP API | [API](docs/api.md) |
| Compose, properties, and a local run | [Running](docs/running.md) |
| Inspecting keys with `redis-cli` | [Redis CLI](docs/redis-cli.md) |
| Browsing keys in Redis Insight | [Redis Insight](docs/redis-insight.md) |
