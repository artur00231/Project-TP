package tp_project.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class RoomItem extends JPanel {
    RoomItem(String name, int players) {
        this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        JButton join_button = new JButton("Join");
        join_button.setEnabled(players != 2);
        join_button.setSize(50, 20);

        JPanel room_info = new JPanel();
        room_info.setLayout(new GridLayout(0, 2, 5, 5));
        room_info.add(new JLabel("ID:", JLabel.TRAILING));
        room_info.add(new JLabel(name));
        room_info.add(new JLabel("Players:", JLabel.TRAILING));
        room_info.add(new JLabel(Integer.toString(players) + "/2"));

        this.add(room_info);
        this.add(Box.createHorizontalGlue());
        this.add(join_button);
    }

}

class RoomList extends JPanel {
    public RoomList() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.add(new RoomItem("r1", 1));
        this.add(Box.createRigidArea(new Dimension(0, 20)));
        this.add(new RoomItem("r2", 2));
    }
}

public class ServerView extends JPanel {
    public enum Action {
        RETURN
    };

    private ActionListener action_listener;
    private JButton return_button = new JButton("Return");

    public ServerView(ActionListener a) {
        action_listener = a;

        return_button.addActionListener(e -> {
            action_listener.actionPerformed(new ActionEvent(Action.RETURN, 0, ""));
        });

        this.setLayout(new BorderLayout());
        this.padd
        this.add(new RoomList(), BorderLayout.CENTER);
        this.add(return_button, BorderLayout.PAGE_END);
    }
}
