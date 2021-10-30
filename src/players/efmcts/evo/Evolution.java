package players.efmcts.evo;



import gnu.trove.set.hash.TIntHashSet;

import java.util.*;

public class Evolution {
    private Random random;

    private Mutation mutationClass;
    private Crossover crossoverClass;
    private Selection selectionClass;

    private int nIterations;
    private Individual[] population;
    private int popsize;
    private int featureLength;
    private int numActions;


    public Evolution(Random random, int popSize, int featureLength, int numActions) {
        this.random = random;
        this.popsize = popSize;
        this.featureLength = featureLength;
        this.numActions = numActions;
        mutationClass = new Mutation(random, featureLength);
        crossoverClass = new Crossover(random);
        selectionClass = new Selection(random);
        nIterations = 0;

    }

    public void init() {
        init_population();
    }

    /**
     * Performs 1 iteration of EA.
     * @return - best action after 1 iteration.
     */
    public void evolve() {
        // Generate offspring
        Individual[] offspring = generate_offspring();

        // Update population
        combine_and_sort_population(offspring);
    }

    private void init_population() {
        population = new Individual[popsize];
//        for (int i = 0; i < popsize; i++) {
//            population[i] = new Individual(numActions, random, featureLength);
//            population[i].randomize();
//        }

        double[][] actions ={{3.11661410245542, 1.853909465111095,  0.5045074485192123,  2.255517456741045,  -3.2045954727232115,  -0.9658258798879004},{-0.41062206821638764,  -1.8173158310626278,  -0.6856400936877366,  3.214616790960874,  -1.3967266916051384,  -2.9659237997188956
        }};
        for (int i = 0; i < popsize; i++) {
            population[i] = new Individual(numActions, random, featureLength);
            population[i].set_actions(actions);
        }
    }

    public void evaluate(Individual individual, double delta){
        individual.set_value(delta);
    }

    public int getPopsize(){
        return popsize;
    }

    public Individual[] getPopulation(){
        return population;
    }

    private Individual select(Individual[] population) {
        return selectionClass.select(population);
    }

    private Individual select(Individual[] population, Individual ignore) {
        Individual[] reduced_pop = new Individual[population.length - 1];
        int idx = 0;
        for (Individual individual : population) {
            if (!individual.equals(ignore)) {
                reduced_pop[idx] = individual;
                idx++;
            }
        }

        return select(reduced_pop);
    }

    private Individual crossover(Individual[] population){
        Individual parent1 = select(population);
        Individual parent2 = select(population, parent1);

        return crossoverClass.cross(parent1, parent2);
    }

    private Individual[] generate_offspring() {
        Individual[] offspring = new Individual[6];
        for (int i = 0; i < 6; i++) {
            offspring[i] = crossover(population);
            List mutateGenes = mutationClass.findGenesToMutate();
            double[] mutationVals = new double[mutateGenes.size()];
            for (int k = 0; k< mutationVals.length; k++){
                mutationVals[k] = random.nextDouble();
            }
            boolean actionPositive = random.nextBoolean();
            for (int j =0; j< numActions; j++) {
                mutationClass.mutateGenes(offspring[i], j, mutateGenes, mutationVals, actionPositive);
                actionPositive = !actionPositive;
                }
            }
        return offspring;
    }

    /**
     * Assumes population and offspring are already sorted in descending order by individual fitness
     * @param offspring - offspring created from parents population
     */
    @SuppressWarnings("unchecked")
    private void combine_and_sort_population(Individual[] offspring){
        int startIdx = 0;

        // Make sure we have enough individuals to choose from for the next population

        //todo elitism count
            // First no_elites individuals remain the same, the rest are replaced
        startIdx = 4;
        Arrays.sort(population, Comparator.reverseOrder());
        //Arrays.sort(offspring, Comparator.reverseOrder());


        // Combine population with offspring, we keep only best individuals. If parents should not be kept, new
        // population is only best POP_SIZE offspring individuals.
        int nextIdx = 0;
        for (int i = startIdx; i < popsize; i++) {
            population[i] = offspring[nextIdx].copy();
            nextIdx ++;
        }

        for (int i = startIdx; i < popsize; i++) {
            List mutateGenes = mutationClass.findGenesToMutate();
            double[] mutationVals = new double[mutateGenes.size()];
            for (int k = 0; k< mutationVals.length; k++){
                mutationVals[k] = random.nextDouble();
            }
            boolean actionPositive = random.nextBoolean();
            for (int j =0; j< numActions; j++) {
                mutationClass.mutateGenes(population[i], j, mutateGenes, mutationVals, actionPositive);
                actionPositive = !actionPositive;
            }
        }
    }


}
