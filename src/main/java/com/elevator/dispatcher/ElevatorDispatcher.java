package com.elevator.dispatcher;

import com.elevator.elevator.Elevator;
import com.elevator.elevator.Direction;

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


        try {
            Elevator bestElevator = findBestElevator(request);

            if (bestElevator != null) {
                System.out.println("Запрос: этаж " + (request.getCallFloor() + 1) +
                        " -> " + (request.getTargetFloor() + 1) +
                        " назначен лифту " + (bestElevator.getElevatorId() + 1));

                bestElevator.addRequest(request);
            } else {
                System.out.println("Нет доступных лифтов для запроса с этажа " + (request.getCallFloor() + 1));
            }
        } catch (Exception e) {
            System.err.println("Ошибка обработки запроса: " + e.getMessage());
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

        Direction direction = elevator.getDirection();
        int callFloor = request.getCallFloor();
        Direction requestedDirection = request.getDirection();

        // дальше лифт - выше стоимость
        int distance = Math.abs(currentFloor - callFloor);
        int score = distance * 10;

        if (direction == Direction.WAIT) {
            //свободные лифты дешевле
            score -= 100;
        } else if (direction == requestedDirection) {
            //лифт уже едет в том направлении, куда хочет пассажир, который ждет лифт
            if ((direction == Direction.UP && currentFloor <= callFloor) || (direction == Direction.DOWN && currentFloor >= callFloor)) {
                score -= 50;
            } else {
                // лифт проехал этаж
                score += 100;
            }
        } else {
            // лифт уже едет НЕ в том направлении, куда хочет пассажир, который ждет лифт
            score += 150;
        }

        // проверяем вместимость
        if (elevator.getActivePassengerCount() >= com.elevator.config.BuildingConfig.ELEVATOR_CAPACITY) {
            score += 200;
        }

        // проверяем, остановится ли лифт на этом этаже
        if (elevator.willStopAtFloor(callFloor)) {
            score -= 30;
        }

        return score;
    }
}