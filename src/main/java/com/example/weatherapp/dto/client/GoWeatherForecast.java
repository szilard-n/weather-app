package com.example.weatherapp.dto.client;

public record GoWeatherForecast(
        int day,
        String temperature,
        String wind) {
}
