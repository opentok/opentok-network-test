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

    if (results.video.bitsPerSecond > 150000 && results.video.packetLossRatioPerSecond < 0.03 && results.audio.packetLossRatioPerSecond < 0.03) {
      return callback(false, {
        text: "You're all set!",
        icon: 'assets/icon_tick.svg'
      });
    }

    if ((results.video.packetLossRatioPerSecond + results.audio.packetLossRatioPerSecond) / 2 < 0.05) {
      return callback(false, {
        text: 'Your bandwidth can support audio only',
        icon: 'assets/icon_warning.svg'
      });
    }

    // try audio only to see if it reduces the packet loss
    statusMessageEl.innerText = 'Trying audio only';
    publisher.publishVideo(false);

    performQualityTest({subscriber: subscriber, timeout: 5000}, function(error, results) {
      if (results.audio.packetLossRatioPerSecond < 0.05) {
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
      statusMessageEl.innerText = 'Could not acquire your camera';
      return;
    }
    statusMessageEl.innerText = 'Connecting to session';
  },
  onPublish: function onPublish(error) {
    if (error) {
      // handle publishing errors here
      statusMessageEl.innerText = 'Could not publish video';
      return;
    }
    statusMessageEl.innerText = 'Subscribing to video';
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
  onSubscribe: function onSubscribe(error, subscribe) {
    if (error) {
      statusMessageEl.innerText = 'Could not subscribe to video';
      return;
    }

    statusMessageEl.innerText = 'Checking your available bandwidth';

    testStreamingCapability(subscribe, function(error, message) {
      statusMessageEl.innerText = message.text;
      statusIconEl.src = message.icon;
      callbacks.cleanup();
    });
  },
  onConnect: function onConnect(error) {
    if (error) {
      statusMessageEl.innerText = 'Could not connect to OpenTok';
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
    statusMessageEl.innerText = 'Publishing video';
    session.publish(publisher, callbacks.onPublish);
  }
);

publisher = OT.initPublisher(publisherEl, {
  // for other resoultions you may need to adjust the bandwidth conditions in
  // testStreamingCapability()
  resolution: '1280x720'
}, callbacks.onInitPublisher);

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