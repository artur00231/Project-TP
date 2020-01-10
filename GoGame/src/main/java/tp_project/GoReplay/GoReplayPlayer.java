package tp_project.GoReplay;

import java.sql.Date;

import tp_project.GoGame.GoBoard;
import tp_project.GoGameDBObject.DBGoGames;
import tp_project.GoGameDBObject.DBGoStatus;
import tp_project.Network.ICommand;
import tp_project.Network.SocketIO;
import tp_project.Network.SocketIO.AVAILABILITY;
import tp_project.Server.Player;
import tp_project.Server.ServerCommand;

public class GoReplayPlayer implements Player {
    private SocketIO socketIO;
    private GoReplayGame game;
    private GoReplayPlayerListener listener = null;
    private boolean is_game_runnig = true;
    private boolean is_connected = true;
    private boolean is_ready = false;

    public GoReplayPlayer(SocketIO socketIO, String player_ID, String player_name) {
        this.socketIO = socketIO;
    }

    public void setListener(GoReplayPlayerListener listener) {
        this.listener = listener;
    }

    public void setGame(GoReplayGame game) {
        this.game = game;
    }

    public boolean isGameRunnig() {
        return is_game_runnig;
    }

    public boolean getGamesList(Date date) {
        if (!is_game_runnig) return false;

        ServerCommand cmd = new ServerCommand();
        cmd.setCode(700);
        cmd.addValue("get", "games");
        cmd.addValue("date", date.toString());
        send(cmd);
        return true;
    }

    public boolean getBoard(int game_id, int round) {
        if (!is_game_runnig) return false;
        
        ServerCommand cmd = new ServerCommand();
        cmd.setCode(700);
        cmd.addValue("get", "board");
        cmd.addValue("game_id", Integer.toString(game_id));
        cmd.addValue("round", Integer.toString(round));
        send(cmd);
        return true;
    }

    public boolean getStatus(int game_id) {
        if (!is_game_runnig) return false;
        
        ServerCommand cmd = new ServerCommand();
        cmd.setCode(700);
        cmd.addValue("get", "status");
        cmd.addValue("game_id", Integer.toString(game_id));
        send(cmd);
        return true;
    }

    public void gameEnded() {
        ServerCommand cmd = new ServerCommand();
        cmd.setCode(703);

        socketIO.isAvailable();
        while (socketIO.popCommand() != null) socketIO.isAvailable();

        send(cmd);
    }

    @Override
    public boolean update() {
        if (!is_game_runnig) return false;

        SocketIO.AVAILABILITY status = socketIO.isAvailable();

        if (status == AVAILABILITY.DISCONNECTED) {
            is_game_runnig = false;
            is_connected = false;

            return false;
        }

        if (!is_ready && listener != null) {
            setRedy();
            is_ready = true;
        }

        while (socketIO.getCommand() != null) {
            if (socketIO.getCommand().getCommand() == null) {
                socketIO.popCommand();
                continue;
            }
            if (socketIO.getCommand().getType().equals("ServerCommand")) {
                if (((ServerCommand) socketIO.getCommand().getCommand()).getCode() == 700) {
                    if (((ServerCommand) socketIO.getCommand().getCommand()).getValue("get") != null) {
                        switch (((ServerCommand) socketIO.getCommand().getCommand()).getValue("get")) {
                            case "games": {
                                if (game != null) {
                                    DBGoGames games = game.getGames(Date.valueOf(((ServerCommand) socketIO.getCommand().getCommand()).getValue("date")));
                                    socketIO.send(games);
                                }
                                break;
                            }
                            case "board": {
                                if (game != null) {
                                    GoBoard board = game.getBoard(Integer.valueOf(((ServerCommand) socketIO.getCommand().getCommand()).getValue("game_id")),
                                    Integer.valueOf(((ServerCommand) socketIO.getCommand().getCommand()).getValue("round")));

                                    if (board != null) {
                                        socketIO.send(board);
                                    } else {
                                        ServerCommand cmd = new ServerCommand();
                                        cmd.setCode(702);
                                        send(cmd);
                                    }
                                }
                                break;
                            }
                            case "status": {
                                if (game != null) {
                                    DBGoStatus go_status = game.getStatus(Integer.valueOf(((ServerCommand) socketIO.getCommand().getCommand()).getValue("game_id")));
                                    if (go_status != null) {
                                        socketIO.send(go_status);
                                    }  else {
                                        ServerCommand cmd = new ServerCommand();
                                        cmd.setCode(702);
                                        send(cmd);
                                    }
                                }
                                break;
                            }
                        }
                    }
                } else if (((ServerCommand) socketIO.getCommand().getCommand()).getCode() == 702) {
                    if (listener != null) listener.error();
                } else if (((ServerCommand) socketIO.getCommand().getCommand()).getCode() == 705) {
                    if (game != null) is_ready = true;
                } else if (((ServerCommand) socketIO.getCommand().getCommand()).getCode() == 706) {
                    if (game != null) game.end();
                } else if (((ServerCommand) socketIO.getCommand().getCommand()).getCode() == 703) {
                    is_game_runnig = false;
                    socketIO.popCommand();
                    if (listener != null) listener.gameEnded();
                    return false;
                } else if (((ServerCommand) socketIO.getCommand().getCommand()).getValue("ping") != null) {
                    ServerCommand cmd = new ServerCommand();
                    cmd.setCode(2);
                    cmd.addValue("ping", "0");
                    socketIO.send(cmd);
                }
            } else if (socketIO.getCommand().getType().equals("GoBoard")) {
                if (listener != null) listener.setBoard((GoBoard) socketIO.getCommand().getCommand());
            } else if (socketIO.getCommand().getType().equals("DBGoGames")) {
                if (listener != null) listener.setGames((DBGoGames) socketIO.getCommand().getCommand());
            } else if (socketIO.getCommand().getType().equals("DBGoStatus")) {
                if (listener != null) listener.setStatus((DBGoStatus) socketIO.getCommand().getCommand());
            }

            socketIO.popCommand();
        }

        return true;
    }

    @Override
    public void yourMove() {
        ServerCommand cmd = new ServerCommand();
        cmd.setCode(704);
        send(cmd);
    }

    private boolean send(ICommand command) {
        if (!socketIO.send(command)) return false;

        return true;
    }

    public boolean isDisconnected() {
        return !is_connected;
    }

    @Override
    public boolean isReady() {
        if (listener == null) {
            return is_ready;
        }

        return true;
    }

    public void exit() {
        ServerCommand cmd = new ServerCommand();
        cmd.setCode(706);

        send(cmd);
    }

    private void setRedy() {
        ServerCommand cmd = new ServerCommand();
        cmd.setCode(705);

        send(cmd);
    }

    @Override
    public void boardUpdated() {
        //DO NOTHING
    }
}