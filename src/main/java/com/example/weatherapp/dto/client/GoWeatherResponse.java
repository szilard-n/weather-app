package com.example.weatherapp.dto.client;

import java.util.List;

public record GoWeatherResponse(List<GoWeatherForecast> forecast) {
}
