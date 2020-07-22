const express = require('express');
const parser = require('body-parser');
const admin = require("firebase-admin");
const gcm = require("node-gcm");
const mailer = require("nodemailer");
const key = require("./ece452-297ff-firebase-adminsdk-o4qg7-41afcae2be.json");

const server = express();
server.use(parser.json());

admin.initializeApp({
  credential: admin.credential.cert(key),
  databaseURL: "https://ece452-297ff.firebaseio.com"
});

const db = admin.database();
const baseRef = db.ref();
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

  baseRef.once("value", function(snapshot) {
    let owing = 0;
    let target_device = "";

    // aggregate balances between two users
    let receipt = snapshot.val().Receipt;
    for(let temp in receipt) {
      let curr = receipt[temp];
      if(curr["payer"] === current_user && curr["payee"] === target_user) {
        owing -= parseFloat(curr["balance"]);
      } else if(curr["payer"] === target_user && curr["payee"] === current_user) {
        owing += parseFloat(curr["balance"]);
      }
    }

    // retreive target device
    let token = snapshot.val().Token;
    for(let temp in token) {
      let curr = token[temp];
      if(curr["user"] === target_user) {
        target_device = curr["device"];
      }
    }

    if(target_device != "") {
      // send GCM notify when device is registered
      sendAndroid(current_user, target_device, owing);
    } else {
      // send email when device is not registered
      sendEMail(current_user, target_user, owing);
    }

    res.status(200).send("Owing notified " + owing + " to " + target_user);
  }, function (errorObject) {
    res.status(400).send("The notify failed: " + errorObject.code);
  });
});

// helper function to deliver GCM notification (registered users)
function sendAndroid(current_user, target_device, owing) {
  let message = new gcm.Message({
    notification : {
        title : current_user + " reminds you that you owe " + owing
    }
  });

  let sender = new gcm.sender(key);
  sender.send(message, target_device);
}

// helper function to deliver email notification (non-registered users)
function sendEMail(current_user, target_user, owing) {
  
  let transporter = mailer.createTransport({
    service: "service",
    port: 1234,
    auth: {
      user: "abcde",
      pass: "abcde"
    }
  });
  
  let mailOptions = {
    from: '"Splitwisr Team <noreply@splitwisr.com>"',
    to: target_user,
    subject: 'Splitwisr Balance Reminder',
    text: 'Your friend ' + current_user + ' reminds you that you owe ' + owing,
    html: '<b> Hi there! </b> <br> This is an automated mail from Splitwisr!'
  };

  transporter.sendMail(mailOptions, (error, info) => {
    if(error) {
      return "Message delivery has failed " + error;
    } else {
      return "Message delivered: " + info.message;
    }
  });
}

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
