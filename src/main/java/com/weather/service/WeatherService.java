package com.weather.service;

import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.weather.entity.Weather;
import com.weather.repository.WeatherRepository;

import jakarta.transaction.Transactional;

@Service
public class WeatherService {

    private final WeatherRepository weatherRepository;

    public WeatherService(WeatherRepository weatherRepository) {
        this.weatherRepository = weatherRepository;
    }

    // Cache all weather data
    // @Cacheable：未指定 key 时，Spring 用 SimpleKeyGenerator 生成默认 key。
    // 无参方法（本方法）→ SimpleKey.EMPTY，Redis 里实际 key 为 weatherCacheAll::SimpleKey []。
    // SimpleKey：把方法参数包成缓存 key；0 个参数用 EMPTY，1 个参数直接用该参数，多个参数则组合成 SimpleKey(p1, p2, ...)。
    @Cacheable(value = "weatherCacheAll")
    public List<Weather> getAllWeather() {
        return weatherRepository.findAll();
    }

    // Cache weather data by city
    // @Cacheable：按 city 缓存单条天气。首次查询走 DB 并写入 Redis（key = weatherCache::<city>），
    // 之后相同 city 直接命中缓存，不再访问数据库；TTL 10 分钟（spring.cache.redis.time-to-live）。
    // Optional.empty 也会被缓存，避免对不存在的城市反复打库。写操作由 @CachePut/@CacheEvict 同步。
    @Cacheable(value = "weatherCache", key = "#city")
    public Optional<Weather> getWeatherByCity(String city) {
        return weatherRepository.findByCity(city);
    }

    // Add new weather data
    // @CachePut：方法始终执行（写入 DB），再用返回值覆盖缓存。key = weatherCache::<city>，保证新增后 getWeatherByCity 能立刻读到最新数据，而不是等 TTL 过期。
    @Transactional
    // @CachePut：方法始终执行（写入 DB），再用返回值覆盖缓存。
    // key = weatherCache::<city>，保证新增后 getWeatherByCity 能立刻读到最新数据，而不是等 TTL 过期。
    // 在 addWeather 执行之后：@CachePut 不会跳过方法，先 save 入库，再用返回值覆盖 Redis。
    @CachePut(value = "weatherCache", key = "#weather.city")
    public Weather addWeather(Weather weather) {
        return weatherRepository.save(weather);
    }

    // Update existing weather data
    @Transactional
    @CachePut(value = "weatherCache", key = "#newWeather.city")
    public Weather updateWeather(String city, Weather newWeather) {
        return weatherRepository.findByCity(city)
                .map(existingWeather -> {
                    existingWeather.setTemperature(newWeather.getTemperature());
                    existingWeather.setHumidity(newWeather.getHumidity());
                    existingWeather.setCondition(newWeather.getCondition());
                    return weatherRepository.save(existingWeather);
                })
                .orElseThrow(() -> new NoSuchElementException("City '" + city + "' not found"));
    }

    // Delete weather data by city
    @Transactional
    @CacheEvict(value = "weatherCache", key = "#city")
    public void deleteWeather(String city) {
        // findByCity 返回 Optional<Weather>。ifPresent：有值才删，城市不存在则什么都不做（不抛异常）。
        // weatherRepository::delete 是实例方法引用，等价于 lambda：weather -> weatherRepository.delete(weather)。
        // 即把 Optional 里的 Weather 实体交给 JPA delete，从数据库删掉该行。
        weatherRepository.findByCity(city).ifPresent(weatherRepository::delete);
    }

    // Clear all cached weather data
    // @CacheEvict(allEntries = true)：不按 key，清空指定 cache 的全部条目。
    // 同时清 weatherCache（按城市）和 weatherCacheAll（全量列表），避免两类缓存不一致。
    // 方法体可为空：驱逐由 Spring Cache 拦截器在方法调用时完成。
    @CacheEvict(value = {"weatherCache", "weatherCacheAll"}, allEntries = true)
    public void evictAllWeatherCache() {
    }
}
