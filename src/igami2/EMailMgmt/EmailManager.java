/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.EMailMgmt;

/**
 *
 * @author VIDYA
 */
public class EmailManager {
    
    private String smtpServ = "smtp.cs.iupui.edu";
    private String from = "wrestore@iupui.edu";
    private String subject ="WRESTORE Project Ready for feedback  - No Reply";
    private String msg = "Hi,\n"
            + "The wrestore website is ready to take your feedback";
    
    private String subject_search_feedback = "Next search session ready!";
    private String subject_introspection = "Next introspection session ready!";
    private String subject_automated = "Automated search has been initiated!";
    private String subject_search_finished = "Search Finished";
    private String subject_search_begin = "Welcome Message Search Begins";
    private String subject_admin1 = "Search Started";
    private String subject_admin2 = "Search Finished";
    
    private String msg_search_feedback = "The next group of newly found alternatives are now ready for your viewing. Please log in to the WRESTORE website and provide your feedback on the alternatives. \nhttp://wrestore.iupui.edu/model/login.php \n\nNote: This is an automated message, so please do not reply.";
    private String msg_introspection = "The updated group of previously found desirable alternatives are now ready for your viewing. Please log in to the WRESTORE website and provide your feedback on the alternatives. \nhttp://wrestore.iupui.edu/model/login.php \n\nNote: This is an automated message, so please do not reply.";
    private String msg_automated = "Thank you for your time and effort. \nAn automated search has been initiated based on your feedback. This search will take many hours to complete, and we do not need any input from you during this process, it will go till next day. When the new alternatives found by this automated search are ready for viewing, you will receive an email asking for a final evaluation.\n\nNote: This is an automated message, so please do not reply.";
    private String msg_search_finished = "Congratulation you have just finished the current search. \n\nNow you can start another search, if you have any pending search requests or you can leave. We will contact you if you need to take some further actions. \n\nThanks for participating in this experiment, we appreciat your hardwork.\n\nNote: This is an automated message, so please do not reply.";
    private String msg_search_begin = "Welcome to Wrestore Experiments. Please provide your feedback on the alternatives. If you have already given the first set of feedback please ignore this message. \nhttp://wrestore.iupui.edu/model/login.php \n\nNote: This is an automated message, so please do not reply.";
    private String msg_admin1 = "Search is started for User ";
    private String msg_admin2 = "Search is finished for User ";
    
    
    private String from_signature = "WRESTORE Team";
    private String del = "\n";
    private String greetings = "Dear ";
    
    public int SEARCH_FEEDBACK = 0;
    public int INTROSPECTION = 1;
    public int AUTOMATED = 2;
    public int ADMIN1 = 3;
    public int SEARCH_FINISHED = 4;
    public int ADMIN2 = 5;
    public int SEARCH_BEGIN = 6;
    
    
    /*
     * I don't know if its thread safe or not so using it global
     */
    
    public EmailManager()
    {
        
    }
    
    
    public synchronized void sendEmail(String to, int type, int UserId, String name)
    {
        //create the msg based on type
        String greet = greetings+name+",";
        if(type==SEARCH_FEEDBACK)
        {
            subject = subject_search_feedback;
            msg =  greet+del+del+del+msg_search_feedback+del+del+del+del+from_signature;
        }
        else if(type==INTROSPECTION)
        {
            subject = subject_introspection;
            msg =  greet+del+del+del+msg_introspection+del+del+del+del+from_signature;
        }
        else if(type==AUTOMATED)
        {
            subject = subject_automated;
            msg =  greet+del+del+del+msg_automated+del+del+del+del+from_signature;
        }        
        else if(type==ADMIN1)
        {
            subject = subject_admin1;
            msg =  greet+del+del+del+msg_admin1+UserId+"."+del+del+del+del+from_signature;
        }
        else if(type==ADMIN2)
        {
            subject = subject_admin2;
            msg =  greet+del+del+del+msg_admin2+UserId+"."+del+del+del+del+from_signature;
        }
        else if(type==SEARCH_FINISHED)
        {
            subject = subject_search_finished;
            msg =  greet+del+del+del+msg_search_finished+del+del+del+del+from_signature;
        }
        else if(type==SEARCH_BEGIN)
        {
            subject = subject_search_begin;
            msg =  greet+del+del+del+msg_search_begin+del+del+del+del+from_signature;
        }
        
        Mail mail = new Mail();
        mail.setTo(to);
        mail.setFrom(from);
        mail.setSubject(subject);
        mail.setMsg(msg);
        mail.setSMTP(smtpServ);

        //mail.sendMail();

        int result = mail.sendMail();
        if (result == 0) {
            System.out.println("Mail "+subject+" Successfully Sent to " + UserId);
        } else {
            System.out.println("Mail NOT Sent to " + UserId);
        }
    }
}
