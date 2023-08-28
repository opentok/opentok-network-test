package com.opentok.qualitystats.sample.models.stats;


import lombok.Builder;

@Builder
public class MediaStatsEntry {
    long ssrc;
    String qualityLimitationReason;
    String resolution;
    int framerate;
    boolean active;
    int pliCount;
    int nackCount;
    long bytesSent;
    long bitrateKbps;

    public MediaStatsEntry(long ssrc, String qualityLimitationReason, String resolution, int framerate, boolean active, int pliCount, int nackCount, long bytesSent, long bitrateKbps) {
        this.ssrc = ssrc;
        this.qualityLimitationReason = qualityLimitationReason;
        this.resolution = resolution;
        this.framerate = framerate;
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

    public int getFramerate() {
        return framerate;
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


}