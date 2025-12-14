package com.elevator.elevator;

import com.elevator.request.Request;
import com.elevator.gui.ElevatorGUI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import com.elevator.config.BuildingConfig;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;


// класс Elevator - один лифт, каждый лифт отдельный поток

public class Elevator extends Thread {
    private final int elevatorId; //id
    private int currentFloor; //этаж сейчас
    private Direction direction; //up down
    private ElevatorState elevatorState; //состояние лифта сейчас
    //private final BlockingQueue<Request> requests; //очередь запросов пассажиров
    private final BlockingQueue<Request> pendingRequests;
    private final List<Request> activeRequests;


    //блокировка для потокобезопасного доступа к состоянию лифта
    private final ReentrantLock lock;

    private final ElevatorGUI gui; //обновления визуал GUI

    // константы времени (в мс)
    private static final int DOOR_OPEN_TIME = 2000;
    private static final int FLOOR_TRAVEL_TIME = 1000;
 //   private static final int CAPACITY = 8;


    public Elevator(int elevatorId, ElevatorGUI gui) {
        this.elevatorId = elevatorId;
        this.currentFloor = 0;
        this.direction = Direction.WAIT;//начальное направление - без движения
        this.elevatorState = ElevatorState.STOPPED; // начальное состояние - остановлен
        this.pendingRequests = new LinkedBlockingQueue<>();
        this.activeRequests = new ArrayList<>();
        this.lock = new ReentrantLock();
        this.gui = gui;
    }

    //выполняется в отдельном потоке, обрабатывает запросы пассажиров
    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                optimizeAndProcessRequests();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    private void optimizeAndProcessRequests() throws InterruptedException {
        if (activeRequests.isEmpty()) {
            Request newRequest = pendingRequests.poll(100, TimeUnit.MILLISECONDS);
            if (newRequest != null) {
                lock.lock();
                try {
                    activeRequests.add(newRequest);
                    direction = calculateInitialDirection(newRequest);
                } finally {
                    lock.unlock();
                }
            } else {
                setElevatorState(ElevatorState.STOPPED, Direction.WAIT);
                return;
            }
        }

        executeOptimizedRoute();

    }

    private Direction calculateInitialDirection(Request request) {
        if (request.getCallFloor() > currentFloor) {
            return Direction.UP;
        } else if (request.getCallFloor() < currentFloor) {
            return Direction.DOWN;
        } else {
            return request.getDirection();
        }
    }


    private void executeOptimizedRoute() throws InterruptedException {
        lock.lock();
        try {
            // Собираем все целевые точки маршрута
            Set<Integer> targetFloors = new TreeSet<>();
            for (Request req : activeRequests) {
                targetFloors.add(req.getCallFloor());
                targetFloors.add(req.getTargetFloor());
            }

            // Конвертируем в отсортированный список в зависимости от направления
            List<Integer> route = new ArrayList<>(targetFloors);
            if (direction == Direction.DOWN) {
                route.sort(Collections.reverseOrder());
            } else {
                Collections.sort(route);
            }

            // Удаляем этажи, где лифт уже находится
            route.removeIf(floor -> floor == currentFloor);
        } finally {
            lock.unlock();
        }

        // Выполняем маршрут с проверкой промежуточных запросов
        executeRouteWithPickups();
    }

    private void executeRouteWithPickups() throws InterruptedException {
        boolean continueRoute = true;

        while (continueRoute && !Thread.currentThread().isInterrupted()) {
            lock.lock();
            Integer nextStop;
            try {
                // Определяем следующую остановку
                nextStop = findNextStop();
                if (nextStop == null) {
                    continueRoute = false;
                    break;
                }
            } finally {
                lock.unlock();
            }

            // Двигаемся к следующей остановке
            moveToFloorWithPickups(nextStop);

            // Обрабатываем остановку
            processStopAtFloor(nextStop);
        }

        // Если маршрут завершен
        lock.lock();
        try {
            if (activeRequests.isEmpty()) {
                setElevatorState(ElevatorState.STOPPED, Direction.WAIT);
            }
        } finally {
            lock.unlock();
        }
    }

