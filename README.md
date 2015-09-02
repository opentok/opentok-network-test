OpenTok Pre-Call Test
=====================

This repository contains sample code that shows how to diagnose if the client's call (publishing
a stream to an OpenTok session) will be succesful or not, given their network conditions. The
pre-call test can be implemented as a step the client runs before joining the session. Based on the
test results, the app can decide if the client should be allowed to publish a stream to the session
and whether that stream should use audio-only mode or not. The test is intended to be used in a
session that connects two clients in a one-to-one call.

This is an experimental/beta feature. It can be a subject to change in future releases.
Please provide any feedback or questions at precall-test-beta@tokbox.com.

The pre-call test is supported in:

*  [OpenTok Android SDK 2.6](https://tokbox.com/developer/sdks/android/)
*  [OpenTok iOS SDK 2.6](https://tokbox.com/developer/sdks/ios/)
*  [OpenTok.js 2.6](https://tokbox.com/developer/sdks/js/)

## How does it work

The sample apps each do the following:

1. Connect to an OpenTok session and publish a test stream to the session.

   Note that the published test stream is visible to all clients in the session. If your
   app has other clients connected while you publish the test stream, to can prevent
   thems from subscribing to the test stream, you can add the name "test" to the
   published stream, and check the stream name when the stream is created in the session.

2. Subscribe to your own test stream for a test period.

   During the test period, the video quality will stabilize, based on the available network
   connection quality.

3. Collect the bitrate and packet loss statistics using the Network Stats API (see below).

4. Compare the network stats against thresholds (see below) to determine the outcome of the test.

Please see the sample code for details.

## Network Stats API

This is an undocumented and experimental API, which you dynamically monitor the following statistics for a subscriber:

* Audio and video bytes received 

* Audio and video packets lost

* Audio and video packets received

This API is only available in sessions that use the OpenTok Media Router.

For details on the API additions, see the README file in each of the sample directories.

## Thresholds and interpretting network statisics

You can use the network statisics to determine the ability to send and receive streams,
and as a result have a quality experience during the OpenTok call.

Please keep in mind, everybody's use case and perception of the call quality is different.
Therefore you should adjust the default thresholds and timeframe in accordance to your use case
and expectations. For example, the 720p, 30 fps video call requires much a much better network
connection than 320x480-pixel, 15 fps video, so you need to set a much higher threshold values
in order to qualify a vialble end user connection. Also the longer you run the test, the more
accurate values you will receive. At the same time, you might want to switch audio-only or not
based on your specific use case. 

The Pre-Call Test is implementated as a sample code to make it easier 
for developer to customize their application logic.

Below are examples of the thresholds for popular video resolution-frame rate combinations.
The following tables interpret results (for audio-video sessions and audio-only sessions),
with the following quality designations:

* Excellent - None or imperceptible impairments in media

* Acceptable - Some impairments in media, leading to some momentary disruptions

### Audio-video streams

For the given qualities and resolutions, all the following conditions must met.

| Quality    | Video resolution @ fps | Video kbps  | Packet loss |
| ---------- | ---------------------- | ----------- | ----------- |
| Excellent  | 1280x720 @ 30          | > 1000      | < 0.5%      |
| Excellent  | 640x480 @ 30           | > 600       | < 0.5%      |
| Excellent  | 352x288 @ 30           | > 300       | < 0.5%      |
| Excellent  | 320x240 @ 30           | > 300       | < 0.5%      |
| Acceptable | 1280x720 @ 30          | > 350       | < 0.3%      |
| Acceptable | 640x480 @ 30           | > 250       | < 0.3%      |
| Acceptable | 352x288 @ 30           | > 150       | < 0.3%      |
| Acceptable | 320x240 @ 30           | > 150       | < 0.3%      |

Note that the default publish settings for video are 640x480 pixels @ 30 fps in OpenTok.js and the
OpenTok iOS SDK. The default is 352x288 @ 30 fps in the OpenTok Android SDK.

You can calculate the video kbps and packet loss based on the video bytes received and
video packets received statistics provided by the Network Statistics API. See the sample app
for code.

The video resolutions listed are representative of common resolutions. You can determine support for
other resolutions by interpolating the results of the closest resolutions listed.

### Audio-only streams

For the given qualities, the following conditions must met.

| Quality    | Audio kbps | Packet loss |
| ---------- | ---------- | ----------- |
| Excellent  | > 30       | < 0.5%      |
| Acceptable | > 25       | < 5%        |

Note that you can calculate the audio kbps and packet loss based on the audio bytes received
and audio packets received statistics provided by the API. See the sample apps for code.

## Sample code

This repo includes sample code showing how to build a pre-call test using version 2.6 of each of
the OpenTok client SDKs: Android, iOS, and JavaScript. Each sample shows how to determine the
the appropriate audio and video settings to use in publishing a stream to an OpenTok session. To do
this, each sample app publishes a test stream to the session and then uses the Network Stats API to
check the quality of that stream. Based on the quality, the app determines what the client can
successfully publish to the session:

* The client can publish an audio-video stream at the specified resolution.

* The client can publish an audio-only stream.

* The client is unable to publish.

Each sample subdirectory includes a README file that describes how the app uses the network
stats API.

## Frequently Asked Questions (FAQ)

* Why does the OpenTok Network Stats API values are different from my Speedtest.net results?

  Speedtest.net tests your network connection, while the Network Stats API shows how
  the WebRTC engine (and OpenTok) will perform on your connection. 

* Why are the Network Stats API results quite inconsistent?

  The WebRTC requires some time to stablize the quality of the call for the specific
  connection. If you will allow the pre-call test to run longer, you should receive
  more consistent results. Also, please, make sure that you're using routed OpenTok session
  instead of a relayed on. For more information, see [The OpenTok Media Router and media
  modes](https://tokbox.com/developer/guides/create-session/#media-mode)

* Why the output values are really low when my user is streaming Netflix movies?

  WebRTC is conservative in choosing the allowed bandwidth. For example, 
  if there is another high-bandwidth consumer on the network, WebRTC will 
  try to set its own usage to the minimum.

* The pre-call test shows the "Excellent" (or "Acceptable") result, but the video still gets
  pixalated during the call.

  You can increase the required threshholds to better qualify the end user connection.
  Please keep in mind, the network connection can change overtime, especially on mobile devices.

* There is no documentation of the Network Stats API on TokBox website.

  This is an experimental/beta API, which may changed in future. For documentation on the current
  API, see the README file in each of the sample directories.

* Why do I get compilation errors on iOS or Android.

  This is a experimental and undocumented API. Please refer to our sample code on how to use
  the API enhancements in your project. You should be using OpenTok iOS SDK 2.6 or
  OpenTok Android SDK 2.6.
