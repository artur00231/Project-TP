package tp_project.GUI;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class MainMenu {
    private Container parent_component;
    private JPanel main_menu_panel[] = { null, null };
    private ActionListener option_selected = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
    };

    public enum Action {
        EXIT, SINGLE, MUL_H, MUL_C
    };

    public MainMenu() {
        createMenu();
    }

    public void show(Container container) {
        parent_component = container;
        setMenu(0);
    }

    public void setActionListener(ActionListener action_lstener) {
        option_selected = action_lstener;
    }

    private void createMenu() {
        main_menu_panel[0] = new JPanel();
        main_menu_panel[0].setLayout(new BorderLayout());
        JPanel plane1 = new JPanel();
        plane1.setLayout(new GridLayout(0, 1));
        JButton single_player_button = new JButton("Singleplayer");
        single_player_button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                option_selected.actionPerformed(new ActionEvent(Action.SINGLE, 0, null));
            }

        });
        single_player_button.setFocusable(false);
        JButton multi_player_button = new JButton("Multiplayer");
        multi_player_button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setMenu(1);
            }

        });
        multi_player_button.setFocusable(false);
        plane1.add(single_player_button);
        plane1.add(multi_player_button);
        JButton exit1_button = new JButton("Exit");
        exit1_button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                option_selected.actionPerformed(new ActionEvent(Action.EXIT, 0, null));
            }

        });
        exit1_button.setFocusable(false);
        main_menu_panel[0].add(plane1, BorderLayout.CENTER);
        main_menu_panel[0].add(exit1_button, BorderLayout.PAGE_END);

        //----------------------------------------

        main_menu_panel[1] = new JPanel();
        main_menu_panel[1].setLayout(new BorderLayout());
        JPanel plane2 = new JPanel();
        plane2.setLayout(new GridLayout(0, 1));
        JButton host_player_button = new JButton("Create game");
        host_player_button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                option_selected.actionPerformed(new ActionEvent(Action.MUL_H, 0, null));
            }

        });
        host_player_button.setFocusable(false);
        JButton client_player_button = new JButton("Connect");
        client_player_button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                option_selected.actionPerformed(new ActionEvent(Action.MUL_C, 0, null));
            }

        });
        client_player_button.setFocusable(false);
        plane2.add(host_player_button);
        plane2.add(client_player_button);
        JButton exit2_button = new JButton("Return");
        exit2_button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setMenu(0);
            }

        });
        exit2_button.setFocusable(false);
        main_menu_panel[1].add(plane2, BorderLayout.CENTER);
        main_menu_panel[1].add(exit2_button, BorderLayout.PAGE_END);
    }

    private void setMenu(int index)
    {
        if (index < 0 || index > 1) return;
        parent_component.removeAll();

        parent_component.setLayout(new BorderLayout());
        parent_component.add(main_menu_panel[index], BorderLayout.CENTER);

        parent_component.revalidate();
        parent_component.repaint();
    }
}