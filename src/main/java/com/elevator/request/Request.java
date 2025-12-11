package com.elevator.request;
import com.elevator.elevator.Direction;

//запросы пассажиров

public class Request {

    // константы
    private final int callFloor; //на каком этаже вызывается лифт
    private final Direction direction; //up down
    private final int targetFloor; // на какой этаж хочет
    private final long timestamp; // время вызова

    public Request(int callFloor, Direction direction, int targetFloor) {
        this.callFloor = callFloor;
        this.direction = direction;
        this.targetFloor = targetFloor;
        this.timestamp = System.currentTimeMillis();
    }

    //геттеры

    //этаж вызова
    public int getCallFloor() {
        return callFloor;
    }

    //up down
    public Direction getDirection() {
        return direction;
    }

    //куда едет
    public int getTargetFloor() {
        return targetFloor;
    }

    // время
    public long getTimestamp() {
        return timestamp;
    }


    @Override
    public String toString() {
        return String.format("Request{callFloor=%d->%d, direction=%s, age=%dms}",
                callFloor + 1, targetFloor + 1, direction,
                System.currentTimeMillis() - timestamp);
    }
}