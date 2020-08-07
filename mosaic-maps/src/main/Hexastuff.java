package main;

import java.util.Locale;
import javax.swing.SwingUtilities;

import gui.MainGUI;
import parameter.ParameterManager;

public class Hexastuff {

    public static void main(final String[] args) {
        System.out.println("start");
        Locale.setDefault(new Locale("en", "US"));
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainGUI gui = new MainGUI();
                if (args.length == 0) {
                    gui.run(false);
                } else {
                    System.out.println("1");
                    ParameterManager.parseArguments(args);
                    gui.run(true);
                }
            }
        });
    }
}
