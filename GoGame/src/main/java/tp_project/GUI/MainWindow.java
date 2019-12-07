package tp_project.GUI;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.*;

import javax.swing.*;

public class MainWindow
{
    JFrame window;
    MainMenu menu;
    ServerView server_view;

    public MainWindow() {
        window = new JFrame("GoGame");
        window.setBounds(new Rectangle(100, 100, 800, 800));

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        menu = new MainMenu(window.getContentPane());
        menu.setActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
                switch ((MainMenu.Action) e.getSource())
                {
                    case EXIT:
                        System.exit(0);
                    break;
                    case PLAY:
                        window.setContentPane(server_view);
                        server_view.refresh();
                        window.pack();

                    default:
                        System.out.println((MainMenu.Action) e.getSource());
                    break;
                }
			}
        });

        server_view = new ServerView(e -> {
            switch ((ServerView.Action) e.getSource()) {
                case RETURN:
                    showMainMenu();
                    break;
                case CREATE:
                    System.out.println("Create " + e.getID());
                    break;
                case JOIN:
                    System.out.println("Join " + e.getActionCommand());
                    break;
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
        window.setContentPane(menu);
    }
}