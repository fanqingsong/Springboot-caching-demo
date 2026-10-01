package com.weather.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.weather.entity.Weather;
import com.weather.service.CacheInspectionService;
import com.weather.service.WeatherService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    private final WeatherService weatherService;
    private final CacheInspectionService cacheInspectionService;

    public WeatherController(WeatherService weatherService, CacheInspectionService cacheInspectionService) {
        this.weatherService = weatherService;
        this.cacheInspectionService = cacheInspectionService;
    }

    @GetMapping
    public List<Weather> getAllWeather(HttpServletResponse response) {
        Object cached = cacheInspectionService.getCacheEntry("weatherCacheAll", "SimpleKey []");
        response.setHeader("X-Cache", cached != null ? "HIT" : "MISS");
        return weatherService.getAllWeather();
    }

    @GetMapping("/{city}")
    public Optional<Weather> getWeatherByCity(@PathVariable String city, HttpServletResponse response) {
        Object cached = cacheInspectionService.getCacheEntry("weatherCache", city);
        response.setHeader("X-Cache", cached != null ? "HIT" : "MISS");
        return weatherService.getWeatherByCity(city);
    }

    @PostMapping
    public Weather addWeather(@RequestBody Weather weather) {
        return weatherService.addWeather(weather);
    }

    @PutMapping("/{city}")
    public Weather updateWeather(@PathVariable String city, @RequestBody Weather newWeather) {
        return weatherService.updateWeather(city, newWeather);
    }

    @DeleteMapping("/{city}")
    public String deleteWeather(@PathVariable String city) {
        weatherService.deleteWeather(city);
        return "Weather data for " + city + " deleted!";
    }
}