package com.elevator.elevator;

import com.elevator.request.Request;
import com.elevator.gui.ElevatorGUI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

// класс Elevator - один лифт, каждый лифт отдельный поток

public class Elevator extends Thread {
    private final int elevatorId; //id
    private int currentFloor; //этаж сейчас
    private Direction direction; //up down
    private ElevatorState elevatorState; //состояние лифта сейчас
    private final BlockingQueue<Request> requests; //очередь запросов пассажиров

    //блокировка для потокобезопасного доступа к состоянию лифта
    private final ReentrantLock lock;


    private final ElevatorGUI gui; //обновления визуал GUI


    //!!!!!!!!!!!!!!потом
    // константы времени (в мс)
    private static final int DOOR_OPEN_TIME = 2000;
    private static final int FLOOR_TRAVEL_TIME = 1000;


    public Elevator(int elevatorId, ElevatorGUI gui) {
        this.elevatorId = elevatorId;
        this.currentFloor = 0;
        this.direction = Direction.WAIT;//начальное направление - без движения
        this.elevatorState = ElevatorState.STOPPED; // начальное состояние - остановлен
        this.requests = new LinkedBlockingQueue<>();
        this.lock = new ReentrantLock();
        this.gui = gui;
    }

    //выполняется в отдельном потоке, обрабатывает запросы пассажиров

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                processNextRequest();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    //обработка след запроса из очереди, блок если очередь пуста
    private void processNextRequest() throws InterruptedException {
        Request request = requests.take();

        moveToFloor(request.getCallFloor()); //едет к этажу где вызван
        openDoors(); // двери открываются
        moveToFloor(request.getTargetFloor()); //едет к пункту назначения
        openDoors(); //двери открываются

        setElevatorState(ElevatorState.STOPPED, Direction.WAIT); //лифт снова в режиме ожидания
    }

    //перемещение лифта на нужный этаж
    private void moveToFloor(int targetFloor) throws InterruptedException {

        if (targetFloor == currentFloor) {
            return;
        }

        //определение направления движения
        Direction moveDirection = (targetFloor > currentFloor) ? Direction.UP : Direction.DOWN;

        setElevatorState(ElevatorState.MOVING, moveDirection);

        //1 этаж за раз
        while (currentFloor != targetFloor) {

            Thread.sleep(FLOOR_TRAVEL_TIME);
            lock.lock();
            try {
                if (moveDirection == Direction.UP) {
                    currentFloor++;
                } else {
                    currentFloor--;
                }
            } finally {
                lock.unlock();
            }

            String guiStatus = "MOVING_" + (moveDirection == Direction.UP ? "UP" : "DOWN");
            gui.updateElevatorState(elevatorId, currentFloor, targetFloor, guiStatus);
        }

        // приехал состояние STOPPED + лог
        System.out.println("Лифт " + (elevatorId + 1) + " прибыл на этаж " + (currentFloor + 1));
        setElevatorState(ElevatorState.STOPPED, Direction.WAIT);
    }

    // открытие дверей
    private void openDoors() throws InterruptedException {
        // состояние DOORS_OPEN
        setElevatorState(ElevatorState.DOORS_OPEN, Direction.WAIT);

        System.out.println("Лифт " + (elevatorId + 1) + " открыл двери на этаже " + (currentFloor + 1));

        Thread.sleep(DOOR_OPEN_TIME);

        System.out.println("Лифт " + (elevatorId + 1) + " закрыл двери на этаже " + (currentFloor + 1));
    }

    // потокобезопасно обновляет состояние и направление. @param newState - Новое состояние лифта. @param newDirection Новое направление движения

    private void setElevatorState(ElevatorState newState, Direction newDirection) {
        lock.lock();
        try {
            this.elevatorState = newState;
            this.direction = newDirection;

            // состояние лифта в GUI
            String guiStatus;
            if (newState == ElevatorState.MOVING) {
                guiStatus = "MOVING_" + (newDirection == Direction.UP ? "UP" : "DOWN");
            } else if (newState == ElevatorState.DOORS_OPEN) {
                guiStatus = "DOORS_OPEN";
            } else {
                guiStatus = "STOPPED";
            }
            gui.updateElevatorState(elevatorId, currentFloor, currentFloor, guiStatus);

        } finally {
            lock.unlock();
        }
    }


    // новый пассажир в очередь
    public void addRequest(Request request) {
        requests.offer(request);
    }

    //геттеры

    public int getElevatorId() {
        return elevatorId;
    }

    public int getCurrentFloor() {
        lock.lock();
        try {
            return currentFloor;
        } finally {
            lock.unlock();
        }
    }

    public Direction getDirection() {
        lock.lock();
        try {
            return direction;
        } finally {
            lock.unlock();
        }
    }


    public ElevatorState getElevatorState() {
        lock.lock();
        try {
            return elevatorState;
        } finally {
            lock.unlock();
        }
    }

    public int getQueueSize() {
        return requests.size();
    }

    public boolean isWAIT() {
        lock.lock();
        try {
            return requests.isEmpty() &&
                    elevatorState == ElevatorState.STOPPED &&
                    direction == Direction.WAIT;
        } finally {
            lock.unlock();
        }
    }

}