//git push origin

package com.elevator;
import com.elevator.gui.ElevatorGUI;

public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            ElevatorGUI gui = new ElevatorGUI();
            gui.setVisible(true);
            System.out.println("Программа работает");
        });
    }
}