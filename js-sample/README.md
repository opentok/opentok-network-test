OpenTok.js Network Test Sample
==============================

This sample shows how to use OpenTok.js to determine the appropriate audio and video settings
to use in publishing a stream to an OpenTok session. To do this, the app publishes a test
stream to a test session and then uses the API to check the quality of that stream. Based on the
quality, the app determines what the client can successfully publish to an OpenTok session:

* The client can publish an audio-video stream at the default resolution (640-by-480 pixels).

* The client can publish an audio-only stream.

* The client is unable to publish.

The sample app only subscribes to the test stream. It does not subscribe to other streams in the
test session. Do not use the test session for your actual call. Use a separate OpenTok session
(and session ID) to share audio-video streams between clients.

## Testing the app

To configure and test the app:

1. At the top of the app.js file, set the following to your OpenTok API, a session ID, and a token
   for that session:

   ```
   var API_KEY = '';
   var SESSION_ID = '';
   var TOKEN = '';
   ```

   **Important:** You must use a unique session, with its own session ID, for the network test. This
   must be a different session than the one you will use to share audio-video streams between
   clients. Do not publish more than one stream to the test session.

   The app requires that each session uses either the routed media mode or the relayed media mode -- one that uses
   the [OpenTok Media Router](https://tokbox.com/developer/guides/create-session/#media-mode).
   Either a routed or a relayed session can get statistics for the stream published by the local client.

   You can get your API key as well as a test session ID and token at the
   [OpenTok dashboard](https://dashboard.tokbox.com/). However, in a shipping application, use
   one of the [OpenTok server SDKs](https://tokbox.com/developer/sdks/server/) to generate a
   session ID and token.

2. Install the sample code on a web server. Note that you must load the code from a web server.
   Browsers do not support WebRTC video in pages loaded from a file:// URL.

3. In a web browser, navigate to the index.html page for the app.

4. Grant the page access to your camera and microphone.

5. The app uses a test stream and session to determine the client's ability to publish a stream
   that has a 640-by-480-pixel video. At the end of the test it reports one of the following:

   * You're all set -- your client can publish an audio-video stream that uses
     640-by-480-pixel video.

   * Your bandwidth is too low for audio.

   * Your bandwidth can support audio only.

## Understanding the code

The main app.js file connects initializes an OpenTok publisher it uses to test stream quality.
It also connects to the OpenTok test session. Upon connecting to the session and publishing
a stream, the app subscribes to the test stream it publishes:

```javascript
subscriber = session.subscribe(
  publisher.stream,
  subscriberEl,
  {
    audioVolume: 0,
    testNetwork: true
  },
  callbacks.onSubscribe
);
```

Note that the code sets the `testNetwork` option of the `Session.subscribe()` method to `true`.
This causes the app to actually use the stream returned by the OpenTok Media Router.
(By default, when you subscribe to a stream you publish, OpenTok.js simply uses the local camera
fead as the video display for the Subscriber element in the HTML DOM.)

Also, the code sets the `audioVolume` option of the `Session.subscribe()` method to `0`, so that
the subscriber does not cause audio feedback.

The DOM elements for the publisher and subscriber are not added to the HTML DOM,
so the test video is not displayed:

```javascript
var publisherEl = document.createElement('div'),
    subscriberEl = document.createElement('div'),
```

Upon subscribing to the test stream, the app calls the `testStreamingCapability()` function.
This function calls the `performQualityTest()` function. This function initiates a
bandwidthCalculatorObj object and calls its `start()` method. This method periodically calls
the `getStats()` method of the Subscriber:

```javascript
config.subscriber.getStats(function(error, stats) {
  // ...
}
```

The `Subscriber.getStats()` method gets the stream statistics for the subscriber and (on success)
passes them into the completion handler. The statistics include information on the stream's audio
and video:

* `bytesReceived` -- The cumulative number of media (audio or video) bytes received by the
   subscriber since the `getStats()` method was called.

* `packetsLost` -- The cumulative number of (audio or video) packets lost by the
   subscriber since the `getStats()` method was called.

* `packetsReceived` -- The cumulative number of (audio or video) packets received by the
   subscriber since the `getStats()` method was called.

Based on these statistics (and comparing them to the statistics from the previous period), the
completion handler for the `Subscriber.getStats()` method assembles a `snapshot` object,
which includes the the following:

* `bitsPerSecond` -- Bits per second

* `packetLossRatioPerSecond` -- Packet loss ratio per second

* `packetsLostPerSecond` -- Packets lost per second

* `packetsPerSecond` -- Packets per second

The bandwidthCalculator object stores these in a buffer. The `performQualityTest()` function logs
the stats to the debug console.

The quality stats test stops after 15 seconds, and the completion handler of the
`performQualityTest()` method checks the video statistics and audio statistics to see if
they support publishing video or publishing audio-only, based on thresholds for the video
bitrate, the video packet loss ratio, and the audio packet loss ratio:

```javascript
var audioVideoSupported = results.video.bitsPerSecond > 250000
  && results.video.packetLossRatioPerSecond < 0.03
  && results.audio.bitsPerSecond > 25000
  && results.audio.packetLossRatioPerSecond < 0.05
if (audioVideoSupported) {
  return callback(false, {
    text: "You're all set!",
    icon: 'assets/icon_tick.svg'
  });
}

if (results.audio.packetLossRatioPerSecond < 0.05) {
  return callback(false, {
    text: 'Your bandwidth can support audio only',
    icon: 'assets/icon_warning.svg'
  });
}
```

If the results of this initial test do not support video or audio-only conditions, the app runs
the quality test again, this time using an audio-only stream. At the end of this test, the
app determines whether conditions support audio-only:

```javascript
// try audio only to see if it reduces the packet loss
statusMessageEl.innerText = 'Trying audio only';
publisher.publishVideo(false);

performQualityTest({subscriber: subscriber, timeout: 5000}, function(error, results) {
  var audioSupported = results.audio.bitsPerSecond > 25000
    && results.audio.packetLossRatioPerSecond < 0.05;

  if (audioSupported) {
    return callback(false, {
      text: 'Your bandwidth can support audio only',
      icon: 'assets/icon_warning.svg'
    });
  }
  return callback(false, {
    text: 'Your bandwidth is too low for audio',
    icon: 'assets/icon_error.svg'
  });
});
```

Note that this sample app uses thresholds based on the table in the "Thresholds and interpreting
network statistics" section of the main README file of this repo. You may change the threshold
values used in your own app, based on the video resolution your app uses and your quality
requirements.
