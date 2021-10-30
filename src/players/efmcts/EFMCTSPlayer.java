package players.efmcts;

import core.GameState;
import players.Player;

import players.efmcts.evo.Evolution;
import players.optimisers.ParameterizedPlayer;
import utils.ElapsedCpuTimer;
import utils.Types;

import java.util.ArrayList;
import java.util.Random;

public class EFMCTSPlayer extends ParameterizedPlayer{

    /**
     * Random generator.
     */
    private Random m_rnd;

    /**
     * All actions available.
     */
    public Types.ACTIONS[] actions;

    /**
     * Params for this MCTS
     */
    public EFMCTSParams params;

    private int effectiveActions = 2;

    public EFMCTSPlayer(long seed, int id) {
        this(seed, id, new EFMCTSParams());
    }

    private Evolution ea;

    public EFMCTSPlayer(long seed, int id, EFMCTSParams params) {
        super(seed, id, params);
        reset(seed, id);

        ArrayList<Types.ACTIONS> actionsList = Types.ACTIONS.all();
        actions = new Types.ACTIONS[actionsList.size()];
        int i = 0;
        for (Types.ACTIONS act : actionsList) {
            actions[i++] = act;
        }

        //Initialize a population of 10 individuals, each Individual is a weight matrix for action cross features.
        ea = new Evolution(m_rnd, 10, 6, effectiveActions);
        ea.init();

    }

    @Override
    public void reset(long seed, int playerID) {
        super.reset(seed, playerID);
        m_rnd = new Random(seed);

        this.params = (EFMCTSParams) getParameters();
        if (this.params == null) {
            this.params = new EFMCTSParams();
            super.setParameters(this.params);
        }
    }

    @Override
    public Types.ACTIONS act(GameState gs) {

        //ea.init();
        // TODO update gs
        if (gs.getGameMode().equals(Types.GAME_MODE.TEAM_RADIO)){
            int[] msg = gs.getMessage();
        }

        ElapsedCpuTimer ect = new ElapsedCpuTimer();
        ect.setMaxTimeMillis(params.num_time);

        // Number of actions available
        int num_actions = actions.length;

        // Root of the tree
        SingleTreeNode m_root = new SingleTreeNode(params, m_rnd, num_actions, actions, ea);
        m_root.setRootGameState(gs);

        //Determine the action using MCTS...
        m_root.efmctsSearch(ect);

        //Determine the best action to take and return it.
        int action = m_root.mostVisitedAction();

        // TODO update message memory

        //System.out.println(Arrays.toString(ea.getPopulation()[0].get_actions()));

        //... and return it.
        return actions[action];
    }

    @Override
    public int[] getMessage() {
        // default message
        int[] message = new int[Types.MESSAGE_LENGTH];
        message[0] = 1;
        return message;
    }

    @Override
    public Player copy() {
        return new EFMCTSPlayer(seed, playerID, params);
    }
}