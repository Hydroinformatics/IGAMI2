/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DistributedSystem.SuperComputer;

/**
 *
 * @author VIDYA
 */
import com.jcraft.jsch.*;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.*;
import java.util.logging.Level;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class RemoteExec {

    private String user = "vbsingh";
 //   private String host = "pegasus.cs.iupui.edu";
    private String host = "bigred2.uits.iu.edu";
    private Session session = null;
    private String wrd = "somepass";
    
    RemoteExec()
    {
        
    }

    public void createRemoteSession() {
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(user, host, 22);
            //session.setPassword(wrd);
            //UserInfo ui = new Shell.MyUserInfo() {};
            UserInfo ui=new RemoteExec.MyUserInfo();
            
            session.setUserInfo(ui);
            session.connect();
        } catch (JSchException ex) {
            java.util.logging.Logger.getLogger(RemoteExec.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void destroyRemoteSession()
    {
        try
        {
            session.disconnect();
            System.out.println("Logout from System Sucessful");
        }catch(Exception ex)
        {
            System.out.println("Destruction of Session Failed");
            ex.printStackTrace();
        }
    }

    public String runRemoteCommand(String command) {
        String res = "";
        try {
            if(session.isConnected())
            {
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            InputStream in = channel.getInputStream();
            channel.connect();
            
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String ll = "";
            while((ll=br.readLine())!=null)
            {
                res = res+ll+"\n";   
                System.out.println(ll);
                if (channel.isClosed()) {
                    System.out.println("exit-status: " + channel.getExitStatus());
                    break;
                }
            }
            channel.disconnect();  
            }
            else
            {
                System.out.println("The session is disconnected ");
            }
        } catch (JSchException ex) {
            java.util.logging.Logger.getLogger(RemoteExec.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(RemoteExec.class.getName()).log(Level.SEVERE, null, ex);
        }

        return res;
    }
    
    public static class MyUserInfo implements UserInfo, UIKeyboardInteractive{
    public String getPassword(){ return passwd; }
    public boolean promptYesNo(String str){
        //return true;
        
      Object[] options={ "yes", "no" };
      int foo=JOptionPane.showOptionDialog(null, 
             str,
             "Warning", 
             JOptionPane.DEFAULT_OPTION, 
             JOptionPane.WARNING_MESSAGE,
             null, options, options[0]);
       return foo==0;       
    }
  
    String passwd;
    JTextField passwordField=(JTextField)new JPasswordField(20);

    public String getPassphrase(){ return null; }
    public boolean promptPassphrase(String message){ return true; }
    public boolean promptPassword(String message){
      Object[] ob={passwordField}; 
      int result=
        JOptionPane.showConfirmDialog(null, ob, message,
                                      JOptionPane.OK_CANCEL_OPTION);
      if(result==JOptionPane.OK_OPTION){
        passwd=passwordField.getText();
        return true;
      }
      else{ 
        return false; 
      }
    }
    public void showMessage(String message){
      JOptionPane.showMessageDialog(null, message);
    }
    final GridBagConstraints gbc = 
      new GridBagConstraints(0,0,1,1,1,1,
                             GridBagConstraints.NORTHWEST,
                             GridBagConstraints.NONE,
                             new Insets(0,0,0,0),0,0);
    private Container panel;
    public String[] promptKeyboardInteractive(String destination,
                                              String name,
                                              String instruction,
                                              String[] prompt,
                                              boolean[] echo){
      panel = new JPanel();
      panel.setLayout(new GridBagLayout());

      gbc.weightx = 1.0;
      gbc.gridwidth = GridBagConstraints.REMAINDER;
      gbc.gridx = 0;
      panel.add(new JLabel(instruction), gbc);
      gbc.gridy++;

      gbc.gridwidth = GridBagConstraints.RELATIVE;

      JTextField[] texts=new JTextField[prompt.length];
      for(int i=0; i<prompt.length; i++){
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.weightx = 1;
        panel.add(new JLabel(prompt[i]),gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 1;
        if(echo[i]){
          texts[i]=new JTextField(20);
        }
        else{
          texts[i]=new JPasswordField(20);
        }
        panel.add(texts[i], gbc);
        gbc.gridy++;
      }

      if(JOptionPane.showConfirmDialog(null, panel, 
                                       destination+": "+name,
                                       JOptionPane.OK_CANCEL_OPTION,
                                       JOptionPane.QUESTION_MESSAGE)
         ==JOptionPane.OK_OPTION){
        String[] response=new String[prompt.length];
        for(int i=0; i<prompt.length; i++){
          response[i]=texts[i].getText();
        }
	return response;
      }
      else{
        return null;  // cancel
      }
    }
  }
    /*
    public static abstract class MyUserInfo
                          implements UserInfo, UIKeyboardInteractive{
    public String getPassword(){ return null; }
    public boolean promptYesNo(String str){ return true; }
    public String getPassphrase(){ return null; }
    public boolean promptPassphrase(String message){ return false; }
    public boolean promptPassword(String message){ return false; }
    public void showMessage(String message){ }
    public String[] promptKeyboardInteractive(String destination,
                                              String name,
                                              String instruction,
                                              String[] prompt,
                                              boolean[] echo){
      return null;
    }
  }
  */
    
    
}
