package igami2.DataBase.hibernateconfig;
// Generated Jul 17, 2013 10:33:09 PM by Hibernate Tools 3.2.1.GA

import javax.persistence.Entity;
import javax.persistence.Id;




/**
 * Users generated by hbm2java
 */
@Entity
public class Users  implements java.io.Serializable {


    @Id
     private int userid;
     private String fname;
     private String lname;
     private String email;
     private String username;
     private String password;
     private Byte tenure;
     private String subbasinOwnFarm;
     private String subbasinFarmCrop;
     private String subbasinFarmCash;
     private String cropKinds;
     private Integer cropFarmingDuration;
     private String interestedFarmbill;
     private String interestedPrograms;
     private String bmp;
     private String chosenff;
     private String landforPractice;
     private Byte practiceSatisfy;
     private Byte practiceFlooding;
     private Byte practiceFertilizer;
     private Byte practiceSoilerosion;
     private Byte practiceSoilhealth;
     private Byte benefitProgramincentive;
     private Byte benefitProductivity;
     private Byte benefitRent;
     private String motives;
     private Byte fertilizeruse;
     private Byte pesticideuse;
     private Byte averageyield;
     private Double bestcornyield;
     private Double worstcornyield;
     private Double bestsoybeanyield;
     private Double worstsoybeanyield;
     private Byte incomesourceFarming;

    public Users() {
    }

	
    public Users(int userid, String fname, String lname, String email) {
        this.userid = userid;
        this.fname = fname;
        this.lname = lname;
        this.email = email;
    }
    public Users(int userid, String fname, String lname, String email, String username, String password, Byte tenure, String subbasinOwnFarm, String subbasinFarmCrop, String subbasinFarmCash, String cropKinds, Integer cropFarmingDuration, String interestedFarmbill, String interestedPrograms, String bmp, String chosenff, String landforPractice, Byte practiceSatisfy, Byte practiceFlooding, Byte practiceFertilizer, Byte practiceSoilerosion, Byte practiceSoilhealth, Byte benefitProgramincentive, Byte benefitProductivity, Byte benefitRent, String motives, Byte fertilizeruse, Byte pesticideuse, Byte averageyield, Double bestcornyield, Double worstcornyield, Double bestsoybeanyield, Double worstsoybeanyield, Byte incomesourceFarming) {
       this.userid = userid;
       this.fname = fname;
       this.lname = lname;
       this.email = email;
       this.username = username;
       this.password = password;
       this.tenure = tenure;
       this.subbasinOwnFarm = subbasinOwnFarm;
       this.subbasinFarmCrop = subbasinFarmCrop;
       this.subbasinFarmCash = subbasinFarmCash;
       this.cropKinds = cropKinds;
       this.cropFarmingDuration = cropFarmingDuration;
       this.interestedFarmbill = interestedFarmbill;
       this.interestedPrograms = interestedPrograms;
       this.bmp = bmp;
       this.chosenff = chosenff;
       this.landforPractice = landforPractice;
       this.practiceSatisfy = practiceSatisfy;
       this.practiceFlooding = practiceFlooding;
       this.practiceFertilizer = practiceFertilizer;
       this.practiceSoilerosion = practiceSoilerosion;
       this.practiceSoilhealth = practiceSoilhealth;
       this.benefitProgramincentive = benefitProgramincentive;
       this.benefitProductivity = benefitProductivity;
       this.benefitRent = benefitRent;
       this.motives = motives;
       this.fertilizeruse = fertilizeruse;
       this.pesticideuse = pesticideuse;
       this.averageyield = averageyield;
       this.bestcornyield = bestcornyield;
       this.worstcornyield = worstcornyield;
       this.bestsoybeanyield = bestsoybeanyield;
       this.worstsoybeanyield = worstsoybeanyield;
       this.incomesourceFarming = incomesourceFarming;
    }
   
    public int getUserid() {
        return this.userid;
    }
    
    public void setUserid(int userid) {
        this.userid = userid;
    }
    public String getFname() {
        return this.fname;
    }
    
    public void setFname(String fname) {
        this.fname = fname;
    }
    public String getLname() {
        return this.lname;
    }
    
