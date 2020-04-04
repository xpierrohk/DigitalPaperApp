package net.sony.dpt.ui.gui.whiteboard;

import net.sony.dpt.command.device.TakeScreenshotCommand;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static java.awt.Image.SCALE_DEFAULT;

/**
 * Whiteboard attempt using the new fast screenshot API
 */
public class Whiteboard {

    private TakeScreenshotCommand takeScreenshotCommand;
    private JFrame frame;
    private JLabel label;

    public Whiteboard(TakeScreenshotCommand takeScreenshotCommand) throws IOException, InterruptedException {
        this.takeScreenshotCommand = takeScreenshotCommand;
        frame = new JFrame("Whiteboard");
        frame.setSize(2200 / 2, 1650 / 2);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        label = new JLabel();

        JPanel panel = new JPanel();
        panel.add(label);
        frame.setContentPane(panel);

        redraw();
        frame.setVisible(true);

        Thread backgroundQueryThread = new Thread(() -> {
            try {
                while (true) {
                    redraw();
                    Thread.sleep(1000);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        backgroundQueryThread.start();

    }

    public BufferedImage rotateAndScale(BufferedImage image, double angle) {
        angle = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
        int width = image.getWidth();
        int height = image.getHeight();
        int rotatedWidth = (int) Math.floor(width * cos + height * sin);
        int rotatedHeight = (int) Math.floor(height * cos + width * sin);

        GraphicsConfiguration gc = frame.getGraphicsConfiguration();
        BufferedImage result = gc.createCompatibleImage(rotatedWidth, rotatedHeight, Transparency.TRANSLUCENT);
        Graphics2D g = result.createGraphics();
        g.translate((rotatedWidth - width) / 2, (rotatedHeight - height) / 2);

        g.rotate(angle, (double) width / 2, (double) height / 2);
        g.drawRenderedImage(image, null);
        g.dispose();
        return result;
    }

    public void redraw() throws IOException, InterruptedException {
        BufferedImage img = ImageIO.read(takeScreenshotCommand.fastScreenshot());
        BufferedImage rotated = rotateAndScale(img, 90);

        ImageIcon icon = new ImageIcon(rotated.getScaledInstance(rotated.getWidth() / 2, rotated.getHeight() / 2, SCALE_DEFAULT));

        EventQueue.invokeLater(() -> {
            label.setIcon(icon);
            frame.revalidate();
            frame.repaint();
        });
    }

}
