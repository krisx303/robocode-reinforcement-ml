package bot;

import java.io.Serializable;


public record GameState(
        Categories.DistanceCategory enemyDistance,
        Categories.AngleCategory angleToEnemy
) implements Serializable {}

record GameStateActionPair(GameState state, Action action) implements Serializable { }