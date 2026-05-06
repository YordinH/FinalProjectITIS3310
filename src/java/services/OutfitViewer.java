package services;

import domain.ClothingItem;
import domain.ClothingType;
import domain.Outfit;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class OutfitViewer {
    private static final int OVERLAP = 30;
    private static final int PANEL_WIDTH = 400;
    private static final ClothingType[] ORDER = {
        ClothingType.HEADWEAR, ClothingType.TOP, ClothingType.BOTTOMS, ClothingType.FOOTWEAR
    };
    private static final Map<ClothingType, Integer> HEIGHTS = new EnumMap<>(ClothingType.class);
    static {
        HEIGHTS.put(ClothingType.HEADWEAR, 130);
        HEIGHTS.put(ClothingType.TOP,      220);
        HEIGHTS.put(ClothingType.BOTTOMS,  220);
        HEIGHTS.put(ClothingType.FOOTWEAR, 130);
    }

    private JFrame frame;
    private OutfitPanel panel;

    public OutfitViewer() {
        SwingUtilities.invokeLater(this::buildFrame);
    }

    private void buildFrame() {
        panel = new OutfitPanel();
        panel.setPreferredSize(new Dimension(PANEL_WIDTH, totalHeight()));

        frame = new JFrame("Outfit Viewer");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private int totalHeight() {
        int total = 0;
        for (ClothingType t : ORDER) total += HEIGHTS.get(t);
        return total - OVERLAP * (ORDER.length - 1);
    }

    public void update(Outfit outfit) {
        SwingUtilities.invokeLater(() -> {
            panel.clearImages();
            for (ClothingType type : ORDER) {
                ClothingItem item = findItem(outfit, type);
                if (item != null && item.getImageFilePath() != null && !item.getImageFilePath().isEmpty()) {
                    try {
                        BufferedImage img = ImageIO.read(new File(item.getImageFilePath()));
                        panel.setImage(type, img);
                    } catch (IOException ignored) {}
                }
            }
            panel.repaint();
        });
    }

    public void dispose() {
        SwingUtilities.invokeLater(() -> {
            if (frame != null) frame.dispose();
        });
    }

    private ClothingItem findItem(Outfit outfit, ClothingType type) {
        for (ClothingItem item : outfit.getItems()) {
            if (item.getCategory().getType() == type) return item;
        }
        return null;
    }

    private class OutfitPanel extends JPanel {
        private final Map<ClothingType, BufferedImage> images = new EnumMap<>(ClothingType.class);

        void setImage(ClothingType type, BufferedImage img) { images.put(type, img); }
        void clearImages() { images.clear(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setBackground(Color.WHITE);
            g2.clearRect(0, 0, getWidth(), getHeight());

            int y = 0;
            for (ClothingType type : ORDER) {
                int targetH = HEIGHTS.get(type);
                BufferedImage img = images.get(type);
                if (img != null) {
                    int targetW = (int) ((double) img.getWidth() / img.getHeight() * targetH);
                    int x = (getWidth() - targetW) / 2;
                    g2.drawImage(img, x, y, targetW, targetH, null);
                } else {
                    g2.setColor(new Color(230, 230, 230));
                    g2.fillRoundRect(20, y + 5, getWidth() - 40, targetH - 10, 12, 12);
                    g2.setColor(new Color(180, 180, 180));
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
                    FontMetrics fm = g2.getFontMetrics();
                    String text = "[" + type.name() + "]";
                    g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, y + targetH / 2 + fm.getAscent() / 2 - 2);
                }
                y += targetH - OVERLAP;
            }
        }
    }
}
