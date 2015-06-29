var PORT = process.env.PORT || 5000,
    API_KEY = process.env.API_KEY,
    API_SECRET = process.env.API_SECRET;

if (!API_KEY) {
  throw new Error('Please set environment variable API_KEY');
}

if (!API_SECRET) {
  throw new Error('Please set environment variable API_SECRET');
}

var OpenTok = require('opentok'),
    express = require('express'),
    app = express(),
    opentok = new OpenTok(API_KEY, API_SECRET);

app.set('view engine', 'jade');

opentok.createSession({mediaMode: 'routed'}, function(err, session) {
  if (err) {
    throw err;
  }

  app.get('/', function(req, res) {
    res.render('index', {
      sessionId: session.sessionId,
      token: opentok.generateToken(session.sessionId),
      apiKey: API_KEY
    });
  });

  app.use(express.static('public'));

  console.log('OpenTok: Network test example app running on port ' + PORT);
  app.listen(PORT);
});