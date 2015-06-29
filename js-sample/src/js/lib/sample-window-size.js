var max = require('lodash.max'),
    min = require('lodash.min'),
    pluck = require('lodash.pluck');

module.exports = function(samples) {
  var times = pluck(samples, 'timestamp');
  return (max(times) - min(times)) / 1000;
};