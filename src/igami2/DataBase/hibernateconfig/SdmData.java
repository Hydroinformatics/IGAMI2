package igami2.DataBase.hibernateconfig;
// Generated Jul 17, 2013 10:33:09 PM by Hibernate Tools 3.2.1.GA

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;




/**
 * SdmData generated by hbm2java
 */
@Entity
public class SdmData  implements java.io.Serializable {


    @EmbeddedId
     private SdmDataId id;
     private Integer nnwin;
     private Double minErrorNn;
     private String minErrorNnvals;
     private String minErrorNnid;
     private Double linearModelMinError;
     private Integer currentLinearId;
     private String linearModelErrors;
     private Double minAnfis;
     private Integer minAnfisid;
     private String range;

    public SdmData() {
    }

	
    public SdmData(SdmDataId id) {
        this.id = id;
    }
    public SdmData(SdmDataId id, Integer nnwin, Double minErrorNn, String minErrorNnvals, String minErrorNnid, Double linearModelMinError, Integer currentLinearId, String linearModelErrors, Double minAnfis, Integer minAnfisid, String range) {
       this.id = id;
       this.nnwin = nnwin;
       this.minErrorNn = minErrorNn;
       this.minErrorNnvals = minErrorNnvals;
       this.minErrorNnid = minErrorNnid;
       this.linearModelMinError = linearModelMinError;
       this.currentLinearId = currentLinearId;
       this.linearModelErrors = linearModelErrors;
       this.minAnfis = minAnfis;
       this.minAnfisid = minAnfisid;
       this.range = range;
    }
   
    public SdmDataId getId() {
        return this.id;
    }
    
    public void setId(SdmDataId id) {
        this.id = id;
    }
    public Integer getNnwin() {
        return this.nnwin;
    }
    
    public void setNnwin(Integer nnwin) {
        this.nnwin = nnwin;
    }
    public Double getMinErrorNn() {
        return this.minErrorNn;
    }
    
    public void setMinErrorNn(Double minErrorNn) {
        this.minErrorNn = minErrorNn;
    }
    public String getMinErrorNnvals() {
        return this.minErrorNnvals;
    }
    
    public void setMinErrorNnvals(String minErrorNnvals) {
        this.minErrorNnvals = minErrorNnvals;
    }
    public String getMinErrorNnid() {
        return this.minErrorNnid;
    }
    
    public void setMinErrorNnid(String minErrorNnid) {
        this.minErrorNnid = minErrorNnid;
    }
    public Double getLinearModelMinError() {
        return this.linearModelMinError;
    }
    
    public void setLinearModelMinError(Double linearModelMinError) {
        this.linearModelMinError = linearModelMinError;
    }
    public Integer getCurrentLinearId() {
        return this.currentLinearId;
    }
    
    public void setCurrentLinearId(Integer currentLinearId) {
        this.currentLinearId = currentLinearId;
    }
    public String getLinearModelErrors() {
        return this.linearModelErrors;
    }
    
    public void setLinearModelErrors(String linearModelErrors) {
        this.linearModelErrors = linearModelErrors;
    }
    public Double getMinAnfis() {
        return this.minAnfis;
    }
    
    public void setMinAnfis(Double minAnfis) {
        this.minAnfis = minAnfis;
    }
    public Integer getMinAnfisid() {
        return this.minAnfisid;
    }
    
    public void setMinAnfisid(Integer minAnfisid) {
        this.minAnfisid = minAnfisid;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }
}


