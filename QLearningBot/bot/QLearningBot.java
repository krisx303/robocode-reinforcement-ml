package bot;

import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.BulletFiredEvent;
import dev.robocode.tankroyale.botapi.events.BulletHitBotEvent;
import dev.robocode.tankroyale.botapi.events.DeathEvent;
import dev.robocode.tankroyale.botapi.events.RoundEndedEvent;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;
import dev.robocode.tankroyale.botapi.events.WonRoundEvent;
import dev.robocode.tankroyale.botapi.Bot;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class QLearningBot extends Bot {

    private double nearestEnemyX, nearestEnemyY;

    private final Map<Integer, GameStateActionPair> activeBullets = new HashMap<>();
    private double epsilon = 0.7;
    private final Random random = new Random();
    private GameState previousState, currentState;
    private Action previousAction, currentAction;
    private double currentReward = 0.0;
    private final Classifier classifier;
    private final QLearningKnowledgeBase knowledgeBase;
    private int scanTurn = -1;
    private final TrainingLogger logger = new TrainingLogger("training_data.csv");
    private Double[] roundResults;

    public static void main(String[] args) {
        new QLearningBot().start();
    }

    QLearningBot() {
        super(BotInfo.fromFile("QLearningBot.json"));
        this.classifier = new Classifier();
        this.knowledgeBase = new QLearningKnowledgeBase(0.3, 0.9);
    }

    // Called when a new round is started -> initialize and do some movement
    @Override
    public void run() {
        if (roundResults == null) {
            roundResults = new Double[getNumberOfRounds()+1];
            Arrays.fill(roundResults, 0.0);
        }
        setBodyColor(new Color(0xFF, 0xAA, 0x00));   // orange
        setGunColor(new Color(0xFF, 0x77, 0x00));    // dark orange
        setTurretColor(new Color(0xFF, 0x77, 0x00)); // dark orange
        setRadarColor(new Color(0xFF, 0x00, 0x00));  // red
        setScanColor(new Color(0xFF, 0x00, 0x00));   // red
        setBulletColor(new Color(0x00, 0x88, 0xFF)); // light blue

        while (isRunning()) {
            int scanAge = getTurnNumber() - scanTurn;
            if (scanAge < 0 || scanAge >= 120) {
                turnGunRight(360);
                go();
            }

            double dx = nearestEnemyX - getX();
            double dy = nearestEnemyY - getY();
            double distance = Math.hypot(dx, dy);

            double angleToEnemy = Math.toDegrees(Math.atan2(dy, dx));  // absolutny kąt względem osi X
            if (angleToEnemy < 0) {
                angleToEnemy += 360;
            }
            double myHeading = getDirection();
            double bearing = angleToEnemy - myHeading;

            currentState = classifier.observeGameState(distance, bearing, scanAge);
            currentAction = selectAction(currentState);
            executeAction(currentAction, bearing);
            go();

            // Update Q-table
            if (previousState != null && previousAction != null) {
                double reward = getReward(currentState, currentAction);
                knowledgeBase.updateKnowledge(previousState, previousAction, currentState, reward);
                roundResults[getRoundNumber()] += reward;
            }

            previousState = currentState;
            previousAction = currentAction;
        }
    }

    private double getReward(GameState state, Action selectedAction) {
        double reward = currentReward == 0.0 ? -0.001 : currentReward;
        currentReward = 0.0; // Reset reward for the next turn
        if (state.angleToEnemy().equals(Categories.AngleCategory.FRONT_TO_SHOT) && selectedAction.isFireAction()) {
            reward += 1;
        }else if(!state.angleToEnemy().equals(Categories.AngleCategory.FRONT_TO_SHOT) && selectedAction.equals(Action.TURN_TO_POINT_ENEMY)){
            reward += 0.4;
        }
        return reward;
    }

    @Override
    public void onScannedBot(ScannedBotEvent e) {
        scanTurn = getTurnNumber();
        nearestEnemyX = e.getX();
        nearestEnemyY = e.getY();
    }

    @Override
    public void onBulletFired(BulletFiredEvent bulletFiredEvent) {
        super.onBulletFired(bulletFiredEvent);
        int bulletId = bulletFiredEvent.getBullet().getBulletId();
        GameState state = currentState == null ? previousState : currentState;
        Action action = currentAction == null ? previousAction : currentAction;
        activeBullets.put(bulletId, new GameStateActionPair(state, action));
    }

    @Override
    public void onBulletHit(BulletHitBotEvent bulletHitBotEvent) {
        super.onBulletHit(bulletHitBotEvent);
        int bulletId = bulletHitBotEvent.getBullet().getBulletId();
        GameStateActionPair pair = activeBullets.get(bulletId);
        if (pair != null) {
            GameState state = pair.state();
            Action action = pair.action();
            knowledgeBase.updateKnowledge(state, action, currentState, 8);
            roundResults[getRoundNumber()] += 8;
            activeBullets.remove(bulletId);
        }
    }

    @Override
    public void onBulletHitWall(dev.robocode.tankroyale.botapi.events.BulletHitWallEvent bulletHitWallEvent) {
        super.onBulletHitWall(bulletHitWallEvent);
        int bulletId = bulletHitWallEvent.getBullet().getBulletId();
        GameStateActionPair pair = activeBullets.get(bulletId);
        if (pair != null) {
            GameState state = pair.state();
            Action action = pair.action();
            knowledgeBase.updateKnowledge(state, action, currentState, -0.05);
            roundResults[getRoundNumber()] -= 0.05;
            activeBullets.remove(bulletId);
        }
    }

    @Override
    public void onRoundEnded(RoundEndedEvent e) {
        scanTurn = -1;
        activeBullets.clear();
        boolean won = e.getResults().getFirstPlaces() == getMyId();
        if (won) {
            roundResults[getRoundNumber()] += 20;
        }
        logger.log(getRoundNumber(), epsilon, roundResults[getRoundNumber()], won, knowledgeBase.getQTableSize());

        if (e.getRoundNumber() % 10 == 0) {
            knowledgeBase.saveQTable();
        }
    }

    private Action selectAction(GameState state) {
        boolean isExploring = random.nextDouble() < epsilon;
        if (isExploring) {
            // Explore: choose a random action
            epsilon = Math.max(0.05, epsilon * 0.9995); // Decay epsilon
            return Action.random();
        } else {
            // Exploit: choose the action with the highest Q-value for the current state
            return knowledgeBase.getBestAction(state);
        }
    }

    private void executeAction(Action action, double bearing) {
        switch (action) {
            case MOVE_FORWARD -> forward(100);
            case MOVE_BACKWARD -> back(100);
            case TURN_LEFT -> turnLeft(10);
            case TURN_RIGHT -> turnRight(10);
            case FIRE_MEDIUM -> fire(1.0);
            case TURN_TO_POINT_ENEMY -> turnLeft(bearing);
            case NOTHING -> {}
        }
    }
}


