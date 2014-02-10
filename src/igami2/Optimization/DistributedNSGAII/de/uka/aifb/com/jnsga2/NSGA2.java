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

import igami2.DistributedSystem.MasterComputer.PopulationEvaluation;
import java.util.*;
import java.io.IOException;
import java.io.Serializable;

/*
 * Changes:
 * ========
 *
 * 2007-09-07: crowdingDistanceAssignment was rewritten: special case for equal minimal and maximal
 *             fitness values for one objective
 */
/**
 * This class implements the multi-objective genetic algorithm NSGA-II as described in
 * 
 * DEB, Kalyanmoy ; PRATAP, Amrit ; AGARWAL, Sameer A. ; MEYARIVAN, T.: "A Fast and Elitist
 * Multiobjective Genetic Algorithm: NSGA-II". In: IEEE Transactions on Evolutionary Computation,
 * vol. 6, no. 2, April 2002, pp. 182-197.
 * 
 * This genetic algorithm tries to <b>minimize</b> the different objectives.
 * 
 * In order to use this algorithm for a special problem, you have to overwrite the class
 * {@link de.uka.aifb.com.jnsga2.Individual} with a genetic representation suitable for this problem.
 * In addition, you have to give an implementation of the interface
 * {@link de.uka.aifb.com.jnsga2.FitnessFunction} for each single objective.
 * <p>
 * <b>Usage:</b>
 * <p>
 * First, you have to create a {@link de.uka.aifb.com.jnsga2.NSGA2Configuration} instance with the
 * necessary NSGA-II parameters (mutation and crossover probability, population size, number of
 * generations and the used fitness functions). With this configuration, a new NSGA-II instance can
 * be created.
 * <p>
 * If you are interested in detailed information after each evolutionary step of the algorithm
 * (e.g. number of current generation, best individuals so far, etc.), you can add an
 * {@link de.uka.aifb.com.jnsga2.NSGA2Listener} to this NSGA-II instance.
 * <p>
 * Next, you have to create the start population with the correct population size as in the used
 * NSGA-II configuration. Then, the method {@link #evolve(LinkedList)} can be invoked. After the
 * specified number of generations, the best found (non-dominated) individuals are returned. 
 * 
 * @author Joachim Melcher, Institut AIFB, Universitaet Karlsruhe (TH), Germany
 * @version 1.1
 */
public class NSGA2 implements Serializable {

    private NSGA2Configuration conf;
    private HashSet<NSGA2Listener> nsga2listeners;
    int userId = 0;
    int numberGeneration=1;
    public transient PopulationEvaluation evaluator; //use to evaluate population
    public transient boolean researchEvaluation = false; //check if it is used for noninteractive search

    /**
     * Constructor.
     * 
     * @param conf configuration
     */
    public NSGA2(NSGA2Configuration conf) {
        if (conf == null) {
            throw new IllegalArgumentException("'conf' must not be null.");
        }

        this.conf = conf;
        nsga2listeners = new HashSet<NSGA2Listener>();
        
    }

    public void setPopulationEvaluator(PopulationEvaluation evaluator)
    {
        this.evaluator = evaluator;
    }

    public void setResearchEvaluation(boolean researchEvaluation) {
        this.researchEvaluation = researchEvaluation;
    }
    
    /**
     * Gets the NSGA-II configuration.
     * 
     * @return NSGA-II configuration
     */
    public NSGA2Configuration getNSGA2Configuration() {
        return conf;
    }

    /**
     * Adds the specified NSGA-II listener.
     * 
     * @param nsga2listener NSGA-II listener
     */
    public void addNSGA2Listener(NSGA2Listener nsga2listener) {
        if (nsga2listener == null) {
            throw new IllegalArgumentException("'nsga2listener' must not be null.");
        }

        nsga2listeners.add(nsga2listener);
    }

    /**
     * Removes the specified NSGA-II listener.
     * 
     * @param nsga2listener NSGA-II listener
     */
    public void removeNSGA2Listener(NSGA2Listener nsga2listener) {
        if (nsga2listener == null) {
            throw new IllegalArgumentException("'nsga2listener' must not be null.");
        }

        nsga2listeners.remove(nsga2listener);
    }

