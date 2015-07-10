var API_KEY = '100'; // your API_KEY
var SESSION_ID = '1_MX4xMDB-fjE0MzY0ODg2NDA5NjZ-aVFLMHlvbVZoelRWZDdqMWVKRk9UeGh4fn4'; // your SESSION_ID
var TOKEN = 'T1==cGFydG5lcl9pZD0xMDAmc2lnPTMxN2ViNmU5ZDhhODI5Mzg1MzYzMTdlYzkwZDNmYzE5ZDA4MmQwYTE6c2Vzc2lvbl9pZD0xX01YNHhNREItZmpFME16WTBPRGcyTkRBNU5qWi1hVkZMTUhsdmJWWm9lbFJXWkRkcU1XVktSazlVZUdoNGZuNCZjcmVhdGVfdGltZT0xNDM2NDg4NjQyJm5vbmNlPTI2Mjgmcm9sZT1tb2RlcmF0b3Imc2Vzc2lvbklkPTFfTVg0eE1EQi1makUwTXpZME9EZzJOREE1TmpaLWFWRkxNSGx2YlZab2VsUldaRGRxTVdWS1JrOVVlR2g0Zm40'; // your TOKEN
var TEST_TIMEOUT_MS = 15000; // 15 seconds

var pluck = function(arr, propertName) {
  return arr.map(function(value) {
    return value[propertName];
  });
};

var sum = function(arr, propertyName) {
  if (typeof propertyName !== 'undefined') {
    arr = pluck(arr, propertyName);
  }

  return arr.reduce(function(previous, current) {
    return previous + current;
  }, 0);
};

var max = function(arr) {
  return Math.max.apply(undefined, arr);
};

var min = function(arr) {
  return Math.min.apply(undefined, arr);
};

var calculatePerSecondStats = function(statsBuffer, seconds) {
  var stats = {};
  ['video', 'audio'].forEach(function(type) {
    stats[type] = {
      packetsPerSecond: sum(pluck(statsBuffer, type), 'packetsReceived') / seconds,
      bitsPerSecond: (sum(pluck(statsBuffer, type), 'bytesReceived') * 8) / seconds,
      packetsLostPerSecond: sum(pluck(statsBuffer, type), 'packetsLost') / seconds
    };
    stats[type].packetLossRatioPerSecond = (
      stats[type].packetsLostPerSecond / stats[type].packetsPerSecond
    );
  });

  stats.windowSize = seconds;
  return stats;
};

var getSampleWindowSize = function(samples) {
  var times = pluck(samples, 'timestamp');
  return (max(times) - min(times)) / 1000;
};

var compositeOfCallbacks = function compositeCallbacks(obj, fns, callback) {
  var results = {};
  var hasError = false;

  var checkDone = function checkDone() {
    if (Object.keys(results).length === fns.length) {
      callback(hasError, results);
      callback = function() {};
    }
  };

  fns.forEach(function(key) {
    var originalCallback = obj[key];

    obj[key] = function(error) {
      results[key] = {
        error: error,
        args: Array.prototype.slice.call(arguments, 1)
      };

      if (error) {
        hasError = true;
      }

      originalCallback.apply(obj, arguments);
      checkDone();
    };
  });
};

var bandwidthCalculatorObj = function(config) {
  var intervalId;

  config.pollingInterval = config.pollingInterval || 500;
  config.windowSize = config.windowSize || 2000;
  config.subscriber = config.subscriber || undefined;

  return {
    start: function(reportFunction) {
      var statsBuffer = [];
      var last = {
        audio: {},
        video: {}
      };

      intervalId = window.setInterval(function() {
        config.subscriber.getStats(function(error, stats) {
          var snapshot = {};
          var nowMs = new Date().getTime();
          var sampleWindowSize;

          ['audio', 'video'].forEach(function(type) {
            snapshot[type] = Object.keys(stats[type]).reduce(function(result, key) {
              result[key] = stats[type][key] - (last[type][key] || 0);
              last[type][key] = stats[type][key];
              return result;
            }, {});
          });

          // get a snapshot of now, and keep the last values for next round
          snapshot.timestamp = stats.timestamp;

          statsBuffer.push(snapshot);
          statsBuffer = statsBuffer.filter(function(value) {
            return nowMs - value.timestamp < config.windowSize;
          });

          sampleWindowSize = getSampleWindowSize(statsBuffer);

          if (sampleWindowSize !== 0) {
            reportFunction(calculatePerSecondStats(
              statsBuffer,
              sampleWindowSize
            ));
          }
        });
      }, config.pollingInterval);
    },

    stop: function() {
      window.clearInterval(intervalId);
    }
  };
};

var performQualityTest = function(config, callback) {
  var startMs = new Date().getTime();
  var testTimeout;
  var currentStats;

  var bandwidthCalculator = bandwidthCalculatorObj({
    subscriber: config.subscriber
  });

  var cleanupAndReport = function() {
    currentStats.elapsedTimeMs = new Date().getTime() - startMs;
    callback(undefined, currentStats);

    window.clearTimeout(testTimeout);
    bandwidthCalculator.stop();

    callback = function() {};
  };

  // bail out of the test after 30 seconds
  window.setTimeout(cleanupAndReport, config.timeout);

  bandwidthCalculator.start(function(stats) {
    console.log(stats);

    // you could do something smart here like determine if the bandwidth is
    // stable or acceptable and exit early
    currentStats = stats;
  });
};

var publisherEl = document.createElement('div');
var subscriberEl = document.createElement('div');
var session;
var publisher;
var subscriber;
var statusContainerEl;
var statusMessageEl;
var statusIconEl;

var testStreamingCapability = function(subscriber, callback) {
  performQualityTest({subscriber: subscriber, timeout: TEST_TIMEOUT_MS}, function(error, results) {
    console.log('Test concluded', results);

    var audioVideoSupported = results.video.bitsPerSecond > 250000 &&
      results.video.packetLossRatioPerSecond < 0.03 &&
      results.audio.bitsPerSecond > 25000 &&
      results.audio.packetLossRatioPerSecond < 0.05;

    if (audioVideoSupported) {
      return callback(false, {
        text: 'You\'re all set!',
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
    statusMessageEl.innerText = 'Trying audio only';
    publisher.publishVideo(false);

    performQualityTest({subscriber: subscriber, timeout: 5000}, function(error, results) {
      var audioSupported = results.audio.bitsPerSecond > 25000 &&
          results.audio.packetLossRatioPerSecond < 0.05;

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

  onSubscribe: function onSubscribe(error, subscriber) {
    if (error) {
      statusMessageEl.innerText = 'Could not subscribe to video';
      return;
    }

    statusMessageEl.innerText = 'Checking your available bandwidth';

    testStreamingCapability(subscriber, function(error, message) {
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

// This publisher uses the default resolution (640x480 pixels) and frame rate (30fps).
// For other resoultions you may need to adjust the bandwidth conditions in
// testStreamingCapability().
publisher = OT.initPublisher(publisherEl, undefined, callbacks.onInitPublisher);

document.addEventListener('DOMContentLoaded', function() {
  session = OT.initSession(API_KEY, SESSION_ID);
  session.connect(TOKEN, callbacks.onConnect);
  statusContainerEl = document.getElementById('status_container');
  statusMessageEl = statusContainerEl.querySelector('p');
  statusIconEl = statusContainerEl.querySelector('img');
});
