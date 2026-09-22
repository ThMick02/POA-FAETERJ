package view;

import javax.swing.SwingUtilities;

public class Programa {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Janela());
    }
}