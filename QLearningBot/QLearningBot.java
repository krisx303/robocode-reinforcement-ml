import dev.robocode.tankroyale.botapi.*;

import java.awt.Color;

public class QLearningBot extends Bot {
    public static void main(String[] args) {
        new QLearningBot().start();
    }

    QLearningBot() {
        super(BotInfo.fromFile("QLearningBot.json"));
    }

    // Called when a new round is started -> initialize and do some movement
    @Override
    public void run() {
        // Set colors
        setBodyColor(new Color(0xFF, 0xAA, 0x00));   // orange
        setGunColor(new Color(0xFF, 0x77, 0x00));    // dark orange
        setTurretColor(new Color(0xFF, 0x77, 0x00)); // dark orange
        setRadarColor(new Color(0xFF, 0x00, 0x00));  // red
        setScanColor(new Color(0xFF, 0x00, 0x00));   // red
        setBulletColor(new Color(0x00, 0x88, 0xFF)); // light blue

        // Spin the gun around slowly... forever
        while (isRunning()) {
            // Turn the gun a bit if the bot if the target speed is 0
            turnGunRight(5);
        }
    }
}
