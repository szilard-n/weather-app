package com.example.weatherapp.dto;

public record WeatherResult(
        String name,
        double temperature,
        double wind) {
}
