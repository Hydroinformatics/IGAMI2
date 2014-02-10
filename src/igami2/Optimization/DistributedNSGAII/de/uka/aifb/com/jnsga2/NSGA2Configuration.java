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

import java.io.Serializable;

/**
 * This class stores the configuration information for a run of the multi-objective genetic
 * algorithm NSGA-II. 
 * 
 * @author Joachim Melcher, Institut AIFB, Universitaet Karlsruhe (TH), Germany
 * @version 1.0
 */
public class NSGA2Configuration implements Serializable{
   
   private FitnessFunction[] fitnessFunctions;
   
   private double mutationProbability;
   private double crossoverProbability;
   
   private int populationSize;
   private int numberOfGenerations;
   
   /**
    * Constructor.
    * 
    * @param fitnessFunctions fitness function for the different objectives
    * @param mutationProbability mutation probability (0 <= . <= 1)
    * @param crossoverProbability crossover probability (0 <= . <= 1)
    * @param populationSize population size (must be divisible by four)
    * @param numberOfGenerations number of generations of the genetic algorithm
    */
   public NSGA2Configuration(FitnessFunction[] fitnessFunctions,
                             double mutationProbability, double crossoverProbability,
                             int populationSize, int numberOfGenerations) {
      if (fitnessFunctions == null) {
         throw new IllegalArgumentException("'fitnessFunctions' must not be null.");
      }
      if (mutationProbability < 0 || mutationProbability > 1) {
         throw new IllegalArgumentException("'mutationProbability' must be between 0 and 1.");
      }
      if (crossoverProbability < 0 || crossoverProbability > 1) {
         throw new IllegalArgumentException("'crossoverProbability' must be between 0 and 1.");
      }
      if (populationSize <= 0) {
         throw new IllegalArgumentException("'populationSize' must be a positive number.");
      }
      if (populationSize % 4 != 0) {
         throw new IllegalArgumentException("'populationSize' must be divisible by four.");
      }
      if (numberOfGenerations <= 0) {
         throw new IllegalArgumentException("'numberOfGenerations' must be a positive number.");
      }
      
      this.fitnessFunctions = fitnessFunctions;
      this.mutationProbability = mutationProbability;
      this.crossoverProbability = crossoverProbability;
      this.populationSize = populationSize;
      this.numberOfGenerations = numberOfGenerations;
   }
   
   /**
    * Gets the fitness function for the index-th objective (0 ... n-1).
    * 
    * @param index index
    * @return fitness function for the index-th objective
    * @throws IndexOutOfBoundsException if index is out of bounds
    */
   public FitnessFunction getFitnessFunction(int index) throws IndexOutOfBoundsException {
      return fitnessFunctions[index];
   }
   
   /**
    * Gets the number of objectives.
    * 
    * @return number of objectives
    */
   public int getNumberOfObjectives() {
      return fitnessFunctions.length;
   }
   
   /**
    * Gets the mutation probability.
    * 
    * @return mutation probability
    */
   public double getMutationProbability() {
      return mutationProbability;
   }
   
   /**
    * Gets the crossover probability.
    * 
    * @return crossover probability
    */
   public double getCrossoverProbability() {
      return crossoverProbability;
   }
   
   /**
    * Gets the population size of one generation of the genetic algorithm.
    * 
    * @return population size
    */
   public int getPopulationSize() {
      return populationSize;
   }
   
   /**
    * Gets the number of generations of the genetic algorithm.
    * 
    * @return number of generations
    */
   public int getNumberOfGenerations() {
      return numberOfGenerations;
   }
}