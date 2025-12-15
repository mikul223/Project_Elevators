package com.elevator.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.*;
import java.util.List;
import com.elevator.config.BuildingConfig;
import java.util.concurrent.ConcurrentHashMap;


public class ElevatorGUI extends JFrame {
    private BuildingPanel buildingPanel;

    private double scale = 1.0;

    //состояния всех лифтов: текущий этаж, этаж цель, статус, цвет
    private List<ElevatorState> elevatorStates;
    private final Map<Integer, Integer> floorPassengers = new ConcurrentHashMap<>();

    public ElevatorGUI() {
        //все лифты изначально на 1 этаже
        elevatorStates = initializeElevatorStates();
        initializePassengerMap();

        //окно основное
        setTitle("Elevator System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null);
        initComponents();
        setupListeners();
    }

    private void initializePassengerMap() {
    }

    //добавить пассажира на этаж
    public void addPassengerToFloor(int floor) {
        if (floor >= 0 && floor < BuildingConfig.TOTAL_FLOORS) {
            int current = floorPassengers.getOrDefault(floor, 0);
            floorPassengers.put(floor, current + 1);
            System.out.println("Пассажир появился на этаже " + (floor + 1) +
                    ". Теперь там: " + floorPassengers.get(floor) + " пассажиров");
            buildingPanel.setFloorPassengers(floorPassengers);
            buildingPanel.repaint();
        } else {
            System.err.println("Ошибка: неверный номер этажа " + floor);
        }
    }

    // удалить пассажира с этажа
    public void removePassengerFromFloor(int floor) {
        if (floor >= 0 && floor < BuildingConfig.TOTAL_FLOORS) {
            int current = floorPassengers.getOrDefault(floor, 0);
            if (current > 0) {
                floorPassengers.put(floor, current - 1);

            } else {
                System.err.println("Предупреждение: попытка удалить пассажира с пустого этажа " + (floor + 1));
            }
            buildingPanel.setFloorPassengers(floorPassengers);
            buildingPanel.repaint();
        } else {
            System.err.println("Ошибка: неверный номер этажа " + floor);
        }
    }

    // получить количество пассажиров на этаже
    public synchronized int getPassengerCountOnFloor(int floor) {
        if (floor >= 0 && floor < BuildingConfig.TOTAL_FLOORS) {
            return floorPassengers.getOrDefault(floor, 0);
        }
        return 0;
    }

    public static class ElevatorState {
        private int currentFloor;  // текущий этаж лифта
        private int targetFloor;   // этаж цель
        private String status;     // текущее состояние: STOPPED, MOVING_UP, MOVING_DOWN, DOORS_OPEN
        private Color color;       // цвет лифта


        public ElevatorState(int startFloor) {
            this.currentFloor = startFloor;
            this.targetFloor = startFloor;
            this.status = "STOPPED";
            this.color = new Color(180, 180, 180);
        }



        public int getCurrentFloor() { return currentFloor; }
        public void setCurrentFloor(int floor) { this.currentFloor = floor; }

        public int getTargetFloor() { return targetFloor; }
        public void setTargetFloor(int floor) { this.targetFloor = floor; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Color getColor() { return color; }
        public void setColor(Color color) { this.color = color; }
    }


    private List<ElevatorState> initializeElevatorStates() {
        List<ElevatorState> states = new ArrayList<>();

        int elevatorsCount = BuildingConfig.ELEVATORS_COUNT;;

        for (int i = 0; i < elevatorsCount; i++) {
            states.add(new ElevatorState(0));
        }
        return states;
    }

    //на будущее цвета
    public void updateElevatorState(int elevatorId, int currentFloor, int targetFloor, String status) {
        // id лифта -- отображаемый номер -1
        if (elevatorId >= 0 && elevatorId < elevatorStates.size()) {
            ElevatorState state = elevatorStates.get(elevatorId);

            state.setCurrentFloor(currentFloor);
            state.setTargetFloor(targetFloor);
            state.setStatus(status);

            switch (status.toUpperCase()) {
                case "MOVING_UP" ->
                        state.setColor(new Color(255, 7, 7));
                case "MOVING_DOWN" ->
                        state.setColor(new Color(27, 59, 255));
                case "DOORS_OPEN" ->
                        state.setColor(new Color(255, 210, 70));
                default ->
                        state.setColor(new Color(180, 180, 180));
            }

            //перерисовка
            SwingUtilities.invokeLater(() -> buildingPanel.repaint());

        } else {
            System.err.println("Ошибка: неверный ID лифта " + elevatorId);
        }
    }

    private void initComponents() {
        buildingPanel = new BuildingPanel(elevatorStates, floorPassengers);
        JScrollPane scrollPane = new JScrollPane(buildingPanel);

        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    private void setupListeners() {
        // регулировать масштаб колесиком мышки
        buildingPanel.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double scaleFactor = 1.1;

                if (e.getWheelRotation() > 0) {
                    scaleFactor = 1.0 / scaleFactor;
                }

                double oldScale = scale;
                scale *= scaleFactor;
                scale = Math.max(0.3, Math.min(scale, 3.0));

                // обновляем панель при изменении масштаба
                if (oldScale != scale) {
                    buildingPanel.updateScale(scale);
                }
            }
        });

        buildingPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                //перерисовка
                buildingPanel.repaint();
            }
        });
    }
}