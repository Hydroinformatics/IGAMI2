package igami2.DataBase.hibernateconfig;
// Generated Jul 17, 2013 10:33:09 PM by Hibernate Tools 3.2.1.GA



/**
 * BmpinfoId generated by hbm2java
 */
public class BmpinfoId  implements java.io.Serializable {


     private long b0;
     private long b1;
     private long b2;
     private long b3;
     private long b4;
     private long b5;
     private long b6;
     private long b7;
     private long b8;
     private long b9;

    public BmpinfoId() {
    }

    public BmpinfoId(long b0, long b1, long b2, long b3, long b4, long b5, long b6, long b7, long b8, long b9) {
       this.b0 = b0;
       this.b1 = b1;
       this.b2 = b2;
       this.b3 = b3;
       this.b4 = b4;
       this.b5 = b5;
       this.b6 = b6;
       this.b7 = b7;
       this.b8 = b8;
       this.b9 = b9;
    }
   
    public long getB0() {
        return this.b0;
    }
    
    public void setB0(long b0) {
        this.b0 = b0;
    }
    public long getB1() {
        return this.b1;
    }
    
    public void setB1(long b1) {
        this.b1 = b1;
    }
    public long getB2() {
        return this.b2;
    }
    
    public void setB2(long b2) {
        this.b2 = b2;
    }
    public long getB3() {
        return this.b3;
    }
    
    public void setB3(long b3) {
        this.b3 = b3;
    }
    public long getB4() {
        return this.b4;
    }
    
    public void setB4(long b4) {
        this.b4 = b4;
    }
    public long getB5() {
        return this.b5;
    }
    
    public void setB5(long b5) {
        this.b5 = b5;
    }
    public long getB6() {
        return this.b6;
    }
    
    public void setB6(long b6) {
        this.b6 = b6;
    }
    public long getB7() {
        return this.b7;
    }
    
    public void setB7(long b7) {
        this.b7 = b7;
    }
    public long getB8() {
        return this.b8;
    }
    
    public void setB8(long b8) {
        this.b8 = b8;
    }
    public long getB9() {
        return this.b9;
    }
    
    public void setB9(long b9) {
        this.b9 = b9;
    }


   public boolean equals(Object other) {
         if ( (this == other ) ) return true;
		 if ( (other == null ) ) return false;
		 if ( !(other instanceof BmpinfoId) ) return false;
		 BmpinfoId castOther = ( BmpinfoId ) other; 
         
		 return (this.getB0()==castOther.getB0())
 && (this.getB1()==castOther.getB1())
 && (this.getB2()==castOther.getB2())
 && (this.getB3()==castOther.getB3())
 && (this.getB4()==castOther.getB4())
 && (this.getB5()==castOther.getB5())
 && (this.getB6()==castOther.getB6())
 && (this.getB7()==castOther.getB7())
 && (this.getB8()==castOther.getB8())
 && (this.getB9()==castOther.getB9());
   }
   
   public int hashCode() {
         int result = 17;
         
         result = 37 * result + (int) this.getB0();
         result = 37 * result + (int) this.getB1();
         result = 37 * result + (int) this.getB2();
         result = 37 * result + (int) this.getB3();
         result = 37 * result + (int) this.getB4();
         result = 37 * result + (int) this.getB5();
         result = 37 * result + (int) this.getB6();
         result = 37 * result + (int) this.getB7();
         result = 37 * result + (int) this.getB8();
         result = 37 * result + (int) this.getB9();
         return result;
   }   


}


