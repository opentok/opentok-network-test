package com.opentok.qualitystats.sample.models.stats;

import android.os.Build;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.N)
public class SubAudioStats {
    private final long audioBitrateKbps;
    private final long audioBytesReceived;
    private final double audioPacketLostRatio;
    private final double timestamp;

    private SubAudioStats(Builder builder) {
        this.audioBitrateKbps = builder.audioBitrateKbps;
        this.audioBytesReceived = builder.audioBytesReceived;
        this.audioPacketLostRatio = builder.audioPacketLostRatio;
        this.timestamp = builder.timestamp;
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

    public static class Builder {
        private long audioBitrateKbps;
        private long audioBytesReceived;
        private double audioPacketLostRatio;
        private double timestamp;

        public Builder() {
        }

        public Builder audioBitrateKbps(long audioBitrateKbps) {
            this.audioBitrateKbps = audioBitrateKbps;
            return this;
        }

        public Builder audioBytesReceived(long audioBytesReceived) {
            this.audioBytesReceived = audioBytesReceived;
            return this;
        }

        public Builder audioPacketLostRatio(double audioPacketLostRatio) {
            this.audioPacketLostRatio = audioPacketLostRatio;
            return this;
        }

        public Builder timestamp(double timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public SubAudioStats build() {
            return new SubAudioStats(this);
        }
    }
}
