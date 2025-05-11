package bot;

import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.BulletFiredEvent;
import dev.robocode.tankroyale.botapi.events.BulletHitBotEvent;
import dev.robocode.tankroyale.botapi.events.DeathEvent;
import dev.robocode.tankroyale.botapi.events.HitWallEvent;
import dev.robocode.tankroyale.botapi.events.RoundEndedEvent;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;
import dev.robocode.tankroyale.botapi.events.WonRoundEvent;
import dev.robocode.tankroyale.botapi.Bot;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class QLearningBot extends Bot {

    private double nearestEnemyDistance = Double.MAX_VALUE;
    private double nearestEnemyX, nearestEnemyY;

    private final Map<Integer, GameStateActionPair> activeBullets = new HashMap<>();
    private double epsilon = 0.7;
    private final Random random = new Random();
    private GameState previousState, currentState;
    private Action previousAction, currentAction;
    private double currentReward = 0.0;
    private final Classifier classifier;
    private final KnowledgeBase knowledgeBase;

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
        setBodyColor(new Color(0xFF, 0xAA, 0x00));   // orange
        setGunColor(new Color(0xFF, 0x77, 0x00));    // dark orange
        setTurretColor(new Color(0xFF, 0x77, 0x00)); // dark orange
        setRadarColor(new Color(0xFF, 0x00, 0x00));  // red
        setScanColor(new Color(0xFF, 0x00, 0x00));   // red
        setBulletColor(new Color(0x00, 0x88, 0xFF)); // light blue

        while (isRunning()) {
            if (getTurnNumber() % 20 == 0) {
                turnGunRight(360);
            }
            double dx = nearestEnemyX - getX();
            double dy = nearestEnemyY - getY();
            double distance = Math.hypot(dx, dy);

            double angleToEnemy = Math.toDegrees(Math.atan2(dy, dx));  // absolutny kąt względem osi X
            double myHeading = getDirection();                          // absolutny kierunek bota (0 = północ)

            double bearing = normalizeRelativeAngle(angleToEnemy - myHeading);  // względny kąt

            currentState = classifier.observeGameState(distance, bearing);
            currentAction = selectAction(currentState);
            executeAction(currentAction);
            go(); // Execute all queued actions

            // Update Q-table
            if (previousState != null && previousAction != null) {
                double reward = getReward();
                System.out.println("Current state: " + currentState + ", Action: " + currentAction + ", Reward: " + reward);
                knowledgeBase.updateKnowledge(previousState, previousAction, currentState, reward);
            }

            previousState = currentState;
            previousAction = currentAction;
        }
    }

    private double getReward() {
        double reward = currentReward == 0.0 ? -0.001 : currentReward;
        currentReward = 0.0; // Reset reward for the next round
        return reward;
    }

    @Override
    public void onScannedBot(ScannedBotEvent e) {
        double dx = e.getX() - getX();
        double dy = e.getY() - getY();

        double distance = Math.hypot(dx, dy);

        if (distance < nearestEnemyDistance) {
            nearestEnemyDistance = distance;
            nearestEnemyX = e.getX();
            nearestEnemyY = e.getY();
        }
    }

    @Override
    public void onBulletFired(BulletFiredEvent bulletFiredEvent) {
        super.onBulletFired(bulletFiredEvent);
        int bulletId = bulletFiredEvent.getBullet().getBulletId();
        GameState state = currentState == null ? previousState : currentState;
        Action action = currentAction == null ? previousAction : currentAction;
        activeBullets.put(bulletId, new GameStateActionPair(state, action));
        System.out.println("Bullet fired: " + bulletId);
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        currentReward -= 0.1;
        System.out.println("Hit wall");
    }

    @Override
    public void onBulletHit(BulletHitBotEvent bulletHitBotEvent) {
        super.onBulletHit(bulletHitBotEvent);
        int bulletId = bulletHitBotEvent.getBullet().getBulletId();
        GameStateActionPair pair = activeBullets.get(bulletId);
        if (pair != null) {
            GameState state = pair.state();
            Action action = pair.action();
            knowledgeBase.updateKnowledge(state, action, currentState, 1);
            activeBullets.remove(bulletId);
        }
        System.out.println("Bullet hit: " + bulletHitBotEvent.getVictimId() + ", " + getMyId());
    }

    @Override
    public void onBulletHitWall(dev.robocode.tankroyale.botapi.events.BulletHitWallEvent bulletHitWallEvent) {
        super.onBulletHitWall(bulletHitWallEvent);
        int bulletId = bulletHitWallEvent.getBullet().getBulletId();
        GameStateActionPair pair = activeBullets.get(bulletId);
        if (pair != null) {
            GameState state = pair.state();
            Action action = pair.action();
            knowledgeBase.updateKnowledge(state, action, currentState, -0.1);
            activeBullets.remove(bulletId);
        }
    }

    @Override
    public void onRoundEnded(RoundEndedEvent e) {
        System.out.println("Round ended: " + e.getResults().getFirstPlaces());
        activeBullets.clear();
    }

    @Override
    public void onDeath(DeathEvent deathEvent) {
        super.onDeath(deathEvent);
        currentReward -= 5;
        System.out.println("Died");
    }

    @Override
    public void onWonRound(WonRoundEvent wonRoundEvent) {
        super.onWonRound(wonRoundEvent);
        System.out.println("Won round!");
        currentReward += 5;
    }

    private Action selectAction(GameState state) {
        boolean isExploring = random.nextDouble() < epsilon;
        if (isExploring) {
            // Explore: choose a random action
            epsilon = Math.max(0.05, epsilon - 0.001);
            return Action.random();
        } else {
            // Exploit: choose the action with the highest Q-value for the current state
            return knowledgeBase.getBestAction(state);
        }
    }

    private void executeAction(Action action) {
        switch (action) {
            case MOVE_FORWARD -> forward(100);
            case MOVE_BACKWARD -> back(100);
            case TURN_LEFT -> turnLeft(10);
            case TURN_RIGHT -> turnRight(10);
            case FIRE_MEDIUM -> fire(1.0);
        }
    }
}


