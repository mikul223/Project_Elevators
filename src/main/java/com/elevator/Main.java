//git push origin

package com.elevator;


import com.elevator.gui.ElevatorGUI;
import com.elevator.core.Building;
import com.elevator.core.Simulation;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            //GUI
            ElevatorGUI gui = new ElevatorGUI();
            gui.setVisible(true);

            //создание здания, диспечера
            Building building = new Building(gui);

            //симуляция
            Simulation simulation = new Simulation(building);
            simulation.start();

            System.out.println("Программа работает");
        });
    }
}