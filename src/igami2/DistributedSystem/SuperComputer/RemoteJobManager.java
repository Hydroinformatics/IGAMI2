/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DistributedSystem.SuperComputer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author VIDYA
 */
public class RemoteJobManager {

    private RemoteExec exec;
    private String userName = "vbsing";
    LinkedList<Job> job; //Stores all the active job
    Job lastJob; //Stores the last job not yet running
    Queue<Job> jobQueue; //Stores all the waiting job
    int sleepmin = 1000 * 5;
    private RemoteJobManagerDaemon jobDaemon;
    private String extn = "|cut -d\" \" -f 49,50,51";
    private int QUEUE = 0;
    private int RUNNING = 1;
    private int COMPLETED = 2;

    public RemoteJobManager(RemoteExec exec) {
        this.exec = exec;
        job = new LinkedList();
        jobQueue = new LinkedList();
        jobDaemon = new RemoteJobManagerDaemon(); //run the job Manager daemon
    }

    void submitJob() {
        Job j = jobQueue.remove();//remove the front job
        String cmd = j.getJobQuery();
        String status = exec.runRemoteCommand(cmd);
        //String status = "121212\n";
        if (status.length() > 0) {
            status = status.substring(0, status.length() - 1);
            System.out.println("Job submission status " + status);
            int jobId = Integer.parseInt(status);
            if (jobId > 0) {
                System.out.println("Job " + cmd + " Submitted sucessfully");
                j.setStatus("Q");
                j.setJobId(jobId);
                job.add(j);
                lastJob = j; //new last job
            }
        } else {
            System.out.println("Job " + cmd + " Submission Failed");
        }
    }

    int addJob(String jobName, String queue) {
        String cmd = "qsub -q " + queue + " " + jobName;
        int jobId = 0;
        Job j = new Job(cmd, jobId, "N");
        jobQueue.add(j);
        return jobId;
    }

    /*
     * Status will be either
     * R : Running
     * Q : In waiting Queue
     * C : Completed
     * otherwise job doesn't exist
     */
    int monitorJobSingle(int jobId) {
        int res = this.QUEUE;
        //jobId = 279603;

        String cmd = "qstat -u " + userName + " -r " + jobId + extn;
        String val = exec.runRemoteCommand(cmd);
        //separate the jobs and their status
        String[] StrAr = val.split("\n");
        if (StrAr.length > 0) {
            String st = StrAr[StrAr.length - 1];//get the latest

            String[] stAr = st.split(" ");

            for (int i = 0; i < stAr.length; i++) {
                if (stAr[i].compareToIgnoreCase("R") == 0)//Running
                {
                    res = this.RUNNING;
                    System.out.println("Status of job " + jobId + " is Running");
                    break;
                } else if (stAr[i].compareToIgnoreCase("C") == 0)//Completed
                {
                    res = this.COMPLETED;
                    System.out.println("Status of job " + jobId + " is Completed");
                    break;
                }
                else if (stAr[i].compareToIgnoreCase("Q") == 0)//Completed
                {
                    res = this.QUEUE;
                    System.out.println("Status of job " + jobId + " is in Queue");
                    break;
                }
            }
        }
        return res;
    }

    boolean monitorJobMultiple(int[] jobId) {
        boolean res = false;
        String cmd = "qstat -u " + userName;
        return res;
    }

    private class RemoteJobManagerDaemon implements Runnable {

        Thread t;

        public RemoteJobManagerDaemon() {
            t = new Thread(this, "RemoteJobManagerDaemon");
            t.start();
        }

        @Override
        public void run() {
            while (true) {
                if (lastJob != null) {
                    int stat = monitorJobSingle(lastJob.getJobId());
                    if (stat == RUNNING) {
                        lastJob.setStatus("R");
                        lastJob=null;
                        //last job is now running add another pending job
                        if (jobQueue.size() > 0)//more pending job
                        {
                            submitJob();
                        }
                    } else if (stat == COMPLETED) {
                        lastJob.setStatus("C");
                        lastJob=null;
                        //last job is now running add another pending job
                        if (jobQueue.size() > 0)//more pending job
                        {
                            submitJob();
                        }
                    }
                } else if (jobQueue.size() > 0)//First job
                {
                    submitJob();
                }
                try {
                    Thread.sleep(sleepmin);
                } catch (InterruptedException ex) {
                    Logger.getLogger(RemoteJobManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