    /**
     * Initializes and runs the NSGA-II algorithm.
     * 
     * @param startPopulation start population (population size as in configuration!)
     * @return best individuals after NSGA-II run (only non-dominated ones => rank 1)
     */
    public LinkedList<Individual> evolveOneGen(LinkedList<Individual> startPopulation) throws IOException {
        if (startPopulation == null) {
            throw new IllegalArgumentException("'startPopulation' must not be null.");
        }
        if (startPopulation.size() != conf.getPopulationSize()) {
            throw new IllegalArgumentException("Incorrect start population size.");
        }

        //sent for first time evaluation
        //startPopulation = EvaluateGeneration(startPopulation);

        // check whether all individuals in start population have this NSGA-II instance
        for (Individual individual : startPopulation) {
            if (individual.nsga2 != this) {
                throw new IllegalArgumentException("All individuals in start population must have this NSGA-II instance.");
            }
        }


        LinkedList<Individual> population_p_t = startPopulation;

        //for (int numberGeneration = 1; numberGeneration <= conf.getNumberOfGenerations(); numberGeneration++) 
        {
        
          //  System.out.println("Generation Number: " + numberGeneration);
            // call NSGA-II listeners        
            if (!nsga2listeners.isEmpty()) {
                LinkedList<LinkedList<Individual>> temp = fastNonDominatedSort(population_p_t);
                // only return non-dominated individuals (=> first frontier)
                LinkedList<Individual> bestIndividuals = temp.getFirst();

                // create NSGA-II event
                NSGA2Event event = new NSGA2Event(this, bestIndividuals, numberGeneration - 1);
                for (NSGA2Listener listener : nsga2listeners) {
                    listener.performNSGA2Event(event);
                }
            }

            LinkedList<Individual> population_q_t = makeNewPopulation(population_p_t);
            LinkedList<Individual> population_r_t = union(population_p_t, population_q_t);
            LinkedList<LinkedList<Individual>> dominationFronts = fastNonDominatedSort(population_r_t);

            LinkedList<Individual> population_p_t_1 = new LinkedList<Individual>();
            int i = 0;
            while (dominationFronts.get(i) != null
                    && population_p_t_1.size() + dominationFronts.get(i).size() <= conf.getPopulationSize()) {
                crowdingDistanceAssignment(dominationFronts.get(i));
                population_p_t_1.addAll(dominationFronts.get(i));
                i++;
            }
            if (population_p_t_1.size() != conf.getPopulationSize()) {
                crowdingDistanceAssignment(dominationFronts.get(i));
                Individual[] sortedIndividuals = sort(dominationFronts.get(i));
                int numberOfMissingIndividuals = conf.getPopulationSize() - population_p_t_1.size();
                for (i = 0; i < numberOfMissingIndividuals; i++) {
                    population_p_t_1.add(sortedIndividuals[i]);
                }
            }

            population_p_t = population_p_t_1;
            numberGeneration++;
        }

        /*
        LinkedList<LinkedList<Individual>> temp = fastNonDominatedSort(population_p_t);
        // only return non-dominated individuals (=> first frontier)
        LinkedList<Individual> bestIndividuals = temp.getFirst();

        // call NSGA-II listeners        
        if (!nsga2listeners.isEmpty()) {
            // create NSGA-II event
            NSGA2Event event = new NSGA2Event(this, bestIndividuals, conf.getNumberOfGenerations());
            for (NSGA2Listener listener : nsga2listeners) {
                listener.performNSGA2Event(event);
            }
        }
         * 
         */

        // only return non-dominated individuals (=> first frontier)
        return population_p_t;
    }
    
