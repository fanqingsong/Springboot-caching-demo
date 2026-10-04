# Cache annotations

These annotations mark methods. They do not choose the store. This application stores the results in Redis; see [Cache providers](cache-providers.md).

## `@EnableCaching`

Turns on Spring's cache support. In this project it is on the application class.

```java
@Configuration
@EnableCaching
public class CacheConfig {
}
```

## `@Cacheable`

Runs the method on a miss and stores the return value. A later call with the same key returns the stored value and skips the method.

```java
@Cacheable(value = "weatherCache", key = "#city")
public Weather getWeather(String city) {
    return weatherRepository.findByCity(city);
}
```

## `@CachePut`

Always runs the method and stores the return value under the key, replacing the previous entry.

```java
@CachePut(value = "weatherCache", key = "#city")
public Weather updateWeather(String city, Weather weather) {
    return weatherRepository.save(weather);
}
```

## `@CacheEvict`

Removes the cache entry. The method still runs.

```java
@CacheEvict(value = "weatherCache", key = "#city")
public void deleteWeather(String city) {
    weatherRepository.deleteByCity(city);
}
```

An entry can also disappear because of its TTL or because Redis runs out of its memory ceiling. Those limits are in [Redis lifetime and memory limits](redis-limits.md).
