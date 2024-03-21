package co.wileyedge.docgen;

//import java.net.Authenticator;
import javax.mail.PasswordAuthentication;

import java.io.File;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class App
{

    //function to send mail
    // arguments -- name of the receiver and email of receiver.
    public void sendEmail(String username,String to, String filepath) {

        String from="bhumisumanth401@outlook.com";
        String password = "Kishore@123";
        System.out.println(username);
        String host="outlook.office365.com";
        Properties properties = System.getProperties();
        System.out.print("Properties: "+properties);

        //setting important info to properties
        //host set
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.auth", "true");

        //to get session obj...
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from,password);    		}
        });

        session.setDebug(true);

        //compose message
        String message = "\tDear "+username+",\n\tThanks for using our services!!!\n\tHere is Your resume...";
        String sub="\nYour Resume is here";

        MimeMessage m = new MimeMessage(session);

        try {

            m.setFrom(from);
            //adding recipient to message
            m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            //adding subject to message
            m.setSubject(sub);

            //attach the file
            String path = filepath;

            MimeMultipart mimeMultipart = new MimeMultipart();

            MimeBodyPart textMime = new MimeBodyPart();

            MimeBodyPart fileMime = new MimeBodyPart();

            try {

                //adding mail body
                textMime.setText(message);
                File file = new File(path);
                fileMime.attachFile(file);

                mimeMultipart.addBodyPart(textMime);
                mimeMultipart.addBodyPart(fileMime);
            }
            catch(Exception E) {
                E.printStackTrace();
            }

            //send the message using transport class
            m.setContent(mimeMultipart);

            Transport.send(m);

            System.out.println("SUCCESSFULL......");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }

}
