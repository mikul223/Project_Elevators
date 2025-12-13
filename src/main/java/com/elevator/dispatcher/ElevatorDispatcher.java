package com.elevator.dispatcher;

import com.elevator.elevator.Elevator;
import com.elevator.elevator.Direction;
import com.elevator.elevator.ElevatorState;
import com.elevator.request.Request;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ElevatorDispatcher {
    private final List<Elevator> elevators;


    public ElevatorDispatcher(List<Elevator> elevators) {
        //потокобезопасная копия списка лифтов
        this.elevators = new CopyOnWriteArrayList<>(elevators);
    }

    public synchronized void handleRequest(Request request) {
        Elevator bestElevator = findBestElevator(request);

        if (bestElevator != null) {
            System.out.println("Запрос: этаж " + (request.getCallFloor() + 1) +
                    " -> " + (request.getTargetFloor() + 1) +
                    " назначен лифту " + (bestElevator.getElevatorId() + 1));

            bestElevator.addRequest(request);
        }
    }

    private Elevator findBestElevator(Request request) {
        Elevator bestElevator = null;
        int bestScore = Integer.MAX_VALUE;

        for (Elevator elevator : elevators) {
            int score = calculateScore(elevator, request);

            if (score < bestScore) {
                bestScore = score;
                bestElevator = elevator;
            }
        }

        return bestElevator;
    }


    // логика - стоимость пути лифта. меньше стоимость - лифт подходит лучше.
    // расстояние до этажа, состояние лифта, направление движения, совпадение направлений

    private int calculateScore(Elevator elevator, Request request) {
        // текущие параметры лифта
        int currentFloor = elevator.getCurrentFloor();
        int callFloor = request.getCallFloor();
        Direction direction = elevator.getDirection();
        ElevatorState state = elevator.getElevatorState();

        // дальше лифт - выше стоимость
        int distance = Math.abs(currentFloor - callFloor);
        int score = distance * 10;

        // свободные лифты дешевле
        if (state == ElevatorState.STOPPED && direction == Direction.WAIT) {
            score -= 50; // Большой бонус (-50 к стоимости)
        }

        //лифт уже едет в том направлении, куда хочет пассажир, который ждет лифт
        if (direction == request.getDirection()) {
            if ((direction == Direction.UP && currentFloor <= callFloor) ||
                    (direction == Direction.DOWN && currentFloor >= callFloor)) {
                score -= 30;
            }
        }

        // лифт уже едет НЕ в том направлении, куда хочет пассажир, который ждет лифт
        if (direction != Direction.WAIT && direction != request.getDirection()) {
            score += 100;
        }

        // идеальный вариант - лифт там где и надо с открытыми дверями
        if (state == ElevatorState.DOORS_OPEN && currentFloor == callFloor) {
            score -= 1000;
        }

        return score;
    }
}