package com.opentok.qualitystats.sample;

import static com.opentok.qualitystats.sample.models.constant.RtcStatsConstants.*;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;


import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Session.SessionListener;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;
import com.opentok.qualitystats.sample.models.NetworkQualityTestCallbackListener;
import com.opentok.qualitystats.sample.models.QualityTestResult;
import com.opentok.qualitystats.sample.models.QualityThreshold;
import com.opentok.qualitystats.sample.models.NetworkQualityTestConfig;
import com.opentok.qualitystats.sample.models.stats.RtcTrackStats;
import com.opentok.qualitystats.sample.models.stats.NetworkQualityStats;
import com.opentok.qualitystats.sample.models.stats.CallbackQualityStats;
import com.opentok.qualitystats.sample.models.stats.SubAudioStats;
import com.opentok.qualitystats.sample.models.stats.SubVideoStats;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

public class NetworkQualityTest extends Activity
        implements SessionListener, PublisherKit.PublisherListener, SubscriberKit.SubscriberListener {

    private static final String LOG_TAG = "network-quality-stats";
    private static final int TIME_WINDOW = 1;
    private static final long MILLIS_TO_SECONDS = 1000;
    private static final long BITS_PER_BYTE = 8;
    private static final long TIME_WINDOW_SECONDS_TO_MS = TIME_WINDOW * 1000;
    private static final double RATIO_TARGET_BITRATE = 0.7;
    private static final double MIN_TEST_DURATION_SEC = 5;

    private final Handler mHandler = new Handler();
    private final Queue<SubscriberKit.SubscriberVideoStats> videoStatsQueue = new LinkedList<>();
    private final Queue<SubscriberKit.SubscriberAudioStats> audioStatsQueue = new LinkedList<>();
    private final Activity context;
    private final NetworkQualityTestConfig config;
    private final NetworkQualityTestCallbackListener listener;
    private final Handler rtcStatsHandler = new Handler();
    private final Map<Long, Long> ssrcToPrevBytesSent = new HashMap<>();
    private final List<NetworkQualityStats> publisherNetworkQualityStatsList = new ArrayList<>();
    private final List<NetworkQualityStats> subscriberNetworkQualityStatsList = new ArrayList<>();
    private final List<SubVideoStats> subVideoStatsList = new ArrayList<>();
    private final List<SubAudioStats> subAudioStatsList = new ArrayList<>();
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;
    private Runnable rtcStatsRunnable;
    private Runnable qualityStatsRunnable;

    private boolean isErrorOccurred = false;
    private long prevVideoTimestamp = 0;
    private long startTestStartTime = 0;

    public NetworkQualityTest(Activity context, NetworkQualityTestConfig config,
                              NetworkQualityTestCallbackListener listener) {
        this.context = context;
        this.config = config;
        this.listener = listener;
    }

    public void startTest() {
        Log.i(LOG_TAG, "Starting network test..");
        connectSession();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void connectSession() {
        Log.i(LOG_TAG, "Connecting session..");
        if (mSession == null) {
            mSession = new Session.Builder(context, config.getApiKey(), config.getSessionId()).build();
            mSession.setSessionListener(this);
            mSession.connect(config.getToken());
        }
    }

    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG, "Session is connected");

        mPublisher = new Publisher.Builder(context)
                .resolution(config.getResolution())
                .build();
        mPublisher.setPublisherListener(this);
        mPublisher.setAudioFallbackEnabled(false);
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG, "Session is disconnected");
        mPublisher = null;
        mSubscriber = null;
        mSession = null;
        stopStatsCollection();
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.i(LOG_TAG, "Session error: " + opentokError.getMessage());
        if (!isErrorOccurred) {
            isErrorOccurred = true;
            handleError(opentokError.getMessage());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "Session onStreamDropped");
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG, "Session onStreamReceived");
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.i(LOG_TAG, "Publisher onStreamCreated");
        if (mSubscriber == null) {
            subscribeToStream(stream);
        }
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.i(LOG_TAG, "Publisher onStreamDestroyed");
        if (mSubscriber == null) {
            unsubscribeFromStream(stream);
        }
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Log.i(LOG_TAG, "Publisher error: " + opentokError.getMessage());
        if (!isErrorOccurred) {
            isErrorOccurred = true;
            handleError(opentokError.getMessage());
        }
    }

    @Override
    public void onConnected(SubscriberKit subscriberKit) {
        Log.i(LOG_TAG, "Subscriber onConnected");
        startTestStartTime = System.currentTimeMillis();
        muteSubscriberAudio(subscriberKit);
        startQualityStatsRunnable();
        setupStopStatsRunnable();
    }

    public void stopTest() {
        mHandler.removeCallbacks(stopStatsRunnable);

        if (mSession != null) {
            rtcStatsHandler.removeCallbacks(rtcStatsRunnable);
            mPublisher.setPublishVideo(false);
            mSession.disconnect();

            long elapsedTime = System.currentTimeMillis() - startTestStartTime;
            if (elapsedTime < MIN_TEST_DURATION_SEC) {
                handleError("Test was stopped before 5 seconds.");
            } else {
                QualityTestResult recommendedSetting = getRecommendedSetting();
                listener.onQualityTestResults(recommendedSetting.getRecommendedResolution());
            }
        }
    }

    private void muteSubscriberAudio(SubscriberKit subscriberKit) {
        subscriberKit.setAudioVolume(0);
    }

    private void setupQualityStatsRunnable() {
        qualityStatsRunnable = () -> {
            if (mSession != null) {
                CallbackQualityStats callbackQualityStats = calculateCallbackQualityStats();
                if (callbackQualityStats != null) {
                    listener.onQualityTestStatsUpdate(callbackQualityStats);
                }
                mHandler.postDelayed(qualityStatsRunnable, 1000); // Call every one second
            }
        };
    }

    private void stopStatsCollection() {
        rtcStatsHandler.removeCallbacks(rtcStatsRunnable);
        mHandler.removeCallbacks(qualityStatsRunnable);
    }

    private void handleError(String error) {
        stopStatsCollection();
        mSession.disconnect();
        listener.onError(error);
    }

    private void setupStopStatsRunnable() {
        mHandler.postDelayed(stopStatsRunnable, config.getTestDurationSec() * 1000L);
    }

    private void setupRtcStatsRunnable() {
        rtcStatsRunnable = () -> {
            if (mPublisher != null && mSubscriber != null) {
                mPublisher.getRtcStatsReport();
                mSubscriber.getRtcStatsReport();
                rtcStatsHandler.postDelayed(rtcStatsRunnable, 500);
            }
        };
    }

    private void startQualityStatsRunnable() {
        setupQualityStatsRunnable();
        setupRtcStatsRunnable();
        rtcStatsHandler.post(rtcStatsRunnable);
        mHandler.post(qualityStatsRunnable);
    }


    @Override
    public void onDisconnected(SubscriberKit subscriberKit) {
        Log.i(LOG_TAG, "Subscriber onDisconnected");
    }

    @Override
    public void onError(SubscriberKit subscriberKit, OpentokError opentokError) {
        Log.i(LOG_TAG, "Subscriber error: " + opentokError.getMessage());
        if (!isErrorOccurred) {
            isErrorOccurred = true;
            handleError("Subscriber error: " + opentokError.getMessage());
        }
    }

    private void subscribeToStream(Stream stream) {

        mSubscriber = new Subscriber.Builder(context, stream).build();
        mSubscriber.setSubscriberListener(this);
        mSession.subscribe(mSubscriber);
        // listen to stats
        mSubscriber.setRtcStatsReportListener(subscriberRtcStatsReportListener);
        mPublisher.setRtcStatsReportListener(publisherRtcStatsReportListener);
        mSubscriber.setVideoStatsListener((subscriber, stats) -> onSubscriberVideoStats(stats));
        mSubscriber.setAudioStatsListener((subscriber, stats) -> onSubscriberAudioStats(stats));

    }

    private void unsubscribeFromStream(Stream stream) {
        if (mSubscriber.getStream().equals(stream)) {
            mSubscriber = null;
        }
    }

    private void onSubscriberVideoStats(SubscriberKit.SubscriberVideoStats videoStats) {
        double videoTimestamp = videoStats.timeStamp;
        if (videoStatsQueue.isEmpty()) {
            videoStatsQueue.add(videoStats);
            return;
        }
        SubscriberKit.SubscriberVideoStats previousVideoStats = videoStatsQueue.peek();

        assert previousVideoStats != null;
        if ((videoTimestamp - previousVideoStats.timeStamp) < TIME_WINDOW * 1000) {
            return;
        }

        long videoPacketsLostInterval = videoStats.videoPacketsLost - previousVideoStats.videoPacketsLost;
        long videoPacketsReceivedInterval = videoStats.videoPacketsReceived - previousVideoStats.videoPacketsReceived;
        long videoTotalPacketsInterval = videoPacketsLostInterval + videoPacketsReceivedInterval;
        double videoPLRatio = 0.0;
        if (videoTotalPacketsInterval > 0) {
            videoPLRatio = (double) videoPacketsLostInterval / (double) videoTotalPacketsInterval;
        }

        long elapsedTimeMs = (long) (videoTimestamp - previousVideoStats.timeStamp);
        long bytesSentDiff = videoStats.videoBytesReceived - previousVideoStats.videoBytesReceived;
        long videoBitrateKbps = (long) ((bytesSentDiff * 8) / (elapsedTimeMs / 1000.0)) / 1000;

        videoStatsQueue.add(videoStats);

        subVideoStatsList.add(new SubVideoStats.Builder()
                .videoBytesKbsReceived(videoBitrateKbps)
                .videoBytesReceived(videoStats.videoBytesReceived)
                .timestamp(videoStats.timeStamp)
                .videoPacketLostRatio(videoPLRatio)
                .build());

    }

    private void onSubscriberAudioStats(SubscriberKit.SubscriberAudioStats audioStats) {
        double currentAudioTimestamp = audioStats.timeStamp;

        // When queue is empty, add the current stats and return
        if (audioStatsQueue.isEmpty()) {
            audioStatsQueue.add(audioStats);
            return;
        }

        SubscriberKit.SubscriberAudioStats previousAudioStats = audioStatsQueue.peek();

        // Check if the time difference is within the time window
        long elapsedTimeMs = 0;
        if (previousAudioStats != null) {
            elapsedTimeMs = (long) (currentAudioTimestamp - previousAudioStats.timeStamp);
        }
        if (elapsedTimeMs < TIME_WINDOW_SECONDS_TO_MS) {
            return;
        }

        // Compute packet lost and received intervals
        long audioPacketsLostInterval = audioStats.audioPacketsLost - previousAudioStats.audioPacketsLost;
        long audioPacketsReceivedInterval = audioStats.audioPacketsReceived - previousAudioStats.audioPacketsReceived;
        long totalAudioPacketsInterval = audioPacketsLostInterval + audioPacketsReceivedInterval;

        // Calculate audio packet lost ratio
        double audioPacketLostRatio = 0.0;
        if (totalAudioPacketsInterval > 0) {
            audioPacketLostRatio = (double) audioPacketsLostInterval / (double) totalAudioPacketsInterval;
        }

        // Calculate audio bandwidth in Kbps
        long audioBytesReceivedDiff = audioStats.audioBytesReceived - previousAudioStats.audioBytesReceived;
        long audioBitrateKbps = (long) ((audioBytesReceivedDiff * BITS_PER_BYTE) / (elapsedTimeMs / (double) MILLIS_TO_SECONDS)) / 1000;

        // Update the queue and stats list
        audioStatsQueue.add(audioStats);
        subAudioStatsList.add(new SubAudioStats.Builder()
                .audioBitrateKbps(audioBitrateKbps)
                .audioBytesReceived(audioStats.audioBytesReceived)
                .timestamp(audioStats.timeStamp)
                .audioPacketLostRatio(audioPacketLostRatio)
                .build());
    }


    private long calculateVideoBitrateKbps(long ssrc, long timestamp, long currentBytesSent) {
        long videoBitrateKbps = 0;

        if (prevVideoTimestamp > 0 && ssrcToPrevBytesSent.containsKey(ssrc)) {
            long elapsedTimeMs = timestamp - prevVideoTimestamp;

            if (elapsedTimeMs == 0) {
                return videoBitrateKbps;
            }

            long previousBytesSent = ssrcToPrevBytesSent.get(ssrc);
            long bytesSentDifference = currentBytesSent - previousBytesSent;

            videoBitrateKbps = (long) ((bytesSentDifference * BITS_PER_BYTE)
                    / (elapsedTimeMs / (double) MILLIS_TO_SECONDS));
        }

        ssrcToPrevBytesSent.put(ssrc, currentBytesSent);

        return videoBitrateKbps;
    }


    private final Runnable stopStatsRunnable = new Runnable() {
        @Override
        public void run() {
            if (mSession != null) {
                rtcStatsHandler.removeCallbacks(rtcStatsRunnable);
                mPublisher.setPublishVideo(false);
                mSession.disconnect();
                listener.onQualityTestResults(getRecommendedSetting().getRecommendedResolution());
            }
        }
    };

    private final PublisherKit.PublisherRtcStatsReportListener publisherRtcStatsReportListener
            = (publisherKit, publisherRtcStats) -> {
        for (PublisherKit.PublisherRtcStats s : publisherRtcStats) {
            try {
                JSONArray rtcStatsJsonArray = new JSONArray(s.jsonArrayOfReports);
                processRtcStats(rtcStatsJsonArray, OUTBOUND_RTP);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void processRtcStats(JSONArray rtcStatsJsonArray, String direction)
            throws JSONException {
        List<RtcTrackStats> videoStatsList = new ArrayList<>();
        RtcTrackStats audioStats = null;
        double jitter = 0.0;
        long availableOutgoingBitrate = 0;
        long timestamp = 0;
        double currentRoundTripTimeMs = 0;

        for (int i = 0; i < rtcStatsJsonArray.length(); i++) {
            JSONObject rtcStatObject = rtcStatsJsonArray.getJSONObject(i);
            String statType = rtcStatObject.getString(TYPE);
            String kind = rtcStatObject.optString(KIND, NONE);

            if (direction.equals(statType) && (VIDEO.equals(kind) || AUDIO.equals(kind))) {
                RtcTrackStats rtcTrackStats = processOutboundRtpStats(rtcStatObject);
                if (VIDEO.equals(kind)) {
                    videoStatsList.add(rtcTrackStats);
                } else {
                    audioStats = rtcTrackStats;
                }
                jitter = rtcStatObject.optDouble(JITTER, -1);
            } else if (CANDIDATE_PAIR.equals(statType)) {
                boolean isNominated = rtcStatObject.optBoolean(IS_NOMINATED, false);
                if (isNominated) {
                    availableOutgoingBitrate = rtcStatObject.optLong(AVAILABLE_OUTGOING_BITRATE, 0);
                    currentRoundTripTimeMs = rtcStatObject.optDouble(CURRENT_ROUND_TRIP_TIME, 0) * 1000;
                    timestamp = rtcStatObject.optLong(TIMESTAMP, 0);
                }
            } else if (REMOTE_INBOUND_RTP.equals(statType)) {
                if (VIDEO.equals(kind)) {
                    jitter = rtcStatObject.optDouble(JITTER, -1);
                }
            }
        }
        prevVideoTimestamp = timestamp;

        NetworkQualityStats networkQualityStats = new NetworkQualityStats.Builder()
                .videoStats(videoStatsList)
                .audioStats(audioStats)
                .jitter(jitter)
                .currentRoundTripTimeMs(currentRoundTripTimeMs)
                .availableOutgoingBitrate(availableOutgoingBitrate)
                .timestamp(timestamp)
                .build();

        if (direction.equals(OUTBOUND_RTP))
            publisherNetworkQualityStatsList.add(networkQualityStats);
        if (direction.equals(INBOUND_RTP))
            subscriberNetworkQualityStatsList.add(networkQualityStats);
    }

    private RtcTrackStats processOutboundRtpStats(JSONObject rtcStatObject) throws JSONException {
        long ssrc = rtcStatObject.getLong(SSRC);
        String qualityLimitationReason = rtcStatObject.optString(QUALITY_LIMITATION_REASON, NONE);
        String resolution = rtcStatObject.optInt(FRAME_WIDTH, 0)
                + "x" + rtcStatObject.optInt(FRAME_HEIGHT, 0);
        int frameRate = rtcStatObject.optInt(FRAMES_PER_SECOND, 0);
        int pliCount = rtcStatObject.optInt(PLI_COUNT, 0);
        int nackCount = rtcStatObject.optInt(NACK_COUNT, 0);
        long bytesSent = rtcStatObject.optInt(BYTES_SENT, 0);
        long bitrateKbps = calculateVideoBitrateKbps(ssrc,
                rtcStatObject.optLong(TIMESTAMP, 0),
                bytesSent);

        return new RtcTrackStats.Builder()
                .ssrc(ssrc)
                .qualityLimitationReason(qualityLimitationReason)
                .resolution(resolution)
                .frameRate(frameRate)
                .pliCount(pliCount)
                .nackCount(nackCount)
                .bytesSent(bytesSent)
                .bitrateKbps(bitrateKbps)
                .build();


    }

    private final SubscriberKit.SubscriberRtcStatsReportListener subscriberRtcStatsReportListener = (subscriberKit, subscriberRtcStats) -> {
        try {
            JSONArray rtcStatsJsonArray = new JSONArray(subscriberRtcStats);
            processRtcStats(rtcStatsJsonArray, INBOUND_RTP);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    };

    private CallbackQualityStats calculateCallbackQualityStats() {
        Optional<SubVideoStats> latestSubVideoStats = getLastElement(subVideoStatsList);
        Optional<SubAudioStats> latestSubAudioStats = getLastElement(subAudioStatsList);
        Optional<NetworkQualityStats> latestPublisherNetworkQualityStats = getLastElement(publisherNetworkQualityStatsList);
        Optional<NetworkQualityStats> latestSubscriberNetworkQualityStats = getLastElement(subscriberNetworkQualityStatsList);

        if (latestSubVideoStats.isPresent() && latestSubAudioStats.isPresent()
                && latestPublisherNetworkQualityStats.isPresent() && latestSubscriberNetworkQualityStats.isPresent()) {
            return new CallbackQualityStats.Builder()
                    .sentVideoBitrateKbps(latestPublisherNetworkQualityStats.get().getTotalVideoKbsSent())
                    .sentAudioBitrateKbps(latestPublisherNetworkQualityStats.get().getAudioStats().getBitrateKbps())
                    .receivedAudioBitrateKbps(latestSubAudioStats.get().getAudioBitrateKbps())
                    .receivedVideoBitrateKbps(latestSubVideoStats.get().getVideoBytesKbsReceived())
                    .currentRoundTripTimeMs(latestPublisherNetworkQualityStats.get().getCurrentRoundTripTimeMs())
                    .availableOutgoingBitrate(latestPublisherNetworkQualityStats.get().getAvailableOutgoingBitrate())
                    .audioPacketLostRatio(latestSubAudioStats.get().getAudioPacketLostRatio())
                    .videoPacketLostRatio(latestSubVideoStats.get().getVideoPacketLostRatio())
                    .timestamp(latestPublisherNetworkQualityStats.get().getTimestamp())
                    .jitter(latestSubscriberNetworkQualityStats.get().getJitter())
                    .qualityLimitationReason(latestPublisherNetworkQualityStats.get().getQualityLimitationReason())
                    .sentVideoResolution(latestPublisherNetworkQualityStats.get().getResolutionBySrc())
                    .receivedVideoResolution(latestSubscriberNetworkQualityStats.get().getResolutionBySrc())
                    .isScalableVideo(latestPublisherNetworkQualityStats.get().isScalableVideo())
                    .build();
        }
        return null;
    }

    private QualityTestResult getRecommendedSetting() {
        final int SAMPLE_SIZE = 5;
        Queue<Long> outgoingBitrateSamples = new LinkedList<>();
        double estimatedOutgoingBitrate = 0.0;
        boolean scalableVideo = publisherNetworkQualityStatsList.stream()
                .anyMatch(NetworkQualityStats::isScalableVideo);

        // Collect the last few outgoing bitrates
        for (NetworkQualityStats stats : publisherNetworkQualityStatsList) {
            if (outgoingBitrateSamples.size() == SAMPLE_SIZE) {
                outgoingBitrateSamples.poll();
            }
            outgoingBitrateSamples.offer(stats.getAvailableOutgoingBitrate());
        }

        // Estimate the available outgoing bitrate by i size
        int i = 0;
        for (long bitrate : outgoingBitrateSamples) {
            estimatedOutgoingBitrate += (bitrate - estimatedOutgoingBitrate) / (i + 1);
            i++;
        }

        Log.d(LOG_TAG, "Estimated available outgoing bitrate: " + estimatedOutgoingBitrate);

        // Determine the recommended setting based on quality thresholds
        for (QualityThreshold threshold : qualityThresholds) {
            double targetBitrate = scalableVideo ? threshold.getTargetBitrateSimulcast() : threshold.getTargetBitrate();
            if (estimatedOutgoingBitrate >= targetBitrate * RATIO_TARGET_BITRATE) {
                Log.d(LOG_TAG, "Recommended Bitrate: " + threshold.getRecommendedSetting());
                return new QualityTestResult(threshold.getRecommendedSetting());
            }
        }

        Log.d(LOG_TAG, "Bitrate is too low for video");
        return new QualityTestResult("Bitrate is too low for video");
    }

    private <T> Optional<T> getLastElement(List<T> list) {
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(list.size() - 1));
    }

    QualityThreshold[] qualityThresholds = new QualityThreshold[]{
            new QualityThreshold(4000000, 5550000, "1920x1080 @ 30FPS"),
            new QualityThreshold(2500000, 3150000, "1280x720 @ 30FPS"),
            new QualityThreshold(1200000, 1550000, "960x540 @ 30FPS"),
            new QualityThreshold(500000, 650000, "640x360 @ 30FPS"),
            new QualityThreshold(300000, 350000, "480x270 @ 30FPS"),
            new QualityThreshold(150000, 150000, "320x180 @ 30FPS")
    };

}
