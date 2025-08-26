package br.com.dio.ui.custom.button;

import javax.swing.*;
import java.awt.event.ActionListener;

public class TipButton extends JButton {

    public TipButton(final ActionListener actionListener){
        this.setText("Dicas");
        this.addActionListener(actionListener);
    }
}
