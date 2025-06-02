package bot;

import java.util.HashMap;
import java.util.Map;
import java.io.*;

public class QLearningKnowledgeBase implements KnowledgeBase {

    private static final long serialVersionUID = 1L;
    private static final String QTABLE_FILENAME = "qtable.ser";

    private double learningRate;
    private double discountFactor;

    private final Map<GameStateActionPair, Double> qTable;

    public QLearningKnowledgeBase(double learningRate, double discountFactor) {
        this.learningRate = learningRate;
        this.discountFactor = discountFactor;
        this.qTable = loadQTable();
    }


    @Override
    public void updateKnowledge(GameState previousState, Action previousAction, GameState currentState, double reward) {
        double oldQValue = qTable.getOrDefault(new GameStateActionPair(previousState, previousAction), 0.0);
        double maxQValueNextState = 0;

        // Znajdź maksymalną Q-wartość dla następnego stanu (currentState)
        for (Action action : Action.values()) {
            maxQValueNextState = Math.max(maxQValueNextState,
                    qTable.getOrDefault(new GameStateActionPair(currentState, action), 0.0));
        }

        // Aktualizuj Q-wartość zgodnie z algorytmem Q-learning
        double newQValue = oldQValue + learningRate * (reward + discountFactor * maxQValueNextState - oldQValue);
        qTable.put(new GameStateActionPair(previousState, previousAction), newQValue);
    }

    @Override
    public Action getBestAction(GameState state) {
        double bestQValue = Double.NEGATIVE_INFINITY;
        Action bestAction = null;

        for (Action action : Action.values()) {
            double qValue = qTable.getOrDefault(new GameStateActionPair(state, action), 0.0);
            if (qValue > bestQValue) {
                bestQValue = qValue;
                bestAction = action;
            }
        }
        return bestAction != null ? bestAction : Action.random(); // If no Q-values are present, choose randomly
    }

    public void saveQTable() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(QTABLE_FILENAME))) {
            oos.writeObject(qTable);
            System.out.println("Saved Q-table (" + qTable.size() + " entries) to " + QTABLE_FILENAME);
        } catch (IOException e) {
            System.err.println("Failed to save Q-table: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<GameStateActionPair, Double> loadQTable() {
        File file = new File(QTABLE_FILENAME);
        if (!file.exists()) {
            return new HashMap<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                System.out.println("Loaded Q-table (" + ((Map<?, ?>) obj).size() + " entries) from " + QTABLE_FILENAME);
                return (Map<GameStateActionPair, Double>) obj;
            } else {
                System.err.println("Q-table file is invalid, starting fresh.");
                return new HashMap<>();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load Q-table, starting fresh: " + e.getMessage());
            return new HashMap<>();
        }
    }

    public int getQTableSize() {
        return qTable.size();
    }
}
