package tp_project.GoGame;

import tp_project.Network.ICommand;
import tp_project.Network.SocketIO;
import tp_project.Network.SocketIO.AVAILABILITY;
import tp_project.Server.Player;
import tp_project.Server.ServerCommand;

public class GoPlayer implements Player {
    private String ID = "";
    private SocketIO socketIO;
    private GoGame game;
    private GoPlayerListener listener = null;
    private boolean wait_for_response = false;
    private boolean is_game_runnig = true;

    public GoPlayer(String ID, SocketIO socketIO) {
        this.ID = ID;
        this.socketIO = socketIO;
    }

    public void setListener(GoPlayerListener listener) {
        this.listener = listener;
    }

    public void setGame(GoGame game) {
        this.game = game;
    }

    public boolean isGameRunnig() {
        return is_game_runnig;
    }

    public boolean makeMove(GoMove move) {
        if (!is_game_runnig) return false;
        if (wait_for_response) return false;

        send(move);
        return true;
    }

    public boolean getGameStatus() {
        if (!is_game_runnig) return false;
        if (wait_for_response) return false;
        
        ServerCommand cmd = new ServerCommand();
        cmd.setCode(700);
        cmd.addValue("get", "status");
        send(cmd);
        return true;
    }

    public boolean getGameBoard() {
        if (!is_game_runnig) return false;
        if (wait_for_response) return false;
        
        ServerCommand cmd = new ServerCommand();
        cmd.setCode(700);
        cmd.addValue("get", "board");
        send(cmd);
        return true;
    }

    @Override
    public void update() {
        if (!is_game_runnig) return;

        SocketIO.AVAILABILITY status = socketIO.isAvailable();

        if (status == AVAILABILITY.DISCONNECTED) {
            is_game_runnig = false;

            return;
        }

        do {
            while (socketIO.getCommand() != null) {
                if (socketIO.getCommand().getType().equals("ServerCommand")) {
                    if (((ServerCommand) socketIO.getCommand().getCommand()).getCode() == 701) {
                        if (listener != null) listener.updated();
                        socketIO.popCommand();
                    } else if (((ServerCommand) socketIO.getCommand().getCommand()).getCode() == 702) {
                        if (listener != null) listener.error();
                        socketIO.popCommand();
                    } else if (((ServerCommand) socketIO.getCommand().getCommand()).getCode() == 703) {
                        is_game_runnig = false;
                        socketIO.popCommand();
                        return;
                    } else {
                        break;
                    }
                } else {
                    break;
                }
                socketIO.popCommand();
            }
        } while (socketIO.getCommand() != null);
    }

    private boolean send(ICommand command) {
        if (!socketIO.send(command)) return false;

        wait_for_response = true;

        return true;
    }
    
}