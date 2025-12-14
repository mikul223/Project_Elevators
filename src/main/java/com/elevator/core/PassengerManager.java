package com.elevator.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PassengerManager {
    // этаж - количество пассажиров
    private final Map<Integer, Integer> floorPassengers = new ConcurrentHashMap<>();
    public PassengerManager() {
        for (int i = 0; i < com.elevator.config.BuildingConfig.TOTAL_FLOORS; i++) {
            floorPassengers.put(i, 0);
        }
    }

    // добавить пассажира на этаж
    public void addPassenger(int floor) {
        floorPassengers.merge(floor, 1, Integer::sum);
        System.out.println("Пассажир появился на этаже " + (floor + 1) +
                ". Теперь там: " + floorPassengers.get(floor) + " пассажиров");
    }

    // удалить пассажира с этажа
    public void removePassenger(int floor) {
        Integer current = floorPassengers.get(floor);
        if (current != null && current > 0) {
            floorPassengers.put(floor, current - 1);
            System.out.println("Пассажир уехал с этажа " + (floor + 1) +
                    ". Осталось: " + floorPassengers.get(floor));
        }
    }

    // количество пассажиров на этаже
    public int getPassengerCount(int floor) {
        return floorPassengers.getOrDefault(floor, 0);
    }

    // этажи с пассажирами
    public Set<Integer> getFloorsWithPassengers() {
        Set<Integer> floors = new HashSet<>();
        for (Map.Entry<Integer, Integer> entry : floorPassengers.entrySet()) {
            if (entry.getValue() > 0) {
                floors.add(entry.getKey());
            }
        }
        return floors;
    }

}
