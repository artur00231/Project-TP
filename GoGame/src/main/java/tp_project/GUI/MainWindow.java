package tp_project.GUI;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.*;

public class MainWindow
{
    JFrame window;
    MainMenu menu;
    ServerView server_view;
    RoomView room_view;
    GameView game_view;

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
                        break;
                    //TODO DELETE
                    case tmp1:
                        server_view.refresh();
                        window.setContentPane(server_view);
                        window.pack();
                        break;
                    case tmp2:
                        room_view.setRoomInfo(new RoomView.RoomInfo("roomID", Arrays.asList(new RoomView.Player[]{new RoomView.Player("p0", true), new RoomView.Player("p1", false)}), false));
                        window.setContentPane(room_view);
                        window.pack();
                        window.repaint();
                        break;
                    case tmp3:
                        room_view.setRoomInfo(new RoomView.RoomInfo("roomID", Arrays.asList(new RoomView.Player[]{new RoomView.Player("p1", false), new RoomView.Player("p2", false)}), true));
                        window.setContentPane(room_view);
                        window.pack();
                        break;
                    case tmp4:
                        window.setContentPane(game_view);
                        window.pack();
                    //
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

        room_view = new RoomView("p1");
        room_view.setActionListener(e -> {
            switch ((RoomView.Action) e.getSource()) {
                case LEAVE:
                    showMainMenu();
                    break;
            }
        });

        game_view = new GameView(19, GameView.PlayerColor.BLACK);
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