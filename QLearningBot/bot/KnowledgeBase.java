package bot;

public interface KnowledgeBase {

    void updateKnowledge(GameState previousState, Action previousAction, GameState currentState, double reward);

    Action getBestAction(GameState state);
}
