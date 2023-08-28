package com.opentok.qualitystats.sample.models.stats;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.List;
import java.util.Objects;

import lombok.Builder;

@Builder
@RequiresApi(api = Build.VERSION_CODES.N)
public class VideoQualityStats {
    List<MediaStatsEntry> videoStats;
    MediaStatsEntry audioStats;
    double jitter;
    double currentRoundTripTimeMs;
    long availableOutgoingBitrate;
    long timestamp;

    public VideoQualityStats(List<MediaStatsEntry> videoStats, MediaStatsEntry audioStats, double jitter,
                             double currentRoundTripTimeMs, long availableOutgoingBitrate, long timestamp) {
        this.videoStats = videoStats;
        this.audioStats = audioStats;
        this.jitter = jitter;
        this.currentRoundTripTimeMs = currentRoundTripTimeMs;
        this.availableOutgoingBitrate = availableOutgoingBitrate;
        this.timestamp = timestamp;
    }

    public String getResolutionBySrc() {
        StringBuilder resolutionBuilder = new StringBuilder();

        if (videoStats != null) {
            for (MediaStatsEntry videoStat : videoStats) {
                resolutionBuilder.append("ssrc: ")
                        .append(videoStat.getSsrc())
                        .append(" -> ")
                        .append(videoStat.getResolution())
                        .append(" (")
                        .append(videoStat.getFramerate())
                        .append("fps)")
                        .append("  ");
            }
        }

        return resolutionBuilder.toString();
    }

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

    public List<MediaStatsEntry> getVideoStats() {
        return videoStats;
    }

    public MediaStatsEntry getAudioStats() {
        return audioStats;
    }

    public double getJitter() {
        return jitter;
    }

    public double getCurrentRoundTripTimeMs() {
        return currentRoundTripTimeMs;
    }

    public long getAvailableOutgoingBitrate() {
        return availableOutgoingBitrate;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