    private Integer findNextStop() {
        if (activeRequests.isEmpty()) {
            return null;
        }

        // Находим ближайшую цель в текущем направлении
        Integer nearestStop = null;
        int minDistance = Integer.MAX_VALUE;

        for (Request req : activeRequests) {
            // Проверяем этаж вызова
            if (shouldStopAtFloor(req.getCallFloor())) {
                int distance = Math.abs(currentFloor - req.getCallFloor());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestStop = req.getCallFloor();
                }
            }

            // Проверяем целевой этаж
            if (shouldStopAtFloor(req.getTargetFloor())) {
                int distance = Math.abs(currentFloor - req.getTargetFloor());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestStop = req.getTargetFloor();
                }
            }
        }

        return nearestStop;
    }

    private boolean shouldStopAtFloor(int floor) {
        if (floor == currentFloor) return false;

        if (direction == Direction.UP) {
            return floor > currentFloor;
        } else if (direction == Direction.DOWN) {
            return floor < currentFloor;
        }

        return true;
    }
    public boolean willStopAtFloor(int floor) {
        lock.lock();
        try {
            if (floor == currentFloor) return true;

            for (Request req : activeRequests) {
                if (req.getCallFloor() == floor || req.getTargetFloor() == floor) {
                    return true;
                }
            }

            if (direction == Direction.UP && floor > currentFloor){
                return true;
            }
            if (direction == Direction.DOWN && floor < currentFloor){
                return true;
            }

            return false;
        } finally {
            lock.unlock();
        }
    }

    private void moveToFloorWithPickups(int targetFloor) throws InterruptedException {
        Direction moveDirection = (targetFloor > currentFloor) ? Direction.UP : Direction.DOWN;
        setElevatorState(ElevatorState.MOVING, moveDirection);

        while (currentFloor != targetFloor && !Thread.currentThread().isInterrupted()) {
            // Проверяем, нужно ли остановиться на промежуточном этаже
            checkForIntermediatePickups();

            try {
                Thread.sleep(FLOOR_TRAVEL_TIME);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            }

            lock.lock();
            try {
                currentFloor += (moveDirection == Direction.UP) ? 1 : -1;
            } finally {
                lock.unlock();
            }

            setElevatorState(ElevatorState.MOVING, moveDirection);
        }
    }

    private void checkForIntermediatePickups() {
        List<Request> toAdd = new ArrayList<>();
        lock.lock();
        try {
            // Проверяем запросы в очереди ожидания
            Iterator<Request> iterator = pendingRequests.iterator();
            while (iterator.hasNext()) {
                Request req = iterator.next();

                // Если запрос на текущем этаже и по текущему направлению
                if (req.getCallFloor() == currentFloor &&
                        (req.getDirection() == direction || direction == Direction.WAIT) &&
                        activeRequests.size() < BuildingConfig.ELEVATOR_CAPACITY) {

                    System.out.println("Лифт " + (elevatorId + 1) +
                            " подбирает пассажира на этаже " + (currentFloor + 1));

                    toAdd.add(req);
                    iterator.remove();
                }
            }
            if (!toAdd.isEmpty()) {
                activeRequests.addAll(toAdd);
                if (direction == Direction.WAIT && !toAdd.isEmpty()) {
                    direction = toAdd.get(0).getTargetFloor() > currentFloor ? Direction.UP : Direction.DOWN;
                }
            }

        } finally {
            lock.unlock();
        }
        //открытие дверей отдельно
        if (!toAdd.isEmpty()) {
            try {
                openDoors();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }

    private void processStopAtFloor(int floor) throws InterruptedException {
        setElevatorState(ElevatorState.STOPPED, Direction.WAIT);

        List<Request> completedRequests = new ArrayList<>();
        boolean passengerBoarded = false;

        lock.lock();
        try {
            // Находим запросы, которые нужно обработать на этом этаже
            for (Request req : activeRequests) {
                if (req.getTargetFloor() == floor) {
                    System.out.println("Лифт " + (elevatorId + 1) +
                            " высадил пассажира на этаже " + (floor + 1));
                    completedRequests.add(req);
                } else if (req.getCallFloor() == floor) {
                    System.out.println("Лифт " + (elevatorId + 1) +
                            " принял пассажира на этаже " + (floor + 1));
                    passengerBoarded = true;
                }
            }

            activeRequests.removeAll(completedRequests);
            direction = calculateOptimalDirection();
        } finally {
            lock.unlock();
        }

        // удаление пассажира с этажа GUI

        if (passengerBoarded && gui != null) {
            //invokeLater для потокобезопасности
            SwingUtilities.invokeLater(() -> {
                try {
                    ((com.elevator.gui.ElevatorGUI) gui).removePassengerFromFloor(floor);
                } catch (Exception e) {
                    System.err.println("Ошибка при удалении пассажира с этажа " + (floor + 1) + ": " + e.getMessage());
                }
            });
        }

        if (!completedRequests.isEmpty() || passengerBoarded || shouldOpenDoorsAtFloor(floor)) {
            openDoors();
        }
    }

    private Direction calculateOptimalDirection() {
        if (activeRequests.isEmpty()) {
            return Direction.WAIT;
        }

        // Проверяем, есть ли запросы выше текущего этажа
        boolean hasRequestsAbove = false;
        boolean hasRequestsBelow = false;

        for (Request req : activeRequests) {
            if (req.getCallFloor() > currentFloor || req.getTargetFloor() > currentFloor) {
                hasRequestsAbove = true;
            }
            if (req.getCallFloor() < currentFloor || req.getTargetFloor() < currentFloor) {
                hasRequestsBelow = true;
            }
        }

        if (direction == Direction.UP && hasRequestsAbove) {
            return Direction.UP;
        } else if (direction == Direction.DOWN && hasRequestsBelow) {
            return Direction.DOWN;
        } else if (hasRequestsAbove) {
            return Direction.UP;
        } else if (hasRequestsBelow) {
            return Direction.DOWN;
        }

        return Direction.WAIT;
    }

    private boolean shouldOpenDoorsAtFloor(int floor) {
        lock.lock();
        try {
            for (Request req : pendingRequests) {
                if (req.getCallFloor() == floor) {
                    return true;
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    // открытие дверей
    private void openDoors() throws InterruptedException {
        // состояние DOORS_OPEN
        setElevatorState(ElevatorState.DOORS_OPEN, Direction.WAIT);

        System.out.println("Лифт " + (elevatorId + 1) + " открыл двери на этаже " + (currentFloor + 1));
        Thread.sleep(DOOR_OPEN_TIME);
        System.out.println("Лифт " + (elevatorId + 1) + " закрыл двери на этаже " + (currentFloor + 1));
    }

    // потокобезопасно обновляет состояние и направление. @param newState - Новое состояние лифта.
    // @param newDirection Новое направление движения
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
        pendingRequests.offer(request);
        System.out.println("Лифт " + (elevatorId + 1) + " получил новый запрос. Очередь: " +
                (pendingRequests.size() + activeRequests.size()));
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
        lock.lock();
        try {
            return pendingRequests.size() + activeRequests.size();
        } finally {
            lock.unlock();
        }
    }

    public boolean isWAIT() {
        lock.lock();
        try {
            return pendingRequests.isEmpty() && activeRequests.isEmpty() && elevatorState == ElevatorState.STOPPED &&
                    direction == Direction.WAIT;
        } finally {
            lock.unlock();
        }
    }

    public int getActivePassengerCount() {
        lock.lock();
        try {
            return activeRequests.size();
        } finally {
            lock.unlock();
        }
    }

}

/*
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















}

 */