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
  if(payer.localeCompare(payee)>0) {
    let payee = req.query.payer
    let payer = req.query.payee
  }

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
  let id_A = req.body.payer;
  let id_B = req.body.payee;
  let blnc = req.body.balance;

  // sort lexicographically
  let entry = {}
  if(id_A.localeCompare(id_B) < 0) {
    entry = {
      payer : id_A,
      payee : id_B,
      balance : blnc
    }
  } else {
    entry = {
      payer : id_B,
      payee : id_A,
      balance : blnc
    }
  }

  // search if already exists
  let keyPrev = "";
  ref.once("value", function(snapshot) {
    for(let temp in snapshot.val()) {
      let curr = snapshot.val()[temp];
      if(curr["payer"] === id_A && curr["payee"] === id_B) {
        keyPrev = temp;
      } else if(curr["payer"] === id_B && curr["payee"] === id_A) {
        keyPrev = temp;
      }
    }
  }, function (errorObject) {
    res.send("The read failed: " + errorObject.code, 400);
  });

  // replace or add new entry
  if(keyPrev != "") {
    ref.child(keyPrev).set(entry);
  } else {
    ref.push(entry);
  }
  res.send("Balance Updated for ", id_A, " and ", id_B, 200)
})

module.exports = app;
