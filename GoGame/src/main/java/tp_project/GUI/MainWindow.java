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
    private ReplayClientView replay_client_view;

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
                        client_view.connect(menu.getConnectIP(), menu.getConnectPort(), menu.getPlayerName());
                        break;
                    case REPLAY:
                        replay_client_view.connect(menu.getConnectIP(), menu.getConnectPort(), menu.getPlayerName());
                        break;
                    case SERVER:
                        runServer(Integer.parseInt(e.getActionCommand()));
                    default:
                        System.out.println(e.getSource());
                    break;
                }
			}
        });

        client_view = new ClientView(e -> {
            ClientView.Action a = (ClientView.Action) e.getSource();
            switch (a) {
                case ERROR:
                    JOptionPane.showMessageDialog(window, (String) a.object);
                    showMainMenu();
                    break;
                case DISCONNECTED:
                    client_view.reset();
                    JOptionPane.showMessageDialog(window, "Disconnected");
                    showMainMenu();
                    break;
                case SET_CONTENT_PANE:
                    window.setContentPane((JPanel)a.object);
                    window.repaint();
                    window.revalidate();

                    ((JPanel)a.object).revalidate();
                    ((JPanel)a.object).repaint();
                    break;
                case PACK:
                    window.pack();
                    window.repaint();
                    window.revalidate();
                    break;
            }
        });

        replay_client_view = new ReplayClientView(e -> {
            ReplayClientView.Action a = (ReplayClientView.Action) e.getSource();
            switch (a) {
                case ERROR:
                    JOptionPane.showMessageDialog(window, (String) a.object);
                    showMainMenu();
                    break;
                case DISCONNECTED:
                    replay_client_view.reset();
                    JOptionPane.showMessageDialog(window, "Disconnected");
                    showMainMenu();
                    break;
                case SET_CONTENT_PANE:
                    window.setContentPane((JPanel)a.object);
                    window.repaint();
                    window.revalidate();

                    ((JPanel)a.object).revalidate();
                    ((JPanel)a.object).repaint();
                    break;
                case PACK:
                    window.pack();
                    window.repaint();
                    window.revalidate();
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

    private void runServer(int port) {
        Server server = new Server(port);
        if (server.isValid()) {
            Thread t = new Thread(server);
            t.start();

            JPanel panel = new JPanel();
            JButton button = new JButton("Stop");
            panel.add(new JLabel("Server is running on port: " + port));
            button.addActionListener(e -> {
                server.kill();
                try {
                    t.join();
                } catch (Exception ex) {System.exit(-1);}
                showMainMenu();
            });
            panel.add(button);
            window.setContentPane(panel);
            window.pack();
        }
        else JOptionPane.showMessageDialog(window, "Failed to start server");
    }
}