package com.elevator.core;

import com.elevator.request.Request;
import com.elevator.elevator.Direction;
import com.elevator.config.BuildingConfig;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.elevator.gui.ElevatorGUI;
import javax.swing.SwingUtilities;

// генерация случайный этаж, случайное время
public class Simulation {
    private final Building building;
    private final Random random = new Random();
    private ScheduledExecutorService scheduler;
    private ElevatorGUI gui;

    public Simulation(Building building) {
        this.building = building;
    }


    public void setGUI(ElevatorGUI gui) {
        this.gui = gui;
    }

    //запуск симуляции
    public void start() {
        building.startElevators();
        scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(this::generateRandomRequest,
                2, 3 + random.nextInt(7), TimeUnit.SECONDS);
    }

    //остановка симуляции
    public void stop() {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        building.stopElevators();
    }

    private void generateRandomRequest() {
        try {
            int callFloor = random.nextInt(BuildingConfig.TOTAL_FLOORS);

            int targetFloor;
            do {
                targetFloor = random.nextInt(BuildingConfig.TOTAL_FLOORS);
            } while (targetFloor == callFloor);

            Direction direction = (targetFloor > callFloor) ? Direction.UP : Direction.DOWN;



            if (gui != null) {
                SwingUtilities.invokeLater(() -> {
                    gui.addPassengerToFloor(callFloor);
                    System.out.println("Пассажир появился на этаже " + (callFloor + 1) +
                            ". Теперь там: " + gui.getPassengerCountOnFloor(callFloor) + " пассажиров");
                });
            } else {
                System.out.println("Пассажир появился на этаже " + (callFloor + 1));
            }

            Request request = new Request(callFloor, direction, targetFloor);

            System.out.println("Новый запрос: пассажир на этаже " + (callFloor + 1) +
                    " хочет на этаж " + (targetFloor + 1) + " (" +
                    (direction == Direction.UP ? "ВВЕРХ" : "ВНИЗ") + ")");

            building.getDispatcher().handleRequest(request);
        } catch (Exception e) {
            System.err.println("Ошибка генерации запроса: " + e.getMessage());
        }
    }
}