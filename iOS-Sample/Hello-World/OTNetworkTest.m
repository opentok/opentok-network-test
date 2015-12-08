//
//  OTNetworkTest.m
//  Hello-World
//
//  Created by Sridhar on 22/05/15.
//  Copyright (c) 2015 TokBox, Inc. All rights reserved.
//

#import "OTNetworkTest.h"
#import "OTDefaultAudioDevice.h"

#define TIME_WINDOW 3000 // 3 seconds
#define AUDIO_ONLY_TEST_DURATION 6 // 6 seconds

@interface OTNetworkTest ()
<OTSessionDelegate, OTSubscriberKitDelegate, OTPublisherDelegate,
OTSubscriberKitNetworkStatsDelegate >

@end

@implementation OTNetworkTest {
    OTSession* _session;
    OTPublisher* _publisher;
    OTSubscriber* _subscriber;
    NSTimer *_sampleTimer;
    NSString *_token;
    BOOL _runQualityStatsTest;
    int _qualityTestDuration;
    enum OTNetworkTestResult _result;
    OTError *_error;
    double prevVideoTimestamp;
    double prevVideoBytes;
    double prevAudioTimestamp;
    double prevAudioBytes;
    uint64_t prevVideoPacketsLost;
    uint64_t prevVideoPacketsRcvd;
    uint64_t prevAudioPacketsLost;
    uint64_t prevAudioPacketsRcvd;
    long video_bw;
    long audio_bw;
    double video_pl_ratio;
    double audio_pl_ratio;
    OTDefaultAudioDevice* _myAudioDevice;
}

- (void)runConnectivityTestWithApiKey:(NSString*)apiKey
                           sessionId:(NSString*)sesssionId
                               token:(NSString*)token
                  executeQualityTest:(BOOL)needsQualityTest
                 qualityTestDuration:(int)qualityTestDuration
                            delegate:(id<OTNetworkTestDelegate>)delegate
{
    prevVideoTimestamp = 0;
    prevVideoBytes = 0;
    prevAudioTimestamp = 0;
    prevAudioBytes = 0;
    prevVideoPacketsLost = 0;
    prevVideoPacketsRcvd = 0;
    prevAudioPacketsLost = 0;
    prevAudioPacketsRcvd = 0;
    video_bw = 0;
    audio_bw = 0;
    video_pl_ratio = -1;
    audio_pl_ratio = -1;

    if(!_myAudioDevice)
    {
        _myAudioDevice = [[OTDefaultAudioDevice alloc] init];
    }

    [OTAudioDeviceManager setAudioDevice:_myAudioDevice];
    [_myAudioDevice setAudioPlayoutMute:YES];

    _token = [token copy];
    _runQualityStatsTest = needsQualityTest;
    _qualityTestDuration = qualityTestDuration;
    self.networkTestDelegate = delegate;
    
    _session = [[OTSession alloc] initWithApiKey:apiKey
                                       sessionId:sesssionId
                                        delegate:self];
    [self doConnect];
}

-(void)dispatchResultsToDelegateWithResult:(enum OTNetworkTestResult)result
                                            error:(OTError*)error
{
    if(_session.sessionConnectionStatus == OTSessionConnectionStatusConnected)
    {
        // Will report result from sessionDidDisconnect callback
        // The callback will in term call the delegate
        _result = result;
        _error = [error copy];
        [_session disconnect:nil];
    } else
    {
        if ([self.networkTestDelegate
             respondsToSelector:@selector(networkTestDidCompleteWithResult:
                                          error:)])
        {
            [_myAudioDevice setAudioPlayoutMute:NO];
            [OTAudioDeviceManager setAudioDevice:nil];
            [self cleanupSession];
            [self.networkTestDelegate networkTestDidCompleteWithResult:result
                                                                 error:error];
        }
    }
}

#pragma mark - OpenTok methods

/**
 * Asynchronously begins the session connect process. Some time later, we will
 * expect a delegate method to call us back with the results of this action.
 */
