# NetworkQualityTest

The `NetworkQualityTest` class is a utility for testing network quality and gathering statistics
during video streaming sessions using the OpenTok platform. It allows you to monitor various network
and media statistics, such as video bitrate, audio quality, round-trip time, and more.

## Usage

1. **Import the Class**

   Make sure you've imported the `NetworkQualityTest` class into your project.

    ```java
    import com.opentok.qualitystats.sample.NetworkQualityTest;
    ```

2. **Instantiate the Class**

   Create an instance of `NetworkQualityTest` by providing the required parameters: the current
   activity, a configuration object, and a callback listener for receiving the test results.

    ```java
    NetworkQualityTest networkQualityTest = new NetworkQualityTest(
        this,  // Current activity
        networkQualityTestConfig,  // Configuration object
        networkQualityTestCallbackListener  // Callback listener
    );
    ```

3. **Start the Test**

   To initiate the network quality test, call the `startTest` method on the `NetworkQualityTest`
   instance.

    ```java
    networkQualityTest.startTest();
    ```

4. **Implement Callback Listener**

   Implement the `NetworkQualityTestCallbackListener` interface to receive test results and updates.

    ```java
    NetworkQualityTest.NetworkQualityTestCallbackListener networkQualityTestCallbackListener =
        new NetworkQualityTest.NetworkQualityTestCallbackListener() {
            @Override
            public void onQualityTestResults(String recommendedSetting) {
                // Handle the quality test results and the recommended setting.
            }

            @Override
            public void onQualityTestStatsUpdate(CallbackQualityStats stats) {
                // Handle the real-time quality test statistics update.
            }

            @Override
            public void onError(String error) {
                // Handle errors that occur during the test.
            }
        };
    ```

5. **Handle Callbacks**

   Once the test is running, you'll receive callbacks in the provided listener methods:

    - `onQualityTestResults`: Receive the final test results and the recommended setting based on
      quality thresholds.
    - `onQualityTestStatsUpdate`: Receive real-time statistics updates during the test.
    - `onError`: Handle errors that occur during the test.

6. **Clean Up**

   To ensure proper resource release, you should handle cleanup when needed. In your `onPause`
   or `onDestroy` methods, call the `disconnectSession` method to stop the test and release
   resources.

    ```java
    @Override
    protected void onPause() {
        super.onPause();
        networkQualityTest.disconnectSession();
    }
    ```

## Permissions

Before using the `NetworkQualityTest` class, make sure to request the necessary permissions in your
AndroidManifest.xml file:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
