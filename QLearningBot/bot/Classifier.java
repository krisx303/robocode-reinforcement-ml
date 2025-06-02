package bot;

public class Classifier {

    public GameState observeGameState(double nearestEnemyDistance, double relativeBearingToNearestEnemy, int ticksSinceLastScan){
        if (ticksSinceLastScan > 20) {
            return new GameState(Categories.DistanceCategory.UNKNOWN, Categories.AngleCategory.UNKNOWN);
        }

        Categories.DistanceCategory enemyDistCat = categorizeDistance(nearestEnemyDistance);
        Categories.AngleCategory angleCat = categorizeAngle(relativeBearingToNearestEnemy);

        return new GameState(enemyDistCat, angleCat);
    }

    private Categories.DistanceCategory categorizeDistance(double d) {
        if (d < 200) return Categories.DistanceCategory.CLOSE;
        if (d < 500) return Categories.DistanceCategory.MEDIUM;
        return Categories.DistanceCategory.FAR;
    }

    private Categories.AngleCategory categorizeAngle(double angle) {
        if (angle >= -5 && angle <= 5) return Categories.AngleCategory.FRONT_TO_SHOT;
        if (angle > -45 && angle < 45) return Categories.AngleCategory.FRONT;
        if (angle >= 45 && angle <= 135) return Categories.AngleCategory.LEFT;
        if (angle <= -45 && angle >= -135) return Categories.AngleCategory.RIGHT;
        return Categories.AngleCategory.BACK;
    }

    private Categories.SpeedCategory categorizeSpeed(double speed) {
        if (speed < 1) return Categories.SpeedCategory.STILL;
        if (speed < 4) return Categories.SpeedCategory.SLOW;
        return Categories.SpeedCategory.FAST;
    }

    private Categories.EnergyCategory categorizeEnergy(double e) {
        if (e < 20) return Categories.EnergyCategory.LOW;
        if (e < 50) return Categories.EnergyCategory.MEDIUM;
        return Categories.EnergyCategory.HIGH;
    }
}
