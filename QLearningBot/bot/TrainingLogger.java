package bot;

import java.io.FileWriter;
import java.io.IOException;

public class TrainingLogger {
    private final String filename;
    private FileWriter writer;

    public TrainingLogger(String filename) {
        this.filename = filename;
        try {
            writer = new FileWriter(filename);
            writer.write("Round,Epsilon,CumulativeReward,Won,QTableSize\n");
        } catch (IOException e) {
            System.err.println("Failed to initialize logger: " + e.getMessage());
        }
    }

    public void log(int round, double epsilon, double cumulativeReward, boolean won, int qTableSize) {
        try {
            writer.write(round + "," + epsilon + "," + cumulativeReward + "," + (won ? 1 : 0) + "," + qTableSize + "\n");
        } catch (IOException e) {
            System.err.println("Failed to log training data: " + e.getMessage());
        }
    }
}
