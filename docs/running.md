# Running

## Docker Compose

PostgreSQL, Redis, Redis Insight, the Spring Boot API, and a frontend demo are defined in `docker-compose.yml`. Cache entries expire 10 minutes after they are written, and Redis evicts the least recently used key if it reaches 64mb. See [Redis lifetime and memory limits](redis-limits.md).

```sh
docker compose up --build
```

- Demo UI: [http://localhost:8080](http://localhost:8080)
- API / Swagger: [http://localhost:8777/swagger-ui.html](http://localhost:8777/swagger-ui.html)
- Redis Insight: [http://localhost:5540](http://localhost:5540). The `weather-cache` connection points at the Compose Redis service (`redis:6379`, no password). See [Redis Insight](redis-insight.md).

The UI calls the API through the frontend nginx proxy (`/api` → backend). Query a city once to see a cache miss (`X-Cache: MISS`, row loaded from PostgreSQL and stored in `weatherCache`). Query it again to see a hit. Update, delete, and evict refresh the Redis panel beside the form.

## Configuration injected by Compose

`src/main/resources/application.properties` is the preset configuration packaged in the jar. Spring Boot loads it from the classpath at startup. Environment variables on the `backend` service have higher precedence, so a matching key in `docker-compose.yml` replaces the file value. Keys with no environment variable keep the value from the file.

Turn a property name into an environment variable by uppercasing it and replacing `.` and `-` with `_`.

| `application.properties` | Environment variable | Value set in Compose |
|---|---|---|
| `spring.datasource.url` | `SPRING_DATASOURCE_URL` | `jdbc:postgresql://postgres:5432/WeatherDB` |
| `spring.datasource.username` | `SPRING_DATASOURCE_USERNAME` | `postgres` |
| `spring.datasource.password` | `SPRING_DATASOURCE_PASSWORD` | same as the properties file |
| `spring.data.redis.host` | `SPRING_DATA_REDIS_HOST` | `redis` |
| `spring.data.redis.port` | `SPRING_DATA_REDIS_PORT` | `6379` |

Inside the Compose network the database and Redis hostnames are the service names `postgres` and `redis`. The `localhost` values in `application.properties` apply only when the app runs on the host.

`spring.cache.redis.time-to-live=600000` already lives in `application.properties`, so Compose does not need to repeat it. To override a property, add it under `backend.environment` with the same naming rule. That TTL override would be:

```yaml
SPRING_CACHE_REDIS_TIME_TO_LIVE: "600000"
```

Recreate the container so the new variable is applied: `docker compose up -d --build backend`.

## Application properties

Preset values below live in `src/main/resources/application.properties`. Docker Compose overrides datasource and Redis host settings through environment variables; see [Configuration injected by Compose](#configuration-injected-by-compose).

```properties
spring.application.name=Weather-App
server.port=8777
logging.level.org.springframework=INFO
logging.level.org.springframework.cache=DEBUG
spring.datasource.url=jdbc:postgresql://localhost:5432/WeatherDB
spring.datasource.username=postgres
spring.datasource.password=sush123$$$
spring.cache.type=redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.cache.cache-names=weatherCache,weatherCacheAll
spring.cache.redis.time-to-live=600000
```

## Local processes

1. Start Redis (`redis-server`) and PostgreSQL.
2. Run the Spring Boot application (`mvn spring-boot:run`).
3. Open Swagger UI at [http://localhost:8777/swagger-ui.html](http://localhost:8777/swagger-ui.html).

A local Redis started with plain `redis-server` does not apply the Compose `--maxmemory` and `--maxmemory-policy` flags. Those limits come from the Redis service in `docker-compose.yml`.
