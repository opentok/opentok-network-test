//
//  ViewController.m
//  Hello-World
//
//  Copyright (c) 2013 TokBox, Inc. All rights reserved.
//

#import "ViewController.h"
#import "OTNetworkTest.h"

// *** Fill the following variables using your own Project info  ***
// ***          https://dashboard.tokbox.com/projects            ***
// Replace with your OpenTok API key
static NSString* const kApiKey = @"47448191";
// Replace with your generated session ID
static NSString* const kSessionId = @"2_MX40NzQ0ODE5MX5-MTY1MzQ2MzQ3NDA3N350RktPT3dxNDFSc1NqVjFlb1BSTjlZcVR-fg";
// Replace with your generated token
static NSString* const kToken = @"T1==cGFydG5lcl9pZD00NzQ0ODE5MSZzaWc9YWI0OGJhODUzNjZhMDE0MzRmNmMzMDIxYzJhYTE3NDg5NDk4YmY5NjpzZXNzaW9uX2lkPTJfTVg0ME56UTBPREU1TVg1LU1UWTFNelEyTXpRM05EQTNOMzUwUmt0UFQzZHhOREZTYzFOcVZqRmxiMUJTVGpsWmNWUi1mZyZjcmVhdGVfdGltZT0xNjUzNDYzNDkxJm5vbmNlPTAuMzgzOTY0MjEzMzgwMjkwMSZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNjUzNDg1MDg5JmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";


#pragma mark - View lifecycle

@interface ViewController ()
<OTNetworkTestDelegate>

@end

@implementation ViewController {
    OTNetworkTest *_networkTest;
}

/**
 * result -
 * OTNetworkTestResultVideoAndVoice - Good for both Video and Audio
 * OTNetworkTestResultVoiceOnly     - Audio only sessions possible (when "bps < 150K
 *                                    and > 50K" or packet loss ratio > 3%)
 * OTNetworkTestResultNotGood       - No Video and Audio (when platform connectivity
 *                                    failed or bps < 50K or packet loss ratio > 5%)
 */
- (void)networkTestDidCompleteWithResult:(enum OTNetworkTestResult)result
                                   error:(OTError*)error
{
    NSString *resultMessage = nil;
    if(result == OTNetworkTestResultVideoAndVoice)
    {
        resultMessage = @"Result : OTNetworkTestResultVideoAndVoice";
        
    }
    else if(result == OTNetworkTestResultVoiceOnly)
    {
        resultMessage = [NSString stringWithFormat:
                         @"Result : OTNetworkTestResultVoiceOnly,Error %@",
                         error.localizedDescription];
    }
    else
    {
        resultMessage = [NSString stringWithFormat:
                         @"Result : OTNetworkTestResultNotGood,Error %@",
                         error.localizedDescription];
    }
    NSLog(@"%@",resultMessage);
    dispatch_async(dispatch_get_main_queue(), ^{
       [self.activityIndicatorView stopAnimating];
        self.activityIndicatorView.hidden = YES;
        self.statusLabel.text = @"Network check finished.";
        self.resultLabel.text = resultMessage;
        
    });
}

- (void)viewDidLoad
{
    self.title = @"OpenTok Test Network";
    [super viewDidLoad];
    
    _networkTest = [[OTNetworkTest alloc] init];
    
    [self.activityIndicatorView startAnimating];
    if (kApiKey.length == 0 || kSessionId.length == 0 || kToken == 0)
    {
        self.statusLabel.text = @"Provide an api key,session id and token";
        self.activityIndicatorView.hidden = YES;
    }
    else
    {
        self.statusLabel.text = @"Checking network...";
    }
    self.resultLabel.text = @"";
    [_networkTest runConnectivityTestWithApiKey:kApiKey
                                      sessionId:kSessionId
                                          token:kToken
                             executeQualityTest:YES
                            qualityTestDuration:10
                                       delegate:self];
    
}

- (BOOL)prefersStatusBarHidden
{
    return YES;
}

@end