- (void)doConnect
{
    OTError *error = nil;
    
    [_session connectWithToken:_token error:&error];
    if (error)
    {
        [self dispatchResultsToDelegateWithResult:OTNetworkTestResultNotGood
                                            error:error];
    }
}

- (void)cleanupSession {
    _session.delegate = nil;
    _publisher.delegate = nil;
    _subscriber.delegate = nil;
    _session = nil;
    _publisher = nil;
    _subscriber = nil;
    _token = nil;
    _error = nil;
    // this is a good place to notify the end-user that publishing has stopped.
}

/**
 * Sets up an instance of OTPublisher to use with this session. OTPubilsher
 * binds to the device camera and microphone, and will provide A/V streams
 * to the OpenTok session.
 */
- (void)doPublish
{
    _publisher =
    [[OTPublisher alloc] initWithDelegate:self
                                     name:[[UIDevice currentDevice] name]];
    _publisher.audioFallbackEnabled = NO;
    OTError *error = nil;
    [_session publish:_publisher error:&error];
    if (error)
    {
        [self dispatchResultsToDelegateWithResult:OTNetworkTestResultNotGood
                                            error:error];
    }
}

/**
 * Cleans up the publisher and its view. At this point, the publisher should not
 * be attached to the session any more.
 */
- (void)cleanupPublisher {
    _publisher = nil;
    // this is a good place to notify the end-user that publishing has stopped.
}

/**
 * Instantiates a subscriber for the given stream and asynchronously begins the
 * process to begin receiving A/V content for this stream. Unlike doPublish,
 * this method does not add the subscriber to the view hierarchy. Instead, we
 * add the subscriber only after it has connected and begins receiving data.
 */
- (void)doSubscribe:(OTStream*)stream
{
    _subscriber = [[OTSubscriber alloc] initWithStream:stream delegate:self];
    _subscriber.networkStatsDelegate = self;
    
    OTError *error = nil;
    [_session subscribe:_subscriber error:&error];
    if (error)
    {
        [self dispatchResultsToDelegateWithResult:OTNetworkTestResultNotGood
                                            error:error];
    }
}

/**
 * Cleans the subscriber from the view hierarchy, if any.
 * NB: You do *not* have to call unsubscribe in your controller in response to
 * a streamDestroyed event. Any subscribers (or the publisher) for a stream will
 * be automatically removed from the session during cleanup of the stream.
 */
- (void)cleanupSubscriber
{
    _subscriber = nil;
}

- (void)subscriber:(OTSubscriberKit*)subscriber
videoNetworkStatsUpdated:(OTSubscriberKitVideoNetworkStats*)stats
{
    if (prevVideoTimestamp == 0)
    {
        prevVideoTimestamp = stats.timestamp;
        prevVideoBytes = stats.videoBytesReceived;
    }
    
    if (stats.timestamp - prevVideoTimestamp >= TIME_WINDOW)
    {
        video_bw = (8 * (stats.videoBytesReceived - prevVideoBytes)) / ((stats.timestamp - prevVideoTimestamp) / 1000ull);

        [self processStats:stats];
        prevVideoTimestamp = stats.timestamp;
        prevVideoBytes = stats.videoBytesReceived;
        NSLog(@"videoBytesReceived %llu, bps %ld, packetsLost %.2f",stats.videoBytesReceived, video_bw, video_pl_ratio);
    }
}

