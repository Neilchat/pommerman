package players.efmcts.evo;

import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//todo mutation strength

public class Mutation {
    private Random random;
    private int featureLength;

    public Mutation(Random random, int featureLength){
        this.random = random;
        this.featureLength = featureLength;
    }

    public List<Integer> findGenesToMutate() {
        List<Integer> genesToMutate = uniform_mutation();
        return genesToMutate;
    }

    private List<Integer> uniform_mutation() {
        List<Integer> genesToMutate = new ArrayList<>();
        for (int i = 0; i < featureLength; i++) {
            if (random.nextFloat() < 0.2) {
                genesToMutate.add(i);
            }
        }
        return genesToMutate;
    }


    public void mutateGenes(Individual ind, int action, List<Integer> genesToMutate, double[] mutationValues, boolean actionPositive){
        int k =0;
        for (int i = 0; i < featureLength; i++) {
            if (genesToMutate.contains(i)) {
                if (actionPositive) ind.set_action(action, i, ind.get_action(action, i) + mutationValues[k] * 0.0001);
                else ind.set_action(action, i, ind.get_action(action, i) - mutationValues[k] * 0.0001);
                k++;
            }
        }
        //normalize(ind, action);
    }

    private void normalize(Individual ind, int action){
        double max = 0;
        double min = 10000;
        for (int i = 0; i < featureLength; i++) {
            if (ind.get_action(action, i)>max) max = ind.get_action(action,i);
            if (ind.get_action(action, i)<min) min = ind.get_action(action, i);
        }
        double range = max-min;
        for (int i = 0; i < featureLength; i++) {
            ind.set_action(action, i, (ind.get_action(action, i)-min)/range);
        }

    }

}
