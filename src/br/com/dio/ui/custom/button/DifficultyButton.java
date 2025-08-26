package br.com.dio.ui.custom.button;

import javax.swing.*;
import java.awt.event.ActionListener;

public class DifficultyButton extends JButton {

    public DifficultyButton(final ActionListener actionListener){
        this.setText("Dificuldade.");
        this.addActionListener(actionListener);
    }
}
