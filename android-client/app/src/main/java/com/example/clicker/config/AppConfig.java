package com.example.clicker.config;

import com.example.clicker.BuildConfig;

public final class AppConfig {
    private AppConfig() {
    }

    public static String getServerBaseUrl() {
        return BuildConfig.SERVER_BASE_URL;
    }
}
