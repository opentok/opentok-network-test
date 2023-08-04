package com.opentok.qualitystats.sample;

import com.opentok.android.Publisher;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class VideoCallTesterConfig {
    private static final int DEFAULT_TEST_DURATION = 30;

    String sessionId;
     String apiKey;
     String token;
     Publisher.CameraCaptureResolution resolution;
     int testDurationSec;

    public VideoCallTesterConfig(@NonNull String sessionId, @NonNull String apiKey,
                                 @NonNull String token,
                                 Publisher.CameraCaptureResolution resolution,
                                 Integer testDurationSec) {
        this.sessionId = sessionId;
        this.apiKey = apiKey;
        this.token = token;
        this.resolution = resolution == null ? Publisher.CameraCaptureResolution.HIGH : resolution ;
        this.testDurationSec = testDurationSec == null? 30 : testDurationSec;
    }
}
