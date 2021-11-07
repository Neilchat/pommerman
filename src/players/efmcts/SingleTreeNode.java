package players.efmcts;

import core.GameState;

import players.efmcts.evo.Evolution;
import players.efmcts.evo.Features;
import players.efmcts.evo.Individual;
import players.heuristics.AdvancedHeuristic;
import players.heuristics.CustomHeuristic;
import players.heuristics.StateHeuristic;
import utils.ElapsedCpuTimer;
import utils.Types;
import utils.Utils;
import utils.Vector2d;

import java.util.ArrayList;
import java.util.Random;

import static java.lang.Math.exp;
import static utils.Types.ACTIONS.*;

public class SingleTreeNode
{
    public EFMCTSParams params;

    private SingleTreeNode parent;
    private SingleTreeNode[] children;
    private double totValue;
    private int nVisits;
    private Random m_rnd;
    private int m_depth;
    private double[] bounds = new double[]{Double.MAX_VALUE, -Double.MAX_VALUE};
    private int childIdx;
    private int fmCallsCount;

    private int num_actions;
    private int effectiveActions;
    private Types.ACTIONS[] actions;

    private GameState rootState;
    private StateHeuristic rootStateHeuristic;

    private Evolution ea;
    private int L;

    SingleTreeNode(EFMCTSParams p, Random rnd, int num_actions, Types.ACTIONS[] actions, Evolution ea) {
        this(p, null, -1, rnd, num_actions, actions, 0, null, ea);
    }

    private SingleTreeNode(EFMCTSParams p, SingleTreeNode parent, int childIdx, Random rnd, int num_actions,
                           Types.ACTIONS[] actions, int fmCallsCount, StateHeuristic sh, Evolution ea) {
        this.params = p;
        this.fmCallsCount = fmCallsCount;
        this.parent = parent;
        this.m_rnd = rnd;
        this.num_actions = num_actions;
        this.actions = actions;
        children = new SingleTreeNode[num_actions];
        totValue = 0.0;
        this.childIdx = childIdx;
        if(parent != null) {
            m_depth = parent.m_depth + 1;
            this.rootStateHeuristic = sh;
        }
        else
            m_depth = 0;
        this.ea = ea;
        this.effectiveActions = params.numEffectiveActions;
        this.L = params.L;
    }

    void setRootGameState(GameState gs)
    {
        this.rootState = gs;
        if (params.heuristic_method == params.CUSTOM_HEURISTIC)
            this.rootStateHeuristic = new CustomHeuristic(gs);
        else if (params.heuristic_method == params.ADVANCED_HEURISTIC) // New method: combined heuristics
            this.rootStateHeuristic = new AdvancedHeuristic(gs, m_rnd);
    }


    void efmctsSearch(ElapsedCpuTimer elapsedTimer) {

        double avgTimeTaken;
        double acumTimeTaken = 0;
        long remaining;
        int numIters = 0;

        int remainingLimit = 5;
        boolean stop = false;

        while(!stop) {

            ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();


            //Loop over the population's individuals
            for (int i = 0; i < ea.getPopsize(); i++) {
                Individual weights = ea.getPopulation()[i];
                //Initialize fitness to compute average reward for individual
                double fitness = 0.0;
                //For each individual perform L MCTS roll outs
                for (int j = 0; j < L; j++) {
                    GameState state = rootState.copy();
                    SingleTreeNode selected = treePolicy(state);
                    double delta = selected.rollOut(state, weights.get_actions());
                    backUp(selected, delta);
                    fitness += delta;
                }
                //Update fitness of individual with average reward from the L rollouts
                ea.evaluate(ea.getPopulation()[i], fitness / L);
            }
            //Evolve the population by performing elitism, crossovers and mutations.
            ea.evolve();

            //Stopping condition is defaulted to Stop Time for our agent
            if (params.stop_type == params.STOP_TIME) {
                numIters++;
                acumTimeTaken += (elapsedTimerIteration.elapsedMillis());
                avgTimeTaken = acumTimeTaken / numIters;
                remaining = elapsedTimer.remainingTimeMillis();
                stop = remaining <= 2 * avgTimeTaken || remaining <= remainingLimit;
            } else if (params.stop_type == params.STOP_ITERATIONS) {
                //Note here that each iteration now is actually popSize*L rollouts. We generally observe ~ 12 iterations for default settings
                numIters++;
                stop = numIters >= params.num_iterations;
            } else if (params.stop_type == params.STOP_FMCALLS) {
                fmCallsCount += params.rollout_depth;
                stop = (fmCallsCount + params.rollout_depth) > params.num_fmcalls;
            }
        }
//  To use for debugging, and getting optimal individuals, and action weights, used for seeding. Prints them every game tick 1 or 500
//        if (rootState.getTick()== 500 || rootState.getTick()==1) {
//            System.out.println();
//            Features features = new Features(rootState, params.numFeatures);
//            double[] featureWeights = features.getStats();
//            double[] actionWeights = new double[effectiveActions];
//            double[][] weights = ea.getPopulation()[0].get_actions();
//            for (int i =0; i< effectiveActions; i++) {
//                for (int j = 0; j < featureWeights.length; j++) {
//                    System.out.print(weights[i][j] + "  ");
//                    actionWeights[i] += weights[i][j]*featureWeights[j];
//                }
//                System.out.println();
//            }
//            actionWeights[0] =5*actionWeights[0];
//            System.out.println("Action weights  ");
//            for (int i = 0; i < effectiveActions; i++) {
//                System.out.print(actionWeights[i] + "  ");
//            }
//            System.out.println();
//            System.out.println("Feature weights  ");
//            for (int i = 0; i < featureWeights.length; i++) {
//                System.out.print(featureWeights[i] + "  ");
//            }
//            System.out.println(" ITERS for efmcts  " + numIters);
//        }
    }

