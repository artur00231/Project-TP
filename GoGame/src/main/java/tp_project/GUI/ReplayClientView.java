package tp_project.GUI;

import tp_project.GoReplay.GoReplayClient;
import tp_project.Network.ICommand;
import tp_project.Server.Client;
import tp_project.Server.ClientListener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;

public class ReplayClientView {
    enum Action {
        ERROR, SET_CONTENT_PANE, DISCONNECTED, PACK;
        Object object;
    }

    ActionListener action_listener;
    GoReplayClient replay_client;
    private Timer update_timer;

    ReplayPicker replay_picker;

    private static ReplayClientView.Action createAction(ReplayClientView.Action a, Object o) {
        a.object = o;
        return a;
    }

    private void sendAction(ReplayClientView.Action a, Object o) {
        action_listener.actionPerformed(new ActionEvent(createAction(a, o), 0, ""));
    }

    public ReplayClientView(ActionListener a) {
        action_listener = a;

        update_timer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                replay_client.update();

                if (replay_client.getPosition() == Client.POSITION.DISCONNECTED) {
                    sendAction(ReplayClientView.Action.DISCONNECTED, null);
                }
            }
        });
        update_timer.setRepeats(true);

        replay_picker = new ReplayPicker(e -> {

        });
    }

    public void connect(String IP, String port, String name) {
        Optional<GoReplayClient> optional = GoReplayClient.create(IP, Integer.parseInt(port), name);
        if (optional.isPresent() && (replay_client = optional.get()) != null) {
            replay_client.setClientListener(new ClientListener() {
                @Override
                public void updated() {

                }

                @Override
                public void positionChanged() {
                    if (replay_client.getPosition() == Client.POSITION.GAMESERVICE) {
                        sendAction(Action.SET_CONTENT_PANE, replay_picker);
                    }
                }

                @Override
                public void recived(ICommand command, String request) {

                }

                @Override
                public void error(String request) {
                    sendAction(Action.ERROR, request);
                }
            });

            update_timer.start();

            replay_client.createGame();
        } else sendAction(Action.ERROR, "Connect Error");
    }
}
