package com.opentok.qualitystats.sample.models;


import lombok.Builder;
import lombok.Value;

@Builder
@Value
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
    @Builder
    public MediaStatsEntry(long ssrc,
                           String qualityLimitationReason,
                           String resolution,
                           int framerate,
                           boolean active,
                           int pliCount,
                           int nackCount,
                           long bytesSent,
                           long bitrateKbps) {
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

//
}
