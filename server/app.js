const express = require('express');
const parser = require('body-parser');
const admin = require("firebase-admin");
const key = require("./ece452-297ff-firebase-adminsdk-o4qg7-41afcae2be.json");
const database = require('./service/database.js');
const notif = require('./service/notification.js');

const server = express();
server.use(parser.json());

admin.initializeApp({
  credential: admin.credential.cert(key),
  databaseURL: "https://ece452-297ff.firebaseio.com"
});

const db = admin.database();
server.use(parser.json());

// ########## Notification ##########

// endpoint for device registration
server.post("/register", (req, res) => {
  let current_user = req.query.current_user;
  let device_id = req.query.device_id;
  notif.register(current_user, device_id, res, db);
});

// endpoint for sending to GCM
server.get("/send", (req, res) => {
  let current_user = req.query.current_user;
  let target_user = req.query.target_user;
  notif.notify(current_user, target_user, res, db);
});

// ########## Balances ##########

// endpoint for reading balances
server.get("/read", (req, res) => {
  let current_user = req.query.current_user;
  database.fetch(current_user, res, db);
});

// endpoint for writing balances
server.post("/write", (req, res) => {
  let id_A = req.query.payer;
  let id_B = req.query.payee;
  let blnc = req.query.balance;
  database.update(id_A, id_B, blnc, res, db);
});

module.exports = server;
