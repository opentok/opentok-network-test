package com.opentok.qualitystats.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.opentok.android.Publisher;
import com.opentok.qualitystats.sample.models.NetworkQualityTestCallbackListener;
import com.opentok.qualitystats.sample.models.NetworkQualityTestConfig;
import com.opentok.qualitystats.sample.models.stats.CallbackQualityStats;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    static final String LOGTAG = "quality-stats-demo";
    private static final String SESSION_ID = "2_MX40NzczMDk4MX5-MTY5MzIxMDM4NjgwM355WjdoT05KR1REVEtub1FHZzkrYTZUc0J-fn4";
    private static final String TOKEN = "T1==cGFydG5lcl9pZD00NzczMDk4MSZzaWc9Zjc1YmI4NWRmZDQwOTI5MzBhOGEwODc2NWFlZTI5YWNmZGQwMDZlMDpzZXNzaW9uX2lkPTJfTVg0ME56Y3pNRGs0TVg1LU1UWTVNekl4TURNNE5qZ3dNMzU1V2pkb1QwNUtSMVJFVkV0dWIxRkhaemtyWVRaVWMwSi1mbjQmY3JlYXRlX3RpbWU9MTY5MzIxMDM4NyZub25jZT0wLjE5MDYwODMyNTQyODEwODU3JnJvbGU9bW9kZXJhdG9yJmV4cGlyZV90aW1lPTE2OTU4MDIzODcmaW5pdGlhbF9sYXlvdXRfY2xhc3NfbGlzdD0=";
    private static final String APIKEY = "47730981";
    private final List<Long> availableOutgoingNitrateResult = new ArrayList<>();
    private final List<Long> sentVideoBitrateResults = new ArrayList<>();
    private final List<Long> sentAudioBitrateResults = new ArrayList<>();

    private LineChart availableOutgoingBitrateChart;
    private LineChart videoUploadSpeedChart;
    private LineChart audioUploadSpeedChart;
    private TextView statsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        availableOutgoingBitrateChart = findViewById(R.id.chart);
        statsTextView = findViewById(R.id.statsTextView);
        videoUploadSpeedChart = findViewById(R.id.videoUploadSpeedChart);
        audioUploadSpeedChart = findViewById(R.id.audioUploadSpeedChart);

        // Setup NetworkQualityTestConfig
        NetworkQualityTestConfig config = new NetworkQualityTestConfig.Builder()
                .sessionId(SESSION_ID)
                .apiKey(APIKEY)
                .token(TOKEN)
                .resolution(Publisher.CameraCaptureResolution.HIGH_1080P)
                .testDurationSec(30)
                .build();

        NetworkQualityTest networkQualityTest = new NetworkQualityTest(this, config,
                new NetworkQualityTestCallbackListener() {
                    @Override
                    public void onQualityTestResults(String recommendedSetting) {
                        Log.d(LOGTAG, "Recommended resolution: " + recommendedSetting);
                        showRecommendedSettingPopup(recommendedSetting);
                    }

                    @Override
                    public void onQualityTestStatsUpdate(CallbackQualityStats stats) {
                        if (stats == null)
                            return;

                        String sentVideoInfo = "Sent Video Bitrate: " + stats.getSentVideoBitrateKbps() + " Kbps\n"
                                + "Sent Audio Bitrate: " + stats.getSentAudioBitrateKbps() + " Kbps \n"
                                + "Sent Video Resolution: " + stats.getSentVideoResolution() + " \n"
                                + "Received Video Bitrate: " + stats.getReceivedVideoBitrateKbps() + " Kbps\n"
                                + "Received Audio Bitrate: " + stats.getReceivedAudioBitrateKbps() + " Kbps\n"
                                + "Received Video Resolution: " + stats.getReceivedVideoResolution() + "\n"
                                + "Quality Limitation Reason: " + stats.getQualityLimitationReason() + "\n"
                                + "Current Round Trip Time: " + stats.getCurrentRoundTripTimeMs() + " ms";

                        statsTextView.setText(sentVideoInfo);

                        Log.d(LOGTAG, "---------------------------------------------------------------");
                        Log.d(LOGTAG, "Sent Video Bitrate: " + stats.getSentVideoBitrateKbps() + " Kbps");
                        Log.d(LOGTAG, "Sent Audio Bitrate: " + stats.getSentAudioBitrateKbps() + " Kbps");
                        Log.d(LOGTAG, "Received Audio Bitrate: " + stats.getReceivedAudioBitrateKbps() + " Kbps");
                        Log.d(LOGTAG, "Received Video Bitrate: " + stats.getReceivedVideoBitrateKbps() + " Kbps");
                        Log.d(LOGTAG, "Current Round Trip Time: " + stats.getCurrentRoundTripTimeMs() + " ms");
                        Log.d(LOGTAG, "Available Outgoing Bitrate: " + stats.getAvailableOutgoingBitrate() + " bps");
                        Log.d(LOGTAG, "Audio Packet Lost Ratio  " + stats.getAudioPacketLostRatio() * 100 + "%");
                        Log.d(LOGTAG, "Video Packet Lost Ratio  " + stats.getVideoPacketLostRatio() * 100 + "%");
                        Log.d(LOGTAG, "Jitter: " + stats.getJitter());
                        Log.d(LOGTAG, "Quality Limitation Reason: " + stats.getQualityLimitationReason());
                        Log.d(LOGTAG, "Sent video resolution: " + stats.getSentVideoResolution());
                        Log.d(LOGTAG, "Received video resolution: " + stats.getReceivedVideoResolution());
                        Log.d(LOGTAG, "Scalable video ? " + stats.isScalableVideo());
                        Log.d(LOGTAG, "---------------------------------------------------------------");
                        availableOutgoingNitrateResult.add(stats.getAvailableOutgoingBitrate());
                        sentVideoBitrateResults.add(stats.getSentVideoBitrateKbps());
                        updateChart(videoUploadSpeedChart, sentVideoBitrateResults, "Sent Video Bitrate");

                        sentAudioBitrateResults.add(stats.getSentAudioBitrateKbps());
                        updateChart(audioUploadSpeedChart, sentAudioBitrateResults, "Sent Audio Bitrate");
                        updateChart(availableOutgoingBitrateChart, availableOutgoingNitrateResult, "AvailableOutgoingBitrate");
                    }


                    @Override
                    public void onError(String error) {
                        Log.d(LOGTAG, "Error " + error);
                    }
                });

        // Start the quality test
        networkQualityTest.startTest();
    }


    private void updateChart(LineChart chart, List<Long> results, String label) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            // i is used as the x-value to represent time in seconds
            entries.add(new Entry(i, results.get(i).floatValue()));
        }

        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(Color.BLUE); // Set the line color
        dataSet.setCircleColor(Color.BLUE); // Set the circle color
        dataSet.setLineWidth(2f); // Set the line width
        dataSet.setCircleRadius(2f); // Set the circle radius
        dataSet.setDrawCircleHole(false); // Disable the circle hole
        dataSet.setDrawValues(false); // Hide the values displayed on the chart
        dataSet.setDrawFilled(true); // Enable filling below the line
        dataSet.setFormLineWidth(1f); // Set the line width of the legend form
        dataSet.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f)); // Set the line dash effect of the legend form
        dataSet.setFormSize(10.f); // Set the size of the legend form

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // Set the minimum values for the axes to 0
        chart.getXAxis().setAxisMinimum(0f);
        chart.getAxisLeft().setAxisMinimum(0f);
        if (label.equals("AvailableOutgoingBitrate")) {
            chart.getAxisLeft().setAxisMaximum(5550000f); // Set the maximum value to 5550000
        }
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
        yAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + " kb/s"; // Format the Y-axis value
            }
        });
        chart.invalidate(); // refresh the chart
    }

    private void showRecommendedSettingPopup(String recommendedSetting) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recommended Setting");
        builder.setMessage("Recommended Setting: " + recommendedSetting);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }


}
