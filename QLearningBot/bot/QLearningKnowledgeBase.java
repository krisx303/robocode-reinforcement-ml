package bot;

import java.util.HashMap;
import java.util.Map;

public class QLearningKnowledgeBase implements KnowledgeBase {

    private double learningRate;
    private double discountFactor;

    public QLearningKnowledgeBase(double learningRate, double discountFactor) {
        this.learningRate = learningRate;
        this.discountFactor = discountFactor;
    }

    private final Map<GameStateActionPair, Double> qTable = new HashMap<>();

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
}
