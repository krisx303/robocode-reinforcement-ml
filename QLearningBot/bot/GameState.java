package bot;

public record GameState(
        Categories.DistanceCategory enemyDistance,
        Categories.AngleCategory angleToEnemy
) {}

record GameStateActionPair(GameState state, Action action) { }