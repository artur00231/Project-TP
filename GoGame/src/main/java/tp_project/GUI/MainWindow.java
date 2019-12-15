package tp_project.GUI;

import tp_project.GoGame.GoClient;
import tp_project.GoGameLogic.GoGameLogic;
import tp_project.Server.Server;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.*;
import java.util.Arrays;

import javax.swing.*;

public class MainWindow
{
    private JFrame window;
    private MainMenu menu;

    private ClientView client_view;

    Server server = new Server(5005);

    public MainWindow() {
        Thread t = new Thread(server);
        t.start();

        GoClient client = GoClient.create("127.0.0.1", 5005, "test").get();
        client.createGame();

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
                        client_view.connect(menu.getConnectIP(), menu.getConnectPort(), menu.getPlayerName());
                        break;
                    default:
                        System.out.println(e.getSource());
                    break;
                }
			}
        });

        client_view = new ClientView(e -> {
            ClientView.Action a = (ClientView.Action) e.getSource();
            switch (a) {
                case RETURN:
                    client_view.reset();
                    showMainMenu();
                    break;
                case ERROR:
                    JOptionPane.showMessageDialog(window, (String) a.object);
                    showMainMenu();
                    break;
                case DISCONNECTED:
                    System.out.println("disconnected");
                    client_view.reset();
                    JOptionPane.showMessageDialog(window, "Disconnected");
                    showMainMenu();
                    break;
                case SET_CONTENT_PANE:
                    window.setContentPane((JPanel)a.object);
                    break;
                case PACK:
                    window.pack();
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