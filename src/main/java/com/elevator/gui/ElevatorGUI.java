package com.elevator.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import com.elevator.config.BuildingConfig;

public class ElevatorGUI extends JFrame {
    private JPanel mainPanel;
    private double scale = 1.0;

    //изменяемые размеры
    private int floorHeight;
    private int elevatorWidth;
    private int elevatorHeight;
    private int buildingWidth;

    public ElevatorGUI() {
        updateScaledDimensions();
        setTitle("Elevators System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 800);
        setLocationRelativeTo(null);
        initComponents();
        setupListeners();
    }

    private void updateScaledDimensions() {
        floorHeight = (int)(BuildingConfig.BASE_FLOOR_HEIGHT * scale);
        elevatorWidth = (int)(BuildingConfig.BASE_ELEVATOR_WIDTH * scale);
        elevatorHeight = (int)(BuildingConfig.BASE_ELEVATOR_HEIGHT * scale);
        buildingWidth = (int)(BuildingConfig.BASE_BUILDING_WIDTH * scale);

    }


    private void initComponents() {
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                //g.setColor(new Color(165, 192, 220));
                //g.fillRect(0, 0, getWidth(), getHeight());
                drawBuilding(g);
            }
        };

        int panelWidth = buildingWidth + 100;
        int panelHeight = BuildingConfig.TOTAL_FLOORS * floorHeight + 100;

        mainPanel.setPreferredSize(new Dimension(panelWidth, panelHeight));
        mainPanel.setBackground(new Color(165, 192, 220));

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    private void drawBuilding(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int totalFloors = BuildingConfig.TOTAL_FLOORS;
        int elevatorsCount = BuildingConfig.ELEVATORS_COUNT;


        int startX = 50;
        int startY = 50;

        //контур здания
        g2d.setColor(new Color(240, 240, 240));
        g2d.fillRect(startX, startY, buildingWidth, totalFloors * floorHeight);
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRect(startX, startY, buildingWidth, totalFloors * floorHeight);

        // этажи
        for (int floor = 0; floor <= totalFloors; floor++) {
            int y = startY + (totalFloors - floor) * floorHeight;

            // линия этажа
            g2d.setColor(new Color(200, 200, 200));
            g2d.drawLine(startX, y, startX + buildingWidth, y);

            // номер этажа
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.PLAIN, (int)(12 * scale)));
            String floorText = String.valueOf(floor);
            g2d.drawString(floorText, startX - 20, y + 5);
        }

        //  лифты M штук
        if (elevatorsCount > 0) {
            int elevatorSpacing = buildingWidth / (elevatorsCount + 1);

            for (int i = 0; i < elevatorsCount; i++) {
                int elevatorX = startX + elevatorSpacing * (i + 1) - elevatorWidth / 2;

                //все лифты изначально на 1 этаже
                int floorNumber = 0; // Этаж 0 (ground floor)
                int elevatorY = startY + (totalFloors - floorNumber) * floorHeight - elevatorHeight;
                g2d.setColor(new Color(180, 180, 180));
                g2d.fillRect(elevatorX, elevatorY, elevatorWidth, elevatorHeight);
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawRect(elevatorX, elevatorY, elevatorWidth, elevatorHeight);

                // номер лифта
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, (int)(14 * scale)));
                String elevatorText = String.valueOf(i + 1);
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(elevatorText);
                g2d.drawString(elevatorText,
                        elevatorX + (elevatorWidth - textWidth)/2,
                        elevatorY + elevatorHeight/2 + 5);
            }
        }

    }



    private void setupListeners() {
        mainPanel.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double oldScale = scale;
                double scaleFactor = 1.1;

                if (e.getWheelRotation() > 0) {
                    scaleFactor = 1.0 / scaleFactor;
                }

                scale *= scaleFactor;
                scale = Math.max(0.3, Math.min(scale, 3.0));
                updateScaledDimensions();

                int totalFloors = BuildingConfig.TOTAL_FLOORS;
                int newPanelWidth = buildingWidth + 100;
                int newPanelHeight = totalFloors * floorHeight + 100;

                mainPanel.setPreferredSize(new Dimension(newPanelWidth, newPanelHeight));
                mainPanel.revalidate();
                repaint();
            }
        });
    }
}