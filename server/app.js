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
  let current_user = req.query.current_user;
  let userToBalance = []
  
  ref.once("value", function(snapshot) {
    let ret = snapshot.val()
    for (let transaction in ret){
      if(ret[transaction]['payer'] === current_user || ret[transaction]['payee'] === current_user){
        userToBalance.push(ret[transaction])
      }
    }
    res.send(userToBalance, 200)
  }, function (errorObject) {
    res.send("The read failed: " + errorObject.code, 400);
  });
});


app.post("/write", (req, res) => {
  let id_A = req.query.payer;
  let id_B = req.query.payee;
  let blnc = req.query.balance;

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
  ref.once("value", function(snapshot) {
    let keyPrev = "";

    for(let temp in snapshot.val()) {
      let curr = snapshot.val()[temp];
      if(curr["payer"] === id_A && curr["payee"] === id_B) {
        keyPrev = temp;
        break;
      } else if(curr["payer"] === id_B && curr["payee"] === id_A) {
        keyPrev = temp;
        break;
      }
    }

    // replace or add new entry
    if(keyPrev != "") {
      ref.child(keyPrev).set(entry);
    } else {
      ref.push(entry);
    }
    res.send("Balance Updated for ", id_A, " and ", id_B, 200)
  }, function (errorObject) {
    res.send("The read failed: " + errorObject.code, 400);
  });
})

module.exports = app;
