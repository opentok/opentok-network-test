package com.opentok.qualitystats.sample.models.stats;

public class QualityStats {
    private final long sentVideoBitrateKbps;
    private final long sentAudioBitrateKbps;
    private final long receivedAudioBitrateKbps;
    private final long receivedVideoBitrateKbps;
    private final String receivedVideoResolution;
    private final String sentVideoResolution;
    private final long receivedVideoFramerate;
    private final long sentVideoFramerate;
    private final double currentRoundTripTimeMs;
    private final long availableOutgoingBitrate;
    private final double audioPacketLostRatio;
    private final double videoPacketLostRatio;
    private final long timestamp;
    private final double jitter;
    private final String qualityLimitationReason;
    private final PublisherStats publisherStats;

    public QualityStats(long sentVideoBitrateKbps, long sentAudioBitrateKbps,
                        long receivedAudioBitrateKbps, long receivedVideoBitrateKbps,
                        String receivedVideoResolution, String sentVideoResolution,
                        long receivedVideoFramerate, long sentVideoFramerate,
                        double currentRoundTripTimeMs, long availableOutgoingBitrate,
                        double audioPacketLostRatio, double videoPacketLostRatio,
                        long timestamp, double jitter, String qualityLimitationReason,
                        PublisherStats publisherStats) {
        this.sentVideoBitrateKbps = sentVideoBitrateKbps;
        this.sentAudioBitrateKbps = sentAudioBitrateKbps;
        this.receivedAudioBitrateKbps = receivedAudioBitrateKbps;
        this.receivedVideoBitrateKbps = receivedVideoBitrateKbps;
        this.receivedVideoResolution = receivedVideoResolution;
        this.sentVideoResolution = sentVideoResolution;
        this.receivedVideoFramerate = receivedVideoFramerate;
        this.sentVideoFramerate = sentVideoFramerate;
        this.currentRoundTripTimeMs = currentRoundTripTimeMs;
        this.availableOutgoingBitrate = availableOutgoingBitrate;
        this.audioPacketLostRatio = audioPacketLostRatio;
        this.videoPacketLostRatio = videoPacketLostRatio;
        this.timestamp = timestamp;
        this.jitter = jitter;
        this.qualityLimitationReason = qualityLimitationReason;
        this.publisherStats = publisherStats;
    }

    public String getReceivedVideoResolution() {
        return receivedVideoResolution;
    }

    public String getSentVideoResolution() {
        return sentVideoResolution;
    }

    public long getReceivedVideoFramerate() {
        return receivedVideoFramerate;
    }

    public long getSentVideoFramerate() {
        return sentVideoFramerate;
    }


    public static class Builder {
        private long sentVideoBitrateKbps;
        private long sentAudioBitrateKbps;
        private long receivedAudioBitrateKbps;
        private long receivedVideoBitrateKbps;
        private String receivedVideoResolution;
        private String sentVideoResolution;
        private long receivedVideoFramerate;
        private long sentVideoFramerate;
        private double currentRoundTripTimeMs;
        private long availableOutgoingBitrate;
        private double audioPacketLostRatio;
        private double videoPacketLostRatio;
        private long timestamp;
        private double jitter;
        private String qualityLimitationReason;
        private PublisherStats publisherStats;

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

        public Builder publisherStats(PublisherStats publisherStats) {
            this.publisherStats = publisherStats;
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

        public Builder receivedVideoFramerate(long receivedVideoFramerate) {
            this.receivedVideoFramerate = receivedVideoFramerate;
            return this;
        }

        public Builder sentVideoFramerate(long sentVideoFramerate) {
            this.sentVideoFramerate = sentVideoFramerate;
            return this;
        }

        // ... Other existing methods ...

        public QualityStats build() {
            return new QualityStats(sentVideoBitrateKbps, sentAudioBitrateKbps,
                    receivedAudioBitrateKbps, receivedVideoBitrateKbps,
                    receivedVideoResolution, sentVideoResolution,
                    receivedVideoFramerate, sentVideoFramerate,
                    currentRoundTripTimeMs, availableOutgoingBitrate,
                    audioPacketLostRatio, videoPacketLostRatio,
                    timestamp, jitter, qualityLimitationReason,
                    publisherStats);
        }
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

    public String getQualityLimitationReason() {
        return qualityLimitationReason;
    }

    public PublisherStats getPublisherStats() {
        return publisherStats;
    }
}

