package tp_project.GoReplay;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Optional;

import tp_project.Network.ICommand;
import tp_project.Network.SocketIO;
import tp_project.Server.Client;
import tp_project.Server.ServerCommand;

public class GoReplayClient extends Client {
    public static Optional<GoReplayClient> create(String IP, int port, String name) {
        try {
            SocketChannel sc = SocketChannel.open();
            sc.connect(new InetSocketAddress(IP, port));
            SocketIO io = new SocketIO(sc);
            GoReplayClient goClient = new GoReplayClient(io, name);

            if (goClient.getPosition() == POSITION.DISCONNECTED) {
                return Optional.ofNullable(null);
            }

            return Optional.of(goClient);
        } catch (IOException e) {
            return Optional.ofNullable(null);
        }
    }

    protected GoReplayClient(SocketIO socketIO, String name) {
        super(socketIO, name);
    }

    /*
    public GoRemotePlayer getPlayer() {
        if (getPosition() != POSITION.GAME)
            return null;

        return new GoRemotePlayer(socketIO, getID(), getName());
    }*/

    @Override
    protected void handleExtendentCommand(ICommand command, String request) {
        if (request.equals("getGoReplayServiceInfo") && command.getCommandType().equals("GoReplayServiceInfo")) {
            GoReplayServiceInfo inf = (GoReplayServiceInfo) command;

            client_listener.recived(inf, request);
        }
    }

    @Override
    public String getGameName() {
        return "GoReplay";
    }

    @Override
    public String getGameFiltr() {
        return "GoReplay";
    }

    public boolean getGoReplayServiceInfo() {
        if (getPosition() != POSITION.GAMESERVICE) return false;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("GoReplay", "getGoReplayServiceInfo");
        pushRequest(new Request(cmd, "getGoReplayServiceInfo", RESPONSETYPE.EXTENDENT));
        sendRequest();

        return true;
    }
    
}