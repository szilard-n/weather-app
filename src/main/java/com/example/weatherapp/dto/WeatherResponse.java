package com.example.weatherapp.dto;

import java.util.List;

public record WeatherResponse(List<WeatherResult> result) {
}
