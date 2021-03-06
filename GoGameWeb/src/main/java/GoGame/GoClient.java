package GoGame;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Optional;

import GoGame.GoGameServiceInfo.PlayerInfo;
import Network.ICommand;
import Network.SocketIO;
import Server.Client;
import Server.ServerCommand;

public class GoClient extends Client {
    private int game_size = 13;
    private int colour = -1;

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
        if (getPosition() != POSITION.GAME)
            return null;

        return new GoRemotePlayer(socketIO, getID(), getName());
    }

    public int getGameSize() {
        return game_size;
    }

    public int getColour() {
        return colour;
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

            for (PlayerInfo p_inf : inf.getPlayersInfo()) {
                if (p_inf.ID.equals(getID())) {
                    colour = p_inf.colour;
                }
            }
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

    public boolean flipColours() {
        if (getPosition() != POSITION.GAMESERVICE) return false;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("GoGame", "flip");
        cmd.addValue("sKey", getSKey());
        pushRequest(new Request(cmd, "flip", RESPONSETYPE.EXTENDENT));
        sendRequest();

        return true;
    }

    public boolean setGameSize(int size) {
        if (getPosition() != POSITION.GAMESERVICE) return false;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("GoGame", "setSize");
        cmd.addValue("size", Integer.toString(size));
        cmd.addValue("sKey", getSKey());
        pushRequest(new Request(cmd, "setSize", RESPONSETYPE.EXTENDENT));
        sendRequest();

        return true;
    }

    public boolean getGoGameServiceInfo() {
        if (getPosition() != POSITION.GAMESERVICE) return false;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("GoGame", "getGoGameServiceInfo");
        pushRequest(new Request(cmd, "getGoGameServiceInfo", RESPONSETYPE.EXTENDENT));
        sendRequest();

        return true;
    }
    
}