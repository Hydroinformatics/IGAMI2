/* ===========================================================
 * JNSGA2: a free NSGA-II library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2006-2007, Joachim Melcher, Institut AIFB, Universitaet Karlsruhe (TH), Germany
 *
 * Project Info:  http://sourceforge.net/projects/jnsga2/
 *
 * This library is free software; you can redistribute it and/or modify it  under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package igami2.Optimization.DistributedNSGAII.de.uka.aifb.com.jnsga2;

import java.io.IOException;
import java.io.Serializable;

/*
 * Changes:
 * ========
 *
 * 2007-09-11: dominates was rewritten: special cases if at least one fitness value is 'NaN'
 *             (not a number)
 */

/**
 * This abstract class implements an individual for the multi-objective genetic algorithm NSGA-II.
 * 
 * @author Joachim Melcher, Institut AIFB, Universitaet Karlsruhe (TH), Germany
 * @version 1.1
 */
public abstract class Individual implements Serializable{
   
   /** NSGA2 instance this individual belongs to */
   public transient NSGA2 nsga2;
   public int rank;
   public int rating=0;//keep the individual feedback
   public double confidence=0; //keep individual confidence
   public double crowdingDistance;
   public FitnessFunction[] fitnessFunctions;
   public double[] fitnessValues;
   public int[] regionSubbasinId;// This consists of the SWAT IDs of sub-basins that are being optimized for wetland installation
   public int[] chosenBMPs; // This stores flags for which BMPs are used to define the individuals.
   public int [] chosenFF; // This stores the flags for which fitness functions are being used in the optimization.
   public double[] assignments; // This array stores the actual decision variable values for all BMPs and sub-basins
   public int UserId =0; //default for the system
   public int IndvId =0;//unique identifier for each individual for a particual user
   public double [][] subbasinsFF; // This stores the fitness function values at sub-basin scale for every sub-basin (except for omittedSubBasins).
   public int[] tenure_regionSubbasinId; // This consists of the tenure in every sub-basin that is being optimized      
   
   
   /**
    * Constructor.
    * 
    * @param nsga2 NSGA-II instance this individual is used for
    */
   public Individual(NSGA2 nsga2) {
       /*
      if (nsga2 == null) {
         throw new IllegalArgumentException("'nsga2' must not be null.");
      }
      * 
      */
      
      this.nsga2 = nsga2;
   }
   
   /**
    * Gets the number of objectives.
    * 
    * @return number of objectives
    */
   public int getNumberOfObjectives() {
      return nsga2.getNSGA2Configuration().getNumberOfObjectives();
   }
   
   /**
    * Gets this individual's fitness value for the index-th objective.
    * 
    * Classes implementing this abstract method must ensure that the fitness values for the
    * different objectives are updated right after creation, crossover or mutation.
    * 
    * @param index index
    * @return fitness value for the index-th objective
    * @throws IndexOutOfBoundsException if the index is out of bounds
    */
   public abstract double getFitnessValue(int index) throws IndexOutOfBoundsException;
   
   /**
    * Gets this individual's rank.
    * 
    * @return rank
    */
   public int getRank() {
      return rank;
   }
   
   /**
    * Sets this individual's rank.
    * 
    * @param rank rank (rank >= 1)
    */
   protected void setRank(int rank) {
      if (rank < 1) {
         throw new IllegalArgumentException("'rank' must be greater than or equal one.");
      }
      
      this.rank = rank;
   }
   
   /**
    * Gets this individual's crowding distance.
    * 
    * @return crowding distance
    */
   public double getCrowdingDistance() {
      return crowdingDistance;
   }
   
   /**
    * Sets this individual's crowding distance.
    * 
    * @param crowdingDistance crowding distance
    */
   protected void setCrowdingDistance(double crowdingDistance) {
      if (crowdingDistance < 0) {
         throw new IllegalArgumentException("'crowdingDistance' must not be negative.");
      }
      
      this.crowdingDistance = crowdingDistance;
   }
   