    private SingleTreeNode treePolicy(GameState state) {

        SingleTreeNode cur = this;

        while (!state.isTerminal() && cur.m_depth < params.rollout_depth)
        {
            if (cur.notFullyExpanded()) {
                return cur.expand(state);

            } else {
                cur = cur.uct(state);
            }
        }

        return cur;
    }


    private SingleTreeNode expand(GameState state) {

        int bestAction = 0;
        double bestValue = -1;

        for (int i = 0; i < children.length; i++) {
            double x = m_rnd.nextDouble();
            if (x > bestValue && children[i] == null) {
                bestAction = i;
                bestValue = x;
            }
        }

        //Roll the state
        roll(state, actions[bestAction]);

        SingleTreeNode tn = new SingleTreeNode(params,this,bestAction,this.m_rnd,num_actions,
                actions, fmCallsCount, rootStateHeuristic, ea);
        children[bestAction] = tn;
        return tn;
    }

    private void roll(GameState gs, Types.ACTIONS act)
    {
        //Simple, all random first, then my position.
        int nPlayers = 4;
        Types.ACTIONS[] actionsAll = new Types.ACTIONS[4];
        int playerId = gs.getPlayerId() - Types.TILETYPE.AGENT0.getKey();

        for(int i = 0; i < nPlayers; ++i)
        {
            if(playerId == i)
            {
                actionsAll[i] = act;
            }else {
                int actionIdx = m_rnd.nextInt(gs.nActions());
                actionsAll[i] = Types.ACTIONS.all().get(actionIdx);
            }
        }

        gs.next(actionsAll);

    }

    private SingleTreeNode uct(GameState state) {
        SingleTreeNode selected = null;
        double bestValue = -Double.MAX_VALUE;
        for (SingleTreeNode child : this.children)
        {
            double hvVal = child.totValue;
            double childValue =  hvVal / (child.nVisits + params.epsilon);

            childValue = Utils.normalise(childValue, bounds[0], bounds[1]);

            double uctValue = childValue +
                    params.K * Math.sqrt(Math.log(this.nVisits + 1) / (child.nVisits + params.epsilon));

            uctValue = Utils.noise(uctValue, params.epsilon, this.m_rnd.nextDouble());     //break ties randomly

            // small sampleRandom numbers: break ties in unexpanded nodes
            if (uctValue > bestValue) {
                selected = child;
                bestValue = uctValue;
            }
        }
        if (selected == null)
        {
            throw new RuntimeException("Warning! returning null: " + bestValue + " : " + this.children.length + " " +
                    + bounds[0] + " " + bounds[1]);
        }

        //Roll the state:
        roll(state, actions[selected.childIdx]);

        return selected;
    }

    private double rollOut(GameState state, double[][] weights)
    {
        int thisDepth = this.m_depth;

        while (!finishRollout(state,thisDepth)) {
            //Instead of any safe action we now use the action weight matrix to compute probabilities over the action space to explore
            int action = safeWeightedAction(state, weights);
            roll(state, actions[action]);
            thisDepth++;
        }

        return rootStateHeuristic.evaluateState(state);
    }

    private int safeWeightedAction(GameState state, double[][] weights)
    {

        //Get the probability distribution using weights and game state
        double[] probs = getProabilities(weights, state);

        double prob = m_rnd.nextDouble();

        //Using the probabilities we decide whether move or bomb.
        for (int i =0; i< effectiveActions; i++){
            prob = prob - probs[i];
            if (prob<=0){
                //If movement we randomly decide the direction or stop provided it is safe
                if (i==0) return safeRandomMovementAction(state);
                if (i==2) return Types.ACTIONS.ACTION_BOMB.getKey();
            }
        }
        //It should never reach this.
        return safeRandomAction(state);
    }

    private boolean isSafeAction(GameState state, int action){
        Types.TILETYPE[][] board = state.getBoard();
        ArrayList<Types.ACTIONS> actionsToTry = Types.ACTIONS.all();
        int width = board.length;
        int height = board[0].length;

        Types.ACTIONS act = actionsToTry.get(action);
        Vector2d dir = act.getDirection().toVec();

        Vector2d pos = state.getPosition();
        int x = pos.x + dir.x;
        int y = pos.y + dir.y;

        if (x >= 0 && x < width && y >= 0 && y < height)
            return board[y][x] != Types.TILETYPE.FLAMES;
        return true;
    }

