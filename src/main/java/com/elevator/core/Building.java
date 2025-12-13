package com.elevator.core;

import com.elevator.elevator.Elevator;
import com.elevator.gui.ElevatorGUI;
import com.elevator.config.BuildingConfig;
import java.util.ArrayList;
import java.util.List;
import com.elevator.dispatcher.ElevatorDispatcher;

public class Building {
    private final List<Elevator> elevators;
    private final ElevatorDispatcher dispatcher;

    public Building(ElevatorGUI gui) {
        this.elevators = new ArrayList<>();

        // BuildingConfig.ELEVATORS_COUNT - количество лифтов из конфига
        for (int i = 0; i < BuildingConfig.ELEVATORS_COUNT; i++) {
            Elevator elevator = new Elevator(i, gui); //новый лифт с уникальным ID и ссылкой на GUI

            elevators.add(elevator);
        }

        this.dispatcher = new ElevatorDispatcher(elevators);
    }

    //запуск лифтов и потоков
    public void startElevators() {
        for (Elevator elevator : elevators) {
            elevator.start();
        }
    }

    //остановка всех лифтов и поток
    public void stopElevators() {
        for (Elevator elevator : elevators) {
            elevator.interrupt();
        }
    }

    public ElevatorDispatcher getDispatcher() {
        return dispatcher;
    }

    public List<Elevator> getElevators() {
        return elevators;
    }
}