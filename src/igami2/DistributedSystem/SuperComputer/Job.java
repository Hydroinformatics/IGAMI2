/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package igami2.DistributedSystem.SuperComputer;

import java.util.Date;

/**
 *
 * @author VIDYA
 */
public class Job {
    
    private int JobId; //jobId assigned to this job
    private String JobQuery; // Query used by the job
    private String Status; // Status of the job
    private Date BeginTime; //
    private Date EndTime; //Stores the final wall time, after which the job will be purged
    
    public Job(String JobQuery,int JobId, String Status)
    {
        this.JobQuery = JobQuery;
        this.JobId = JobId;
        this.Status = Status;
    }

    public int getJobId() {
        return JobId;
    }

    public void setJobId(int JobId) {
        this.JobId = JobId;
    }

    public String getJobQuery() {
        return JobQuery;
    }

    public void setJobQuery(String JobQuery) {
        this.JobQuery = JobQuery;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String Status) {
        this.Status = Status;
    }
    
    
}
