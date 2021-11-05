package players.efmcts.evo;


import java.util.Arrays;
import java.util.Random;

public class Individual implements Comparable {
    private int numActions;
    private int numFeatures;
    double[][] actionsWeights;
    private Random gen;

    private double value;

    public Individual(int numActions, Random gen, int numFeatures) {
        actionsWeights = new double[numActions][numFeatures];
        this.gen = gen;
        this.numActions = numActions;
        this.numFeatures = numFeatures;
    }

    void init() {
        for (int i = 0; i < numActions; i++) {
            for (int j = 0; j < numFeatures; j++)
                actionsWeights[i][j] = 0.0;
            //todo explore if adding random small weights helps during init
        }
    }



    public double get_action(int actionNum, int featureNum) {
        return actionsWeights[actionNum][featureNum];
    }

    public void set_action(int actionNum, int featureNum, double newAction) {
        actionsWeights[actionNum][featureNum] = newAction;
    }

    public void set_actions(double[][] newActions) {
        actionsWeights = newActions.clone();
    }

    public double[][] get_actions() {
        return actionsWeights;
    }

    public int get_max_actions() {
        return numActions;
    }

    public int get_numFeatures() {
        return numFeatures;
    }

    public void set_value(double value) {
        this.value = value;
    }


    @Override
    public int compareTo(Object o) {
        if (o instanceof Individual) {
            return Double.compare(value, ((Individual) o).value);
        }
        return 0;
    }

    public Individual copy() {
        Individual a = new Individual(numActions, gen, numFeatures);
        a.set_value(value);
        a.set_actions(actionsWeights);
        return a;
    }

    @Override
    public String toString() {
        return "(" + value + ": " + Arrays.toString(actionsWeights) + ")";
    }


}