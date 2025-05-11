package bot;

import java.util.Random;

enum Action {
    MOVE_FORWARD,
    MOVE_BACKWARD,
    TURN_LEFT,
    TURN_RIGHT,
    FIRE_MEDIUM;

    public static Action random() {
        Action[] values = values();
        return values[new Random().nextInt(values.length)];
    }
}
