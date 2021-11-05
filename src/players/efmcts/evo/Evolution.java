package players.efmcts.evo;



import gnu.trove.set.hash.TIntHashSet;

import java.util.*;

public class Evolution {
    private Random random;

    private Mutation mutationClass;
    private Crossover crossoverClass;
    private Selection selectionClass;

    private Individual[] population;
    private int popsize;
    private int featureLength;
    private int numActions;
    private int numElites;


    public Evolution(Random random, int popSize, int featureLength, int numActions, double mutationProb, double mutationStrength, int numElites) {
        this.random = random;
        this.popsize = popSize;
        this.featureLength = featureLength;
        this.numActions = numActions;
        this.mutationClass = new Mutation(random, featureLength, mutationProb, mutationStrength);
        this.crossoverClass = new Crossover(random);
        this.selectionClass = new Selection(random);
        this.numElites = numElites;
    }

    public void init() {
        init_population();
    }

    /**
     * Performs 1 iteration of evolution
     */
    public void evolve() {
        // Generate offspring
        Individual[] offspring = generate_offspring();

        // Update population
        combine_and_sort_population(offspring);
    }

    private void init_population() {
        population = new Individual[popsize];

        //For an unseeded agent
//        for (int i = 0; i < popsize; i++) {
//            population[i] = new Individual(numActions, random, featureLength);
//            population[i].init();
//        }

        //For a seeded agent use:
        //Seed obtained by experimentation against MCTS, RHEA and OSLA when the agent was on a winning streak
        double[][] actions ={{ 0.2702793692827582,  0.18408435060940917,  0.3277143777498021,  0.3031121226272133,  0.3859720291483031},
                {0.21093693712606898 ,  0.2920830971083769,  0.22817448797837608,  0.25026563595402523,  0.2468879471030393
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

    //Tournament Selection is used here
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
            //For each inidividual we select a set of features for which we mutate the weight for every action
            List mutateGenes = mutationClass.findGenesToMutate();
            for (int j =0; j< numActions; j++) {
                mutationClass.mutateGenes(offspring[i], j, mutateGenes);
                }
            }
        return offspring;
    }

    @SuppressWarnings("unchecked")
    private void combine_and_sort_population(Individual[] offspring){
        int startIdx = numElites;
        Arrays.sort(population, Comparator.reverseOrder());


        //We keep the elites from parent pop, and replace rest with the offsprings created
        int nextIdx = 0;
        for (int i = startIdx; i < popsize; i++) {
            population[i] = offspring[nextIdx].copy();
            nextIdx++;
        }
    }


}
