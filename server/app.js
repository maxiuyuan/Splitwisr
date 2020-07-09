
const admin = require("firebase-admin");

const serviceAccount = require("/Users/richardma/Desktop/ECE452/server/ece452-297ff-firebase-adminsdk-o4qg7-41afcae2be.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://ece452-297ff.firebaseio.com"
});

const db = admin.database();
const ref = db.ref("test");
ref.once("value", function(snapshot) {
  console.log(snapshot.val());
}, function (errorObject) {
  console.log("The read failed: " + errorObject.code);
});
