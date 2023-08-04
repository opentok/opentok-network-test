package com.opentok.qualitystats.sample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;
import com.opentok.qualitystats.sample.models.QualityStats;

import java.util.List;

import lombok.Builder;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;



import pub.devrel.easypermissions.AppSettingsDialog;


public class VideoQualityTest extends Activity implements
        Session.SessionListener,
        PublisherKit.PublisherListener,
        SubscriberKit.SubscriberListener,
        EasyPermissions.PermissionCallbacks {

    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;
    private final VideoCallTesterListener listener;
    private static final int RC_VIDEO_APP_PERM = 124;
    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    static final String LOGTAG = "quality-stats";

    private final VideoCallTesterConfig config;

    private final Context context;

    public interface VideoCallTesterListener {
        void onTestResult(String recommendedSetting);
        void onTestUpdate(QualityStats stats);
        void onError(String error);
    }

    @Builder
    public VideoQualityTest(@NonNull Context context, @NonNull VideoCallTesterConfig config,
                            @NonNull VideoCallTesterListener listener) {

        this.context = context;
        this.config = config;
        this.listener = listener;
    }


    public void startTest() {
        // Initialize and connect to the OpenTok session
        mSession.setSessionListener(sessionListener);

        // Initialize the Publisher
        mPublisher = new Publisher.Builder(context).resolution(config.getResolution()).build();
        mPublisher.setPublisherListener(publisherListener);

        // Connect to the session
        mSession.connect(config.getToken());
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {

        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {
            startTest();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_video_app), RC_VIDEO_APP_PERM, perms);
        }
    }
    private final Session.SessionListener sessionListener = new Session.SessionListener() {
        @Override
        public void onConnected(Session session) {
            // Publisher and Subscriber logic here
            mSession.publish(mPublisher);
        }

        @Override
        public void onDisconnected(Session session) {
            Log.i(LOGTAG, "Session is disconnected");

            mPublisher = null;
            mSubscriber = null;
            mSession = null;
            //stop test()
        }

        @Override
        public void onStreamReceived(Session session, Stream stream) {
            Log.i(LOGTAG, "Session onStreamReceived");
        }

        @Override
        public void onStreamDropped(Session session, Stream stream) {
            Log.i(LOGTAG, "Session onStreamDropped");
        }

        @Override
        public void onError(Session session, OpentokError opentokError) {
            Log.i(LOGTAG, "Session error: " + opentokError.getMessage());
            session.disconnect();
        }

    };

    private final PublisherKit.PublisherListener publisherListener = new PublisherKit.PublisherListener() {
        @Override
        public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
            // Subscriber logic here
            mSubscriber = new Subscriber.Builder(context, stream).build();
            mSession.subscribe(mSubscriber);
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
        }

        // Other methods (e.g., onStreamDestroyed, onError, etc.)
    };

    // Other methods (e.g., checkVideoStats, checkAudioStats, checkVideoQuality, etc.)

    private void generateVideoQualityResult() {
        if (mSession != null) {
            Log.i(LOGTAG, "Check video quality stats data");
            mSession.disconnect();
            String recommendedSetting = getRecommendedSetting();
            listener.onTestResult(recommendedSetting);
        }
    }



    private String getRecommendedSetting() {
        /**Log.d(LOGTAG, "Recommended Bitrate: " + lastAvailableOutgoingBitrate);
        if (lastAvailableOutgoingBitrate != null) {
            for (MainActivity.QualityThreshold threshold : qualityThresholds) {
                if (lastAvailableOutgoingBitrate >= threshold.targetBitrate) {
                    Log.d(LOGTAG, "Recommended Bitrate: " + threshold.recommendedSetting);
                    return threshold.recommendedSetting;
                }
            }
        }
        Log.d(LOGTAG, "Bitrate is too low for video");
        **/
        return "Bitrate is too low for video";
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
        Log.d(LOGTAG, "Session error: " + opentokError.getMessage());
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
    }

    @Override
    public void onConnected(SubscriberKit subscriberKit) {
        Log.i(LOGTAG, "Subscriber onConnected");
        // Mute Subscriber Audio
        subscriberKit.setAudioVolume(0);

    }

    @Override
    public void onDisconnected(SubscriberKit subscriberKit) {
        Log.i(LOGTAG, "Subscriber onDisconnected");
    }

    @Override
    public void onError(SubscriberKit subscriberKit, OpentokError opentokError) {
        Log.i(LOGTAG, "Subscriber error: " + opentokError.getMessage());
        //stopTest(su);
    }

    private void subscribeToStream(Stream stream) {

        mSubscriber = new Subscriber.Builder(context, stream).build();
        mSubscriber.setSubscriberListener(this);
        mSession.subscribe(mSubscriber);
        mSubscriber.setVideoStatsListener((subscriber, stats) -> {
           /** if (mStartTestTime == 0) {
                mStartTestTime = System.currentTimeMillis() / 1000;
            }
            checkVideoStats(stats);
            mPublisher.setRtcStatsReportListener(publisherRtcStatsReportListener);
            //check quality of the video call after TIME_VIDEO_TEST seconds
            if (((System.currentTimeMillis() / 1000 - mStartTestTime) > TIME_VIDEO_TEST) && !audioOnly) {
                checkVideoQualwity();
            }**/
        });

        // mSubscriber.setAudioStatsListener((subscriber, stats) -> checkAudioStats(stats));
    }

    private void unsubscribeFromStream(Stream stream) {
        if (mSubscriber.getStream().equals(stream)) {
            mSubscriber = null;
        }
    }




    private void checkAudioQuality() {
        if (mSession != null) {
          /**  Log.i(LOGTAG, "Check audio quality stats data");
            if (mAudioBw < 25000 || mAudioPLRatio > 0.05) {
                showAlert("Not good", "You can't connect successfully");
            } else {
                showAlert("Voice-only", "Your bandwidth is too low for video");
            }
           **/
        }
    }
}
