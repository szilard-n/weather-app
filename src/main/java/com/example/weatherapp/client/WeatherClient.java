package com.example.weatherapp.client;

import com.example.weatherapp.dto.client.GoWeatherForecast;
import com.example.weatherapp.dto.client.GoWeatherResponse;
import com.example.weatherapp.exception.CityNotFoundException;
import com.example.weatherapp.exception.ExternalServiceUnavailableException;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.List;

@Service
public class WeatherClient {

    private static final String WEATHER_PATH = "/weather/{city}";

    private WebClient webClient;

    @Value("${api.clients.go-weather}")
    private String baseUrl;

    @PostConstruct
    public void init() {
        webClient = WebClient
                .builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Fetches weather data for a given city using the web client.
     * If the response from the API indicates an empty forecast for the city,
     * this method will raise a {@link CityNotFoundException}.
     *
     * @param city the name of the city for which the weather data should be fetched.
     * @return a {@link Mono} emitting a tuple containing the city name and its corresponding {@link GoWeatherResponse}.
     */
    public Mono<Tuple2<String, GoWeatherResponse>> getWeatherData(String city) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder.path(WEATHER_PATH).build(city))
                .retrieve()
                .bodyToMono(GoWeatherResponse.class)
                .onErrorResume(e -> e instanceof WebClientResponseException.ServiceUnavailable,
                        ex -> Mono.error(new ExternalServiceUnavailableException("Go Weather API not available")))
                .flatMap(goWeatherResponse -> {
                    if (isEmptyResponse(goWeatherResponse.forecast())) {
                        return Mono.error(new CityNotFoundException("Provided city not found: " + city));
                    }
                    return Mono.just(Tuples.of(city, goWeatherResponse));
                });
    }

    /**
     * Checks if the provided forecast list is empty by examining its properties.
     * The external API (HerokuApp GoWeather) used for weather data returns empty string values for each
     * property in case a city was not found.
     * <p>
     * A forecast is considered empty if all its entries have blank temperature, wind, and a day value of 0.
     *
     * @param forecast a list of {@link GoWeatherForecast} representing the forecast data.
     * @return true if all forecast entries are empty, false otherwise.
     */
    private boolean isEmptyResponse(List<GoWeatherForecast> forecast) {
        return !forecast.isEmpty() && forecast.stream()
                .allMatch(dailyForecast ->
                        StringUtils.isBlank(dailyForecast.temperature()) &&
                                StringUtils.isBlank(dailyForecast.wind()) &&
                                dailyForecast.day() == 0
                );
    }

}
