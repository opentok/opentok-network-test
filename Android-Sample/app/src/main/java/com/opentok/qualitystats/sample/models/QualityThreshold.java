package com.opentok.qualitystats.sample.models;

import lombok.Value;

@Value
public class QualityThreshold {
    long targetBitrate;
    long targetBitrateSimulcast;
    String recommendedSetting;

    public QualityThreshold(long targetBitrate, long targetBitrateSimulcast, String recommendedSetting) {
        this.targetBitrate = targetBitrate;
        this.targetBitrateSimulcast = targetBitrateSimulcast;
        this.recommendedSetting = recommendedSetting;
    }
}