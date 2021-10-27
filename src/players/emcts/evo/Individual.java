package players.emcts.evo;


import java.util.Arrays;
import java.util.Random;

public class Individual implements Comparable {
    private int length;
    double[] actions;
    private Random gen;
    private double epsilon = 1e-6;

    private double value;

    public Individual(int length, Random gen) {
        actions = new double[length];
        this.gen = gen;
        this.length = length;
    }

    void randomize() {
        for (int i = 0; i < length; i++) {
            //todo add small var
            actions[i] = 0.0;
        }
    }



    public double get_action(int idx) {
        return actions[idx];
    }

    public void set_action(int idx, double newAction) {
        actions[idx] = newAction;
    }

    public void set_actions(double[] newActions) {
        actions = newActions.clone();
    }

    public double[] get_actions() {
        return actions;
    }

    public int get_max_actions() {
        return length;
    }

    public int get_length() {
        return length;
    }

    public void set_value(double value) {
        this.value = value;
    }

    public double get_value() {
        return this.value;
    }

    public void discount_value(double discount) {
        value *= discount;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof Individual) {
            return Double.compare(value, ((Individual) o).value);
        }
        return 0;
    }

    public Individual copy() {
        Individual a = new Individual(length, gen);
        a.set_value(value);
        a.set_actions(actions);
        return a;
    }

    @Override
    public String toString() {
        return "(" + value + ": " + Arrays.toString(actions) + ")";
    }

    public String fullString() {
        return "(" + value + ": " + Arrays.toString(actions)
                + " / " + length + "; " + gen.toString() + ")";
    }
}