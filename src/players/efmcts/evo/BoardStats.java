package players.efmcts.evo;

import core.GameState;
import utils.Types;

public class BoardStats {

    int tick, nTeammates, nEnemies, blastStrength;
    boolean canKick;
    int nWoods;
    static double maxWoods = -1;

    double FACTOR_ENEMY;
    double FACTOR_TEAM;

    public BoardStats(GameState gs) {
        nEnemies = gs.getAliveEnemyIDs().size();

        // Init weights based on game mode
        if (gs.getGameMode() == Types.GAME_MODE.FFA) {
            FACTOR_TEAM = 0;
            FACTOR_ENEMY = 0.5;
        } else {
            FACTOR_TEAM = 0.1;
            FACTOR_ENEMY = 0.4;
            nTeammates = gs.getAliveTeammateIDs().size();  // We only need to know the alive teammates in team modes
            nEnemies -= 1;  // In team modes there's an extra Dummy agent added that we don't need to care about
        }

        // Save game state information
        this.tick = gs.getTick();
        this.blastStrength = gs.getBlastStrength();
        this.canKick = gs.canKick();

        // Count the number of wood walls
        this.nWoods = 1;
        for (Types.TILETYPE[] gameObjectsTypes : gs.getBoard()) {
            for (Types.TILETYPE gameObjectType : gameObjectsTypes) {
                if (gameObjectType == Types.TILETYPE.WOOD)
                    nWoods++;
            }
        }
        if (maxWoods == -1) {
            maxWoods = nWoods;
        }
    }


}
