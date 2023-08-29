package com.opentok.qualitystats.sample.models.stats;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.List;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.N)
public class NetworkQualityStats {
    List<RtcTrackStats> videoStats;
    RtcTrackStats audioStats;
    double jitter;
    double currentRoundTripTimeMs;
    long availableOutgoingBitrate;
    long timestamp;

    private NetworkQualityStats(Builder builder) {
        this.videoStats = builder.videoStats;
        this.audioStats = builder.audioStats;
        this.jitter = builder.jitter;
        this.currentRoundTripTimeMs = builder.currentRoundTripTimeMs;
        this.availableOutgoingBitrate = builder.availableOutgoingBitrate;
        this.timestamp = builder.timestamp;
    }

    public String getResolutionBySrc() {
        StringBuilder resolutionBuilder = new StringBuilder();

        if (videoStats != null) {
            for (RtcTrackStats videoStat : videoStats) {
                resolutionBuilder.append("ssrc: ")
                        .append(videoStat.getSsrc())
                        .append(" -> ")
                        .append(videoStat.getResolution())
                        .append(" (")
                        .append(videoStat.getFrameRate())
                        .append("fps)")
                        .append("  ");
            }
        }

        return resolutionBuilder.toString();
    }

    public double getTotalVideoBytesSent() {
        return videoStats.stream()
                .mapToLong(RtcTrackStats::getBytesSent)
                .sum();
    }

    public long getTotalVideoKbsSent() {
        return videoStats.stream()
                .mapToLong(RtcTrackStats::getBitrateKbps)
                .sum();
    }

    public boolean isScalableVideo() {
        return videoStats != null && videoStats.size() > 1;
    }

    // Method to get the value of qualityLimitationReason if it is not null/none/undefined
    public String getQualityLimitationReason() {
        return videoStats.stream()
                .map(RtcTrackStats::getQualityLimitationReason)
                .filter(Objects::nonNull)
                .filter(reason -> !reason.equals("none") && !reason.equals("undefined"))
                .findFirst()
                .orElse("");
    }

    public static class Builder {
        private List<RtcTrackStats> videoStats;
        private RtcTrackStats audioStats;
        private double jitter;
        private double currentRoundTripTimeMs;
        private long availableOutgoingBitrate;
        private long timestamp;

        public Builder videoStats(List<RtcTrackStats> videoStats) {
            this.videoStats = videoStats;
            return this;
        }

        public Builder audioStats(RtcTrackStats audioStats) {
            this.audioStats = audioStats;
            return this;
        }

        public Builder jitter(double jitter) {
            this.jitter = jitter;
            return this;
        }

        public Builder currentRoundTripTimeMs(double currentRoundTripTimeMs) {
            this.currentRoundTripTimeMs = currentRoundTripTimeMs;
            return this;
        }

        public Builder availableOutgoingBitrate(long availableOutgoingBitrate) {
            this.availableOutgoingBitrate = availableOutgoingBitrate;
            return this;
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public NetworkQualityStats build() {
            return new NetworkQualityStats(this);
        }
    }

    public List<RtcTrackStats> getVideoStats() {
        return videoStats;
    }

    public RtcTrackStats getAudioStats() {
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
