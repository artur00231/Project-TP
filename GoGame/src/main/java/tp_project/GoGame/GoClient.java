package tp_project.GoGame;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Optional;

import tp_project.Network.ICommand;
import tp_project.Network.SocketIO;
import tp_project.Server.Client;
import tp_project.Server.ServerCommand;

public class GoClient extends Client {
    private int game_size = 13;

    public static Optional<GoClient> create(String IP, int port, String name) {
        try {
            SocketChannel sc = SocketChannel.open();
            sc.connect(new InetSocketAddress(IP, port));
            SocketIO io = new SocketIO(sc);
            GoClient goClient = new GoClient(io, name);

            if (goClient.getPosition() == POSITION.DISCONNECTED) {
                return Optional.ofNullable(null);
            }

            return Optional.of(goClient);
        } catch (IOException e) {
            return Optional.ofNullable(null);
        }
    }

    protected GoClient(SocketIO socketIO, String name) {
        super(socketIO, name);
    }

    public GoRemotePlayer getPlayer() {
        if (getPosition() != POSITION.GAME) return null;

        return new GoRemotePlayer(socketIO, getID());
    }

    public int getGameSize() {
        return game_size;
    }

    @Override
    protected void handleExtendentCommand(ICommand command, String request) {
        if (request.equals("flip") && command.getCommandType().equals("ServerCommand")) {
            ServerCommand cmd = (ServerCommand) command;

            if (cmd.getCode() != 200) {
                client_listener.error(request);
            }
        } else if (request.equals("setSize") && command.getCommandType().equals("ServerCommand")) {
            ServerCommand cmd = (ServerCommand) command;

            if (cmd.getCode() != 200) {
                client_listener.error(request);
            }
        } else if (request.equals("getGoGameServiceInfo") && command.getCommandType().equals("GoGameServiceInfo")) {
            GoGameServiceInfo inf = (GoGameServiceInfo) command;

            game_size = inf.board_size;
            client_listener.recived(inf, request);
        }
    }

    @Override
    public String getGameName() {
        return "GoGame";
    }

    @Override
    public String getGameFiltr() {
        return "GoGame";
    }

    public STATUS flipColours() {
        if (getPosition() != POSITION.GAMESERVICE) return STATUS.WPOS;
        if (isWaiting()) return STATUS.BUSY;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("GoGame", "flip");
        cmd.addValue("sKey", getSKey());
        send(cmd, RESPONSETYPE.EXTENDENT, "flip");

        return STATUS.OK;
    }

    public STATUS setGameSize(int size) {
        if (getPosition() != POSITION.GAMESERVICE) return STATUS.WPOS;
        if (isWaiting()) return STATUS.BUSY;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("GoGame", "setSize");
        cmd.addValue("size", Integer.toString(size));
        cmd.addValue("sKey", getSKey());
        send(cmd, RESPONSETYPE.EXTENDENT, "setSize");

        return STATUS.OK;
    }

    public STATUS getGoGameServiceInfo() {
        if (getPosition() != POSITION.GAMESERVICE) return STATUS.WPOS;
        if (isWaiting()) return STATUS.BUSY;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("GoGame", "getGoGameServiceInfo");
        send(cmd, RESPONSETYPE.EXTENDENT, "getGoGameServiceInfo");

        return STATUS.OK;
    }
    
}