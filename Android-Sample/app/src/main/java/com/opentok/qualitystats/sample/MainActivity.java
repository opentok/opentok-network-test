package com.opentok.qualitystats.sample;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;
import com.opentok.qualitystats.sample.models.MediaStatsEntry;
import com.opentok.qualitystats.sample.models.PublisherStats;
import com.opentok.qualitystats.sample.models.QualityTestResult;
import com.opentok.qualitystats.sample.models.QualityThreshold;
import com.opentok.qualitystats.sample.models.SubscriberAudioStats;
import com.opentok.qualitystats.sample.models.SubscriberVideoStats;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends Activity implements
        Session.SessionListener,
        PublisherKit.PublisherListener,
        SubscriberKit.SubscriberListener,
        EasyPermissions.PermissionCallbacks {

    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;

    private static final int SUB_STATS_INTERVAL = 1;

    private LineChart chartPublisherVideoBitrate;
    private LineChart chartSubscriberVideoBitrate;


    static final String LOGTAG = "quality-stats-demo";
    private static final String SESSION_ID = "1_MX40NzUyMTkyMX5-MTY5MDI4MjQwNzI4Mn5IcHI1VUg4QXV2dTVyZmZkUkhyTTFyazJ-fn4";
    private static final String TOKEN = "T1==cGFydG5lcl9pZD00NzUyMTkyMSZzaWc9OTA3N2ExODk3NzBmOTZiYTIyNDI2MzU3ZDY4YWNlYTE0ZTUxNWViNzpzZXNzaW9uX2lkPTFfTVg0ME56VXlNVGt5TVg1LU1UWTVNREk0TWpRd056STRNbjVJY0hJMVZVZzRRWFYyZFRWeVptWmtVa2h5VFRGeWF6Si1mbjQmY3JlYXRlX3RpbWU9MTY5MDI4MjQyNyZub25jZT0wLjIxOTYwMjY4MTI4NzI4NCZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNjkyODc0NDI2JmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";
    private static final String APIKEY = "47521921";
    private static final int TEST_DURATION = 30; //test quality duration in seconds
    private static final int TIME_WINDOW = 1; //3 seconds
    private static final int TIME_VIDEO_TEST = 15; //time interval to check the video quality in seconds
    private final HashMap<Long, JSONObject> ssrcStatsMap = new HashMap<>();
    private TextView statsTextView;
    private TextView statsTextViewSub;

    private final Handler rtcStatsHandler = new Handler();

    private Runnable rtcStatsRunnable;

    private LineChart chart;
    private final List<Double> testResults = new ArrayList<>();
    private final Double lastAvailableOutgoingBitrate = null;

    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;

    private long prevVideoTimestamp = 0;

    private long mStartTestTime = 0;

    private final boolean audioOnly = false;

    private final Handler mHandler = new Handler();

    private AlertDialog dialog;

    private final Map<Long, Long> ssrcToPrevBytesSent = new HashMap<>();

    private final List<PublisherStats> publisherStatsList = new ArrayList<>();
    private final List<SubscriberVideoStats> subscriberVideoStatsList = new ArrayList<>();
    private final List<SubscriberAudioStats> subscriberAudioStatsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chart = findViewById(R.id.chart);
        statsTextView = findViewById(R.id.statsTextView);
        statsTextViewSub = findViewById(R.id.statsSubscriber);

        requestPermissions();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (mSession != null) {
            mSession.disconnect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

        Log.d(LOGTAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

        Log.d(LOGTAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this)
                    .setTitle(getString(R.string.title_settings_dialog))
                    .setRationale(getString(R.string.rationale_ask_again))
                    .setPositiveButton(getString(R.string.setting))
                    .setNegativeButton(getString(R.string.cancel))
                    .setRequestCode(RC_SETTINGS_SCREEN_PERM)
                    .build()
                    .show();
        }
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {

        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {
            sessionConnect();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_video_app), RC_VIDEO_APP_PERM, perms);
        }
    }

    public void sessionConnect() {
        Log.i(LOGTAG, "Connecting session");
        if (mSession == null) {
            mSession = new Session.Builder(this, APIKEY, SESSION_ID).build();
            mSession.setSessionListener(this);

            //mProgressDialog = ProgressDialog.show(this, "Checking your available bandwidth", "Please wait");
            mSession.connect(TOKEN);
        }
    }

    @Override
    public void onConnected(Session session) {
        Log.i(LOGTAG, "Session is connected");

        mPublisher = new Publisher.Builder(this).resolution(Publisher.CameraCaptureResolution.HIGH_1080P).build();
        mPublisher.setPublisherListener(this);
        mPublisher.setAudioFallbackEnabled(false);
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOGTAG, "Session is disconnected");
        mPublisher = null;
        mSubscriber = null;
        mSession = null;
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.i(LOGTAG, "Session error: " + opentokError.getMessage());
        showAlert("Error", "Session error: " + opentokError.getMessage());
    }

    private QualityTestResult getRecommendedSetting() {
        Log.d(LOGTAG, "Recommended Bitrate: " + lastAvailableOutgoingBitrate);
        for (PublisherStats publisherStats : publisherStatsList) {
            String videoQualityLimitationReason = publisherStats.getQualityLimitationReason();
            long totalVideoKbsSent = publisherStats.getTotalVideoKbsSent();
            double totalVideoBytesSent = publisherStats.getTotalVideoBytesSent();
            boolean isSimulcast = publisherStats.isScalableVideo();
            // Log the values
            Log.i(LOGTAG, "Video Quality Limitation Reason: " + videoQualityLimitationReason);
            Log.i(LOGTAG, "Total Video Kbps Sent: " + totalVideoKbsSent);
            Log.i(LOGTAG, "Total Video Bytes Sent: " + totalVideoBytesSent);
            Log.i(LOGTAG, "Simulcast enable: " + isSimulcast);

        }
        if (lastAvailableOutgoingBitrate != null) {
            for (QualityThreshold threshold : qualityThresholds) {
                if (lastAvailableOutgoingBitrate >= threshold.getTargetBitrate()) {
                    Log.d(LOGTAG, "Recommended Bitrate: " + threshold.getRecommendedSetting());
                    return new QualityTestResult(threshold.getRecommendedSetting());
                }
            }
        }
        Log.d(LOGTAG, "Bitrate is too low for video");
        return new QualityTestResult( "Bitrate is too low for video");
    }


    private void updateChart() {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < testResults.size(); i++) {
            // i is used as the x-value to represent time in seconds
            entries.add(new Entry(i, testResults.get(i).floatValue()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Available Outgoing Bitrate");
        dataSet.setColor(Color.BLUE); // Set the line color
        dataSet.setCircleColor(Color.BLUE); // Set the circle color
        dataSet.setLineWidth(2f); // Set the line width
        dataSet.setCircleRadius(3f); // Set the circle radius
        dataSet.setDrawCircleHole(true); // Disable the circle hole
        dataSet.setDrawValues(false); // Hide the values displayed on the chart
        dataSet.setDrawFilled(true); // Enable filling below the line
        dataSet.setFormLineWidth(1f); // Set the line width of the legend form
        dataSet.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f)); // Set the line dash effect of the legend form
        dataSet.setFormSize(15.f); // Set the size of the legend form

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // Set the minimum values for the axes to 0
        chart.getXAxis().setAxisMinimum(0f);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setAxisMaximum(5550000f); // Set the maximum value to 5550000

        // Disable the right y-axis
        chart.getAxisRight().setEnabled(false);

        // Set the x-axis to display at the bottom
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        // Set the description text
        chart.getDescription().setText("Time (s)");

        // Customize the chart appearance
        chart.setDrawGridBackground(false); // Disable the grid background
        chart.setDrawBorders(false); // Disable the borders
        chart.getLegend().setEnabled(false); // Disable the legend

        // Customize the x-axis appearance
        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawGridLines(false); // Disable the x-axis grid lines
        xAxis.setDrawAxisLine(false); // Disable the x-axis line

        // Customize the y-axis appearance
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setDrawGridLines(false); // Disable the y-axis grid lines
        yAxis.setDrawAxisLine(false); // Disable the y-axis line

        chart.invalidate(); // refresh the chart
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOGTAG, "Session onStreamDropped");
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOGTAG, "Session onStreamReceived");
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.i(LOGTAG, "Publisher onStreamCreated");
        if (mSubscriber == null) {
            subscribeToStream(stream);
        }
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.i(LOGTAG, "Publisher onStreamDestroyed");
        if (mSubscriber == null) {
            unsubscribeFromStream(stream);
        }
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Log.i(LOGTAG, "Publisher error: " + opentokError.getMessage());
        showAlert("Error", "Publisher error: " + opentokError.getMessage());
    }

    @Override
    public void onConnected(SubscriberKit subscriberKit) {
        Log.i(LOGTAG, "Subscriber onConnected");
        // Mute Subscriber Audio
        subscriberKit.setAudioVolume(0);
        mHandler.postDelayed(statsRunnable, TEST_DURATION * 1000);
        // Initialize and post the Runnable to get RTC stats every second
        rtcStatsRunnable = new Runnable() {
            @Override
            public void run() {
                if (mPublisher != null) {
                    mPublisher.getRtcStatsReport();
                    rtcStatsHandler.postDelayed(this, 500);
                }
            }
        };
        rtcStatsHandler.post(rtcStatsRunnable);

    }

    @Override
    public void onDisconnected(SubscriberKit subscriberKit) {
        Log.i(LOGTAG, "Subscriber onDisconnected");
    }

    @Override
    public void onError(SubscriberKit subscriberKit, OpentokError opentokError) {
        Log.i(LOGTAG, "Subscriber error: " + opentokError.getMessage());
        showAlert("Error", "Subscriber error: " + opentokError.getMessage());
    }

    private void subscribeToStream(Stream stream) {

        mSubscriber = new Subscriber.Builder(MainActivity.this, stream).build();
        mSubscriber.setSubscriberListener(this);
        mSession.subscribe(mSubscriber);
        mSubscriber.setVideoStatsListener((subscriber, stats) -> {
            if (mStartTestTime == 0) {
                mStartTestTime = System.currentTimeMillis() / 1000;
            }
            onSubscriberVideoStats(stats);
            mPublisher.setRtcStatsReportListener(publisherRtcStatsReportListener);
            //check quality of the video call after TIME_VIDEO_TEST seconds
            if (((System.currentTimeMillis() / 1000 - mStartTestTime) > TIME_VIDEO_TEST) && !audioOnly) {
               getRecommendedSetting();
            }
        });
        mSubscriber.setAudioStatsListener((subscriber, stats) -> onSubscriberAudioStats(stats));

    }

    private void unsubscribeFromStream(Stream stream) {
        if (mSubscriber.getStream().equals(stream)) {
            mSubscriber = null;
        }
    }

    private final Queue<SubscriberKit.SubscriberVideoStats > videoStatsQueue = new LinkedList<>();
    private final Queue<SubscriberKit.SubscriberAudioStats > audioStatsQueue = new LinkedList<>();

    private void onSubscriberVideoStats(SubscriberKit.SubscriberVideoStats videoStats) {
        double videoTimestamp = videoStats.timeStamp ;
        // Initialize values for video
        if (videoStatsQueue.isEmpty()) {
            videoStatsQueue.add(videoStats);
            return;
        }
        SubscriberKit.SubscriberVideoStats  previousVideoStats = videoStatsQueue.peek();

        if ((videoTimestamp - previousVideoStats.timeStamp) < TIME_WINDOW * 1000)
        {
            return;
        }

        // Video Stats
        long videoPacketsLostInterval = videoStats.videoPacketsLost - previousVideoStats.videoPacketsLost;
        long videoPacketsReceivedInterval = videoStats.videoPacketsReceived - previousVideoStats.videoPacketsReceived;
        long videoTotalPacketsInterval = videoPacketsLostInterval + videoPacketsReceivedInterval;
        double videoPLRatio = 0.0;
        if (videoTotalPacketsInterval > 0) {
            videoPLRatio = (double) videoPacketsLostInterval / (double) videoTotalPacketsInterval;
        }

        long elapsedTimeMs = (long) (videoTimestamp - previousVideoStats.timeStamp);
        long bytesSentDiff = videoStats.videoBytesReceived - previousVideoStats.videoBytesReceived;
        long videoBitrateKbps = (long) ((bytesSentDiff * 8) / (elapsedTimeMs / 1000.0)) /1000;

        videoStatsQueue.add(videoStats);

        subscriberVideoStatsList.add(SubscriberVideoStats.builder()
                .videoBytesKbsReceived(videoBitrateKbps)
                .videoBytesReceived(videoStats.videoBytesReceived)
                .timestamp(videoStats.timeStamp)
                .videoPacketLostRatio(videoPLRatio)
                .build());

    }

    private void onSubscriberAudioStats(SubscriberKit.SubscriberAudioStats audioStats) {
        double audioTimestamp = audioStats.timeStamp;

        // Initialize values for audio
        if (audioStatsQueue.isEmpty()) {
            audioStatsQueue.add(audioStats);
            return;
        }

        SubscriberKit.SubscriberAudioStats previousAudioStats = audioStatsQueue.peek();

        // Check if the time difference is within the time window
        if ((audioTimestamp - previousAudioStats.timeStamp) < TIME_WINDOW * 1000) {
            return;
        }

        // Audio Stats
        long audioPacketsLostInterval = audioStats.audioPacketsLost - previousAudioStats.audioPacketsLost;
        long audioPacketsReceivedInterval = audioStats.audioBytesReceived - previousAudioStats.audioBytesReceived;
        long audioTotalPacketsInterval = audioPacketsLostInterval + audioPacketsReceivedInterval;

        double audioPLRatio = 0.0;
        if (audioTotalPacketsInterval > 0) {
            audioPLRatio = (double) audioPacketsLostInterval / (double) audioTotalPacketsInterval;
        }

        // Calculate audio bandwidth
        long elapsedTimeMs = (long) (audioTimestamp - previousAudioStats.timeStamp);
        long audioBw = (long) ((8 * (audioStats.audioBytesReceived - previousAudioStats.audioBytesReceived)) / (elapsedTimeMs / 1000.0));

        subscriberAudioStatsList.add(SubscriberAudioStats.builder()
                .audioBitrateKbps(audioBw)
                .audioBytesReceived(audioStats.audioBytesReceived)
                .timestamp(audioStats.timeStamp)
                .audioPacketLostRatio(audioPLRatio)
                .build());
    }


    private void showAlert(String title, String Message) {
        dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage(Message)
                .setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                try {
                                    finish();
                                } catch (Throwable throwable) {
                                    throwable.printStackTrace();
                                }
                            }
                        }).setIcon(R.drawable.ic_launcher)
                .show();
    }

    private final PublisherKit.PublisherRtcStatsReportListener publisherRtcStatsReportListener = (publisherKit, publisherRtcStats) -> {
        for (PublisherKit.PublisherRtcStats s : publisherRtcStats) {
            try {
                JSONArray rtcStatsJsonArray = new JSONArray(s.jsonArrayOfReports);
                List<MediaStatsEntry> videoStatsList = new ArrayList<>();
                MediaStatsEntry audioStats = null;

                double jitter = 0.0;
                long availableOutgoingBitrate = 0;
                long timestamp = 0;
                long currentRoundTripTimeMs = 0;

                for (int i = 0; i < rtcStatsJsonArray.length(); i++) {
                    JSONObject rtcStatObject = rtcStatsJsonArray.getJSONObject(i);
                    String statType = rtcStatObject.getString("type");
                    String kind = rtcStatObject.optString("kind", "none");
                    // Handle video and audio stats
                    if (statType.equals("outbound-rtp") && (kind.equals("video") || kind.equals("audio"))) {
                        long ssrc = rtcStatObject.getLong("ssrc");
                        String qualityLimitationReason = rtcStatObject.optString("qualityLimitationReason", "none");
                        String resolution = rtcStatObject.optInt("frameWidth", 0) + "x" + rtcStatObject.optInt("frameHeight", 0);
                        int framerate = rtcStatObject.optInt("framesPerSecond", 0);
                        int pliCount = rtcStatObject.optInt("pliCount", 0);
                        int nackCount = rtcStatObject.optInt("nackCount", 0);
                        long bytesSent = rtcStatObject.optInt("bytesSent", 0);
                        long bitrateKbps = calculateVideoBitrateKbps(ssrc,
                                rtcStatObject.optLong("timestamp",0),
                                bytesSent);


                        MediaStatsEntry mediaStatsEntry = MediaStatsEntry.builder()
                                .ssrc(ssrc)
                                .qualityLimitationReason(qualityLimitationReason)
                                .resolution(resolution)
                                .framerate(framerate)
                                .pliCount(pliCount)
                                .nackCount(nackCount)
                                .bytesSent(bytesSent)
                                .bitrateKbps(bitrateKbps)
                                .build();

                        if ("video".equals(kind)) {
                            videoStatsList.add(mediaStatsEntry);
                        } else if ("audio".equals(kind)) {
                            audioStats = mediaStatsEntry;
                        }
                    }
                    // Handle candidate-pair stats
                    else if (statType.equals("candidate-pair")) {
                        boolean isNominated = rtcStatObject.optBoolean("nominated", false);
                        if (isNominated) {
                            availableOutgoingBitrate = rtcStatObject.optLong("availableOutgoingBitrate", 0);
                            currentRoundTripTimeMs = rtcStatObject.optLong("currentRoundTripTime", 0) * 1000;
                            timestamp = rtcStatObject.optLong("timestamp", 0);
                        }
                    }
                }

                prevVideoTimestamp = timestamp;

                PublisherStats publisherStats = PublisherStats.builder()
                        .videoStats(videoStatsList)
                        .audioStats(audioStats)
                        .jitter(jitter)
                        .currentRoundTripTimeMs(currentRoundTripTimeMs)
                        .availableOutgoingBitrate(availableOutgoingBitrate)
                        .timestamp(timestamp)
                        .build();

                publisherStatsList.add(publisherStats);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private long calculateVideoBitrateKbps(long ssrc, long timestamp, long currentBytesSent) {
        long videoBitrateKbps = 0;
        if (prevVideoTimestamp > 0 && ssrcToPrevBytesSent.containsKey(ssrc)) {
            long elapsedTimeMs = timestamp - prevVideoTimestamp;
            long prevBytesSent = ssrcToPrevBytesSent.get(ssrc);
            long bytesSentDiff = currentBytesSent - prevBytesSent;
            videoBitrateKbps = (long) ((bytesSentDiff * 8) / (elapsedTimeMs / 1000.0)); // Calculate Kbps
        }
        ssrcToPrevBytesSent.put(ssrc, currentBytesSent);
        return videoBitrateKbps;
    }
    private final Runnable statsRunnable = new Runnable() {

        @Override
        public void run() {
            if (mSession != null) {
                rtcStatsHandler.removeCallbacks(rtcStatsRunnable);
                mSession.disconnect();
                // Stop getting RTC stats

            }
        }
    };

    QualityThreshold[] qualityThresholds = new QualityThreshold[]{
            new QualityThreshold(4000000, 5550000, "1920x1080 @ 30FPS"),
            new QualityThreshold(2500000, 3150000, "1280x720 @ 30FPS"),
            new QualityThreshold(1200000, 1550000, "960x540 @ 30FPS"),
            new QualityThreshold(500000, 650000, "640x360 @ 30FPS"),
            new QualityThreshold(300000, 350000, "480x270 @ 30FPS"),
            new QualityThreshold(150000, 150000, "320x180 @ 30FPS")
    };

}
