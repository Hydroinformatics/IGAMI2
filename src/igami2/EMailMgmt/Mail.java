package igami2.EMailMgmt;


import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author VIDYA
 */
public class Mail {

    private String to;
    private String from;
    private String message;
    private String subject;
    private String smtpServ;
    
    public void setTo(String to)
    {
        this.to = to;
    }
    
    public void setFrom(String from)
    {
        this.from = from;
    }
    public void setMsg(String msg)
    {
        this.message = msg;
    }
    
    public void setSubject(String subject)
    {
        this.subject = subject;
    }
    
    
    public void setSMTP(String smtpServ)
    {
        this.smtpServ = smtpServ;
    }
    

    public int sendMail() {
        try {
            Properties props = System.getProperties();
            // -- Attaching to default Session, or we could start a new one --
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.starttls.enable", "false");
            props.put("mail.smtp.host", smtpServ);
            props.put("mail.smtp.auth", "true");
            
            
            
            Authenticator auth = new SMTPAuthenticator();
            Session session = Session.getInstance(props, auth);
            
            // -- Create a new message --
            Message msg = new MimeMessage(session);
            // -- Set the FROM and TO fields --
            msg.setFrom(new InternetAddress(from));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            msg.setSubject(subject);
            msg.setText(message);
            // -- Set some other header information --
            msg.setHeader("ProjectEMail", "VIDYA");
            msg.setSentDate(new Date());
            // -- Send the message --
            Transport.send(msg);
            //System.out.println("Message sent to " + to + " OK.");
            return 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            //System.out.println("Exception " + ex);
            return -1;
        }
    }

// Also include an inner class that is used for authentication purposes
    //no need for IGAMI2
    private class SMTPAuthenticator extends javax.mail.Authenticator {

        @Override
        public PasswordAuthentication getPasswordAuthentication() {
            String username = "";           // specify your email id here (sender's email id)
            String password = "";                                  // specify your password here
            return new PasswordAuthentication(username, password);
        }
    }
}
