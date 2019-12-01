package tp_project.GUI;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.*;

import javax.swing.*;

public class MainWindow
{
    JFrame window;
    MainMenu menu;

    public MainWindow() {
        window = new JFrame("GoGame");
        window.setBounds(new Rectangle(100, 100, 800, 800));

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        menu = new MainMenu();
        menu.setActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
                switch ((MainMenu.Action) e.getSource())
                {
                    case EXIT:
                        System.exit(0);
                    break;

                    default:
                        System.out.println((MainMenu.Action) e.getSource());
                    break;
                }
			}
        });
    }

    public int exec() {
        showMainMenu();
        window.setVisible(true);

        return 0;
    }

    public void showMainMenu() {
        window.setSize(new Dimension(300, 500));
        menu.show(window.getContentPane());
    }
}