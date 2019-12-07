package tp_project.GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class RoomView extends JPanel {
    enum Action {
        KICK,
        READY,
        NOT_READY,
        LEAVE,
        ADD_BOT
    }

    private ActionListener action_listener;

    private RoomInfo room_info;
    private String player_id;

    private JLabel room_id_label = new JLabel();
    private PlayerList player_list = new PlayerList();
    private ControlPanel control_panel = new ControlPanel();
    private JButton add_bot_button = new JButton("Add Bot");
    private JButton leave_button = new JButton("Leave");
    private JCheckBox ready_check_box = new JCheckBox("Ready");

    public RoomView(String player_id) {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.player_id = player_id;
        this.add(room_id_label);
        this.add(new JScrollPane(player_list));
        this.add(control_panel);

        leave_button.addActionListener(e -> {action_listener.actionPerformed(new ActionEvent(Action.LEAVE, 0 ,""));});
        add_bot_button.addActionListener(e -> action_listener.actionPerformed(new ActionEvent(Action.ADD_BOT, 0, "")));
        ready_check_box.addActionListener(e -> {
            action_listener.actionPerformed(new ActionEvent(((JCheckBox) e.getSource()).isSelected() ? Action.READY : Action.NOT_READY, 0, ""));
        });
    }

    public void setRoomInfo(RoomInfo room_info) {
        this.room_info = room_info;
        room_id_label.setText(room_info.id);
        player_list.set();
        control_panel.set();
    }

    void setActionListener(ActionListener a) {
        this.action_listener = a;
    }

    public static class RoomInfo {
        String id;
        List<Player> players;
        boolean host;

        public RoomInfo(String id, List<Player> players, boolean host) {
            this.id = id;
            this.players = players;
            this.host = host;
        }
    }

    public static class Player {
        String id;
        boolean is_ready;

        public Player(String id, boolean is_ready) {
            this.id = id;
            this.is_ready = is_ready;
        }
    }

    private class PlayerListItem extends JPanel {
        public PlayerListItem(Player p) {
            this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            this.setBorder(BorderFactory.createCompoundBorder(
                    new EmptyBorder(5, 5, 5, 5), BorderFactory.createCompoundBorder(
                            new LineBorder(Color.BLACK, 1),
                            new EmptyBorder(5, 5, 5, 5)
                    )
            ));
            NameLabel player_label = new NameLabel(p.id);
            this.add(player_label);
            this.add(Box.createRigidArea(new Dimension(10, 0)));
            JLabel l = new JLabel(p.is_ready ? "Ready" : "Not Ready");
            l.setForeground(p.is_ready ? Color.GREEN : Color.RED);
            this.add(l);
            this.add(Box.createGlue());
            if (room_info.host && p.id != player_id) {
                JButton kick_button = new JButton("Kick");
                kick_button.addActionListener(e -> {
                    action_listener.actionPerformed(new ActionEvent(Action.KICK, 0, p.id));
                });
                this.add(Box.createRigidArea(new Dimension(5, 0)));
                this.add(kick_button);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width, 50);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(super.getMaximumSize().width, getPreferredSize().height);
        }
    }

    private class PlayerList extends JPanel {
        public PlayerList() {
            this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        }
        public void set() {
            removeAll();
            for (Player p : room_info.players) {
                this.add(new PlayerListItem(p));
            }
        }
    }

    private class ControlPanel extends JPanel {
        public ControlPanel() {
            this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        }
        public void set() {
            this.removeAll();
            if (room_info.host) {
                this.add(add_bot_button);
            }
            this.add(ready_check_box);
            this.add(leave_button);
        }
    }

    private class NameLabel extends JLabel {
        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(100, d.height);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        public NameLabel(String s) {
            super(s);
        }
    }
}
