# Cache providers

`@Cacheable`, `@CachePut`, and `@CacheEvict` do not store data. They tell Spring which cache name and key to use. The `CacheManager` chosen by `spring.cache.type` is the store.

This application sets `spring.cache.type=redis`, so those annotations read and write Redis. Caffeine and the `ConcurrentHashMap` fallback are not used.

The providers below are alternatives. Spring Boot enables one `CacheManager`. Having both Caffeine and Redis on the classpath does not build a two-level cache. A local cache in front of Redis has to be wired explicitly.

## Unset `spring.cache.type`

When the property is absent and no `CacheManager` bean is defined, Spring Boot 3.4 picks the first provider it finds on the classpath, in this order:

1. Generic
2. JCache (JSR-107)
3. EhCache 2.x
4. Hazelcast
5. Infinispan
6. Couchbase
7. Redis
8. Caffeine
9. Cache2k
10. Simple

Simple is the fallback: `ConcurrentMapCacheManager`, backed by `ConcurrentHashMap`. It is selected only when none of the providers above is present.

This project depends on `spring-boot-starter-data-redis`. Removing `spring.cache.type=redis` would still select Redis, because Redis appears in that list before Simple.

## `simple`, Caffeine, and Redis

| | `simple` | `caffeine` | `redis` |
|---|---|---|---|
| When it is used | No other cache library is on the classpath, or `spring.cache.type=simple` | `spring.cache.type=caffeine`, which requires the Caffeine dependency | `spring.cache.type=redis`, which requires Spring Data Redis. This is the provider the application runs with. |
| Implementation | `ConcurrentMapCacheManager` / `ConcurrentHashMap` | `CaffeineCacheManager` | `RedisCacheManager` |
| Where entries live | JVM heap of this process | JVM heap of this process | A separate Redis process |
| Shared across app instances | No | No | Yes |
| App restart | Drops the cache | Drops the cache | Keeps the cache while Redis stays up |
| Size limit | None. The map grows until the process runs out of heap. | `maximumSize` or `maximumWeight` evicts entries | Compose starts Redis with `--maxmemory 64mb` and `--maxmemory-policy allkeys-lru` |
| Expiry | None. An entry stays until `@CacheEvict`, a cache clear, or process exit. | `expireAfterWrite`, `expireAfterAccess` | `spring.cache.redis.time-to-live=600000` (10 minutes after write). `allkeys-lru` can still evict the key earlier |

```properties
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=500,expireAfterWrite=10m
```

```properties
spring.cache.type=redis
spring.cache.redis.time-to-live=600000
```

`spring.cache.caffeine.spec` applies only to Caffeine. `spring.cache.redis.time-to-live` applies only to Redis. The value is milliseconds.

How this application configures the Redis row is in [Redis lifetime and memory limits](redis-limits.md).