    public LinkedList<Individual> getBestIndividuals(LinkedList<Individual> startPopulation)
    {
        LinkedList<LinkedList<Individual>> temp = fastNonDominatedSort(startPopulation);
        // only return non-dominated individuals (=> first frontier)
        LinkedList<Individual> bestIndividuals = temp.getFirst();

        // call NSGA-II listeners        
        if (!nsga2listeners.isEmpty()) {
            // create NSGA-II event
            NSGA2Event event = new NSGA2Event(this, bestIndividuals, conf.getNumberOfGenerations());
            for (NSGA2Listener listener : nsga2listeners) {
                listener.performNSGA2Event(event);
            }
        }
        return bestIndividuals;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // private helper methods for evolution
    /////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Makes a new population out of the specified one using tournament selection, crossover and
     * mutation. The new population has the same size as the original one.
     * 
     * @param individuals original population
     * @return new population
     */
    private LinkedList<Individual> makeNewPopulation(LinkedList<Individual> individuals) throws IOException {
        if (individuals == null) {
            throw new IllegalArgumentException("'individuals' must not be null.");
        }
        if (individuals.size() % 4 != 0) {
            throw new IllegalArgumentException("Size of 'individuals' must be divisible by four.");
        }

        LinkedList<Individual> newPopulation = new LinkedList<Individual>();

        // tournament selection and crossover
        Individual[] a1 = new Individual[individuals.size()];
        Individual[] a2 = new Individual[individuals.size()];

        for (int i = 0; i < individuals.size(); i++) {
            a1[i] = individuals.get(i);
            a2[i] = individuals.get(i);
        }

        for (int i = 0; i < individuals.size(); i++) {
            int randomIndex = randomNumber(i, individuals.size());
            Individual temp = a1[randomIndex];
            a1[randomIndex] = a1[i];
            a1[i] = temp;

            randomIndex = randomNumber(i, individuals.size());
            temp = a2[randomIndex];
            a2[randomIndex] = a2[i];
            a2[i] = temp;
        }

        for (int i = 0; i < individuals.size(); i += 4) {
            Individual parent1 = binaryTournament(a1[i], a1[i + 1]);
            Individual parent2 = binaryTournament(a1[i + 2], a1[i + 3]);

            Individual child1 = (Individual) parent1.clone();
            Individual child2 = (Individual) parent2.clone();
            child1.crossover(child2);
            newPopulation.add(child1);
            newPopulation.add(child2);

            parent1 = binaryTournament(a2[i], a2[i + 1]);
            parent2 = binaryTournament(a2[i + 2], a2[i + 3]);

            child1 = (Individual) parent1.clone();
            child2 = (Individual) parent2.clone();
            child1.crossover(child2);
            newPopulation.add(child1);
            newPopulation.add(child2);
        }

        // mutation
        for (Individual individual : newPopulation) {
            individual.mutate();
        }

        newPopulation = EvaluateGeneration(newPopulation);

        return newPopulation;
    }

    /**
     * Returns a random number between 'min' (inclusive) and 'max' (exclusive).
     * 
     * @param min minimal value
     * @param max maximal value
     * @return random number
     */
    private int randomNumber(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("'min' must be smaller than 'max'.");
        }

        return min + (int) Math.floor(Math.random() * (max - min));
    }

    /**
     * Selects one out of two individuals using a binary tournament selection with the crowded
     * comparison operator.
     * 
     * @param individual1 first individual
     * @param individual2 second individual
     * @return winning individual
     */
    private Individual binaryTournament(Individual individual1, Individual individual2) {
        if (individual1 == null) {
            throw new IllegalArgumentException("'individual1' must not be null.");
        }
        if (individual2 == null) {
            throw new IllegalArgumentException("'individual2' must not be null.");
        }

        if (individual1.isCrowdedComparisonOperatorBetter(individual2)) {
            return individual1;
        }
        if (individual2.isCrowdedComparisonOperatorBetter(individual1)) {
            return individual2;
        }

        // both individuals are "equal" -> select one randomly
        if (Math.random() < 0.5) {
            return individual1;
        } else {
            return individual2;
        }
    }

