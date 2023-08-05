package com.opentok.qualitystats.sample.models;

import android.os.Build;

import androidx.annotation.RequiresApi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@AllArgsConstructor
@Builder
@Getter
@RequiresApi(api = Build.VERSION_CODES.N)
public class SubscriberVideoStats {
    long videoBytesKbsReceived;
    long videoBytesReceived;
    double timestamp;
    double videoPacketLostRatio;
}
