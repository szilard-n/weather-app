package com.example.weatherapp.handler;

import lombok.Getter;

@Getter
public enum ApiErrorKeys {
    CODE("code"),
    MESSAGE("message"),
    TIME("timestamp");

    private final String key;

    ApiErrorKeys(String key) {
        this.key = key;
    }
}
