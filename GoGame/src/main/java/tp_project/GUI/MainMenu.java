package tp_project.GUI;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class MainMenu {
    private Container parent_component;
    private JPanel[] main_menu_panel = new JPanel[3];

    // Connect menu
    JTextField connect_ip;
    JTextField connect_port;

    // Start Server Menu
    JTextField server_port;

    private ActionListener option_selected = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
    };

    public enum Action {
        EXIT, SERVER, PLAY
    };

    private enum Menu {
        MainMenu,
        ConnectMenu,
        StartServerMenu
    }

    public MainMenu() {
        createMenu();
    }

    public void show(Container container) {
        parent_component = container;
        setMenu(Menu.MainMenu);
    }

    public void setActionListener(ActionListener action_listener) {
        option_selected = action_listener;
    }

    private JPanel createMainMenu() {
        JPanel main_menu = new JPanel();
        main_menu.setLayout(new BorderLayout());
        JPanel plane = new JPanel();
        plane.setLayout(new GridLayout(0, 1));

        JButton play_button = new JButton("PLAY");
        play_button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setMenu(Menu.ConnectMenu);
                option_selected.actionPerformed(new ActionEvent(Action.PLAY, 0, null));
            }

        });
        play_button.setFocusable(false);

        JButton server_button = new JButton("START SERVER");
        server_button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setMenu(Menu.StartServerMenu);
            }

        });
        server_button.setFocusable(false);

        plane.add(play_button);
        plane.add(server_button);

        JButton exit1_button = new JButton("Exit");
        exit1_button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                option_selected.actionPerformed(new ActionEvent(Action.EXIT, 0, null));
            }

        });
        exit1_button.setFocusable(false);

        main_menu.add(plane, BorderLayout.CENTER);
        main_menu.add(exit1_button, BorderLayout.PAGE_END);
        return main_menu;
    }

    private JPanel createConnectMenu() {
        JPanel connect_menu = new JPanel();
        connect_menu.setLayout(new BorderLayout());

        JPanel plane = new JPanel();
        plane.setLayout(new GridLayout(0, 1));

        connect_ip = new JTextField();
        connect_port = new JTextField();

        plane.add(new JLabel("IP"));
        plane.add(connect_ip);
        plane.add(new JLabel("Port"));
        plane.add(connect_port);

        JButton return_button = new JButton("Return");

        return_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setMenu(Menu.MainMenu);
                connect_ip.setText("");
                connect_port.setText("");
            }

        });
        return_button.setFocusable(false);
        connect_menu.add(new JButton("connect"), BorderLayout.PAGE_START);
        connect_menu.add(plane, BorderLayout.CENTER);
        connect_menu.add(return_button, BorderLayout.PAGE_END);
        return connect_menu;
    }

    private JPanel createStartServerMenu() {
        final JPanel connect_menu = new JPanel();
        connect_menu.setLayout(new BorderLayout());

        JPanel plane = new JPanel();
        plane.setLayout(new GridLayout(0, 1));

        server_port = new JTextField();
        plane.add(new JLabel("Port"));
        plane.add(server_port);

        JButton return_button = new JButton("Return");
        return_button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                server_port.setText("");
                setMenu(Menu.MainMenu);
            }

        });
        return_button.setFocusable(false);
        connect_menu.add(new JButton("connect"), BorderLayout.PAGE_START);
        connect_menu.add(plane, BorderLayout.CENTER);
        connect_menu.add(return_button, BorderLayout.PAGE_END);
        return connect_menu;
    }



    private void createMenu() {
        main_menu_panel[Menu.MainMenu.ordinal()] = createMainMenu();
        main_menu_panel[Menu.ConnectMenu.ordinal()] = createConnectMenu();
        main_menu_panel[Menu.StartServerMenu.ordinal()] = createStartServerMenu();
    }

    private void setMenu(Menu m)
    {
        int index = m.ordinal();
        if (index < 0 || index >= Menu.values().length) return;
        parent_component.removeAll();

        parent_component.setLayout(new BorderLayout());
        parent_component.add(main_menu_panel[index], BorderLayout.CENTER);

        parent_component.revalidate();
        parent_component.repaint();
    }
}