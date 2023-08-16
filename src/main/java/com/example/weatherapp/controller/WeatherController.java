package com.example.weatherapp.controller;

import com.example.weatherapp.dto.WeatherResponse;
import com.example.weatherapp.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping
    public Mono<WeatherResponse> getAverageWeatherValues(@RequestParam("city") String city) {
        return weatherService.getAverageWeatherValues(city.split(","));
    }
}
