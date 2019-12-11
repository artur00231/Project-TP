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

    @Override
    protected void handleExtendentCommand(ICommand command, String request) {
        if (request.equals("flip") && command.getCommandType().equals("ServerCommand")) {
            ServerCommand cmd = (ServerCommand) command;

            if (cmd.getCode() != 200) {
                client_listener.error(request);
            }
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

    public STATUS getGoGameServiceInfo() {
        if (getPosition() != POSITION.GAMESERVICE) return STATUS.WPOS;
        if (isWaiting()) return STATUS.BUSY;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("GoGame", "getGoGameServiceInfo");
        send(cmd, RESPONSETYPE.OBJECT, "getGoGameServiceInfo");

        return STATUS.OK;
    }
    
}