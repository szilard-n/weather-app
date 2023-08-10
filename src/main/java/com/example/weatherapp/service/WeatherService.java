package com.example.weatherapp.service;

import com.example.weatherapp.client.WeatherClient;
import com.example.weatherapp.dto.WeatherResponse;
import com.example.weatherapp.dto.WeatherResult;
import com.example.weatherapp.dto.client.GoWeatherForecast;
import com.example.weatherapp.exception.ValueCalculationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherClient weatherClient;
    private final CsvService csvService;

    /**
     * Fetches and calculates the average weather values for the provided cities.
     * The results are sorted by city name, temperature, and wind. After gathering
     * and sorting the results, it writes the data to a CSV file.
     *
     * @param cities An array of city names to fetch weather data for.
     * @return A {@link Mono} containing the average weather response for the given cities.
     */
    public Mono<WeatherResponse> getAverageWeatherValues(String[] cities) {
        return Flux.fromArray(cities)
                .flatMap(weatherClient::getWeatherData)
                .map(cityWeatherTuple -> calculateAverageValues(cityWeatherTuple.getT1(), cityWeatherTuple.getT2().forecast()))
                .sort(Comparator.comparing(WeatherResult::name)
                        .thenComparing(WeatherResult::temperature)
                        .thenComparing(WeatherResult::wind)
                )
                .collectList()
                .map(WeatherResponse::new)
                .flatMap(response ->
                        csvService.writeWeatherData(response.result())
                                .thenReturn(response)
                );
    }

    /**
     * Calculates the average temperature and wind values for a given city based
     * on its weather forecast data.
     *
     * @param city The name of the city.
     * @param forecast The list of {@link GoWeatherForecast} data for the city.
     * @return A {@link WeatherResult} object containing the average weather values for the city.
     * @throws RuntimeException if the average temperature or wind cannot be calculated.
     */
    private WeatherResult calculateAverageValues(String city, List<GoWeatherForecast> forecast) {
        double averageTemperature = forecast.stream()
                .mapToDouble(dailyForecast -> extractDouble(dailyForecast.temperature()))
                .average()
                .orElseThrow(() -> new ValueCalculationException("Average temperature could not be calculated"));

        double averageWind = forecast.stream()
                .mapToDouble(dailyForecast -> extractDouble(dailyForecast.wind()))
                .average()
                .orElseThrow(() -> new ValueCalculationException("Average wind could not be calculated"));

        return new WeatherResult(city, averageTemperature, averageWind);
    }

    /**
     * Extracts and returns the numeric value from the provided string.
     * Any non-numeric characters in the string are ignored.
     * <p>
     * Temperature and wind values as fetched as strings from the Go Weather API.
     * Ex: "27 °C", "20 KM/H".
     *
     * @param value The string containing a numeric value.
     * @return The extracted double value, or 0.0 if the string does not contain a valid number.
     */
    private double extractDouble(String value) {
        String numberStr = value.replaceAll("[^0-9.]", ""); // Remove non-numeric characters
        try {
            return Double.parseDouble(numberStr);
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }
}