- (void)subscriber:(OTSubscriberKit*)subscriber
audioNetworkStatsUpdated:(OTSubscriberKitAudioNetworkStats*)stats
{
    if (prevAudioTimestamp == 0)
    {
        prevAudioTimestamp = stats.timestamp;
        prevAudioBytes = stats.audioBytesReceived;
    }
    
    if (stats.timestamp - prevAudioTimestamp >= TIME_WINDOW)
    {
        audio_bw = (8 * (stats.audioBytesReceived - prevAudioBytes)) / ((stats.timestamp - prevAudioTimestamp) / 1000ull);

        [self processStats:stats];
        prevAudioTimestamp = stats.timestamp;
        prevAudioBytes = stats.audioBytesReceived;
        NSLog(@"audioBytesReceived %llu, bps %ld, packetsLost %.2f",stats.audioBytesReceived, audio_bw,audio_pl_ratio);
    }
}
- (void)checkQualityAndDisconnectSession
{
    enum OTNetworkTestResult result = OTNetworkTestResultVideoAndVoice;
    NSDictionary* userInfo = nil;
    
    BOOL canDoVideo = (video_bw >= 150000 && video_pl_ratio <= 0.03);
    BOOL canDoAudio = (audio_bw >= 25000 && audio_pl_ratio <= 0.05);
    
    if (!canDoVideo && !canDoAudio)
    {
        NSLog(@"Starting Audio Only Test");
        // test for audio only stream
        _publisher.publishVideo = NO;
        
        dispatch_time_t delay = dispatch_time(DISPATCH_TIME_NOW,
                                              AUDIO_ONLY_TEST_DURATION * NSEC_PER_SEC);
        dispatch_after(delay,dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT,0),^{
            
            enum OTNetworkTestResult result = OTNetworkTestResultVideoAndVoice;
            NSDictionary* userInfo = nil;
            // you can tune audio bw threshold value based on your needs.
            if (audio_bw >= 25000 && audio_pl_ratio <= 0.05)
            {
                result = OTNetworkTestResultVoiceOnly;
                userInfo = [NSDictionary
                            dictionaryWithObjectsAndKeys:@"The quality of your network is medium "
                            "you can start a voice only call",
                            NSLocalizedDescriptionKey,
                            nil];
            } else
            {
                result = OTNetworkTestResultNotGood;
                userInfo = [NSDictionary
                            dictionaryWithObjectsAndKeys:@"The quality of your network is not enough "
                            "to start a video or audio call, please try it again later "
                            "or connect to another network",
                            NSLocalizedDescriptionKey,
                            nil];
            }
            _error = [[OTError alloc] initWithDomain:@"OTSubscriber"
                                                code:-1
                                            userInfo:userInfo];
            _result = result;
            [_session disconnect:nil];
        });
    }
    else if (canDoAudio && !canDoVideo)
    {
        result = OTNetworkTestResultVoiceOnly;
        userInfo = [NSDictionary
                    dictionaryWithObjectsAndKeys:@"The quality of your network is medium "
                    "you can start a voice only call",
                    NSLocalizedDescriptionKey,
                    nil];
        _error = [[OTError alloc] initWithDomain:@"OTSubscriber"
                                            code:-1
                                        userInfo:userInfo];
        _result = result;
        [_session disconnect:nil];
        
    } else
    {
        NSLog(@"The quality of your network is good for video and audio call");
        _result = result;
        [_session disconnect:nil];
    }
}
- (void)processStats:(id)stats
{
    if ([stats isKindOfClass:[OTSubscriberKitVideoNetworkStats class]])
    {
        video_pl_ratio = -1;
        OTSubscriberKitVideoNetworkStats *videoStats =
        (OTSubscriberKitVideoNetworkStats *) stats;
        if (prevVideoPacketsRcvd != 0) {
            uint64_t pl = videoStats.videoPacketsLost - prevVideoPacketsLost;
            uint64_t pr = videoStats.videoPacketsReceived - prevVideoPacketsRcvd;
            uint64_t pt = pl + pr;
            if (pt > 0)
                video_pl_ratio = (double) pl / (double) pt;
        }
        prevVideoPacketsLost = videoStats.videoPacketsLost;
        prevVideoPacketsRcvd = videoStats.videoPacketsReceived;
    }
    if ([stats isKindOfClass:[OTSubscriberKitAudioNetworkStats class]])
    {
        audio_pl_ratio = -1;
        OTSubscriberKitAudioNetworkStats *audioStats =
        (OTSubscriberKitAudioNetworkStats *) stats;
        if (prevAudioPacketsRcvd != 0) {
            uint64_t pl = audioStats.audioPacketsLost - prevAudioPacketsLost;
            uint64_t pr = audioStats.audioPacketsReceived - prevAudioPacketsRcvd;
            uint64_t pt = pl + pr;
            if (pt > 0)
                audio_pl_ratio = (double) pl / (double) pt;
        }
        prevAudioPacketsLost = audioStats.audioPacketsLost;
        prevAudioPacketsRcvd = audioStats.audioPacketsReceived;
    }
}

