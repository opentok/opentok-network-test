package com.opentok.qualitystats.sample.models;

import com.opentok.qualitystats.sample.models.stats.CallbackQualityStats;

public interface NetworkQualityTestCallbackListener {
    void onQualityTestResults(String recommendedSetting);

    void onQualityTestStatsUpdate(CallbackQualityStats stats);

    void onError(String error);
}
