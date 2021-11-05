package players.efmcts.evo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Mutation {
    private Random random;
    private int featureLength;
    private double mutationStrength;
    private double mutationProb;

    public Mutation(Random random, int featureLength, double mutationProb, double mutationStrength){
        this.random = random;
        this.featureLength = featureLength;
        this.mutationStrength = mutationStrength;
        this.mutationProb = mutationProb;
    }

    public List<Integer> findGenesToMutate() {
        List<Integer> genesToMutate = uniform_mutation();
        return genesToMutate;
    }

    //Returns a subset of features to mutate
    private List<Integer> uniform_mutation() {
        List<Integer> genesToMutate = new ArrayList<>();
        for (int i = 0; i < featureLength; i++) {
            if (random.nextFloat() < mutationProb) {
                genesToMutate.add(i);
            }
        }
        return genesToMutate;
    }


    //For each action we mutate the corresponding features which are in genesToMutate
    public void mutateGenes(Individual ind, int action, List<Integer> genesToMutate){
        for (int i = 0; i < featureLength; i++) {
            if (genesToMutate.contains(i)) {
                //To maintain positive weights we add more often than subtract.
                if (random.nextDouble()>0.45)
                    ind.set_action(action, i, ind.get_action(action, i) + random.nextDouble()* mutationStrength);
                else
                    ind.set_action(action, i, ind.get_action(action, i) - random.nextDouble()* mutationStrength);
            }
        }
        //todo Instead of the add/subtract conundrum maybe we could try some form of normalization
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
