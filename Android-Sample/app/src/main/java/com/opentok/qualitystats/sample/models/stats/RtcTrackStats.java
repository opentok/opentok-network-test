package com.opentok.qualitystats.sample.models.stats;

public class RtcTrackStats {
    private final long ssrc;
    private final String qualityLimitationReason;
    private final String resolution;
    private final int frameRate;
    private final boolean active;
    private final int pliCount;
    private final int nackCount;
    private final long bytesSent;
    private final long bitrateKbps;

    private RtcTrackStats(long ssrc, String qualityLimitationReason, String resolution, int frameRate,
                          boolean active, int pliCount, int nackCount, long bytesSent, long bitrateKbps) {
        this.ssrc = ssrc;
        this.qualityLimitationReason = qualityLimitationReason;
        this.resolution = resolution;
        this.frameRate = frameRate;
        this.active = active;
        this.pliCount = pliCount;
        this.nackCount = nackCount;
        this.bytesSent = bytesSent;
        this.bitrateKbps = bitrateKbps;
    }

    public long getSsrc() {
        return ssrc;
    }

    public String getQualityLimitationReason() {
        return qualityLimitationReason;
    }

    public String getResolution() {
        return resolution;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public boolean isActive() {
        return active;
    }

    public int getPliCount() {
        return pliCount;
    }

    public int getNackCount() {
        return nackCount;
    }

    public long getBytesSent() {
        return bytesSent;
    }

    public long getBitrateKbps() {
        return bitrateKbps;
    }

    // Builder class for RtcMediaStats
    public static class Builder {
        private long ssrc;
        private String qualityLimitationReason;
        private String resolution;
        private int frameRate;
        private boolean active;
        private int pliCount;
        private int nackCount;
        private long bytesSent;
        private long bitrateKbps;

        public Builder ssrc(long ssrc) {
            this.ssrc = ssrc;
            return this;
        }

        public Builder qualityLimitationReason(String qualityLimitationReason) {
            this.qualityLimitationReason = qualityLimitationReason;
            return this;
        }

        public Builder resolution(String resolution) {
            this.resolution = resolution;
            return this;
        }

        public Builder frameRate(int frameRate) {
            this.frameRate = frameRate;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder pliCount(int pliCount) {
            this.pliCount = pliCount;
            return this;
        }

        public Builder nackCount(int nackCount) {
            this.nackCount = nackCount;
            return this;
        }

        public Builder bytesSent(long bytesSent) {
            this.bytesSent = bytesSent;
            return this;
        }

        public Builder bitrateKbps(long bitrateKbps) {
            this.bitrateKbps = bitrateKbps;
            return this;
        }

        public RtcTrackStats build() {
            return new RtcTrackStats(ssrc, qualityLimitationReason, resolution, frameRate, active, pliCount, nackCount, bytesSent, bitrateKbps);
        }
    }
}
