package com.elevator.gui;

import com.elevator.config.BuildingConfig;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BuildingPanel extends JPanel {
    private double scale = 1.0;



    //размеры элементов из-за масштаба
    private int floorHeight;
    private int elevatorWidth;
    private int elevatorHeight;
    private final int totalFloors;
    private final int elevatorsCount;
    private List<ElevatorGUI.ElevatorState> elevatorStates;
    private Map<Integer, Integer> floorPassengers;


    // ссновной экран
    public BuildingPanel(List<ElevatorGUI.ElevatorState> elevatorStates, Map<Integer, Integer> floorPassengers) {
        this.elevatorStates = elevatorStates;
        this.floorPassengers = floorPassengers;

        this.totalFloors = BuildingConfig.TOTAL_FLOORS;
        this.elevatorsCount = BuildingConfig.ELEVATORS_COUNT;
        this.elevatorStates = elevatorStates;
        this.floorPassengers = floorPassengers;
        updateScaledDimensions();

        setBackground(new Color(165, 192, 220));

        int panelWidth = 1200;
        int panelHeight = totalFloors * floorHeight + 200; // +отступ
        setPreferredSize(new Dimension(panelWidth, panelHeight));
    }


    public void setFloorPassengers(Map<Integer, Integer> floorPassengers) {
        for (int i = 0; i < BuildingConfig.TOTAL_FLOORS; i++) {
            int count = floorPassengers.getOrDefault(i, 0);
        }
        this.floorPassengers = floorPassengers;
        repaint();
    }
    // обновление масштаба (от колёсика мышки)
    public void updateScale(double newScale) {
        this.scale = newScale;
        updateScaledDimensions();
        updatePanelSize();
        repaint();
    }

    //изменение размера элементов от масштаба
    private void updateScaledDimensions() {
        floorHeight = (int)(BuildingConfig.BASE_FLOOR_HEIGHT * scale);
        elevatorWidth = (int)(BuildingConfig.BASE_ELEVATOR_WIDTH * scale);
        elevatorHeight = (int)(BuildingConfig.BASE_ELEVATOR_HEIGHT * scale);
    }

    //изменение размера панели от масштаба
    private void updatePanelSize() {
        int newPanelHeight = totalFloors * floorHeight + 200;
        setPreferredSize(new Dimension(getPreferredSize().width, newPanelHeight));
        revalidate();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBuilding(g);
        drawElevators(g);
        drawPassengers(g);
    }

    //контур, этажи, номера этажей
    private void drawBuilding(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int indent = 50; //отступы
        int buildingWidth = Math.max(getWidth() - 2 * indent, 300);
        //int buildingWidth = getWidth() - 2 * indent;
        //if (buildingWidth < 300) buildingWidth = 300;

        int startX = indent;
        int startY = indent;

        //фон дома
        g2d.setColor(new Color(213, 164, 139));
        g2d.fillRect(startX, startY, buildingWidth, totalFloors * floorHeight);

        //контур дома
        g2d.setColor(new Color(0, 0, 0));
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRect(startX, startY, buildingWidth, totalFloors * floorHeight);

        //этажи и номера, y - нижняяя граница этажа
        for (int floor = 0; floor < totalFloors; floor++) {

            int passengerCount = floorPassengers.getOrDefault(floor, 0);

            int lineY = startY + (totalFloors - floor) * floorHeight;
            if (floor > 0) {
                g2d.setColor(new Color(184, 42, 42));
                g2d.drawLine(startX, lineY, startX + buildingWidth, lineY);
            }

            //номер этажа
            g2d.setColor(Color.DARK_GRAY);
            g2d.setFont(new Font("Arial", Font.BOLD, (int)(12 * scale)));
            String floorText = String.valueOf(floor + 1);

            int textY = startY + (totalFloors - floor) * floorHeight - (floorHeight / 2);
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(floorText);
            g2d.drawString(floorText, startX - textWidth - 10, textY + 5);
        }
        //int topLineY = startY + totalFloors * floorHeight;
        //g2d.setColor(new Color(184, 42, 42));
        //g2d.drawLine(startX, topLineY, startX + buildingWidth, topLineY);
    }

    private void drawPassengers(Graphics g) {

        if (floorPassengers == null) return;
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int indent = 50;
        int buildingWidth = Math.max(getWidth() - 2 * indent, 300);
        int startX = indent;
        int startY = indent;
        int floorHeight = (int)(BuildingConfig.BASE_FLOOR_HEIGHT * scale);

        for (int floor = 0; floor < BuildingConfig.TOTAL_FLOORS; floor++) {
            Integer passengerCountObj = floorPassengers.get(floor);
            int passengerCount = (passengerCountObj != null) ? passengerCountObj : 0;

            if (passengerCount > 0) {
                int floorY = startY + (BuildingConfig.TOTAL_FLOORS - floor - 1) * floorHeight;

                // черные кружочки
                g2d.setColor(Color.BLACK);
                int spacing = buildingWidth / (passengerCount + 1);

                for (int i = 0; i < passengerCount; i++) {
                    int passengerX = startX + spacing * (i + 1);
                    int passengerY = floorY + floorHeight / 2;
                    g2d.fillOval(passengerX - 6, passengerY - 6, 12, 12);

                    // обводка
                    g2d.setColor(Color.WHITE);
                    g2d.drawOval(passengerX - 6, passengerY - 6, 12, 12);
                    g2d.setColor(Color.BLACK);
                }
            }
        }




    }



    //лифты
    private void drawElevators(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        int indent = 50;
        int buildingWidth = Math.max(getWidth() - 2 * indent, 300);
        //int buildingWidth = getWidth() - 2 * indent;
        //if (buildingWidth < 300) buildingWidth = 300;

        int startX = indent;
        int startY = indent;

        // проверка. Если лифты не убираются - уменьшаются
        if (elevatorsCount > 0 && elevatorStates != null) {
            int minSpace = 15;
            int availableWidth = buildingWidth - 40;

            int currentElevatorWidth = elevatorWidth;
            if (elevatorsCount * (elevatorWidth + minSpace) - minSpace > availableWidth) {
                currentElevatorWidth = (availableWidth - (elevatorsCount - 1) * minSpace) / elevatorsCount;
                currentElevatorWidth = Math.max(currentElevatorWidth, 25);
            }

            int currentElevatorHeight = (int)(currentElevatorWidth * 1.5);
            int totalGroupWidth = elevatorsCount * currentElevatorWidth + (elevatorsCount - 1) * minSpace;
            int groupStartX = startX + (buildingWidth - totalGroupWidth) / 2;

            //каждый лифт
            for (int i = 0; i < elevatorsCount; i++) {
                ElevatorGUI.ElevatorState state = elevatorStates.get(i);
                int currentFloor = state.getCurrentFloor(); // текущий этаж

                int elevatorX = groupStartX + i * (currentElevatorWidth + minSpace);

                // -1 потому что этажи нумеруются с 0, но рисуются с 1
                int elevatorY = startY + (totalFloors - currentFloor - 1) * floorHeight;

                // цвет лифта зависит от состояния
                g2d.setColor(state.getColor());
                g2d.fillRect(elevatorX, elevatorY, currentElevatorWidth, currentElevatorHeight);

                g2d.setColor(Color.DARK_GRAY);
                g2d.drawRect(elevatorX, elevatorY, currentElevatorWidth, currentElevatorHeight);

                //НОМЕР ЛИФТА
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, Math.max(10, currentElevatorWidth / 3)));
                String elevatorText = String.valueOf(i + 1);
                FontMetrics fm = g2d.getFontMetrics();

                // порядковый номер лифта в центре
                int textWidth = fm.stringWidth(elevatorText);
                int textHeight = fm.getHeight();
                g2d.drawString(elevatorText,
                        elevatorX + (currentElevatorWidth - textWidth) / 2,
                        elevatorY + (currentElevatorHeight + textHeight) / 2 - 3);

                //номер этажа, на котором лифт сейчас
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, Math.max(8, currentElevatorWidth / 3)));
                String floorText = String.valueOf(currentFloor + 1); // этаж + 1
                int floorTextWidth = g2d.getFontMetrics().stringWidth(floorText);

                g2d.drawString(floorText,
                        elevatorX + (currentElevatorWidth - floorTextWidth) / 2,
                        elevatorY - 5);
            }
        }
    }
}