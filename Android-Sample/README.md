OpenTok Android Network Test Sample
===================================

# NetworkQualityTest

The `NetworkQualityTest` class is a utility for testing network quality and gathering statistics during video streaming sessions using the OpenTok platform. It allows you to monitor various network and media statistics and also get the recommanded resoltion: - [Handle Callbacks](#handle-callbacks)


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
   ### Configurable Parameters:

- **`sessionId`**:  
  The session ID of your OpenTok session. This is required to identify which session you are testing.
  - Type: `String`
  
- **`apiKey`**:  
  Your OpenTok API key. This is required for authentication when accessing the OpenTok session.
  - Type: `String`
  
- **`token`**:  
  The token used to connect to the OpenTok session. This is necessary for a participant to join the session.
  - Type: `String`

- **`resolution`**:  
  The camera capture resolution for the publisher. If not specified, the default resolution is set to **HIGH**.
  - Type: `String`
  - Possible Values:
    - **"HIGH"**: HD resolution (1280x720).
    - **"HIGH_1080P"**: 1080p resolution (1920x1080).
    - **"LOW"**: Lowest available resolution (typically 352x288).
    - **"MEDIUM"**: VGA resolution (640x480).
  - Default: `"HIGH"`

- **`testDurationSec`**:  
  The duration of the test, in seconds. If not specified, the test will default to **30 seconds**.
  - Type: `int`
  - Default: `30 sec`

## Handle Callbacks

Once the test is running, you'll receive callbacks in the provided listener methods:

### `onQualityTestResults`
Receive the final stats and the recommended setting based on quality thresholds.

#### Recommended Resolutions:

If the estimated outgoing bitrate meets the required threshold for any of the defined quality levels, the method will return one of the following resolutions:

- **"1920x1080 @ 30FPS"**  
  Recommended if the estimated outgoing bitrate is **greater than or equal to 4 Mbps** (4000000 bits/sec).

- **"1280x720 @ 30FPS"**  
  Recommended if the estimated outgoing bitrate is **greater than or equal to 2.5 Mbps** (2500000 bits/sec) but less than **4 Mbps**.

- **"960x540 @ 30FPS"**  
  Recommended if the estimated outgoing bitrate is **greater than or equal to 1.2 Mbps** (1200000 bits/sec) but less than **2.5 Mbps**.

- **"640x360 @ 30FPS"**  
  Recommended if the estimated outgoing bitrate is **greater than or equal to 500 Kbps** (500000 bits/sec) but less than **1.2 Mbps**.

- **"480x270 @ 30FPS"**  
  Recommended if the estimated outgoing bitrate is **greater than or equal to 300 Kbps** (300000 bits/sec) but less than **500 Kbps**.

- **"320x180 @ 30FPS"**  
  Recommended if the estimated outgoing bitrate is **greater than or equal to 150 Kbps** (150000 bits/sec) but less than **300 Kbps**.

#### Bitrate Too Low for Video:

If the estimated outgoing bitrate is too low to support any video resolution, the method will return:

- **"Bitrate is too low for video"**  
  This message is returned if the estimated outgoing bitrate is **lower than 150 Kbps** (150000 bits/sec).

### `onQualityTestStatsUpdate`
Receive real-time statistics updates during the test.

**Stats Logged:**

- **Sent Video Bitrate** (`stats.getSentVideoBitrateKbps()`):  
  Bitrate (in Kbps) of the video being sent from the local device to the receiver.

- **Sent Audio Bitrate** (`stats.getSentAudioBitrateKbps()`):  
  Bitrate (in Kbps) of the audio being sent from the local device to the receiver.

- **Received Audio Bitrate** (`stats.getReceivedAudioBitrateKbps()`):  
  Bitrate (in Kbps) of the audio being received by the local device from the remote participant.

- **Received Video Bitrate** (`stats.getReceivedVideoBitrateKbps()`):  
  Bitrate (in Kbps) of the video being received by the local device from the remote participant.

- **Current Round Trip Time** (`stats.getCurrentRoundTripTimeMs()`):  
  Round-trip time (RTT) in milliseconds, indicating the time it takes for a packet to travel from the sender to the receiver and back.

- **Available Outgoing Bitrate** (`stats.getAvailableOutgoingBitrate()`):  
  Available outgoing bitrate (in bps) that can be used for sending video or audio data.

- **Audio Packet Lost Ratio** (`stats.getAudioPacketLostRatio()`):  
  The percentage of audio packets lost during transmission.

- **Video Packet Lost Ratio** (`stats.getVideoPacketLostRatio()`):  
  The percentage of video packets lost during transmission.

- **Jitter** (`stats.getJitter()`):  
  The variation in packet arrival times (in milliseconds), a measure of network instability.

- **Quality Limitation Reason** (`stats.getQualityLimitationReason()`):  
  The reason the quality of the media stream is limited (e.g., low network bandwidth, high packet loss).

- **Sent Video Resolution** (`stats.getSentVideoResolution()`):  
  The resolution of the video being sent from the local device to the receiver.

- **Received Video Resolution** (`stats.getReceivedVideoResolution()`):  
  The resolution of the video being received by the local device from the remote participant.

###  `onError`
Handle errors that occur during the test.

---

### Example of `NetworkQualityTestCallbackListener` Implementation:
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
}



5. **Start the Test**

    To initiate the network quality test, call the `startTest` method on the `NetworkQualityTest` instance.

    ```java
    networkQualityTest.startTest();
    ```

6. **Stop the test**

    To stop the test, you can call NetworkQualityTest.stopTest(). If the test is halted before 5 seconds, an error will be triggered.

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