   /**
    * Checks wheter this individual dominates the specified other individual, i.e. it is at least as
    * good as the other one in all objectives and for at least one objective it is better (i.e.
    * smaller fitness value).
    * 
    * @param otherIndividual other individual
    * @return <code>true</code> iff this individual dominiates the specified one
    */
   public boolean dominates(Individual otherIndividual) {
      if (otherIndividual == null) {
         throw new IllegalArgumentException("'otherIndividual' must not be null.");
      }
      
      if (nsga2 != otherIndividual.nsga2) {
         throw new IllegalArgumentException("Both individuals must belong to the same NSGA-II instance.");
      }
      
      /* check special cases: at least one fitness value is 'NaN' (not a number) */
      boolean hasThisNaN = false;
      boolean hasOtherNaN = false;
      
      for (int i = 0; i < nsga2.getNSGA2Configuration().getNumberOfObjectives(); i++) {
         if (new Double(getFitnessValue(i)).equals(Double.NaN)) {
            hasThisNaN = true;
         }
         if (new Double(otherIndividual.getFitnessValue(i)).equals(Double.NaN)) {
            hasOtherNaN = true;
         }
      }
      
      if (hasThisNaN) {
         return false;
      }
      
      if (!hasThisNaN && hasOtherNaN) {
         return true;
      }
      
      // both individuals have no 'NaN'
      
      boolean atLeastOneObjectiveBetter = false;
      
      for (int i = 0; i < nsga2.getNSGA2Configuration().getNumberOfObjectives(); i++) {
         if (getFitnessValue(i) > otherIndividual.getFitnessValue(i)) {
            return false;
         }
         if (getFitnessValue(i) < otherIndividual.getFitnessValue(i)) {
            atLeastOneObjectiveBetter = true;
         }
      }
      
      return atLeastOneObjectiveBetter;
   }
   
   /**
    * Checks whether this individual is better using the crowded comparison operator than the
    * specified other individual. An individual is better (1) if it has a smaller rank or (2) if
    * it has the same rank but its crowding distance is greater.
    * 
    * Before invoking this method, the rank and crowding distances have to be set using
    * {@link #setRank(int)} and {@link #setCrowdingDistance(double)} respectively.
    * 
    * @param otherIndividual other individual
    * @return <code>true</code> iff this individual is better using the crowded comparison operator
    *                           than the specified one
    */
   public boolean isCrowdedComparisonOperatorBetter(Individual otherIndividual) {
      if (otherIndividual == null) {
         throw new IllegalArgumentException("'otherIndividual' must not be null.");
      }
      
      if (nsga2 != otherIndividual.nsga2) {
         throw new IllegalArgumentException("Both individuals must belong to the same NSGA-II instance.");
      }
      
      if (getRank() < otherIndividual.getRank()) {
         return true;
      }
      if (getRank() == otherIndividual.getRank()
            && getCrowdingDistance() > otherIndividual.getCrowdingDistance()) {
         return true;
      }
      
      return false;
   }
   
   /**
    * Mutates this individual.
    * 
    * Classes implementing this abstract method must ensure that the fitness values for the
    * different objectives are updated right after mutation.
    */
   protected abstract void mutate ();
   
   /**
    * Does a crossover between the two individuals. Afterwards, both individuals are altered. If
    * the origial individuals are still needed, use the {@link #clone()} method to get clones and
    * use them instead.
    * 
    * Classes implementing this abstract method must ensure that the fitness values for the
    * different objectives (for both individuals!) are updated right after crossover.
    * 
    * @param otherIndividual other individual
 * @throws IOException 
    */
   protected abstract void crossover (Individual otherIndividual) throws IOException;
   
   /**
    * Creates a copy of this object, so that changes on the clone do not change the intern data of
    * the original.
    * 
    * @return cloned object
    */
   public Object clone() {
      Individual clone = null;
      try {
		clone = createClonedIndividual();
	  } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
      
      // clone rank
      int rank = getRank();
      if (rank != 0 ) {
         clone.setRank(rank);
      }
      
      // clone crowding distance
      clone.setCrowdingDistance(getCrowdingDistance());
      
      return clone;
   }
   
   /**
    * Creates a clone of this individual, so that changes on the clone do not change the intern data
    * of the original. The rank and crowding distance are not copied by this method. The NSGA-II
    * instance is only copied.
    * 
    * @return cloned individual (without correct rank and crowding distance)
 * @throws IOException 
    */
   protected abstract Individual createClonedIndividual() throws IOException;
   
   public abstract void evaluateIndividual(int idx);
   public abstract void evaluateRating(); //evaluate the last fitness function
   
   public void printAssignment()
   {
       
       for(int i=0;i<assignments.length;i++)
       {
           System.out.print(assignments[i]+" ");           
       }
       System.out.println();
   }
   
   public void printFitnessValues()
   {
       //System.out.println(" Fitness Values ");
       for(int i=0;i<fitnessValues.length;i++)
       {
           System.out.print("\tF"+i+" "+fitnessValues[i]);
       }
       System.out.println();
   }
}