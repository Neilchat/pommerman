package players.emcts.evo;

import gnu.trove.set.hash.TIntHashSet;

import java.util.Random;

//todo mutation strength

public class Mutation {
    private Random random;
    private TIntHashSet genesToMutate;
    private int length;

    public Mutation(Random random, int length){
        this.random = random;
        this.length =length;
    }

    void findGenesToMutate() {
        TIntHashSet genesToMutate;
        genesToMutate = uniform_mutation();
        this.genesToMutate = genesToMutate;
    }

    private TIntHashSet uniform_mutation() {
        TIntHashSet genesToMutate = new TIntHashSet();
        for (int i = 0; i < length; i++) {
            if (random.nextFloat() < 0.5) {
                genesToMutate.add(i);
            }
        }
        return genesToMutate;
    }

    public void mutateGeneToNewValue(Individual ind, int idx) {
        if (random.nextFloat() < 0.5) {
            ind.set_action(idx, ind.get_action(idx) + random.nextDouble() * 0.01);
        }
        else ind.set_action(idx, ind.get_action(idx)-random.nextDouble()*0.01);

        }

    public void mutateGenes(Individual ind){
        for (int i = 0; i < length; i++) {
                if (genesToMutate.contains(i))
                mutateGeneToNewValue(ind, i);
            }
        //normalize(ind);
    }

    private void normalize(Individual ind){
        double max = 0;
        double min = 10000;
        for (int i = 0; i < length; i++) {
            if (ind.get_action(i)>max) max = ind.get_action(i);
            if (ind.get_action(i)<min) min = ind.get_action(i);
        }
        double range = max-min;
        for (int i = 0; i < length; i++) {
            ind.set_action(i, (ind.get_action(i)-min)/range);
        }

    }

}
