module.exports = function compositeCallbacks(obj, fns, callback) {
  var results = {}, hasError = false;

  var checkDone = function checkDone() {
    if (Object.keys(results).length == fns.length) {
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