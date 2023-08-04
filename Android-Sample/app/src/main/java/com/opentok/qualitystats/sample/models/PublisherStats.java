package com.opentok.qualitystats.sample.models;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.List;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@AllArgsConstructor
@Builder
@RequiresApi(api = Build.VERSION_CODES.N)
public class PublisherStats {
    List<MediaStatsEntry> videoStats;
    MediaStatsEntry audioStats;
    double jitter;
    long currentRoundTripTimeMs;
    long availableOutgoingBitrate;
    long timestamp;

    public double getTotalVideoBytesSent() {
        return videoStats.stream()
                .mapToLong(MediaStatsEntry::getBytesSent)
                .sum();
    }

    public long getTotalVideoKbsSent() {
        return videoStats.stream()
                .mapToLong(MediaStatsEntry::getBitrateKbps)
                .sum();
    }

    public boolean isScalableVideo() {
        return videoStats != null && videoStats.size() > 1;
    }

    // Method to get the value of qualityLimitationReason if it is not null/none/undefined
    public String getQualityLimitationReason() {
        return videoStats.stream()
                .map(MediaStatsEntry::getQualityLimitationReason)
                .filter(Objects::nonNull)
                .filter(reason -> !reason.equals("none") && !reason.equals("undefined"))
                .findFirst()
                .orElse("");
    }
}
