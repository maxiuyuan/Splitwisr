const express = require('express');
const parser = require('body-parser');
const app = express();
app.use(parser.json())

const admin = require("firebase-admin");
const key = require("./ece452-297ff-firebase-adminsdk-o4qg7-41afcae2be.json");

admin.initializeApp({
  credential: admin.credential.cert(key),
  databaseURL: "https://ece452-297ff.firebaseio.com"
});

const db = admin.database();
const ref = db.ref("Receipt");

app.get("/read", (req, res) => {
  let payer = req.query.payer
  let payee = req.query.payee
  // let parserKey = payer.split("@")[0] + "_" + payee.split("@")[0];
  ref.once("value", function(snapshot) {
    let ret = snapshot.val()
    console.log(ret)
    let temp = ''
    for (let transaction in ret){
      console.log(ret[transaction])
      if(ret[transaction]['payer'] === payer && ret[transaction]['payee'] === payee){
        temp = transaction
        break;
      }
    }
    res.send(ret[temp], 200)
  }, function (errorObject) {
    res.send("The read failed: " + errorObject.code, 400);
  });
});

app.post("/write", (req, res) => {
  console.log(req.body)
  ref.child("receipt").set(req.body);
  res.send("Gucci", 200)
})

module.exports = app;
