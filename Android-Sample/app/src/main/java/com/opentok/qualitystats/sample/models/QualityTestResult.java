package com.opentok.qualitystats.sample.models;

public class QualityTestResult {
    String recommendedResolution;

    public QualityTestResult(String recommendedResolution) {
        this.recommendedResolution = recommendedResolution;
    }

    public String getRecommendedResolution() {
        return recommendedResolution;
    }
}
