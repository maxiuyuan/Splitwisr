const external = require('./external.js');

// fetches balances for current user
function fetch(current_user, res, db) {
    let userToBalance = []

    db.ref("Receipt").once("value", function(snapshot) {
        let ret = snapshot.val()
        for (let transaction in ret){
            if(ret[transaction]['payer'] === current_user || ret[transaction]['payee'] === current_user){
            userToBalance.push(ret[transaction])
            }
        }
        res.status(200).send(userToBalance)
    }, function (errorObject) {
        res.status(418).send("The read failed: " + errorObject.code);
    });
}

// overwrites balance between two users
function update(id_A, id_B, blnc, res, db) {
    // check lexicographic order
    if(!(id_A.localeCompare(id_B) < 0)) {
        res.status(400).send("Not lexicographically sorted!");''
    } else {
        db.ref().once("value", function(snapshot) {
            let keyPrev = "";
            let target_device_A = "";
            let target_device_B = "";
            let entry = {
                payer : id_A,
                payee : id_B,
                balance : blnc
            }
          
            let receipt = snapshot.val().Receipt;
            for(let temp in receipt) {
                let curr = receipt[temp];
                if(curr["payer"] === id_A && curr["payee"] === id_B) {
                    keyPrev = temp;
                    break;
                }
            }

            // if(blnc === "0" || blnc === "0.0" || blnc === 0 || blnc === 0.0) { // TODO: dirty fix to filter remove zero balance bug sent from client side
            //     // just remove the old balance
            //     db.ref("Receipt").child(keyPrev).remove();
            // } else {     
                // add or update balance
            if(keyPrev != "") {
                db.ref("Receipt").child(keyPrev).set(entry);
            } else {
                db.ref("Receipt").push(entry);
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
        
            // TODO: dirty fix to reverse the payer and payee bug from client side
            let message = (blnc > 0) ? "Balance updated: "+id_B+" needs to pay "+id_A+" $"+blnc : "Balance updated: "+id_A+" needs to pay "+id_B+" $"+(-1*parseInt(blnc));
            if(target_device_A != "") {
                external.sendAndroid(target_device_A, message);
            } else {
                external.sendEMail(id_A, message);
            }
            if(target_device_B != "") {
                external.sendAndroid(target_device_B, message);
            } else {
                external.sendEMail(id_B, message);
            }
            // }
        
            res.status(200).send("Balance Updated for " + id_A + " and " + id_B);
        }, function (errorObject) {
            res.status(418).send("The write failed: " + errorObject.code);
        });
    }
}

module.exports = {
    fetch,
    update
  };