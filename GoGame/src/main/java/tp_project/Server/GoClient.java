package tp_project.Server;

import tp_project.Network.SocketIO;

public class GoClient extends Client {

    protected GoClient(SocketIO socketIO) {
        super(socketIO);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void handleExtendentCommand() {
        // TODO Auto-generated method stub
    }

    @Override
    public String getGameName() {
        return "GoGame";
    }

    @Override
    public String getGameFiltr() {
        return "GoGame";
    }
    
}