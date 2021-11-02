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


    public void mutateGenes(Individual ind, int action, List<Integer> genesToMutate){
        for (int i = 0; i < featureLength; i++) {
            if (genesToMutate.contains(i)) {
                if (random.nextDouble()>0.45) ind.set_action(action, i, ind.get_action(action, i) + random.nextDouble()* 0.0001);
                else ind.set_action(action, i, ind.get_action(action, i) - random.nextDouble()* 0.0001);
                //ind.set_action(action, i, ind.get_action(action, i) + random.nextDouble()* 0.001);
            }
        }
        //normalize(ind, action);
    }



    public void normalize(Individual ind, int actions){
        for (int j = 0; j<featureLength; j++)  {
            double max = 0;
            double min = 10000;
            for (int i = 0; i < actions; i++) {
                if (ind.get_action(i, j) > max) max = ind.get_action(i, j);
                if (ind.get_action(i, j) < min) min = ind.get_action(i, j);
            }
            double range = max-min+random.nextDouble()*0.01;
            for (int i = 0; i < actions; i++) {
                ind.set_action(i, j,(ind.get_action(i, j)-min)/range);
            }
        }
    }
}
