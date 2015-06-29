//
//  OTNetworkStatsKit.h
//  otkit-objc-libs
//
//  Created by Charley Robinson on 12/1/14.
//
//

#import <Foundation/Foundation.h>
#import <OpenTok/OpenTokObjC.h>

/**
 * This is a private header. It should not be distributed normally along with
 * the rest of the framework headers.
 */

/**
 * Network statistics describing the network state for the device.
 */
@interface OTSessionNetworkStats : NSObject

@property (readonly) double uploadBitsPerSecond;
@property (readonly) double downloadBitsPerSecond;
@property (readonly) double packetLossRatio;
@property (readonly) double roundTripTimeMilliseconds;

- (instancetype)initWithUpload:(double)upload
                      download:(double)download
               packetLossRatio:(double)packetLossRatio
                 roundTripTime:(double)roundTripTime;

@end

@interface OTSession (NetworkStats)

/**
 * Tests the network connection. Results passed to the session delegate.
 */
- (void)testNetworkWithToken:(NSString*)token
                       error:(OTError**)error;


@end

@protocol OTSessionDelegateNetworkStats <OTSessionDelegate>
@optional
/**
 * Sent after a network connection test completes.
 * NOTE - Known Issue: Packet Loss Ratio and Round Trip Time will are not
 * reported when this test is run from the simulator.
 */
- (void)session:(OTSession*)session
networkTestCompletedWithResult:(OTSessionNetworkStats*)result;
@end


/**
 * Network statistics describing the network state for this subscriber.
 */
@interface OTSubscriberKitVideoNetworkStats : NSObject

/**
 * Estimated number of video packets lost by this subscriber.
 */
@property (readonly) uint64_t videoPacketsLost;

/**
 * Number of video packets received by this subscriber.
 */
@property (readonly) uint64_t videoPacketsReceived;

/**
 * Number of video bytes received by this subscriber.
 */
@property (readonly) uint64_t videoBytesReceived;

@property (readonly) double timestamp;

- (instancetype)initWithPacketsLost:(uint64_t)packetsLost
                    packetsReceived:(uint64_t)packetsReceived
                      bytesReceived:(uint64_t)bytesReceived
                          timestamp:(double)timestamp;

@end


/**
 * Network statistics describing the network state for this subscriber.
 */
@interface OTSubscriberKitAudioNetworkStats : NSObject

/**
 * Estimated number of audio packets lost by this subscriber.
 */
@property (readonly) uint64_t audioPacketsLost;

/**
 * Number of audio packets received by this subscriber.
 */
@property (readonly) uint64_t audioPacketsReceived;

/**
 * Number of audio bytes received by this subscriber.
 */
@property (readonly) uint64_t audioBytesReceived;

@property (readonly) double timestamp;

- (instancetype)initWithPacketsLost:(uint64_t)packetsLost
                    packetsReceived:(uint64_t)packetsReceived
                      bytesReceived:(uint64_t)bytesReceived
                          timestamp:(double)timestamp;

@end


@protocol OTSubscriberKitNetworkStatsDelegate <NSObject>

@optional

- (void)subscriber:(OTSubscriberKit*)subscriber
videoNetworkStatsUpdated:(OTSubscriberKitVideoNetworkStats*)stats;

- (void)subscriber:(OTSubscriberKit*)subscriber
audioNetworkStatsUpdated:(OTSubscriberKitAudioNetworkStats*)stats;

@end


/**
 * Additional property on OTSubscriberKit allows for binding a delegate to
 * receive periodic network stats data.
 */
@interface OTSubscriberKit ()

@property (nonatomic, assign)
id<OTSubscriberKitNetworkStatsDelegate> networkStatsDelegate;

@end
