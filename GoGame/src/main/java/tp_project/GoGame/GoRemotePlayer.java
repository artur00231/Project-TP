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

    public GoRemotePlayer(SocketIO socketIO) {
        this.socketIO = socketIO;
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
        send(cmd);
    }

    public GoStatus getLastStatus() {
        return last_status;
    }

    @Override
    public void update() {
        if (!is_game_runnig) return;

        SocketIO.AVAILABILITY status = socketIO.isAvailable();

        if (status == AVAILABILITY.DISCONNECTED) {
            is_game_runnig = false;

            return;
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
                       if (listener != null) listener.updated();
                } else if (((ServerCommand) socketIO.getCommand().getCommand()).getCode() == 702) {
                    if (listener != null) listener.error();
                } else if (((ServerCommand) socketIO.getCommand().getCommand()).getCode() == 703) {
                    is_game_runnig = false;
                    socketIO.popCommand();
                    return;
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
    }

    private boolean send(ICommand command) {
        if (!socketIO.send(command)) return false;

        return true;
    }
}