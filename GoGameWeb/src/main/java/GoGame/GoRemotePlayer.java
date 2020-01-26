package GoGame;

import Network.ICommand;
import Network.SocketIO;
import Network.SocketIO.AVAILABILITY;
import Server.ServerCommand;

public class GoRemotePlayer implements GoPlayer {
    private SocketIO socketIO;
    private GoPlayerListener listener = null;
    private boolean is_game_runnig = true;
    private GoStatus last_status;
    private String player_ID;
    private String player_name;
    private boolean is_connected = true;
    private boolean is_ready = false;

    public GoRemotePlayer(SocketIO socketIO, String player_ID, String player_name) {
        this.socketIO = socketIO;
        this.player_ID = player_ID;
        this.player_name = player_name;
    }

    @Override
    public void setListener(GoPlayerListener listener) {
        this.listener = listener;
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
        //Server funcionality
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
            if (socketIO.getCommand().getCommand() == null) {
                socketIO.popCommand();
                continue;
            }
            if (socketIO.getCommand().getType().equals("ServerCommand")) {
                if (((ServerCommand) socketIO.getCommand().getCommand()).getCode() == 700) {
                    if (((ServerCommand) socketIO.getCommand().getCommand()).getValue("get") != null) {
                        //Server funcionality
                    }
                } else if (((ServerCommand) socketIO.getCommand().getCommand()).getCode() == 701) {
                       if (listener != null) listener.boardUpdated();
                } else if (((ServerCommand) socketIO.getCommand().getCommand()).getCode() == 702) {
                    if (listener != null) listener.error();
                } else if (((ServerCommand) socketIO.getCommand().getCommand()).getCode() == 704) {
                    if (listener != null) listener.yourMove();
                } else if (((ServerCommand) socketIO.getCommand().getCommand()).getCode() == 705) {
                    //Server funcionality
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
                //Server funcionality
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
        //Server funcionality
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
    public String getName() {
        return player_name;
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