    /**
     * Makes a fast non-domination sort of the specified individuals. The method returns the different
     * domination fronts in ascending order by their rank and sets their rank value.
     * 
     * @param individuals individuals to sort
     * @return domination fronts in ascending order by their rank
     */
    public LinkedList<LinkedList<Individual>> fastNonDominatedSort(LinkedList<Individual> individuals) {
        if (individuals == null) {
            throw new IllegalArgumentException("'individuals' must not be null.");
        }

        LinkedList<LinkedList<Individual>> dominationFronts = new LinkedList<LinkedList<Individual>>();

        HashMap<Individual, LinkedList<Individual>> individual2DominatedIndividuals =
                new HashMap<Individual, LinkedList<Individual>>();
        HashMap<Individual, Integer> individual2NumberOfDominatingIndividuals =
                new HashMap<Individual, Integer>();

        for (Individual individualP : individuals) {
            individual2DominatedIndividuals.put(individualP, new LinkedList<Individual>());
            individual2NumberOfDominatingIndividuals.put(individualP, 0);

            for (Individual individualQ : individuals) {
                if (individualP.dominates(individualQ)) {
                    individual2DominatedIndividuals.get(individualP).add(individualQ);
                } else {
                    if (individualQ.dominates(individualP)) {
                        individual2NumberOfDominatingIndividuals.put(individualP,
                                individual2NumberOfDominatingIndividuals.get(individualP) + 1);
                    }
                }
            }

            if (individual2NumberOfDominatingIndividuals.get(individualP) == 0) {
                // p belongs to the first front
                individualP.setRank(1);
                if (dominationFronts.isEmpty()) {
                    LinkedList<Individual> firstDominationFront = new LinkedList<Individual>();
                    firstDominationFront.add(individualP);
                    dominationFronts.add(firstDominationFront);
                } else {
                    LinkedList<Individual> firstDominationFront = dominationFronts.getFirst();
                    firstDominationFront.add(individualP);
                }
            }
        }

        int i = 1;
        while (dominationFronts.size() == i) {
            LinkedList<Individual> nextDominationFront = new LinkedList<Individual>();
            for (Individual individualP : dominationFronts.get(i - 1)) {
                for (Individual individualQ : individual2DominatedIndividuals.get(individualP)) {
                    individual2NumberOfDominatingIndividuals.put(individualQ,
                            individual2NumberOfDominatingIndividuals.get(individualQ) - 1);
                    if (individual2NumberOfDominatingIndividuals.get(individualQ) == 0) {
                        individualQ.setRank(i + 1);
                        nextDominationFront.add(individualQ);
                    }
                }
            }
            i++;
            if (!nextDominationFront.isEmpty()) {
                dominationFronts.add(nextDominationFront);
            }
        }

        return dominationFronts;
    }

    /**
     * Executes the crowding distance assignment for the specified individuals.
     * 
     * @param individuals individuals
     */
    private void crowdingDistanceAssignment(LinkedList<Individual> individuals) {
        if (individuals == null) {
            throw new IllegalArgumentException("'individuals' must not be null.");
        }

        for (Individual individual : individuals) {
            // initialize crowding distance
            individual.setCrowdingDistance(0);
        }

        int numberOfObjectives = individuals.getFirst().getNumberOfObjectives();
        for (int m = 0; m < numberOfObjectives; m++) {
            Individual[] sortedIndividuals = individuals.toArray(new Individual[0]);

            // sort using m-th objective value
            Arrays.sort(sortedIndividuals, new FitnessValueComparator(m));

            // so that boundary points are always selected
            sortedIndividuals[0].setCrowdingDistance(Double.POSITIVE_INFINITY);
            sortedIndividuals[sortedIndividuals.length - 1].setCrowdingDistance(Double.POSITIVE_INFINITY);

            // If minimal and maximal fitness value for this objective are equal,
            // do not change crowding distance 
            if (sortedIndividuals[0].getFitnessValue(m) != sortedIndividuals[sortedIndividuals.length - 1].getFitnessValue(m)) {
                for (int i = 1; i < sortedIndividuals.length - 1; i++) {
                    double newCrowdingDistance = sortedIndividuals[i].getCrowdingDistance();
                    newCrowdingDistance +=
                            (sortedIndividuals[i + 1].getFitnessValue(m) - sortedIndividuals[i - 1].getFitnessValue(m))
                            / (sortedIndividuals[sortedIndividuals.length - 1].getFitnessValue(m) - sortedIndividuals[0].getFitnessValue(m));

                    sortedIndividuals[i].setCrowdingDistance(newCrowdingDistance);
                }
            }
        }
    }

    /**
     * Returns the specified individuals sorted in ascending order by the crowded comparison
     * operator.
     * 
     * @param individuals individuals to sort
     * @return individuals sorted in ascending order by the crowded comparison operator
     */
    private Individual[] sort(LinkedList<Individual> individuals) {
        if (individuals == null) {
            throw new IllegalArgumentException("'individuals' must not be null.");
        }

        Individual[] result = individuals.toArray(new Individual[0]);

        Arrays.sort(result, new CrowdedComparisonOperatorComparator());

        return result;
    }

