package tp_project.GUI;

import tp_project.GoGame.GoBoard;
import tp_project.GoGameDBObject.DBGoGame;
import tp_project.GoGameDBObject.DBGoGames;
import tp_project.GoGameDBObject.DBGoStatus;
import tp_project.GoReplay.GoReplayClient;
import tp_project.GoReplay.GoReplayPlayer;
import tp_project.GoReplay.GoReplayPlayerListener;
import tp_project.Network.ICommand;
import tp_project.Server.Client;
import tp_project.Server.ClientListener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.util.Optional;

public class ReplayClientView {
    enum Action {
        ERROR, SET_CONTENT_PANE, DISCONNECTED, PACK;

        Object object;
    }

    private ActionListener action_listener;
    private GoReplayClient replay_client;
    private GoReplayPlayer replay_player;
    private Timer update_timer_client;
    private Timer update_timer_player;
    private boolean startup = true;
    private ReplayPicker replay_picker;
    private ReplayGameView replay_game;
    private DBGoStatus status = null;
    private GoBoard go_board = null;
    private DBGoGame game = null;
    private boolean runnig = false;
    private int round = 0;
    private int max_round = Integer.MAX_VALUE;

    private static ReplayClientView.Action createAction(ReplayClientView.Action a, Object o) {
        a.object = o;
        return a;
    }

    private void sendAction(ReplayClientView.Action a, Object o) {
        action_listener.actionPerformed(new ActionEvent(createAction(a, o), 0, ""));
    }

    public ReplayClientView(ActionListener a) {
        action_listener = a;

        replay_picker = new ReplayPicker();
        replay_picker.setActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switch ((ReplayPicker.Action) e.getSource()) {
                case SELECT:
                    game = replay_picker.getValue().get();
                    replay_player.getBoard(game.getID(), 0);
                    break;
                case RETURN:
                    replay_player.exit();
                    update_timer_player.stop();
                    update_timer_client.start();
                    break;
                case GET:
                    replay_player.getGamesList(Date.valueOf(e.getActionCommand()));
                    break;
                }
            }
        });

        update_timer_client = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                replay_client.update();

                if (replay_client.getPosition() == Client.POSITION.DISCONNECTED) {
                    sendAction(ReplayClientView.Action.DISCONNECTED, null);
                }
            }
        });
        update_timer_client.setRepeats(true);

        update_timer_player = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                replay_player.update();

                if (replay_player.isDisconnected()) {
                    update_timer_player.stop();
                    action_listener.actionPerformed(new ActionEvent(Action.DISCONNECTED, 0, ""));
                }
            }
        });
        update_timer_player.setRepeats(true);
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
                    System.out.println("pos changed");
                    switch (replay_client.getPosition()) {
                    case DISCONNECTED:
                        update_timer_client.stop();
                        sendAction(Action.DISCONNECTED, null);
                        break;
                    case GAMESERVICE:
                        if (startup) {
                            replay_client.setReady(true);
                            startup = false;
                        } else {
                            update_timer_player.stop();
                            update_timer_client.start();
                            replay_client.exit();
                        }
                        break;
                    case SERVER:
                        replay_client.exit();
                        break;
                    case GAME:
                        update_timer_client.stop();
                        update_timer_player.start();

                        sendAction(Action.SET_CONTENT_PANE, replay_picker.getView());
                        break;
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

            replay_player = replay_client.getPlayer();
            replay_player.setListener(new GoReplayPlayerListener() {
                @Override
                public void setGames(DBGoGames games) {
                    replay_picker.setAvaiableGames(games.games);
                }

                @Override
                public void setBoard(GoBoard board) {
                    if (!runnig) {
                        go_board = board;
                        replay_player.getStatus(game.getID());
                    } else {
                        go_board.fromText(board.toText());
                        replay_game.setNextBoard(board, round != 0, round != max_round);
                    }
                }

                @Override
                public void gameEnded() {

                }

                @Override
                public void error() {
                    if (!runnig) {
                        JOptionPane.showMessageDialog(null, "Invalid game, try another");
                        status = null;
                        go_board = null;
                    } else {
                        round--;
                        max_round = round;
                        replay_game.setButtons(round != 0, round != max_round);
                    }
                }

                @Override
                public void setStatus(DBGoStatus db_status) {
                    status = db_status;
                    runnig = true;
                    
                    replay_game = new ReplayGameView(go_board.getSize(), status, game, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            switch ((ReplayGameView.ACTION) e.getSource()) {
                                case PREV:
                                    if (round != 0) {
                                        round--;
                                        replay_player.getBoard(game.getID(), round);
                                    } else {
                                        replay_game.setButtons(round != 0, round != max_round);
                                    }
                                    break;
                                case NEXT:
                                    if (round != max_round) {
                                        round++;
                                        replay_player.getBoard(game.getID(), round);
                                    } else {
                                        replay_game.setButtons(round != 0, round != max_round);
                                    }
                                    break;
                                case END:
                                    replay_player.exit();
                                    update_timer_player.stop();
                                    update_timer_client.start();
                                    break;
                            }
                        }
                    });

                    replay_game.setButtons(false, true);
                    sendAction(Action.SET_CONTENT_PANE, replay_game);
                    sendAction(Action.PACK, null);
                }
            });

            startup = true;
            replay_game = null;
            status = null;
            go_board = null;
            game = null;
            runnig = false;
            round = 0;
            max_round = Integer.MAX_VALUE;

            update_timer_client.start();

            replay_client.createGame();
        } else sendAction(Action.ERROR, "Connect Error");
    }

    public void reset() {
        update_timer_client.stop();
        update_timer_player.stop();
        startup = true;

        startup = true;
        replay_game = null;
        status = null;
        go_board = null;
        game = null;
        runnig = false;
        round = 0;
        max_round = Integer.MAX_VALUE;
    }
}
