package tp_project.GUI;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class MainMenu extends JPanel {
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
        EXIT, SERVER, PLAY, tmp1, tmp2, tmp3
    };

    private enum Menu {
        MainMenu,
        ConnectMenu,
        StartServerMenu
    }

    public MainMenu(Container parent) {
        createMenu();
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
        play_button.addActionListener(e -> {
            setMenu(Menu.ConnectMenu);
        });
        play_button.setFocusable(false);

        JButton server_button = new JButton("START SERVER");
        server_button.addActionListener(e -> {
            setMenu(Menu.StartServerMenu);
        });
        server_button.setFocusable(false);

        plane.add(play_button);
        plane.add(server_button);

        //TODO Delete
        JButton btn1 = new JButton("ServerView preview");
        btn1.addActionListener(e -> option_selected.actionPerformed(new ActionEvent(Action.tmp1, 0, "")));
        plane.add(btn1);
        JButton btn2 = new JButton("RoomView preview");
        btn2.addActionListener(e -> option_selected.actionPerformed(new ActionEvent(Action.tmp2, 0, "")));
        plane.add(btn2);
        JButton btn3 = new JButton("host RoomView preview");
        btn3.addActionListener(e -> option_selected.actionPerformed(new ActionEvent(Action.tmp3, 0, "")));
        plane.add(btn3);
        //

        JButton exit1_button = new JButton("Exit");
        exit1_button.addActionListener(e -> {
            option_selected.actionPerformed(new ActionEvent(Action.EXIT, 0, null));
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
        JButton connect_button = new JButton("Connect");

        connect_button.addActionListener(e -> {
            if (isValidIP(connect_ip.getText()) && isValidPort(connect_port.getText()))
                option_selected.actionPerformed(new ActionEvent(Action.PLAY, 0, null));
            else
                JOptionPane.showMessageDialog(this, "Invalid input");
        });
        connect_button.setFocusable(false);

        return_button.addActionListener(e -> {
            setMenu(Menu.MainMenu);
            connect_ip.setText("");
            connect_port.setText("");
        });
        return_button.setFocusable(false);

        connect_menu.add(connect_button, BorderLayout.PAGE_START);
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
        return_button.addActionListener(e -> {
            server_port.setText("");
            setMenu(Menu.MainMenu);
        });
        return_button.setFocusable(false);

        JButton start_button = new JButton("Start Server");
        start_button.addActionListener(e -> {
            if (isValidPort(server_port.getText()))
                option_selected.actionPerformed(new ActionEvent(Action.SERVER, 0, null));
            else
                JOptionPane.showMessageDialog(this, "Invalid port");
        });
        start_button.setFocusable(false);

        connect_menu.add(start_button, BorderLayout.PAGE_START);
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
        removeAll();

        setLayout(new BorderLayout());
        add(main_menu_panel[index], BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    static boolean isValidIP(String ip) {
        String[] l = ip.split("\\.");
        if (l.length != 4) return false;
        for (String v : l) {
            try {
                int i = Integer.parseInt(v);
                if (i < 0 || i > 255)
                    return false;
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    static boolean isValidPort(String port) {
        try {
            int i = Integer.parseInt(port);
                if (i < 0 || i > (1 << 16) - 1)
                    return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    static int parseIP(String ip) {
        int a = 0;
        for (String v : ip.split("\\.")) {
            a *= 256;
            a += Integer.parseInt(v);
        }
        return a;
    }
}