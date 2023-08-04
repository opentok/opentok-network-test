package com.opentok.qualitystats.sample.models;

import android.os.Build;

import androidx.annotation.RequiresApi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@AllArgsConstructor
@Builder
@RequiresApi(api = Build.VERSION_CODES.N)
public class SubscriberVideoStats {
    long receivedVideoBitrateKbps;
    long videoBytesReceived;
    double timestamp;
    double videoPacketLostRatio;
    String resolution;
}
