package tp_project.GUI;

import tp_project.Server.GameServiceInfo;
import tp_project.Server.GameServicesInfo;
import tp_project.Server.Server;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ServerView extends JPanel {
    public enum Action {
        RETURN,
        CREATE,
        JOIN,
        REFRESH,
        PACK
    }

    private ActionListener action_listener;
    private JButton return_button = new JButton("Return");
    private JButton refresh_button = new JButton("Refresh");
    private JButton create_button = new JButton("Create");
    private Dimension dimension = new Dimension(400, 500);
    private RoomList room_list = new RoomList();
    private JPanel control_panel = new JPanel();
    private JDialog create_room_dialog = new RoomCreate(this);

    public ServerView(ActionListener a) {
        setSize(dimension);
        setMaximumSize(dimension);
        setMinimumSize(dimension);
        setPreferredSize(dimension);
        action_listener = a;

        return_button.addActionListener(e -> {
            action_listener.actionPerformed(new ActionEvent(Action.RETURN, 0, ""));
        });

        refresh_button.addActionListener((e) -> {action_listener.actionPerformed(new ActionEvent(Action.REFRESH, 0, ""));});
        create_button.addActionListener((e) -> {
            create_room_dialog.setVisible(true);
        });

        control_panel.setLayout(new BoxLayout(control_panel, BoxLayout.LINE_AXIS));
        control_panel.add(refresh_button);
        control_panel.add(create_button);
        control_panel.add(Box.createGlue());
        control_panel.add(return_button);
        control_panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        this.setLayout(new BorderLayout());
        this.add(new JScrollPane(room_list), BorderLayout.CENTER);
        this.add(control_panel, BorderLayout.PAGE_END);
    }

    public void setRooms(GameServicesInfo info) {
        room_list.set(info.game_services);
    }

    private class RoomCreate extends JDialog {
        JComboBox values = new JComboBox(new Integer[] {9, 13, 19});
        RoomCreate(JPanel owner) {
            super((JFrame) SwingUtilities.getWindowAncestor(owner), true);
            this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
            JPanel p1 = new JPanel();
            p1.add(new Label("Select size: "));
            p1.add(values);
            JPanel p2 = new JPanel();
            JButton b1 = new JButton("Create");
            b1.addActionListener((e) -> {
                action_listener.actionPerformed(new ActionEvent(Action.CREATE, (Integer) values.getSelectedItem(), ""));
                this.setVisible(false);
            });
            p2.add(b1);
            JButton b2 = new JButton("Cancel");
            b2.addActionListener((e) -> this.setVisible(false));
            p2.add(b2);
            this.add(p1);
            this.add(p2);
            this.pack();
        }
    }

    private class RoomItem extends JPanel {
        JPanel room_info = new JPanel();

        RoomItem(String host_name, String room_id, int players, int max_players) {
            this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

            JButton join_button = new JButton("Join");
            join_button.addActionListener(e -> {
                action_listener.actionPerformed(new ActionEvent(Action.JOIN, 0, room_id));
            });
            join_button.setEnabled(players < max_players);

            room_info.setLayout(new GridLayout(0, 2, 2, 5));
            room_info.add(new JLabel("Host name:", JLabel.TRAILING));
            room_info.add(new JLabel(host_name));
            room_info.add(new JLabel("Players:", JLabel.TRAILING));
            room_info.add(new JLabel(players + "/" + max_players));

            this.add(room_info);
            this.add(join_button);

            this.setBorder(BorderFactory.createCompoundBorder(
                    new EmptyBorder(5, 5, 5, 5), BorderFactory.createCompoundBorder(
                            new LineBorder(Color.BLACK, 1),
                            new EmptyBorder(5, 5, 5, 5))));
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(super.getMaximumSize().width, getPreferredSize().height);
        }
    }

    private class RoomList extends JPanel {
        public RoomList() {
            this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        }

        synchronized public void set(List<GameServiceInfo> l) {
            this.removeAll();

            for (GameServiceInfo info : l) {
                String host_name = info.players.get(info.host_id);
                this.add(new RoomItem(host_name, info.ID, info.players.size(), info.max_players));
            }

            action_listener.actionPerformed(new ActionEvent(Action.PACK, 0, ""));
        }
    }
}
