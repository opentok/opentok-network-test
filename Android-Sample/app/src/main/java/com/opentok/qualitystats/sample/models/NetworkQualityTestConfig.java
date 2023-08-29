package com.opentok.qualitystats.sample.models;

import com.opentok.android.Publisher;

import lombok.NonNull;

public class NetworkQualityTestConfig {
    private static final int DEFAULT_TEST_DURATION = 30;

    String sessionId;
    String apiKey;
    String token;
    Publisher.CameraCaptureResolution resolution;
    int testDurationSec;

    public NetworkQualityTestConfig(@NonNull String sessionId, @NonNull String apiKey,
                                    @NonNull String token,
                                    Publisher.CameraCaptureResolution resolution,
                                    Integer testDurationSec) {
        this.sessionId = sessionId;
        this.apiKey = apiKey;
        this.token = token;
        this.resolution = resolution == null ? Publisher.CameraCaptureResolution.HIGH : resolution;
        this.testDurationSec = testDurationSec == null ? DEFAULT_TEST_DURATION : testDurationSec;
    }

    public static class Builder {
        private String sessionId;
        private String apiKey;
        private String token;
        private Publisher.CameraCaptureResolution resolution;
        private int testDurationSec;

        public Builder() {
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder resolution(Publisher.CameraCaptureResolution resolution) {
            this.resolution = resolution;
            return this;
        }

        public Builder testDurationSec(int testDurationSec) {
            this.testDurationSec = testDurationSec;
            return this;
        }

        public NetworkQualityTestConfig build() {
            return new NetworkQualityTestConfig(sessionId, apiKey, token, resolution, testDurationSec);
        }
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getToken() {
        return token;
    }

    public Publisher.CameraCaptureResolution getResolution() {
        return resolution;
    }

    public int getTestDurationSec() {
        return testDurationSec;
    }
}
