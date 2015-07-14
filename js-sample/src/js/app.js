var API_KEY,
    SESSION_ID,
    TOKEN,
    TEST_TIMEOUT_MS = 15000, // 15 seconds
    currentScript = document.currentScript;

var compositeOfCallbacks = require('./lib/composite-callbacks.js'),
    performQualityTest = require('./lib/quality-test.js');

var publisherEl = document.createElement('div'),
    subscriberEl = document.createElement('div'),
    session,
    publisher,
    subscriber,
    statusContainerEl,
    statusMessageEl,
    statusIconEl;

var testStreamingCapability = function(subscriber, callback) {
  performQualityTest({subscriber: subscriber, timeout: TEST_TIMEOUT_MS}, function(error, results) {
    console.log('Test concluded', results);

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

    // try audio only to see if it reduces the packet loss
    statusMessageEl.textContent = 'Trying audio only';
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
  });
};

var callbacks = {
  onInitPublisher: function onInitPublisher(error) {
    if (error) {
      statusMessageEl.textContent = 'Could not acquire your camera';
      return;
    }
    statusMessageEl.textContent = 'Connecting to session';
  },
  onPublish: function onPublish(error) {
    if (error) {
      // handle publishing errors here
      statusMessageEl.textContent = 'Could not publish video';
      return;
    }
    statusMessageEl.textContent = 'Subscribing to video';
    subscriber = session.subscribe(
      publisher.stream,
      subscriberEl,
      {
        audioVolume: 0,
        testNetwork: true
      },
      callbacks.onSubscribe
    );
  },
  cleanup: function() {
    session.unsubscribe(subscriber);
    session.unpublish(publisher);
  },
  onSubscribe: function onSubscribe(error, subscriber) {
    if (error) {
      statusMessageEl.textContent = 'Could not subscribe to video';
      return;
    }

    statusMessageEl.textContent = 'Checking your available bandwidth';

    testStreamingCapability(subscriber, function(error, message) {
      statusMessageEl.textContent = message.text;
      statusIconEl.src = message.icon;
      callbacks.cleanup();
    });
  },
  onConnect: function onConnect(error) {
    if (error) {
      statusMessageEl.textContent = 'Could not connect to OpenTok';
    }
  }
};

compositeOfCallbacks(
  callbacks,
  ['onInitPublisher', 'onConnect'],
  function(error) {
    if (error) {
      return;
    }
    statusMessageEl.textContent = 'Publishing video';
    session.publish(publisher, callbacks.onPublish);
  }
);

// This publisher uses the default resolution (640x480 pixels) and frame rate (30fps).
// For other resoultions you may need to adjust the bandwidth conditions in
// testStreamingCapability().
publisher = OT.initPublisher(publisherEl, null, callbacks.onInitPublisher);

document.addEventListener('DOMContentLoaded', function() {
  API_KEY = currentScript.attributes.api_key.nodeValue;
  SESSION_ID = currentScript.attributes.session_id.nodeValue;
  TOKEN = currentScript.attributes.token.nodeValue;

  session = OT.initSession(API_KEY, SESSION_ID);
  session.connect(TOKEN, callbacks.onConnect);
  statusContainerEl = document.getElementById('status_container');
  statusMessageEl = statusContainerEl.querySelector('p');
  statusIconEl = statusContainerEl.querySelector('img');
});