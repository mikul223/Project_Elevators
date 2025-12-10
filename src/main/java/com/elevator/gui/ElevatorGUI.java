package com.elevator.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class ElevatorGUI extends JFrame {
    private JPanel mainPanel;
    private double scale = 1.0;

    public ElevatorGUI() {
        setTitle("Elevators System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 800);
        setLocationRelativeTo(null);
        initComponents();
        setupListeners();
    }

    private void initComponents() {
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(165, 192, 220));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };


        mainPanel.setPreferredSize(new Dimension(2000, 2000));

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    private void setupListeners() {
        mainPanel.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double scaleFactor = 1.1;
                if (e.getWheelRotation() > 0) {
                    scaleFactor = 1.0 / scaleFactor;
                }

                scale *= scaleFactor;
                scale = Math.max(0.1, Math.min(scale, 5.0));

                Dimension currentSize = mainPanel.getPreferredSize();
                Dimension newSize = new Dimension(
                        (int)(currentSize.width * scaleFactor),
                        (int)(currentSize.height * scaleFactor)
                );
                mainPanel.setPreferredSize(newSize);
                mainPanel.revalidate();
                repaint();
            }
        });
    }
}