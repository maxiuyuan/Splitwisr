const admin     = require("firebase-admin");
const mailer    = require("nodemailer");

// deliver GCM notification (registered users)
function sendAndroid(target_device, plaintext) {
    let message = {notification : {title: "Balance Updated", body : plaintext}, token: target_device};
    
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
        user: "splitwisr@gmail.com",
        pass: "splitwisr2020" // TODO: hide this somewhere
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

module.exports = {
    sendAndroid,
    sendEMail
  };