    public void setLname(String lname) {
        this.lname = lname;
    }
    public String getEmail() {
        return this.email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    public String getUsername() {
        return this.username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return this.password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    public Byte getTenure() {
        return this.tenure;
    }
    
    public void setTenure(Byte tenure) {
        this.tenure = tenure;
    }
    public String getSubbasinOwnFarm() {
        return this.subbasinOwnFarm;
    }
    
    public void setSubbasinOwnFarm(String subbasinOwnFarm) {
        this.subbasinOwnFarm = subbasinOwnFarm;
    }
    public String getSubbasinFarmCrop() {
        return this.subbasinFarmCrop;
    }
    
    public void setSubbasinFarmCrop(String subbasinFarmCrop) {
        this.subbasinFarmCrop = subbasinFarmCrop;
    }
    public String getSubbasinFarmCash() {
        return this.subbasinFarmCash;
    }
    
    public void setSubbasinFarmCash(String subbasinFarmCash) {
        this.subbasinFarmCash = subbasinFarmCash;
    }
    public String getCropKinds() {
        return this.cropKinds;
    }
    
    public void setCropKinds(String cropKinds) {
        this.cropKinds = cropKinds;
    }
    public Integer getCropFarmingDuration() {
        return this.cropFarmingDuration;
    }
    
    public void setCropFarmingDuration(Integer cropFarmingDuration) {
        this.cropFarmingDuration = cropFarmingDuration;
    }
    public String getInterestedFarmbill() {
        return this.interestedFarmbill;
    }
    
    public void setInterestedFarmbill(String interestedFarmbill) {
        this.interestedFarmbill = interestedFarmbill;
    }
    public String getInterestedPrograms() {
        return this.interestedPrograms;
    }
    
    public void setInterestedPrograms(String interestedPrograms) {
        this.interestedPrograms = interestedPrograms;
    }
    public String getBmp() {
        return this.bmp;
    }
    
    public void setBmp(String bmp) {
        this.bmp = bmp;
    }
    public String getChosenff() {
        return this.chosenff;
    }
    
    public void setChosenff(String chosenff) {
        this.chosenff = chosenff;
    }
    public String getLandforPractice() {
        return this.landforPractice;
    }
    
    public void setLandforPractice(String landforPractice) {
        this.landforPractice = landforPractice;
    }
    public Byte getPracticeSatisfy() {
        return this.practiceSatisfy;
    }
    
    public void setPracticeSatisfy(Byte practiceSatisfy) {
        this.practiceSatisfy = practiceSatisfy;
    }
    public Byte getPracticeFlooding() {
        return this.practiceFlooding;
    }
    
    public void setPracticeFlooding(Byte practiceFlooding) {
        this.practiceFlooding = practiceFlooding;
    }
    public Byte getPracticeFertilizer() {
        return this.practiceFertilizer;
    }
    
    public void setPracticeFertilizer(Byte practiceFertilizer) {
        this.practiceFertilizer = practiceFertilizer;
    }
    public Byte getPracticeSoilerosion() {
        return this.practiceSoilerosion;
    }
    
    public void setPracticeSoilerosion(Byte practiceSoilerosion) {
        this.practiceSoilerosion = practiceSoilerosion;
    }
    public Byte getPracticeSoilhealth() {
        return this.practiceSoilhealth;
    }
    
    public void setPracticeSoilhealth(Byte practiceSoilhealth) {
        this.practiceSoilhealth = practiceSoilhealth;
    }
    public Byte getBenefitProgramincentive() {
        return this.benefitProgramincentive;
    }
    
    public void setBenefitProgramincentive(Byte benefitProgramincentive) {
        this.benefitProgramincentive = benefitProgramincentive;
    }
    public Byte getBenefitProductivity() {
        return this.benefitProductivity;
    }
    
    public void setBenefitProductivity(Byte benefitProductivity) {
        this.benefitProductivity = benefitProductivity;
    }
    public Byte getBenefitRent() {
        return this.benefitRent;
    }
    
    public void setBenefitRent(Byte benefitRent) {
        this.benefitRent = benefitRent;
    }
    public String getMotives() {
        return this.motives;
    }
    
    public void setMotives(String motives) {
        this.motives = motives;
    }
    public Byte getFertilizeruse() {
        return this.fertilizeruse;
    }
    
    public void setFertilizeruse(Byte fertilizeruse) {
        this.fertilizeruse = fertilizeruse;
    }
    public Byte getPesticideuse() {
        return this.pesticideuse;
    }
    
    public void setPesticideuse(Byte pesticideuse) {
        this.pesticideuse = pesticideuse;
    }
    public Byte getAverageyield() {
        return this.averageyield;
    }
    
    public void setAverageyield(Byte averageyield) {
        this.averageyield = averageyield;
    }
    public Double getBestcornyield() {
        return this.bestcornyield;
    }
    
    public void setBestcornyield(Double bestcornyield) {
        this.bestcornyield = bestcornyield;
    }
    public Double getWorstcornyield() {
        return this.worstcornyield;
    }
    
    public void setWorstcornyield(Double worstcornyield) {
        this.worstcornyield = worstcornyield;
    }
    public Double getBestsoybeanyield() {
        return this.bestsoybeanyield;
    }
    
    public void setBestsoybeanyield(Double bestsoybeanyield) {
        this.bestsoybeanyield = bestsoybeanyield;
    }
    public Double getWorstsoybeanyield() {
        return this.worstsoybeanyield;
    }
    
    public void setWorstsoybeanyield(Double worstsoybeanyield) {
        this.worstsoybeanyield = worstsoybeanyield;
    }
    public Byte getIncomesourceFarming() {
        return this.incomesourceFarming;
    }
    
    public void setIncomesourceFarming(Byte incomesourceFarming) {
        this.incomesourceFarming = incomesourceFarming;
    }




}


