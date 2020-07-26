const express = require('express');
const parser = require('body-parser');
const admin = require("firebase-admin");
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

    // add or update device token
    for(let temp in snapshot.val()) {
      let curr = snapshot.val()[temp];
      if(curr["user"] === current_user) {
        keyPrev = temp;
        break;
      };
    }
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

    // retreive target device for user
    let token = snapshot.val().Token;
    for(let temp in token) {
      let curr = token[temp];
      if(curr["user"] === target_user) {
        target_device = curr["device"];
      }
    }

    let message = current_user+" reminds you that you owe $"+owing;
    if(target_device != "") {
      sendAndroid(target_device, message); // push notification for registered device
    } else {
      sendEMail(target_user, message); // email notification for not-registered users
    }

    res.status(200).send("Owing notified " + owing + " to " + target_user);
  }, function (errorObject) {
    res.status(400).send("The notify failed: " + errorObject.code);
  });
});

// deliver GCM notification (registered users)
function sendAndroid(target_device, plaintext) {
  let message = {notification : {title : plaintext}, token: target_device};
  
  admin.messaging().send(message)
  .then((response) => {
    console.log('Successfully sent message:', response);
  })
  .catch((error) => {
    console.log('Error sending message:', error);
  });
}

// deliver email notification (non-registered users)
function sendEMail(target_user, plaintext) {
  let transporter = mailer.createTransport({
    service: "gmail",
    auth: {
      // user: "<some-service-account>@gmail.com",
      // pass: "<some-password>"
    }
  });
  
  let mailOptions = {
    from: 'noreply@splitwisr.com',
    to: target_user,
    subject: 'Splitwisr Balance Update',
    // text: plaintext,
    html: '<b> Hey there! </b> <br>' + plaintext + '<br> This is an automated email, please do not reply.'
  };

  transporter.sendMail(mailOptions, (error, info) => {
    if(error) {
      console.log("Error sending email:", error);
    } else {
      console.log("Successfully sent email:", info.response);
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

  baseRef.once("value", function(snapshot) {
    let keyPrev = "";
    let target_device_A = "";
    let target_device_B = "";
    let entry = {
      payer : id_A,
      payee : id_B,
      balance : blnc
    }

    // add or update balance
    let receipt = snapshot.val().Receipt;
    for(let temp in receipt) {
      let curr = receipt[temp];
      if(curr["payer"] === id_A && curr["payee"] === id_B) {
        keyPrev = temp;
        break;
      }
    }
    if(keyPrev != "") {
      receiptRef.child(keyPrev).set(entry);
    } else {
      receiptRef.push(entry);
    }

    // balance update notification
    let token = snapshot.val().Token;
    for(let temp in token) {
      let curr = token[temp];
      if(curr["user"] === id_A) {
        target_device_A = curr["device"];
      } else if(curr["user"] === id_B) {
        target_device_B = curr["device"];
      }
    }

    let message = (blnc < 0) ? "Balance updated: "+id_B+" needs to pay "+id_A+" $"+(-1*parseInt(blnc)) : "Balance updated: "+id_A+" needs to pay "+id_B+" $"+blnc;
    if(target_device_A != "") {
      sendAndroid(target_device_A, message);
    } else {
      sendEMail(id_A, message);
    }
    if(target_device_B != "") {
      sendAndroid(target_device_B, message);
    } else {
      sendEMail(id_B, message);
    }

    res.status(200).send("Balance Updated for " + id_A + " and " + id_B);
  }, function (errorObject) {
    res.status(400).send("The write failed: " + errorObject.code);
  });
});

module.exports = server;
