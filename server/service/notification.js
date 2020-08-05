const external = require('./external.js');

// device registration workflow
function register(current_user, device_id, res, db) {
    db.ref("Token").once("value", function(snapshot) {
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
            db.ref("Token").child(keyPrev).set(entry);
        } else {
            db.ref("Token").push(entry);
        }

        res.status(200).send("Device registered for " + current_user)
    }, function (errorObject) {
        res.status(418).send("The register failed: " + errorObject.code);
    });
}

// device notification workflow
function notify(current_user, target_user, res, db) {
    db.ref().once("value", function(snapshot) {
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
    
        let message = current_user+" reminds you that you owe $"+(-1*owing); // TODO: consequence of the dirty fix from client side bug
        if(target_device != "") {
            external.sendAndroid(target_device, message); // push notification for registered device
        } else {
            external.sendEMail(target_user, message); // email notification for not-registered users
        }
    
        res.status(200).send("Owing notified " + owing + " to " + target_user);
      }, function (errorObject) {
        res.status(418).send("The notify failed: " + errorObject.code);
    });
}

module.exports = {
    register,
    notify
  };
