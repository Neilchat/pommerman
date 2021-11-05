package players.efmcts.evo;

import core.GameState;
import objects.Bomb;
import objects.GameObject;
import utils.Types;
import utils.Vector2d;

import java.util.*;

public class Features {

    private GameState gs;
    private int featureLength;
    public Features(GameState gs, int featureLength){
        this.gs = gs;
        this.featureLength = featureLength;
    }


    //Calculates a feature array for the game state
    public double[] getStats(){
        double[] statsToReturn = new double[featureLength];
        Vector2d myPosition = gs.getPosition();

        Types.TILETYPE[][] board = gs.getBoard();
        int[][] bombBlastStrength = gs.getBombBlastStrength();
        int[][] bombLife = gs.getBombLife();

        ArrayList<Types.TILETYPE> enemiesObs = gs.getAliveEnemyIDs();

        int boardSizeX = board.length;
        int boardSizeY = board[0].length;

        //Used to store positions of different tile types from the game state
        ArrayList<Bomb> bombs = new ArrayList<>();
        ArrayList<GameObject> enemies = new ArrayList<>();
        ArrayList<GameObject> powerUps = new ArrayList<>();
        ArrayList<GameObject> flames = new ArrayList<>();
        ArrayList<GameObject> rigids = new ArrayList<>();
        ArrayList<GameObject> woods = new ArrayList<>();



        for (int x = 0; x < boardSizeX; x++) {
            for (int y = 0; y < boardSizeY; y++) {

                Types.TILETYPE type = board[y][x];

                if(type == Types.TILETYPE.BOMB || bombBlastStrength[y][x] > 0){
                    Bomb bomb = new Bomb();
                    bomb.setPosition(new Vector2d(x, y));
                    bomb.setBlastStrength(bombBlastStrength[y][x]);
                    bomb.setLife(bombLife[y][x]);
                    bombs.add(bomb);
                }
                else if(Types.TILETYPE.getAgentTypes().contains(type) &&
                        type.getKey() != gs.getPlayerId()){
                    if(enemiesObs.contains(type)) {
                        GameObject enemy = new GameObject(type);
                        enemy.setPosition(new Vector2d(x, y));
                        enemies.add(enemy);
                    }
                }
                else if (Types.TILETYPE.getPowerUpTypes().contains(type)){
                    GameObject powerUp = new GameObject(type);
                    powerUp.setPosition(new Vector2d(x, y));
                    powerUps.add(powerUp);
                }
                else if (type == Types.TILETYPE.FLAMES){
                    GameObject flame = new GameObject(type);
                    flame.setPosition(new Vector2d(x, y));
                    flames.add(flame);
                }
                else if (type == Types.TILETYPE.RIGID){
                    GameObject rigid = new GameObject(type);
                    rigid.setPosition(new Vector2d(x, y));
                    rigids.add(rigid);
                }
                else if (type == Types.TILETYPE.WOOD){
                    GameObject wood = new GameObject(type);
                    wood.setPosition(new Vector2d(x, y));
                    woods.add(wood);
                }
            }
        }

        //Calculate minimum distances from the agent for each tile type
        //40 is the max
        double minBombDist = 40.0;
        for (Bomb bomb : bombs) {
            double bombDist = myPosition.dist(bomb.getPosition());
            if (bombDist < minBombDist) minBombDist = bombDist;
        }

        double minEnemyDist = 40.0;
        for (GameObject enemy : enemies) {
            double enemyDist = myPosition.dist(enemy.getPosition());
            if (enemyDist < minEnemyDist) minEnemyDist = enemyDist;
        }

        double minPowerUp = 40.0;
        for (GameObject powerUp : powerUps) {
            double dist = myPosition.dist(powerUp.getPosition());
            if (dist < minPowerUp) minPowerUp = dist;
        }

        double minRigid = 40.0;
        for (GameObject rigid : rigids) {
            double dist = myPosition.dist(rigid.getPosition());
            if (dist < minRigid) minRigid = dist;
        }

        double minFlame = 40.0;
        for (GameObject flame : flames) {
            double dist = myPosition.dist(flame.getPosition());
            if (dist < minFlame) minFlame = dist;
        }

        double minWood = 40.0;
        for (GameObject wood : woods) {
            double dist = myPosition.dist(wood.getPosition());
            if (dist < minWood) minWood = dist;
        }

        //Compute features to use and change the number numFeatures according to the features selected.
        //For distances we want the effect to be inverse, as being close to a tile type should have more
        // impact on decision than being far from it. and avoid infinite values.
        //todo add feature weights.
        statsToReturn[0] = 1.0/(minBombDist+1);
        statsToReturn[1] = 1.0/(minEnemyDist+1);
        statsToReturn[2] = 1.0/(minPowerUp+1);
//        statsToReturn[3] = (double)gs.getAmmo()*gs.getBlastStrength()/20;
//        statsToReturn[4] = (double) stats.nWoods /20;
//
        if (gs.canKick())
            statsToReturn[3] = 0.3;
        else statsToReturn[3] = 0.0;

        statsToReturn[4] = 1.0/(minRigid+1);
//        statsToReturn[6] = 1.0/(minFlame+1);
//        statsToReturn[3] = 1.0/(minWood+1);


        return statsToReturn;
    }
}
