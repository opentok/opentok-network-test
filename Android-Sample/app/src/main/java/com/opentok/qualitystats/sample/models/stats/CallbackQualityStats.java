package com.opentok.qualitystats.sample.models.stats;

public class CallbackQualityStats {
    private final long sentVideoBitrateKbps;
    private final long sentAudioBitrateKbps;
    private final long receivedAudioBitrateKbps;
    private final long receivedVideoBitrateKbps;
    private final String receivedVideoResolution;
    private final String sentVideoResolution;
    private final long receivedVideoFrameRate;
    private final long sentVideoFrameRate;
    private final double currentRoundTripTimeMs;
    private final long availableOutgoingBitrate;
    private final double audioPacketLostRatio;
    private final double videoPacketLostRatio;
    private final long timestamp;
    private final double jitter;
    private final String qualityLimitationReason;
    private final boolean isScalableVideo;
    private final NetworkQualityStats networkQualityStats;

    public CallbackQualityStats(long sentVideoBitrateKbps, long sentAudioBitrateKbps,
                                long receivedAudioBitrateKbps, long receivedVideoBitrateKbps,
                                String receivedVideoResolution, String sentVideoResolution,
                                long receivedVideoFrameRate, long sentVideoFrameRate,
                                double currentRoundTripTimeMs, long availableOutgoingBitrate,
                                double audioPacketLostRatio, double videoPacketLostRatio,
                                long timestamp, double jitter, String qualityLimitationReason,
                                boolean isScalableVideo, NetworkQualityStats networkQualityStats) {
        this.sentVideoBitrateKbps = sentVideoBitrateKbps;
        this.sentAudioBitrateKbps = sentAudioBitrateKbps;
        this.receivedAudioBitrateKbps = receivedAudioBitrateKbps;
        this.receivedVideoBitrateKbps = receivedVideoBitrateKbps;
        this.receivedVideoResolution = receivedVideoResolution;
        this.sentVideoResolution = sentVideoResolution;
        this.receivedVideoFrameRate = receivedVideoFrameRate;
        this.sentVideoFrameRate = sentVideoFrameRate;
        this.currentRoundTripTimeMs = currentRoundTripTimeMs;
        this.availableOutgoingBitrate = availableOutgoingBitrate;
        this.audioPacketLostRatio = audioPacketLostRatio;
        this.videoPacketLostRatio = videoPacketLostRatio;
        this.timestamp = timestamp;
        this.jitter = jitter;
        this.qualityLimitationReason = qualityLimitationReason;
        this.isScalableVideo = isScalableVideo;
        this.networkQualityStats = networkQualityStats;
    }

    public static class Builder {
        private long sentVideoBitrateKbps;
        private long sentAudioBitrateKbps;
        private long receivedAudioBitrateKbps;
        private long receivedVideoBitrateKbps;
        private String receivedVideoResolution;
        private String sentVideoResolution;
        private long receivedVideoFrameRate;
        private long sentVideoFrameRate;
        private double currentRoundTripTimeMs;
        private long availableOutgoingBitrate;
        private double audioPacketLostRatio;
        private double videoPacketLostRatio;
        private long timestamp;
        private double jitter;
        private String qualityLimitationReason;
        private boolean isScalableVideo;
        private NetworkQualityStats networkQualityStats;

        public Builder sentVideoBitrateKbps(long sentVideoBitrateKbps) {
            this.sentVideoBitrateKbps = sentVideoBitrateKbps;
            return this;
        }

        public Builder sentAudioBitrateKbps(long sentAudioBitrateKbps) {
            this.sentAudioBitrateKbps = sentAudioBitrateKbps;
            return this;
        }

        public Builder receivedAudioBitrateKbps(long receivedAudioBitrateKbps) {
            this.receivedAudioBitrateKbps = receivedAudioBitrateKbps;
            return this;
        }

        public Builder receivedVideoBitrateKbps(long receivedVideoBitrateKbps) {
            this.receivedVideoBitrateKbps = receivedVideoBitrateKbps;
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

        public Builder audioPacketLostRatio(double audioPacketLostRatio) {
            this.audioPacketLostRatio = audioPacketLostRatio;
            return this;
        }

        public Builder videoPacketLostRatio(double videoPacketLostRatio) {
            this.videoPacketLostRatio = videoPacketLostRatio;
            return this;
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder jitter(double jitter) {
            this.jitter = jitter;
            return this;
        }

        public Builder qualityLimitationReason(String qualityLimitationReason) {
            this.qualityLimitationReason = qualityLimitationReason;
            return this;
        }

        public Builder publisherStats(NetworkQualityStats networkQualityStats) {
            this.networkQualityStats = networkQualityStats;
            return this;
        }

        public Builder receivedVideoResolution(String receivedVideoResolution) {
            this.receivedVideoResolution = receivedVideoResolution;
            return this;
        }

        public Builder sentVideoResolution(String sentVideoResolution) {
            this.sentVideoResolution = sentVideoResolution;
            return this;
        }

        public Builder receivedVideoFrameRate(long receivedVideoFrameRate) {
            this.receivedVideoFrameRate = this.receivedVideoFrameRate;
            return this;
        }

        public Builder sentVideoFrameRate(long sentVideoFrameRate) {
            this.sentVideoFrameRate = this.sentVideoFrameRate;
            return this;
        }

        public Builder isScalableVideo(boolean isScalableVideo) {
            this.isScalableVideo = this.isScalableVideo;
            return this;
        }

        public CallbackQualityStats build() {
            return new CallbackQualityStats(sentVideoBitrateKbps, sentAudioBitrateKbps,
                    receivedAudioBitrateKbps, receivedVideoBitrateKbps,
                    receivedVideoResolution, sentVideoResolution,
                    receivedVideoFrameRate, sentVideoFrameRate,
                    currentRoundTripTimeMs, availableOutgoingBitrate,
                    audioPacketLostRatio, videoPacketLostRatio,
                    timestamp, jitter, qualityLimitationReason,
                    isScalableVideo, networkQualityStats);
        }
    }

    public String getReceivedVideoResolution() {
        return receivedVideoResolution;
    }

    public String getSentVideoResolution() {
        return sentVideoResolution;
    }

    public long getReceivedVideoFrameRate() {
        return receivedVideoFrameRate;
    }

    public long getSentVideoFrameRate() {
        return sentVideoFrameRate;
    }

    public long getSentVideoBitrateKbps() {
        return sentVideoBitrateKbps;
    }

    public long getSentAudioBitrateKbps() {
        return sentAudioBitrateKbps;
    }

    public long getReceivedAudioBitrateKbps() {
        return receivedAudioBitrateKbps;
    }

    public long getReceivedVideoBitrateKbps() {
        return receivedVideoBitrateKbps;
    }

    public double getCurrentRoundTripTimeMs() {
        return currentRoundTripTimeMs;
    }

    public long getAvailableOutgoingBitrate() {
        return availableOutgoingBitrate;
    }

    public double getAudioPacketLostRatio() {
        return audioPacketLostRatio;
    }

    public double getVideoPacketLostRatio() {
        return videoPacketLostRatio;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getJitter() {
        return jitter;
    }

    public boolean isScalableVideo() {
        return isScalableVideo;
    }

    public String getQualityLimitationReason() {
        return qualityLimitationReason;
    }

    public NetworkQualityStats getPublisherStats() {
        return networkQualityStats;
    }
}

