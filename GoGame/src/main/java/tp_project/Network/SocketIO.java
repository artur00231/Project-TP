package tp_project.Network;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class SocketIO {
    public class ConnectionStatus {
        boolean is_connected;
    }


    private Socket socket;
    private BufferedInputStream input;
    private OutputStream out;

    public SocketIO(Socket socket) throws IOException
    {
        this.socket = socket;
        input = new BufferedInputStream(socket.getInputStream());
        out = socket.getOutputStream();
    }

    public ConnectionStatus getSatus()
    {
        ConnectionStatus status = new ConnectionStatus();
        status.is_connected = socket.isConnected();
        return status;
    }

    public boolean send(ICommand iCommand)
    {
        //TODO

        return false;
    }

    public boolean isAvaiable() 
    {
        try {
        return input.available() != 0;
        } catch (IOException exception) {
        }

        return false;
    }
}