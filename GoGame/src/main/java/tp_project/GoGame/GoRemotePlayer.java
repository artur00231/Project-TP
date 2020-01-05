package tp_project.GoGame;

import tp_project.Network.ICommand;
import tp_project.Network.SocketIO;
import tp_project.Network.SocketIO.AVAILABILITY;
import tp_project.Server.ServerCommand;

public class GoRemotePlayer implements GoPlayer {
    private SocketIO socketIO;
    private GoGame game;
    private GoPlayerListener listener = null;
    private boolean is_game_runnig = true;
    private GoStatus last_status;
    private String player_ID;
    private boolean is_connected = true;
    private boolean is_ready = false;

    public GoRemotePlayer(SocketIO socketIO, String player_ID) {
        this.socketIO = socketIO;
        this.player_ID = player_ID;
    }

    @Override
    public void setListener(GoPlayerListener listener) {
        this.listener = listener;
    }

    @Override
    public void setGame(GoGame game) {
        this.game = game;
    }

    public boolean isGameRunnig() {
        return is_game_runnig;
    }

    public boolean makeMove(GoMove move) {
        if (!is_game_runnig) return false;

        send(move);
        return true;
    }

    public boolean getGameStatus() {
        if (!is_game_runnig) return false;
        
        ServerCommand cmd = new ServerCommand();
        cmd.setCode(700);
        cmd.addValue("get", "status");
        send(cmd);
        return true;
    }

    public boolean getGameBoard() {
        if (!is_game_runnig) return false;

        ServerCommand cmd = new ServerCommand();
        cmd.setCode(700);
        cmd.addValue("get", "board");
        send(cmd);
        return true;
    }

    @Override
    public void gameEnded() {
        send(game.getGameStatus());
        ServerCommand cmd = new ServerCommand();
        cmd.setCode(703);

        socketIO.isAvailable();
        while (socketIO.popCommand() != null) socketIO.isAvailable();

        send(cmd);
    }

    public GoStatus getLastStatus() {
        return last_status;
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
            if (socketIO.getCommand().getType().equals("ServerCommand")) {
                if (((ServerCommand) socketIO.getCommand().getCommand()).getCode() == 700) {
                    if (((ServerCommand) socketIO.getCommand().getCommand()).getValue("get") != null) {
                        switch (((ServerCommand) socketIO.getCommand().getCommand()).getValue("get")) {
                            case "status": {
                                if (game != null) {
                                    GoStatus go_status = game.getGameStatus();
                                    socketIO.send(go_status);
                                }
                                break;
                            }
                            case "board": {
                                if (game != null) {
                                    GoBoard go_status = game.getBoard();
                                    socketIO.send(go_status);
                                }
                                break;
                            }
                        }
                    }
                } else if (((ServerCommand) socketIO.getCommand().getCommand()).getCode() == 701) {
                       if (listener != null) listener.boardUpdated();
                } else if (((ServerCommand) socketIO.getCommand().getCommand()).getCode() == 702) {
                    if (listener != null) listener.error();
                } else if (((ServerCommand) socketIO.getCommand().getCommand()).getCode() == 704) {
                    if (listener != null) listener.yourMove();
                } else if (((ServerCommand) socketIO.getCommand().getCommand()).getCode() == 705) {
                    if (listener != null) is_ready = true;
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
            } else if (socketIO.getCommand().getType().equals("GoMove")) {
                if (game != null) {
                    boolean success = game.makeMove(((GoMove)socketIO.getCommand().getCommand()), this);
                    if (!success) {
                        ServerCommand cmd = new ServerCommand();
                        cmd.setCode(702);
                        socketIO.send(cmd);
                    }
                }
            } else if (socketIO.getCommand().getType().equals("GoStatus")) {
                last_status = (GoStatus) socketIO.getCommand().getCommand();
                if (listener != null) listener.setStatus((GoStatus) socketIO.getCommand().getCommand());
            } else if (socketIO.getCommand().getType().equals("GoBoard")) {
                if (listener != null) listener.setBoard((GoBoard) socketIO.getCommand().getCommand());
            }

            socketIO.popCommand();
        }

        return true;
    }

    @Override
    public void yourMove() {
        send(game.getGameStatus());
        ServerCommand cmd = new ServerCommand();
        cmd.setCode(704);
        send(cmd);
    }

    private boolean send(ICommand command) {
        if (!socketIO.send(command)) return false;

        return true;
    }

    @Override
    public String getID() {
        return player_ID;
    }

    @Override
    public void boardUpdated() {
        ServerCommand cmd = new ServerCommand();
        cmd.setCode(701);
        send(cmd);
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

    private void setRedy() {
        ServerCommand cmd = new ServerCommand();
        cmd.setCode(705);

        send(cmd);
    }
}