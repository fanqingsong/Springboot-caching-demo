package com.weather.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.weather.entity.Weather;
import com.weather.repository.WeatherRepository;

@Component
public class WeatherDataInitializer implements ApplicationRunner {

    private final WeatherRepository weatherRepository;

    public WeatherDataInitializer(WeatherRepository weatherRepository) {
        this.weatherRepository = weatherRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (weatherRepository.count() > 0) {
            return;
        }
        weatherRepository.save(weather("Bangalore", 28.5, 65, "Cloudy"));
        weatherRepository.save(weather("Mysore", 30.2, 60, "Sunny"));
        weatherRepository.save(weather("Hubli", 32.8, 55, "Partly Cloudy"));
    }

    private Weather weather(String city, double temperature, int humidity, String condition) {
        Weather weather = new Weather();
        weather.setCity(city);
        weather.setTemperature(temperature);
        weather.setHumidity(humidity);
        weather.setCondition(condition);
        return weather;
    }
}
