//git push origin

package com.elevator;


import com.elevator.gui.ElevatorGUI;
import com.elevator.core.Building;
import com.elevator.core.Simulation;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            try {
                //GUI
                ElevatorGUI gui = new ElevatorGUI();
                gui.setVisible(true);

                //создание здания, диспечера
                Building building = new Building(gui);
                //симуляция
                Simulation simulation = new Simulation(building);

                simulation.setGUI(gui);
                simulation.start();

                System.out.println("Программа работает");

                gui.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                        simulation.stop(); // Теперь simulation доступна
                        System.out.println("Система остановлена");
                    }
                });
            } catch (Exception e) {
                System.err.println("Ошибка запуска приложения: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}