package com.example.weatherapp.service;

import com.example.weatherapp.dto.WeatherResult;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


@Service
@Slf4j
public class CsvService {

    private static final String FILE_NAME = "weather-data.csv";

    /**
     * Writes a list of {@link WeatherResult} to a CSV file.
     * <p>
     * This method takes a list of weather results, extracts their details, and writes
     * them to a predefined CSV file. The writing process is performed on a bounded elastic scheduler
     * to avoid blocking the main thread.
     * <p>
     * In case of any I/O errors during the writing process, the error details are
     * logged and the method continues its execution.
     * </p>
     *
     * @param weatherResults The list of weather results to be written to the CSV file.
     * @return A {@link Mono} signaling the completion of the write operation.
     */
    public Mono<Void> writeWeatherData(List<WeatherResult> weatherResults) {
        return Mono.fromRunnable(() -> {
                    try (FileWriter fileWriter = new FileWriter(FILE_NAME);
                         CSVWriter csvWriter = new CSVWriter(fileWriter)) {

                        String[] headers = {"Name", "Temperature", "Wind"};
                        csvWriter.writeNext(headers);

                        for (WeatherResult weatherResult : weatherResults) {
                            String[] weatherData = {
                                    weatherResult.name(),
                                    String.valueOf(weatherResult.temperature()),
                                    String.valueOf(weatherResult.wind())
                            };
                            csvWriter.writeNext(weatherData);
                        }
                    } catch (IOException ex) {
                        log.error("Error while writing to file: {}", ex.getMessage());
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
