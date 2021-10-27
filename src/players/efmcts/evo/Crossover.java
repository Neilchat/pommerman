package players.efmcts.evo;


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
        int num_actions = parent1.get_max_actions();
        int num_features = parent1.get_numFeatures();

        Individual ind = new Individual(num_actions, random, num_features);
        double[][] actions = new double[num_actions][num_features];
        for (int i =0; i< num_actions; i++) {
            for (int j = 0; j < num_features; j++) {
                boolean functionResult = function.apply(j);
                if (functionResult) {
                    actions[i][j] = parent1.get_action(i, j);
                } else {
                    actions[i][j] = parent2.get_action(i, j);
                }
            }
        }
        ind.set_actions(actions);
        return ind;
    }
}
