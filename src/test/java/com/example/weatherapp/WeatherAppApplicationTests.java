package com.example.weatherapp;

import com.example.weatherapp.dto.WeatherResponse;
import com.example.weatherapp.dto.client.GoWeatherForecast;
import com.example.weatherapp.dto.client.GoWeatherResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class WeatherAppApplicationTests {

    private static ClientAndServer mockServer;
    private static final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    static void init() {
        mockServer = ClientAndServer.startClientAndServer(9999);
    }

    @BeforeEach
    void restState() {
        mockServer.reset();
    }

    @AfterAll
    static void stop() {
        mockServer.stop();
    }

    @Test
    void contextLoads() {
        Assertions.assertTrue(true);
    }

    @Test
    @DisplayName("Average wind and temperature data should be calculated to the given cities")
    void getAverageWindAndTemperatureData() throws JsonProcessingException {
        List<GoWeatherForecast> goWeatherForecasts = List.of(
                new GoWeatherForecast(1, "27 C", "6 km/h"),
                new GoWeatherForecast(2, "24 C", "10 km/h"),
                new GoWeatherForecast(3, "15 C", "20 km/h")
        );
        GoWeatherResponse goWeatherResponse = new GoWeatherResponse(goWeatherForecasts);
        String city = "Cluj-Napoca";

        mockServer.when(
                request()
                        .withMethod(HttpMethod.GET.name())
                        .withPath("/weather/" + city)
        ).respond(
                response()
                        .withStatusCode(200)
                        .withHeader(Header.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                        .withBody(mapper.writeValueAsString(goWeatherResponse))
        );

        var response = given()
                .contentType(ContentType.JSON)
                .get("/api/weather?city=" + city)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(WeatherResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.result()).hasSize(1);
        assertThat(response.result().get(0).name()).isEqualTo(city);
        assertThat(response.result().get(0).temperature()).isEqualTo(22);
        assertThat(response.result().get(0).wind()).isEqualTo(12);
    }

    @Test
    @DisplayName("Bad Request should be returned as city is not found")
    void getAverageWindAndTemperatureData_cityNotFound() throws JsonProcessingException {
        List<GoWeatherForecast> goWeatherForecasts = List.of(
                new GoWeatherForecast(0, "", ""),
                new GoWeatherForecast(0, "", ""),
                new GoWeatherForecast(0, "", "")
        );
        GoWeatherResponse goWeatherResponse = new GoWeatherResponse(goWeatherForecasts);
        String city = "asd";

        mockServer.when(
                request()
                        .withMethod(HttpMethod.GET.name())
                        .withPath("/weather/" + city)
        ).respond(
                response()
                        .withStatusCode(200)
                        .withHeader(Header.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                        .withBody(mapper.writeValueAsString(goWeatherResponse))
        );

        given()
                .contentType(ContentType.JSON)
                .get("/api/weather?city=" + city)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Service shold respond with service unavailable as external API is down")
    void getAverageWindAndTemperatureData_serviceUnavailable() {
        String city = "Bucharest";

        mockServer.when(
                request()
                        .withMethod(HttpMethod.GET.name())
                        .withPath("/weather/" + city)
        ).respond(
                response()
                        .withStatusCode(503)
        );

        given()
                .contentType(ContentType.JSON)
                .get("/api/weather?city=" + city)
                .then()
                .statusCode(HttpStatus.SERVICE_UNAVAILABLE.value());
    }

    @Test
    @DisplayName("Bad Gateway should be returned external API returned incorrect data")
    void getAverageWindAndTemperatureData_badGateway() throws JsonProcessingException {
        List<GoWeatherForecast> goWeatherForecasts = List.of(); // empty list should provoke the exception
        GoWeatherResponse goWeatherResponse = new GoWeatherResponse(goWeatherForecasts);
        String city = "Bucharest";

        mockServer.when(
                request()
                        .withMethod(HttpMethod.GET.name())
                        .withPath("/weather/" + city)
        ).respond(
                response()
                        .withStatusCode(200)
                        .withHeader(Header.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                        .withBody(mapper.writeValueAsString(goWeatherResponse))
        );

        given()
                .contentType(ContentType.JSON)
                .get("/api/weather?city=" + city)
                .then()
                .statusCode(HttpStatus.BAD_GATEWAY.value());
    }



}
