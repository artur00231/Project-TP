package tp_project.app;

import tp_project.GUI.MainWindow;

import javax.swing.*;

public class App 
{
    public static void main( String[] args )
    {
        MainWindow main_window = new MainWindow();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                main_window.exec();
            }
        });
    }
}