    /**
     * Returns the union of the two collections of individuals.
     * 
     * @param individuals1 first individuals
     * @param individuals2 second individuals
     * @return union of both collections of individuals
     */
    private LinkedList<Individual> union(LinkedList<Individual> individuals1,
            LinkedList<Individual> individuals2) {
        if (individuals1 == null) {
            throw new IllegalArgumentException("'individuals1' must not be null.");
        }
        if (individuals2 == null) {
            throw new IllegalArgumentException("'individuals2' must not be null.");
        }

        LinkedList<Individual> result = new LinkedList<Individual>(individuals1);
        result.addAll(individuals2);

        return result;
    }

    /**
     * Calculate and return total number of BMPs modeled in the optimization
     *
     * @param chosenBMPs array that specifies which BMPs are chosen
     * @return totalNumBMPs created by mbabbars 4/25/2012
     */
    private int returnTotNumBMPs(int[] chosenBMPs) {
        int totalBMPs = 0;
        for (int i = 0; i < chosenBMPs.length; i++) {
            totalBMPs = totalBMPs + chosenBMPs[i];
        }
        return totalBMPs;
    }
    
    public LinkedList<Individual> doNonDominatedSortingFirstPop(LinkedList<Individual> Indv) {
       
        LinkedList<LinkedList<Individual>> ins = null;
        ins = fastNonDominatedSort(Indv);
        LinkedList<Individual> res = new LinkedList();
        int[] chosenff = Indv.get(0).chosenFF;
        
          //choose at least population size indvs
        
        int popCount = 0;    
        int popSize = this.conf.getPopulationSize();
        int count=0;
        while(popCount<popSize && count<ins.size())  
        {
            LinkedList<Individual> indv1 = ins.get(count++);
            for(int i=0;i<indv1.size();i++)
            {                
                res.add(indv1.get(i));
                popCount++;
                if(popCount>=popSize)
                    break;
            }
        } 
        return res;      
    }
    

    /**
     * This inner class implemens a comparator using the crowded comparison operator. 
     */
    private class CrowdedComparisonOperatorComparator implements Comparator<Individual> {

        /**
         * Compares the two specified individuals using the crowded comparison operator. Returns -1,
         * 0 or 1 as the first argument is less than, equal to, or greater than the second.
         */
        public int compare(Individual individual1, Individual individual2) {
            if (individual1 == null) {
                throw new IllegalArgumentException("'individual1' must not be null.");
            }
            if (individual2 == null) {
                throw new IllegalArgumentException("'individual2' must not be null.");
            }

            if (individual1.isCrowdedComparisonOperatorBetter(individual2)) {
                return -1;
            }
            if (individual2.isCrowdedComparisonOperatorBetter(individual1)) {
                return 1;
            }

            return 0;
        }
    }

    /**
     * This inner class implemens a comparator using the index-th fitness value of two individuals.
     */
    private class FitnessValueComparator implements Comparator<Individual> {

        private int indexObjective;

        /**
         * Constructor.
         * 
         * @param indexObjective objective index
         */
        private FitnessValueComparator(int indexObjective) {
            this.indexObjective = indexObjective;
        }

        /**
         * Compares the two specified individuals using their index-th fitness value. Returns -1,
         * 0 or 1 as the first argument is less than, equal to, or greater than the second.
         */
        public int compare(Individual individual1, Individual individual2) {
            if (individual1 == null) {
                throw new IllegalArgumentException("'individual1' must not be null.");
            }
            if (individual2 == null) {
                throw new IllegalArgumentException("'individual2' must not be null.");
            }

            if (individual1.getFitnessValue(indexObjective) < individual2.getFitnessValue(indexObjective)) {
                return -1;
            }
            if (individual1.getFitnessValue(indexObjective) > individual2.getFitnessValue(indexObjective)) {
                return 1;
            }

            return 0;
        }
    }

    public LinkedList<Individual> EvaluateGeneration(LinkedList<Individual> startPopulation) {
        LinkedList<Individual> res = null;
        if(researchEvaluation)
        {
            res = evaluator.evaluateIndividualOnceSystem(startPopulation); //for research
        }
        else
        {
            res = evaluator.evaluateGen(startPopulation); //for normal search with feedback
        }
        return res;
        
    }
    
    @Override
    public NSGA2 clone()
    {
        return this.clone();
    }
}