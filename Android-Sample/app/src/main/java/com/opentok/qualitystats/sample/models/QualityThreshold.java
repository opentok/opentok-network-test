package com.opentok.qualitystats.sample.models;

public class QualityThreshold {
    long targetBitrate;
    long targetBitrateSimulcast;
    String recommendedSetting;

    public QualityThreshold(long targetBitrate, long targetBitrateSimulcast, String recommendedSetting) {
        this.targetBitrate = targetBitrate;
        this.targetBitrateSimulcast = targetBitrateSimulcast;
        this.recommendedSetting = recommendedSetting;
    }

    public long getTargetBitrate() {
        return targetBitrate;
    }

    public long getTargetBitrateSimulcast() {
        return targetBitrateSimulcast;
    }

    public String getRecommendedSetting() {
        return recommendedSetting;
    }
}