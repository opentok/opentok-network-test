package com.opentok.qualitystats.sample.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Builder
@AllArgsConstructor
@Value
public class QualityStats {
    long sentVideoBitrateKbps;
    long sentAudioBitrateKbps;
    long receivedAudioBitrateKbps;
    long receivedVideoBitrateKbps;
    long currentRoundTripTimeMs;
    long availableOutgoingBitrate;
    long audioPacketLostRatio;
    long videoPacketLostRation;
    long timestamp;
    double jitter;
    String qualityLimitationReason;
    private DebugStats debugStats; // Changed access modifier to private

    // If you want to provide access to ExtraStats within the package, add a getter method:
    public DebugStats getExtraStats() {
        return debugStats;
    }

}

@AllArgsConstructor
@Getter
class DebugStats {
    PublisherStats publisherStats;
}
