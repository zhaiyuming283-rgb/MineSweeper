package ui;

import javax.swing.*;
import java.awt.*;

/**
 * A simple timer used to display the elapsed game time.
 */
public class MiniTimer extends JPanel {
    private Timer timer;
    private int totalSeconds = 0;
    private JLabel timeLabel;

    public MiniTimer () {
        setLayout(new FlowLayout(FlowLayout.CENTER, 4, 2));
        timeLabel = new JLabel("00:00", JLabel.CENTER);

        timer = new Timer(1000, e -> {
            totalSeconds++;
            timeLabel.setText(formatTime(totalSeconds));
        });
        startTimer();

        add(timeLabel);
    }

    public void resetTimer() {
        totalSeconds = 0;
        timeLabel.setText(formatTime(0));
        timer.restart();
    }

    public void startTimer() {
        timer.start();
    }

    public void pauseTimer() {
        timer.stop();
    }

    // Converts elapsed seconds into MM:SS format.
    private String formatTime(int seconds) {
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }
}
