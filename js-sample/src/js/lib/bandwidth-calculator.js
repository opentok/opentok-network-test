var defaults = require('lodash.defaults'),
    calculatePerSecondStats = require('./per-second-average-stats.js'),
    getSampleWindowSize = require('./sample-window-size.js');

module.exports = function(config) {
  var intervalId;

  config = defaults(config, {
    pollingInterval: 500,
    windowSize: 2000,
    subscriber: undefined
  });

  return {
    start: function(reportFunction) {
      var statsBuffer = [],
          last = {
            audio: {},
            video: {}
          };

      intervalId = window.setInterval(function() {
        config.subscriber.getStats(function(error, stats) {
          var snapshot = {},
              nowMs = new Date().getTime(),
              sampleWindowSize;

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