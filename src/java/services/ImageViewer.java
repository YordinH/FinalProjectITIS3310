package services;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ImageViewer {
    private JFrame frame;

    public void show(File imageFile) {
        SwingUtilities.invokeLater(() -> {
            if (frame != null) frame.dispose();
            frame = new JFrame(imageFile.getName());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            ImageIcon raw = new ImageIcon(imageFile.getAbsolutePath());
            int w = 400;
            int h = raw.getIconWidth() > 0
                    ? (int) ((double) raw.getIconHeight() / raw.getIconWidth() * w)
                    : w;
            Image scaled = raw.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            frame.add(new JLabel(new ImageIcon(scaled)));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public void close() {
        SwingUtilities.invokeLater(() -> {
            if (frame != null) {
                frame.dispose();
                frame = null;
            }
        });
    }
}
