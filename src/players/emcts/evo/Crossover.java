package players.emcts.evo;


import java.util.Random;
import java.util.function.Function;


public class Crossover {
    private Random random;
    public Crossover(Random random) {
        this.random = random;
    }
    
    Individual cross(Individual parent1, Individual parent2) {
        return uniform_cross(parent1, parent2);
    }
    private Individual uniform_cross(Individual parent1, Individual parent2) {
        return apply_crossover(parent1, parent2, this::u_bool);
    }

    private boolean u_bool(int i) {
        return random.nextFloat() < 0.5;
    }


    private Individual apply_crossover(Individual parent1, Individual parent2, Function<Integer, Boolean> function) {
        int length = parent1.get_length();
        Individual ind = new Individual(length, random);
        double[] actions = new double[length];

        // Make sure the gene size is minimum 1 and maximum individual length
        int gene_size = 1;

        int i = 0;
        while (i < length) {
            // Apply the crossover function to find the next chosen action
            boolean functionResult = function.apply(i);

            // Set all actions part of this gene to the action corresponding to the gene's crossover result
            for (int j = 0; j < gene_size && (i + j) < length; j++) {
                if (functionResult) {
                    actions[i + j] = parent1.get_action(i);
                } else {
                    actions[i + j] = parent2.get_action(i);
                }
            }

            // Move to next gene
            i += gene_size;
        }

        ind.actions = actions;
        return ind;
    }
}
