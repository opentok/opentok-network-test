package com.opentok.qualitystats.sample.models.stats;

import android.os.Build;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.N)
public class SubVideoStats {
    private final long videoBytesKbsReceived;
    private final long videoBytesReceived;
    private final double timestamp;
    private final double videoPacketLostRatio;

    private SubVideoStats(long videoBytesKbsReceived, long videoBytesReceived,
                          double timestamp, double videoPacketLostRatio) {
        this.videoBytesKbsReceived = videoBytesKbsReceived;
        this.videoBytesReceived = videoBytesReceived;
        this.timestamp = timestamp;
        this.videoPacketLostRatio = videoPacketLostRatio;
    }

    public static class Builder {
        private long videoBytesKbsReceived;
        private long videoBytesReceived;
        private double timestamp;
        private double videoPacketLostRatio;

        public Builder videoBytesKbsReceived(long videoBytesKbsReceived) {
            this.videoBytesKbsReceived = videoBytesKbsReceived;
            return this;
        }

        public Builder videoBytesReceived(long videoBytesReceived) {
            this.videoBytesReceived = videoBytesReceived;
            return this;
        }

        public Builder timestamp(double timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder videoPacketLostRatio(double videoPacketLostRatio) {
            this.videoPacketLostRatio = videoPacketLostRatio;
            return this;
        }

        public SubVideoStats build() {
            return new SubVideoStats(videoBytesKbsReceived, videoBytesReceived,
                    timestamp, videoPacketLostRatio);
        }
    }

    public long getVideoBytesKbsReceived() {
        return videoBytesKbsReceived;
    }

    public long getVideoBytesReceived() {
        return videoBytesReceived;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public double getVideoPacketLostRatio() {
        return videoPacketLostRatio;
    }
}
