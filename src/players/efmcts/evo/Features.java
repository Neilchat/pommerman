package players.efmcts.evo;

import core.GameState;
import objects.Bomb;
import objects.GameObject;
import utils.Types;
import utils.Vector2d;

import java.util.*;

import static java.lang.Math.abs;
import static utils.Utils.*;

public class Features {

    private GameState gs;
    private Random random;
    public Features(GameState gs, Random random){
        this.gs = gs;
        this.random = random;
    }


    public double[] getStats(){
        double[] statsToReturn = new double[4];
        BoardStats stats = new BoardStats(gs);
        Vector2d myPosition = gs.getPosition();

        Types.TILETYPE[][] board = gs.getBoard();
        int[][] bombBlastStrength = gs.getBombBlastStrength();
        int[][] bombLife = gs.getBombLife();

        ArrayList<Types.TILETYPE> enemiesObs = gs.getAliveEnemyIDs();

        int boardSizeX = board.length;
        int boardSizeY = board[0].length;

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
                    // Create bomb object
                    Bomb bomb = new Bomb();
                    bomb.setPosition(new Vector2d(x, y));
                    bomb.setBlastStrength(bombBlastStrength[y][x]);
                    bomb.setLife(bombLife[y][x]);
                    bombs.add(bomb);
                }
                else if(Types.TILETYPE.getAgentTypes().contains(type) &&
                        type.getKey() != gs.getPlayerId()){ // May be an enemy
                    if(enemiesObs.contains(type)) { // Is enemy
                        // Create enemy object
                        GameObject enemy = new GameObject(type);
                        enemy.setPosition(new Vector2d(x, y));
                        enemies.add(enemy); // no copy needed
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

        double minBombDist = 40.0;
        double totalBombDistance = 0.0;
        for (Bomb bomb : bombs) {
            double bombDist = myPosition.dist(bomb.getPosition());
            if (bombDist < minBombDist) minBombDist = bombDist;
            totalBombDistance += bombDist;
        }

        double minEnemyDist = 40.0;
        double totalEnemyDistance = 0.0;
        for (GameObject enemy : enemies) {
            double enemyDist = myPosition.dist(enemy.getPosition());
            if (enemyDist < minEnemyDist) minEnemyDist = enemyDist;
            totalEnemyDistance += enemyDist;
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

//        Dijkstra.Container container = dijkstra.getDistances(board, myPosition, bombs, enemies);
//        HashMap<Types.TILETYPE, ArrayList<Vector2d>> items = container.items;
//        HashMap<Vector2d, Integer> dist = container.dist;
//
//        double bombFeat =0.0;
//        for (int i =0; i< bombs.size();i++){
//            Bomb bomb = bombs.get(i);
//            if (dist.containsKey(bomb.getPosition())){
//                bombFeat += bomb.getBlastStrength()/(Double.valueOf(dist.get(bomb.getPosition()))+random.nextDouble());
//            }
//        }

//        double enemyFeat =0.0;
//        for (int i =0; i< enemies.size();i++){
//            if (dist.containsKey(enemies.get(i).getPosition())){
//                enemyFeat += Double.valueOf(dist.get(enemies.get(i).getPosition()))/1000000000.0;
//            }
//        }

        statsToReturn[0] = 1.0/(minBombDist+1);
        statsToReturn[1] = 1.0/(minEnemyDist+1);
        statsToReturn[2] = 1.0/(minPowerUp+1);
//        statsToReturn[3] = (double)gs.getAmmo()*gs.getBlastStrength()/20;
//        statsToReturn[4] = (double) stats.nWoods /20;
//
        if (stats.canKick)
            statsToReturn[3] = 0.3;
        else statsToReturn[3] = 0.0;

//        statsToReturn[5] = 1.0/(minRigid+1);
//        statsToReturn[6] = 1.0/(minFlame+1);
//        statsToReturn[7] = 1.0/(minWood+1);


        return statsToReturn;
    }


    /**
     * Checks if a given position is considered to be one where I couldn't get out of and a bomb would kill me.
     * @param nextPosition - My next position to check.
     * @param bombRange - Range of the bomb that could kill me
     * @param nextBoard - Board of the game.
     * @param enemies - List of enemies
     * @return true if the position is not a good one to be in.
     */
    private boolean isStuckPosition(Vector2d nextPosition, int bombRange, Types.TILETYPE[][] nextBoard,
                                    ArrayList<GameObject> enemies) {
        // A tuple class for PriorityQueue since it does not support pair of values in default
        class Tuple implements Comparable<Tuple>{
            private int distance;
            private Vector2d position;

            private Tuple(int distance, Vector2d position){
                this.distance = distance;
                this.position = position;
            }

            @Override
            public int compareTo(Tuple tuple) {
                return this.distance - tuple.distance;
            }
        }

        PriorityQueue<Tuple> Q = new PriorityQueue<>();
        Q.add(new Tuple(0, nextPosition));

        Set<Vector2d> seen = new HashSet<>();

        boolean is_stuck = true;

        while(!Q.isEmpty()){
            Tuple tuple = Q.remove();
            int dist = tuple.distance;
            Vector2d position = tuple.position;

            seen.add(position);

            if(nextPosition.x != position.x && nextPosition.y != position.y){
                is_stuck = false;
                break;
            }

            if(dist > bombRange){
                is_stuck = false;
                break;
            }

            Types.DIRECTIONS[] directions = {Types.DIRECTIONS.LEFT, Types.DIRECTIONS.RIGHT,
                    Types.DIRECTIONS.UP, Types.DIRECTIONS.DOWN};
            //Types.DIRECTIONS.values();

            for (Types.DIRECTIONS direction : directions) {
                Vector2d newPosition = position.copy();
                newPosition = newPosition.add(direction.toVec());

                if(seen.contains(newPosition)) continue;

                if(!positionOnBoard(nextBoard, newPosition)) continue;

                if(!positionIsPassable(nextBoard, newPosition, enemies)) continue;

                dist = abs(direction.x() + position.x - nextPosition.x) +
                        abs(direction.y() + position.y - nextPosition.y);

                Q.add(new Tuple(dist, newPosition));
            }
        }
        return is_stuck;
    }
    /**
     * Finds a list of directions that is Safe to move to.
     * @param board - Current game board
     * @param myPosition - Current position of the agent.
     * @param unsafeDirections - Set of previously determined unsafe directions.
     * @param bombs - List of bombs currently in hte game.
     * @param enemies - List of enemies in hte game.
     * @return A set of directions that would be safe to move (may be empty)
     */
    private ArrayList<Types.DIRECTIONS> findSafeDirections(Types.TILETYPE[][] board, Vector2d myPosition,
                                                           HashMap<Types.DIRECTIONS, Integer> unsafeDirections,
                                                           ArrayList<Bomb> bombs, ArrayList<GameObject> enemies) {
        // All directions are unsafe. Return a position that won't leave us locked.
        ArrayList<Types.DIRECTIONS> safe = new ArrayList<>();

        if(unsafeDirections.size() == 4){

            Types.TILETYPE[][] nextBoard = new Types.TILETYPE[board.length][];
            for (int i = 0; i < board.length; i++) {
                nextBoard[i] = new Types.TILETYPE[board[i].length];
                for (int i1 = 0; i1 < board[i].length; i1++) {
                    if (board[i][i1] != null) {
                        // Power-ups array contains null elements, don't attempt to copy those.
                        nextBoard[i][i1] = board[i][i1];
                    }
                }
            }

            nextBoard[myPosition.y][myPosition.x] = Types.TILETYPE.BOMB;

            for (Map.Entry<Types.DIRECTIONS, Integer> entry : unsafeDirections.entrySet()){

                Types.DIRECTIONS direction = entry.getKey();
                int bomb_range = entry.getValue();

                Vector2d nextPosition = myPosition.copy();
                nextPosition = nextPosition.add(direction.toVec());

                if(!positionOnBoard(nextBoard, nextPosition) ||
                        !positionIsPassable(nextBoard, nextPosition, enemies))
                    continue;

                if(!isStuckPosition(nextPosition, bomb_range, nextBoard, enemies)){
                    return new ArrayList<>(Arrays.asList(direction));
                }
            }
            return safe;
        }

        // The directions that will go off the board.
        Set<Types.DIRECTIONS> disallowed = new HashSet<>();

        Types.DIRECTIONS[] directions = {Types.DIRECTIONS.LEFT, Types.DIRECTIONS.RIGHT,
                Types.DIRECTIONS.UP, Types.DIRECTIONS.DOWN};

        //Types.DIRECTIONS.values();

        for (Types.DIRECTIONS current_direction : directions) {

            Vector2d position = myPosition.copy();
            position = position.add(current_direction.toVec());

            Types.DIRECTIONS direction = getDirection(myPosition, position);

            if(!positionOnBoard(board, position)){
                disallowed.add(direction);
                continue;
            }

            if(unsafeDirections.containsKey(direction)) {
                continue;
            }

            if(positionIsPassable(board, position, enemies) || positionIsFog(board, position)){
                safe.add(direction);
            }
        }

        if(safe.isEmpty()){
            // We don't have any safe directions, so return something that is allowed.
            for(Types.DIRECTIONS k : unsafeDirections.keySet()) {
                if(!disallowed.contains(k))
                    safe.add(k);
            }
        }

        return safe;
    }

    /**
     * Checks if there's an adjecent enemy.
     * @param objects - Game objects in the board.
     * @param dist - Distance to different positions around me.
     * @param enemies - Set of enemy players.
     * @return true if an agent is next to this player.
     */
    private boolean isAdjacentEnemy(
            HashMap<Types.TILETYPE, ArrayList<Vector2d> > objects,
            HashMap<Vector2d, Integer> dist,
            ArrayList<GameObject> enemies)
    {
        for(GameObject enemy : enemies){
            if(objects.containsKey(enemy.getType())) {
                ArrayList<Vector2d> items_list = objects.get(enemy.getType());
                for (Vector2d position : items_list) {
                    if (dist.get(position) == 1)
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines whether we can safely bomb right now.
     * @param ammo - our ammo count
     * @param blastStrength - our blast strength
     * @param objects - list of objects in the board.
     * @param dist - distances to positions in the board.
     * @param myPosition - our agent's position
     * @return true if if would be a good idea to drop a bomb here
     */
    private boolean maybeBomb(int ammo, int blastStrength, HashMap<Types.TILETYPE, ArrayList<Vector2d> > objects,
                              HashMap<Vector2d, Integer> dist, Vector2d myPosition) {
        // Do we have ammo?
        if(ammo < 1)
            return false;

        if(objects.containsKey(Types.TILETYPE.PASSAGE)){
            ArrayList<Vector2d> items_list = objects.get(Types.TILETYPE.PASSAGE);

            // Will we be stuck?
            for (Vector2d position : items_list) {

                if(dist.containsKey(position)){
                    if(dist.get(position) > Integer.MAX_VALUE)
                        continue;
                }

                // We can reach a passage that's outside of the bomb strength.
                if(dist.containsKey(position)){
                    if(dist.get(position) > blastStrength)
                        return true;
                }

                // We can reach a passage that's outside of the bomb scope.
                if(position.x != myPosition.x && position.y != myPosition.y)
                    return true;
            }
        }
        return false;
    }

    /**
     * Checks unsafe directions of movements, where a bomb could hit me if exploding
     * @param myPosition - The position the agent is in
     * @param directions - Directions I could move towards
     * @param bombs - current set of bombs in the level
     * @return The list of safe directions to move to.
     */
    private ArrayList<Types.DIRECTIONS> filterUnsafeDirections(Vector2d myPosition, ArrayList<Types.DIRECTIONS> directions, ArrayList<Bomb> bombs){
        ArrayList<Types.DIRECTIONS> safeDirections = new ArrayList<>();
        for (Types.DIRECTIONS dir : directions){
            Vector2d myPos = getNextPosition(myPosition, dir);
            boolean isBad = false;
            for (Bomb b: bombs){
                int bombX = b.getPosition().x;
                int bombY = b.getPosition().y;
                int blastStrenght = b.getBlastStrength();
                if ((myPos.x == bombX && Math.abs(bombY - myPos.y) <= blastStrenght) ||
                        (myPos.y == bombY && Math.abs(bombX - myPos.x) <= blastStrenght)){
                    isBad = true;
                    break;
                }
            }
            if (!isBad){
                safeDirections.add(dir);
            }

        }

        return safeDirections;
    }

    /**
     * List of valid directions from current position. Avoids leaving board and moving against walls.
     * @param board - The current board.
     * @param myPosition - Position of this agent.
     * @param directions - Possible directions to move to
     * @param enemies - List of enemies in the game.
     * @return A subset of the directions received that would be valid to move to
     */
    private ArrayList<Types.DIRECTIONS> filterInvalidDirections(Types.TILETYPE[][] board,
                                                                Vector2d myPosition, ArrayList<Types.DIRECTIONS> directions,
                                                                ArrayList enemies){
        ArrayList<Types.DIRECTIONS> validDirections = new ArrayList<>();
        for (Types.DIRECTIONS d: directions){
            Vector2d position = getNextPosition(myPosition, d);
            if (positionOnBoard(board, position) && (positionIsPassable(board, position, enemies))){
                validDirections.add(d);
            }
        }
        return validDirections;
    }

    /**
     * Checks for directions that would take the agent to positions that have been recently visted.
     * @param directions - Set of initial possible directions
     * @param myPosition - Current position of the agent.
     * @param recentlyVisitedPositions - set of recently visited positions.
     * @return A subset of the directions received that would be okay to move to
     */
    private ArrayList<Types.DIRECTIONS> filterRecentlyVisited(ArrayList<Types.DIRECTIONS> directions,
                                                              Vector2d myPosition, ArrayList<Vector2d> recentlyVisitedPositions){
        ArrayList<Types.DIRECTIONS> filtered = new ArrayList<>();
        for (Types.DIRECTIONS d : directions){
            if (!recentlyVisitedPositions.contains(getNextPosition(myPosition, d)))
                filtered.add(d);
        }
        if (filtered.size() > 0)
            return directions;

        return filtered;
    }
}
