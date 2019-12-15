package tp_project.GUI;

import tp_project.GoGame.GoClient;
import tp_project.GoGame.GoGameServiceInfo;
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
        RETURN,
        DISCONNECTED,
        PACK;

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
            if (go_client == null) {
                sendAction(Action.ERROR, "Error");
                return;
            }
            if(!go_client.getPosition().equals(GoClient.POSITION.SERVER)) {
                return;
            }
            GoClient.STATUS s;
            switch ((ServerView.Action) e.getSource()) {
                case RETURN:
                    //action_listener.actionPerformed(new ActionEvent(createAction(Action.RETURN, null), 0, ""));
                    while (go_client.exit().equals(GoClient.STATUS.BUSY));
                    break;
                case CREATE:
                    do {
                        s = go_client.createGame();
                    } while (s.equals(GoClient.STATUS.BUSY));
                    break;
                case JOIN:
                    do {
                        s = go_client.connect(e.getActionCommand());
                    } while (s.equals(GoClient.STATUS.BUSY));
                    break;
                case REFRESH:
                    do {
                        s = go_client.getGameServicesInfo();
                    } while (s.equals(GoClient.STATUS.BUSY));
                    break;
                case PACK:
                    action_listener.actionPerformed(new ActionEvent(Action.PACK, 0, ""));
                    break;
            }
        });

        room_view = new RoomView();
        room_view.setActionListener(e -> {
            if (go_client == null) {
                sendAction(Action.ERROR, "Error");
                return;
            }
            if (!go_client.getPosition().equals(Client.POSITION.GAMESERVICE)) {
                return;
            }
            GoClient.STATUS s;
            switch ((RoomView.Action) e.getSource()) {
                case KICK:
                    do {
                        s = go_client.kick(e.getActionCommand());
                    } while (s.equals(Client.STATUS.BUSY));
                    break;
                case LEAVE:
                    do {
                        s = go_client.exit();
                    } while (s.equals(Client.STATUS.BUSY));
                    break;
                case READY:
                    do {
                        s = go_client.setReady(true);
                    } while (s.equals(Client.STATUS.BUSY));
                    break;
                case NOT_READY:
                    do {
                        s = go_client.setReady(false);
                    } while (s.equals(Client.STATUS.BUSY));
                    break;
                case ADD_BOT:
                    //TODO
                    break;
                case SWITCH_COLORS:
                    do {
                        s = go_client.flipColours();
                    } while (s.equals(Client.STATUS.BUSY));
                    break;
                case PACK:
                    action_listener.actionPerformed(new ActionEvent(Action.PACK, 0, ""));
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
                    System.out.println("updated");
                    switch (go_client.getPosition()) {
                        case SERVER:
                            while(go_client.getGameServicesInfo().equals(Client.STATUS.BUSY));
                            break;
                        case GAMESERVICE:
                            while(go_client.getGoGameServiceInfo().equals(Client.STATUS.BUSY));
                            break;
                    }
                }

                @Override
                public void positionChanged() {
                    System.out.println("pos changed");
                    switch (go_client.getPosition()) {
                        case DISCONNECTED:
                            sendAction(Action.DISCONNECTED, null);
                            break;
                        case GAMESERVICE:
                            while(go_client.getGoGameServiceInfo().equals(Client.STATUS.BUSY));
                            sendAction(Action.SET_CONTENT_PANE, room_view);
                            break;
                        case SERVER:
                            while(go_client.getGameServicesInfo().equals(Client.STATUS.BUSY));
                            sendAction(Action.SET_CONTENT_PANE, server_view);
                            break;
                    }
                }

                @Override
                public void recived(ICommand command, String request) {
                    System.out.println("command received");
                    switch (command.getCommandType()) {
                        case "GameServicesInfo":
                            server_view.setRooms((GameServicesInfo) command);
                            break;
                        case "GoGameServiceInfo":
                            room_view.setRoomInfo((GoGameServiceInfo) command);
                    }
                }

                @Override
                public void error(String request) {
                    System.out.println("error");
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
