package co.wileyedge.docgen;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Properties;

public class Email {

    private boolean emailSent = false;
    private int attempts = 0; // Current attempt count

    // Function to send mail
    // Arguments -- name of the receiver and email of receiver.
    public void sendEmail(String username, String to, String filepath) {

        if (emailSent) {
            System.out.println("Email already sent. Skipping further attempts.");
            return;
        }

        // Maximum number of attempts to send email
        int maxAttempts = 1;
        while (attempts < maxAttempts) {
            try {
                String from = ""; //tried with outlook working fine
                String password = "";
                System.out.println(username);
                String host = "outlook.office365.com";
                Properties properties = System.getProperties();
                System.out.print("Properties: " + properties);

                // host set
                properties.put("mail.smtp.host", host);
                properties.put("mail.smtp.port", "587");
                properties.put("mail.smtp.starttls.enable", "true");
                properties.put("mail.smtp.auth", "true");

                // to get session obj...
                Session session = Session.getInstance(properties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(from, password);
                    }
                });

                session.setDebug(true);

                // compose message
                String message = "\tDear " + username + ",\n\tThanks for using our services!!!\n\tHere is Your CV...";
                String sub = "\nYour CV is here";

                MimeMessage m = new MimeMessage(session);

                m.setFrom(from);
                m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
                m.setSubject(sub);

                MimeMultipart mimeMultipart = new MimeMultipart();

                MimeBodyPart textMime = new MimeBodyPart();

                MimeBodyPart fileMime = new MimeBodyPart();

                // add mail body
                textMime.setText(message);
                File file = new File(filepath);
                fileMime.attachFile(file);

                mimeMultipart.addBodyPart(textMime);
                mimeMultipart.addBodyPart(fileMime);

                // send the message using transport class
                m.setContent(mimeMultipart);

                Transport.send(m);

                // Set the flag to true indicating that email has been sent
                emailSent = true;

                System.out.println("SUCCESSFUL...");
                break; // Exit the loop if email sent successfully
            } catch (Exception e) {
                attempts++; // Increment the attempt count
                if (attempts >= maxAttempts) {
                    System.out.println("Maximum attempts reached. Email could not be sent.");
                } else {
                    System.out.println("Error occurred while sending email. Retrying...");
                }
            }
        }
    }
}
