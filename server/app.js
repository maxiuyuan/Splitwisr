const express = require('express');
const parser = require('body-parser');

const admin = require("firebase-admin");
const key = require("./ece452-297ff-firebase-adminsdk-o4qg7-41afcae2be.json");

const server = express();
server.use(parser.json())

admin.initializeApp({
  credential: admin.credential.cert(key),
  databaseURL: "https://ece452-297ff.firebaseio.com"
});

const db = admin.database();
const tokenRef = db.ref("Token");
const receiptRef = db.ref("Receipt");

// ########## Notification ##########

// endpoint for device registration
server.post("/register", (req, res) => {
  let current_user = req.query.current_user;
  let device_id = req.query.device_id;
  
  tokenRef.once("value", function(snapshot) {
    let keyPrev = "";
    let entry = {
      user : current_user,
      device : device_id
    }

    // search if already exists
    for(let temp in snapshot.val()) {
      let curr = snapshot.val()[temp];
      if(curr["user"] === current_user) {
        keyPrev = temp;
        break;
      };
    }

    // replace or add new entry
    if(keyPrev != "") {
      tokenRef.child(keyPrev).set(entry);
    } else {
      tokenRef.push(entry);
    }
    res.status(200).send("Device registered for " + current_user)
  }, function (errorObject) {
    res.status(400).send("The register failed: " + errorObject.code);
  });
});

// endpoint for sending to GCM
server.get("/send", (req, res) => {
  let current_user = req.query.current_user;
  let target_user = req.query.target_user;

  receiptRef.once("value", function(snapshot) {
    let owing = 0;

    // aggregate balances between two users
    for(let temp in snapshot.val()) {
      let curr = snapshot.val()[temp];
      if(curr["payer"] === current_user && curr["payee"] === target_user) {
        owing -= parseFloat(curr["balance"]);
      } else if(curr["payer"] === target_user && curr["payee"] === current_user) {
        owing += parseFloat(curr["balance"]);
      }
    }

    // TODO: send notification to GCM
    
    res.status(200).send("Owing notified " + owing + " to " + target_user);
  }, function (errorObject) {
    res.status(400).send("The notify failed: " + errorObject.code);
  });
});

// ########## Balances ##########

// endpoint for reading balances
server.get("/read", (req, res) => {
  let current_user = req.query.current_user;
  let userToBalance = []

  receiptRef.once("value", function(snapshot) {
    let ret = snapshot.val()
    for (let transaction in ret){
      if(ret[transaction]['payer'] === current_user || ret[transaction]['payee'] === current_user){
        userToBalance.push(ret[transaction])
      }
    }
    res.status(200).send(userToBalance)
  }, function (errorObject) {
    res.status(400).send("The read failed: " + errorObject.code);
  });
});

// endpoint for writing balances
server.post("/write", (req, res) => {
  let id_A = req.query.payer;
  let id_B = req.query.payee;
  let blnc = req.query.balance;

  // check lexicographic order
  if(!(id_A.localeCompare(id_B) < 0)) {
    res.status(400).send("Not lexicographically sorted!");
  }

  receiptRef.once("value", function(snapshot) {
    let keyPrev = "";
    let entry = {
      payer : id_A,
      payee : id_B,
      balance : blnc
    }

    // search if already exists
    for(let temp in snapshot.val()) {
      let curr = snapshot.val()[temp];
      if(curr["payer"] === id_A && curr["payee"] === id_B) {
        keyPrev = temp;
        break;
      }
    }

    // replace or add new entry
    if(keyPrev != "") {
      receiptRef.child(keyPrev).set(entry);
    } else {
      receiptRef.push(entry);
    }
    res.status(200).send("Balance Updated for " + id_A + " and " + id_B);
  }, function (errorObject) {
    res.status(400).send("The write failed: " + errorObject.code);
  });
});

module.exports = server;