# pragma mark - OTSession delegate callbacks

- (void)sessionDidConnect:(OTSession*)session
{
    NSLog(@"sessionDidConnect (%@)", session.sessionId);
    
    // Step 2: We have successfully connected, now instantiate a publisher and
    // begin pushing A/V streams into OpenTok.
    [self doPublish];
}

- (void)sessionDidDisconnect:(OTSession*)session
{
    NSString* alertMessage =
    [NSString stringWithFormat:@"Session disconnected: (%@)",
     session.sessionId];
    NSLog(@"sessionDidDisconnect (%@)", alertMessage);
    
    enum OTNetworkTestResult result = _result;
    OTError *error = [_error copy];

    [self cleanupSession];
    
    [self dispatchResultsToDelegateWithResult:result
                                        error:error];
}


- (void)session:(OTSession*)mySession
  streamCreated:(OTStream *)stream
{
    NSLog(@"session streamCreated (%@)", stream.streamId);
}

- (void)session:(OTSession*)session
streamDestroyed:(OTStream *)stream
{
    NSLog(@"session streamDestroyed (%@)", stream.streamId);
    
    if ([_subscriber.stream.streamId isEqualToString:stream.streamId])
    {
        [self cleanupSubscriber];
    }
}

- (void)  session:(OTSession *)session
connectionCreated:(OTConnection *)connection
{
    NSLog(@"session connectionCreated (%@)", connection.connectionId);
}

- (void)    session:(OTSession *)session
connectionDestroyed:(OTConnection *)connection
{
    NSLog(@"session connectionDestroyed (%@)", connection.connectionId);
    if ([_subscriber.stream.connection.connectionId
         isEqualToString:connection.connectionId])
    {
        [self cleanupSubscriber];
    }
}

- (void) session:(OTSession*)session
didFailWithError:(OTError*)error
{
    NSLog(@"didFailWithError: (%@)", error);
    [self dispatchResultsToDelegateWithResult:OTNetworkTestResultNotGood
                                        error:error];
}

# pragma mark - OTSubscriber delegate callbacks

- (void)subscriberDidConnectToStream:(OTSubscriberKit*)subscriber
{
    NSLog(@"subscriberDidConnectToStream (%@)",
          subscriber.stream.connection.connectionId);
    assert(_subscriber == subscriber);

    if(!_runQualityStatsTest)
    {
        _result = OTNetworkTestResultVideoAndVoice;
        [_session disconnect:nil];
    } else
    {
        dispatch_time_t delay = dispatch_time(DISPATCH_TIME_NOW,
                                              _qualityTestDuration * NSEC_PER_SEC);
        dispatch_after(delay,dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT,0),^{
            
            [self checkQualityAndDisconnectSession];
        });
    }
}

- (void)subscriber:(OTSubscriberKit*)subscriber
  didFailWithError:(OTError*)error
{
    NSLog(@"subscriber %@ didFailWithError %@",
          subscriber.stream.streamId,
          error);
    [self dispatchResultsToDelegateWithResult:OTNetworkTestResultNotGood
                                        error:error];

}

# pragma mark - OTPublisher delegate callbacks

- (void)publisher:(OTPublisherKit *)publisher
    streamCreated:(OTStream *)stream
{
    [self doSubscribe:stream];
}

- (void)publisher:(OTPublisherKit*)publisher
  streamDestroyed:(OTStream *)stream
{
    if ([_subscriber.stream.streamId isEqualToString:stream.streamId])
    {
        [self cleanupSubscriber];
    }
    
    [self cleanupPublisher];
}

- (void)publisher:(OTPublisherKit*)publisher
 didFailWithError:(OTError*) error
{
    NSLog(@"publisher didFailWithError %@", error);
    [self cleanupPublisher];
    [self dispatchResultsToDelegateWithResult:OTNetworkTestResultNotGood
                                        error:error];
}

@end
