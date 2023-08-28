package com.opentok.qualitystats.sample.models.stats;

import android.os.Build;

import androidx.annotation.RequiresApi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;


@Builder
@RequiresApi(api = Build.VERSION_CODES.N)
public class SubscriberAudioStats {
    long audioBitrateKbps;
    long audioBytesReceived;
    double audioPacketLostRatio;
    double timestamp;

    public SubscriberAudioStats(long audioBitrateKbps, long audioBytesReceived,
                                double audioPacketLostRatio, double timestamp) {
        this.audioBitrateKbps = audioBitrateKbps;
        this.audioBytesReceived = audioBytesReceived;
        this.audioPacketLostRatio = audioPacketLostRatio;
        this.timestamp = timestamp;
    }

    public long getAudioBitrateKbps() {
        return audioBitrateKbps;
    }

    public long getAudioBytesReceived() {
        return audioBytesReceived;
    }

    public double getAudioPacketLostRatio() {
        return audioPacketLostRatio;
    }

    public double getTimestamp() {
        return timestamp;
    }
}
