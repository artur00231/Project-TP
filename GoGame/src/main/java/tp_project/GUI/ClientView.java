package tp_project.GUI;

import tp_project.GoGame.GoClient;
import tp_project.GoGame.GoGameServiceInfo;
import tp_project.GoGame.GoRemotePlayer;
import tp_project.GoGameLogic.GoGameLogic;
import tp_project.Network.ICommand;
import tp_project.Server.Client;
import tp_project.Server.ClientListener;
import tp_project.Server.GameServiceInfo;
import tp_project.Server.GameServicesInfo;
import tp_project.Server.Client.POSITION;

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
    private GoRemotePlayer go_player;
    private GoGameLogic.Player color;
    private String room_id;
    private String host_id;

    private ActionListener action_listener;
    private Thread t;
    private GoClientThread go_client_thread;

    private ServerView server_view;
    private RoomView room_view;
    private GameView game_view;

    enum Action {
        ERROR,
        SET_CONTENT_PANE,
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
            switch ((ServerView.Action) e.getSource()) {
                case RETURN:
                    sendAction(Action.DISCONNECTED, null);
                    go_client.exit();
                    break;
                case CREATE:
                    go_client.createGame();
                    while(go_client.getPosition() != POSITION.GAMESERVICE) go_client.update();
                    go_client.setGameSize(e.getID());
                    break;
                case JOIN:
                    room_id = e.getActionCommand();
                    go_client.connect(room_id);
                    break;
                case REFRESH:
                    go_client.getGameServicesInfo();
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
            switch ((RoomView.Action) e.getSource()) {
                case KICK:
                    go_client.kick(e.getActionCommand());
                    break;
                case LEAVE:
                    go_client.exit();
                    break;
                case READY:
                    go_client.setReady(true);
                    break;
                case NOT_READY:
                    go_client.setReady(false);
                    break;
                case ADD_BOT:
                    go_client.addBot();
                    break;
                case SWITCH_COLORS:
                    go_client.flipColours();
                    break;
                case PACK:
                    action_listener.actionPerformed(new ActionEvent(Action.PACK, 0, ""));
                    break;
            }
        });
    }

    public void connect(String IP, String port, String name) {
        Optional<GoClient> optional = GoClient.create(IP, Integer.parseInt(port), name);
        if (optional.isPresent() && (go_client = optional.get()) != null) {
            go_client.setClientListener(new ClientListener() {
                @Override
                public void updated() {
                    System.out.println("updated");
                    switch (go_client.getPosition()) {
                        case GAMESERVICE:
                            go_client.getGameServiceInfo();
                            go_client.getGoGameServiceInfo();
                            break;
                    }
                }

                @Override
                public void positionChanged() {
                    System.out.println("pos changed");
                    switch (go_client.getPosition()) {
                        case DISCONNECTED:
                            go_client_thread.stop();
                            sendAction(Action.DISCONNECTED, null);
                            break;
                        case GAMESERVICE:
                            if (!go_client_thread.running) { go_client_thread = new GoClientThread(go_client);
                                Thread t = new Thread(go_client_thread);
                                t.start();
                            }
                            go_client.getGameServiceInfo();
                            go_client.getGoGameServiceInfo();
                            sendAction(Action.SET_CONTENT_PANE, room_view);
                            break;
                        case SERVER:
                            if (!go_client_thread.running) { go_client_thread = new GoClientThread(go_client);
                                Thread t = new Thread(go_client_thread);
                                t.start();
                            }
                            go_client.getGameServicesInfo();
                            sendAction(Action.SET_CONTENT_PANE, server_view);
                            break;
                        case GAME:
                            go_client_thread.stop();
                            game_view = new GameView(go_client.getPlayer(), go_client.getGameSize(), go_client.getColour() == 0 ? GoGameLogic.Player.BLACK : GoGameLogic.Player.WHITE,
                                new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        switch ((GameView.ACTION) e.getSource() ) {
                                            case END:
                                                while (go_client.getPosition() != POSITION.GAMESERVICE)
                                                    go_client.update();
                                        }
                                    }
                                });
                            sendAction(Action.SET_CONTENT_PANE, game_view);
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
                            room_view.updateRoomInfo((GoGameServiceInfo) command);
                            break;
                        case "GameServiceInfo":
                            room_view.setRoomInfo(go_client.getID(), (GameServiceInfo) command);
                            break;
                    }
                }

                @Override
                public void error(String request) {
                    if (request.equals("addBot")) {
                        JOptionPane.showMessageDialog(null, "Bot couldn't be added");
                        return;
                    }
                    System.out.println("error");
                    sendAction(Action.ERROR, request);
                }
            });

            go_client_thread = new GoClientThread(go_client);
            t = new Thread(go_client_thread);
            t.start();

            go_client.getGameServicesInfo();
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
