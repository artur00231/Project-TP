package tp_project.GUI;

import tp_project.GoGame.GoClient;
import tp_project.GoGameLogic.GoGame;
import tp_project.Network.ICommand;
import tp_project.Server.Client;
import tp_project.Server.ClientListener;
import tp_project.Server.GameServicesInfo;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;

class GoClientThread implements Runnable {
    GoClient go_client;
    boolean running = true;

    public GoClientThread (GoClient go_client) {
        this.go_client = go_client;
    }

    @Override
    public void run() {
        while (true) {
            if (!running)
                return;
            go_client.update();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public void stop() {
        running = false;
    }
}

public class ClientView {
    private GoClient go_client;
    private ActionListener action_listener;
    private Thread t;
    private GoClientThread go_client_thread;

    private ServerView server_view;
    private RoomView room_view;
    private GameView game_view;

    enum Action {
        ERROR,
        SET_CONTENT_PANE,
        RETURN;

        Object object;
    }

    private static Action createAction(Action a, Object o) {
        a.object = o;
        return a;
    }

    private void sendAction(Action a, Object o) {
        action_listener.actionPerformed(new ActionEvent(createAction(a, o), 0, ""));
    }

    public ClientView(ActionListener a) {
        action_listener = a;

        server_view = new ServerView(e -> {
            if (!(go_client != null && go_client.getPosition().equals(GoClient.POSITION.SERVER))) {
                sendAction(Action.ERROR, "Error");
            }
            GoClient.STATUS s;
            switch ((ServerView.Action) e.getSource()) {
                case RETURN:
                    action_listener.actionPerformed(new ActionEvent(createAction(Action.RETURN, null), 0, ""));
                    while (go_client.exit().equals(GoClient.STATUS.BUSY));
                    break;
                case CREATE:
                    do {
                        s = go_client.createGame();
                    } while (s.equals(GoClient.STATUS.BUSY));

                    if (s.equals(GoClient.STATUS.OK)) {
                        sendAction(Action.SET_CONTENT_PANE, room_view);
                    } else {
                        sendAction(Action.ERROR, "Error");
                    }
                    break;
                case JOIN:
                    do {
                        s = go_client.connect(e.getActionCommand());
                    } while (s.equals(GoClient.STATUS.BUSY));

                    if (s.equals(GoClient.STATUS.OK)) {
                        sendAction(Action.SET_CONTENT_PANE, room_view);
                    } else {
                        sendAction(Action.ERROR, "Error");
                    }
                    break;
                case REFRESH:
                    do {
                        s = go_client.getGameServicesInfo();
                    } while (s.equals(GoClient.STATUS.BUSY));

                    if (! s.equals(Client.STATUS.OK))
                        sendAction(Action.ERROR, "Error");
                    break;
            }
        });

        room_view = new RoomView("p1");
        room_view.setActionListener(e -> {
            if (go_client != null && go_client.getPosition().equals(GoClient.POSITION.GAMESERVICE)) {
                sendAction(Action.ERROR, "Error");
            }
            GoClient.STATUS s;
            switch ((RoomView.Action) e.getSource()) {
                case KICK:
                    do {
                        s = go_client.kick(e.getActionCommand());
                    } while (s.equals(Client.STATUS.BUSY));

                    if (! s.equals(Client.STATUS.OK))
                        sendAction(Action.ERROR, "Error");
                    break;
                case LEAVE:
                    do {
                        s = go_client.exit();
                    } while (s.equals(Client.STATUS.BUSY));
                    if (s.equals(Client.STATUS.OK)) {
                        sendAction(Action.SET_CONTENT_PANE, server_view);
                    } else {
                        sendAction(Action.ERROR, "Error");
                    }
                    break;
                case READY:
                    do {
                        s = go_client.setReady(true);
                    } while (s.equals(Client.STATUS.BUSY));
                    if (!s.equals(Client.STATUS.OK)) {
                        sendAction(Action.ERROR, "Error");
                    }
                    break;
                case NOT_READY:
                    do {
                        s = go_client.setReady(false);
                    } while (s.equals(Client.STATUS.BUSY));
                    if (!s.equals(Client.STATUS.OK)) {
                        sendAction(Action.ERROR, "Error");
                    }
                    break;
                case ADD_BOT:
                    //TODO
                    break;
                case SWITCH_COLORS:
                    do {
                        s = go_client.flipColours();
                    } while (s.equals(Client.STATUS.BUSY));
                    if (!s.equals(Client.STATUS.OK)) {
                        sendAction(Action.ERROR, "Error");
                    }
                    break;
            }
        });

        game_view = new GameView(19, GoGame.Player.BLACK);
    }

    public void connect(String IP, String port, String name) {
        Optional<GoClient> optional = GoClient.create(IP, Integer.parseInt(port), name);
        if (optional.isPresent() && (go_client = optional.get()) != null) {
            go_client.setClientListener(new ClientListener() {
                @Override
                public void updated() {

                }

                @Override
                public void positionChanged() {

                }

                @Override
                public void recived(ICommand command, String request) {
                    if (command instanceof GameServicesInfo) {
                        server_view.setRooms((GameServicesInfo) command);
                    }
                }

                @Override
                public void error(String request) {
                    sendAction(Action.ERROR, request);
                }
            });

            go_client_thread = new GoClientThread(go_client);
            t = new Thread(go_client_thread);
            t.start();

            while (go_client.getGameServicesInfo().equals(GoClient.STATUS.BUSY));
            sendAction(Action.SET_CONTENT_PANE, server_view);
        } else
        sendAction(Action.ERROR, "Connect Error");
    }

    public void reset() {
        go_client_thread.stop();
        try {
            t.join();
        }
        catch (Exception e) {};
    }
}
