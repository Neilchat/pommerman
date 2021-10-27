package players.emcts.evo;


import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.function.BiFunction;


public class Selection {
    
    private Random random;
    private double prob;
    
    //todo tournament size


    public Selection(Random random){
        this.random = random;
    }

    Individual select(Individual[] population) {
        return tournament_selection(population);

    }
    private Individual tournament_selection(Individual[] population) {
        Individual[] tournament_pop = new Individual[2];
        for (int i = 0; i < 2; i++) {
            tournament_pop[i] = population[random.nextInt(population.length)];
        }
        Arrays.sort(tournament_pop, Comparator.reverseOrder());
        prob = random.nextFloat();

        return apply_selection(tournament_pop, this::tournament_prob);
    }

    private double tournament_prob(Individual[] population, int i) {
        return prob * (Math.pow((1 - prob), i));
    }

    /**
     * General selection method, using given function to apply probability depending on selection type
     * @param function - function to compute the correct selection probability, given population and individual index
     */
    private Individual apply_selection(Individual[] population, BiFunction<Individual[], Integer, Double> function) {
        double sum = 0;
        for (int i = 0; i < 2; i++) {
            sum += function.apply(population, i);
        }

        if ((int)sum > 0) {  // It may be that all individuals have probability 0. Return random one in this case.
            double chosen = random.nextInt((int) sum);
            double newsum = 0;
            int max = population.length - 1;
            for (int i = max; i >= 0; i--) {
                if (newsum >= chosen) {
                    return population[i];
                }
                newsum += function.apply(population, i);
            }
        }

        return population[random.nextInt(population.length)];
    }

}
