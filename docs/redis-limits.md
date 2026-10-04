# Redis lifetime and memory limits

A cache entry can disappear for two reasons that do not depend on each other.

## 10 minutes after it was written

`application.properties` sets `spring.cache.redis.time-to-live=600000`. `@Cacheable` and `@CachePut` store that duration on the Redis key. Free memory does not keep the key past that time. A later read does not reset the timer. The next write does.

`CacheConfig` copies the property onto its `RedisCacheConfiguration` bean with `entryTtl`. That bean replaces Spring Boot's auto-configured `RedisCacheConfiguration`, so the property would otherwise be ignored.

## 64mb of Redis memory

The Redis service in `docker-compose.yml` starts with `--maxmemory 64mb` and `--maxmemory-policy allkeys-lru`. LRU means least recently used. Once Redis reaches 64mb it deletes the key that has gone the longest without a read or a write, even when that key's 10-minute TTL has not elapsed.

`allkeys-lru` ranks every key. `volatile-lru` ranks only keys that already have a TTL. `noeviction` is what Redis does when `maxmemory` is set and no policy is set: it rejects writes instead of deleting keys.

`@CacheEvict` and a cache clear still remove an entry immediately, before either limit fires.

## Check a live entry

After querying a city:

```sh
redis-cli TTL "weatherCache::Bangalore"
redis-cli CONFIG GET maxmemory
redis-cli CONFIG GET maxmemory-policy
```

`TTL` returns the remaining seconds. A fresh entry is near 600, because 600000 milliseconds is 600 seconds. `-1` means the key exists and has no expiry. `-2` means the key is gone. `maxmemory` is `67108864` (64mb). `maxmemory-policy` is `allkeys-lru`.

More Redis commands are in [Redis CLI](redis-cli.md). The same keys and remaining TTL can be browsed in [Redis Insight](redis-insight.md).
