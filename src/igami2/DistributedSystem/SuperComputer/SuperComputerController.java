/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DistributedSystem.SuperComputer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author VIDYA
 */
public class SuperComputerController {
    private static String queue_type = "cpu";//use CPU as defaul queue for the remote job
    private static String homeDirCmd = "~/matlab_dir/";//move to the default matlab directory to begin
    private RemoteJobManager remotejobManager;

    public SuperComputerController()
    {
        
    }
    
    public void initSuperComputer()
    {
        RemoteExec re = new RemoteExec();
        re.createRemoteSession();
        remotejobManager = new RemoteJobManager(re);
    }
    
    public void addRemoteJob(String cmd)
    {
        remotejobManager.addJob(homeDirCmd+cmd,queue_type);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        RemoteExec re = new RemoteExec();
        re.createRemoteSession();
            //re.runRemoteCommand(homeDirCmd);
            System.out.println("Present Working Remote Directory is "+re.runRemoteCommand("pwd"));
            String cmd="";
            RemoteJobManager remotejobManager = new RemoteJobManager(re);
        try {
            
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("\nPlease Enter Jobs as rj jobscriptName");
            
            while(true)
            {
                System.out.println("\nPlease Enter Command");
                cmd = br.readLine();
                if(cmd.compareToIgnoreCase("logout")==0)
                    break;
                /*
                String val = re.runRemoteCommand(cmd);
                System.out.println(val);
                String[] StrAr = val.split("\n");
                String[] stAr = StrAr[StrAr.length-1].split(" ");
                String stat = stAr[52];
                System.out.println("Status is "+stat);
                */
                String[] stAr = cmd.split(" ");
                if(stAr[0].compareToIgnoreCase("rj")==0) //a remote job
                {
                    remotejobManager.addJob(homeDirCmd+stAr[1],queue_type);
                }
                //System.out.println("Sucessfully Executed Remote Cmd");
            }            
            
        } catch (IOException ex) {
            Logger.getLogger(SuperComputerController.class.getName()).log(Level.SEVERE, null, ex);
        }
        re.destroyRemoteSession();
    }
    
    
}