    private int safeRandomAction(GameState state)
    {
        ArrayList<Types.ACTIONS> actionsToTry = Types.ACTIONS.all();
        while(actionsToTry.size() > 0) {

            int nAction = m_rnd.nextInt(actionsToTry.size());
            if (isSafeAction(state, nAction))
                return nAction;

            actionsToTry.remove(nAction);
        }

        //Uh oh...
        return m_rnd.nextInt(num_actions);
    }

    //Searches for a safe movement action including stop, instead of all actions
    private int safeRandomMovementAction(GameState state)
    {
        ArrayList<Types.ACTIONS> actionsToTry = movement();
        while(actionsToTry.size() > 0) {

            int nAction = m_rnd.nextInt(actionsToTry.size());
            if (isSafeAction(state, nAction))
                return nAction;

            actionsToTry.remove(nAction);
        }

        //Uh oh...
        return m_rnd.nextInt(num_actions);
    }

    public static ArrayList<Types.ACTIONS> movement()
    {
        ArrayList<Types.ACTIONS> allActions = new ArrayList<Types.ACTIONS>();
        allActions.add(Types.ACTIONS.ACTION_UP);
        allActions.add(ACTION_DOWN);
        allActions.add(ACTION_LEFT);
        allActions.add(ACTION_RIGHT);
        allActions.add(ACTION_STOP);
        return allActions;
    }

    //calculates the probability distribution over the effective action space (2 in this case)
    //using weight matrix and features from the game state
    private double[] getProabilities(double[][] weights, GameState state){

        // Gets the action weights to perform softmax with
        double[] actionWeights = getActionWeights(weights, state);
        double sum=0;
        double[] prob = new double[actionWeights.length];
        //Perform softmax
        for (int i = 0; i<actionWeights.length; i++){
            sum+=exp(-actionWeights[i]);
        }
        for (int i = 0; i<actionWeights.length; i++){
            prob[i] = exp(-actionWeights[i])/sum;
        }
        return prob;
    }

    //Performs a sum of Weight matrix multiplied by feature weight over the features for each action
    private double[] getActionWeights(double[][] weights, GameState state){
        Features features = new Features(state, params.numFeatures);
        double[] featureWeights = features.getStats();
        double[] actionWeights = new double[effectiveActions];
        for (int i =0; i< effectiveActions; i++) {
            for (int j = 0; j < featureWeights.length; j++) {
                actionWeights[i] += weights[i][j]*featureWeights[j];
            }
        }
        //Since the first action weight encodes for movement which is 5 actual actions we multiply by 5
        actionWeights[0] = actionWeights[0]*5;
        return actionWeights;
    }

    @SuppressWarnings("RedundantIfStatement")
    private boolean finishRollout(GameState rollerState, int depth)
    {
        if (depth >= params.rollout_depth)      //rollout end condition.
            return true;

        if (rollerState.isTerminal())               //end of game
            return true;

        return false;
    }

    private void backUp(SingleTreeNode node, double result)
    {
        SingleTreeNode n = node;
        while(n != null)
        {
            n.nVisits++;
            n.totValue += result;
            if (result < n.bounds[0]) {
                n.bounds[0] = result;
            }
            if (result > n.bounds[1]) {
                n.bounds[1] = result;
            }
            n = n.parent;
        }
    }


    int mostVisitedAction() {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;
        boolean allEqual = true;
        double first = -1;

        for (int i=0; i<children.length; i++) {

            if(children[i] != null)
            {
                if(first == -1)
                    first = children[i].nVisits;
                else if(first != children[i].nVisits)
                {
                    allEqual = false;
                }

                double childValue = children[i].nVisits;
                childValue = Utils.noise(childValue, params.epsilon, this.m_rnd.nextDouble());     //break ties randomly
                if (childValue > bestValue) {
                    bestValue = childValue;
                    selected = i;
                }
            }
        }

        if (selected == -1)
        {
            selected = 0;
        }else if(allEqual)
        {
            //If all are equal, we opt to choose for the one with the best Q.
            selected = bestAction();
        }

        return selected;
    }

    private int bestAction()
    {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;

        for (int i=0; i<children.length; i++) {

            if(children[i] != null) {
                double childValue = children[i].totValue / (children[i].nVisits + params.epsilon);
                childValue = Utils.noise(childValue, params.epsilon, this.m_rnd.nextDouble());     //break ties randomly
                if (childValue > bestValue) {
                    bestValue = childValue;
                    selected = i;
                }
            }
        }

        if (selected == -1)
        {
            System.out.println("Unexpected selection!");
            selected = 0;
        }

        return selected;
    }


    private boolean notFullyExpanded() {
        for (SingleTreeNode tn : children) {
            if (tn == null) {
                return true;
            }
        }

        return false;
    }
}
