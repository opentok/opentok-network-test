OpenTok Android Network Test Sample
===================================

# NetworkQualityTest

The `NetworkQualityTest` class is a utility for testing network quality and gathering statistics during video streaming sessions using the OpenTok platform. It allows you to monitor various network and media statistics, such as video bitrate, audio quality, round-trip time, and more.

The `MainActivity`class is sample shows how to use this OpenTok Android SDK to determine the appropriate audio and video
settings to use in publishing a stream to an OpenTok session. To do this, the app publishes a test
stream to a test session and then uses the API to check the quality of that stream. Based on the
quality, the app determines what the client can successfully publish to an OpenTok session:

* The client can publish an audio-video stream at the specified resolution.

* The client can publish an audio-only stream.

* The client is unable to publish.

The sample app only subscribes to the test stream. It does not subscribe to other streams in the
test session. Do not use the test session for your actual call. Use a separate OpenTok session
(and session ID) to share audio-video streams between clients.

## Usage

1. **Import the Class**

    Make sure you've imported the `NetworkQualityTest` class into your project.

    ```java
    import com.opentok.qualitystats.sample.NetworkQualityTest;
    ```

2. **Instantiate the Class**

    Create an instance of `NetworkQualityTest` by providing the required parameters: the current activity, a configuration object, and a callback listener for receiving the test results.

    ```java
    NetworkQualityTest networkQualityTest = new NetworkQualityTest(
        this,  // Current activity
        networkQualityTestConfig,  // Configuration object
        networkQualityTestCallbackListener  // Callback listener
    );
    ```

3. **NetworkQualityTestConfig**

    The `NetworkQualityTestConfig` class is used to configure the parameters of the network quality test. It provides the following constructor:

    You can configure the following parameters:
    
    - `sessionId`: The session ID of your OpenTok session.
    - `apiKey`: Your OpenTok API key.
    - `token`: The token for connecting to the session.
    - `resolution`: The camera capture resolution for the publisher. Defaults to `HIGH` if not specified.
    - `testDurationSec`: The duration of the test in seconds. Defaults to `30 sec` if not specified.

4. **Handle Callbacks**

    Once the test is running, you'll receive callbacks in the provided listener methods:
    
    - `onQualityTestResults`: Receive the final test results and the recommended setting based on quality thresholds.
    - `onQualityTestStatsUpdate`: Receive real-time statistics updates during the test.
    - `onError`: Handle errors that occur during the test.

    Here an example of ```NetworkQualityTestCallbackListener```:
    ```java
    new NetworkQualityTestCallbackListener() {
        @Override
        public void onQualityTestResults(String recommendedSetting) {
            Log.d(LOGTAG, "Recommended resolution: " + recommendedSetting);
        }

        @Override
        public void onQualityTestStatsUpdate(CallbackQualityStats stats) {
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
            Log.d(LOGTAG, "---------------------------------------------------------------");
        }

        @Override
        public void onError(String error) {
            Log.d(LOGTAG, "Error " + error);
        }

    ```


5. **Start the Test**

    To initiate the network quality test, call the `startTest` method on the `NetworkQualityTest` instance.

    ```java
    networkQualityTest.startTest();
    ```

6. **Stop the test**

    To stop the test, you can call stopTest. If the test is halted before 5 seconds, an error will be triggered.

    ```java
     networkQualityTest.stopTest();
    ```

## Permissions

Before using the `NetworkQualityTest` class, make sure to request the necessary permissions in your AndroidManifest.xml file:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

## Example

You can check `MainActivity` to see a sample usage and interface using `NetworkQualityTest`
