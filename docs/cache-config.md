# Cache configuration beans

`CacheConfig` registers two objects with Spring. Both are created once at startup. Neither runs on its own after that. Other components use them when a request reads or writes Redis.

## How a `@Bean` method runs

`CacheConfig` is a `@Configuration` class. On startup Spring finds it, calls each `@Bean` method once, and stores the return value in the application context. The default scope is singleton: one instance for the whole process.

Method parameters are other beans Spring already created. Spring passes them in, then calls the method:

| Parameter | Where it comes from |
|---|---|
| `CacheProperties` | `spring.cache.*` in `application.properties` |
| `RedisConnectionFactory` | `spring.data.redis.host` and `spring.data.redis.port` (localhost:6379) |

Incoming HTTP requests do not call these methods again. A request uses the objects that were created at startup.

## `redisCacheConfiguration`

This bean tells Spring Cache how to store entries in Redis. `WeatherService` methods marked with `@Cacheable`, `@CachePut`, and `@CacheEvict` do not talk to Redis themselves.

`@EnableCaching` and `spring.cache.type=redis` make Spring Boot build a `RedisCacheManager`. If the context contains a `RedisCacheConfiguration` bean, Boot uses that bean instead of its default configuration.

The bean sets three things:

- Keys are serialized as strings, for example `weatherCache::Beijing`.
- Values are serialized as Jackson JSON, so `Weather`, `Optional`, and `List` can be stored.
- When `spring.cache.redis.time-to-live=600000` is set, each entry expires after 10 minutes.

`RedisCacheConfiguration` is a settings object. It has no loop and no thread. The cache interceptor reads, writes, or deletes Redis when a cached method runs, using a cache that `RedisCacheManager` built from this configuration.

## `redisTemplate`

This bean is a Redis client for code that reads and writes Redis without cache annotations. Keys are strings and values are JSON, the same formats as the cache configuration.

`CacheInspectionService` asks for `RedisTemplate<String, Object>` in its constructor. Spring injects this bean. Calls such as `keys`, `get`, and `delete` run only when that service lists cache contents, reads one entry, or clears a cache by name.

## How the two beans divide the work

| Bean | Used by | When its methods run |
|---|---|---|
| `RedisCacheConfiguration` | `RedisCacheManager`, then the cache interceptor around `WeatherService` | On each `@Cacheable` / `@CachePut` / `@CacheEvict` call |
| `RedisTemplate` | `CacheInspectionService` | On cache inspection and manual eviction |

Matching serializers on both beans keeps inspection reads in the same format that the cache annotations write.
