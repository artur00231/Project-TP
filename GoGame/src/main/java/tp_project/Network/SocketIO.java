package tp_project.Network;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

public class SocketIO {
    public static class ConnectionStatus {
        public boolean is_connected = true;
        public boolean sent = true;
        public boolean received = true;
    }

    public enum AVAILABILITY { YES, NO, DISCONNECTED }

    private SocketChannel socket;
    private ConnectionStatus connection_status;
    private ByteBuffer byte_buffer;
    private String buffer = "";
    private int buffer_max_size = 1024;
    private List<Command> commands;

    public SocketIO(SocketChannel socket) throws IOException {
        this.socket = socket;
        socket.configureBlocking(false);
        commands = new LinkedList<>();
        connection_status = new ConnectionStatus();
        byte_buffer = ByteBuffer.allocate(1024);
        connection_status.is_connected = true;
    }

    @Override
    public void finalize() {
        try {
            socket.close();
        } catch (IOException exception) {
            // DO NOTHING
        }
    }

    public ConnectionStatus getSatus() {
        return connection_status;
    }

    public boolean send(ICommand iCommand) {
        String data = iCommand.toText();
        NetworkDataParser network_data_parser = NetworkDataParser.getNetworkDataParser();
        
        if (!network_data_parser.isValid(data)) return false;

        byte[] raw_data = network_data_parser.getNetworkData(iCommand.getCommandType(), data);

        try {
            ByteBuffer to_send = ByteBuffer.wrap(raw_data);
            socket.write(to_send);
        } catch (IOException e) {
            connection_status.sent = false;
            return false;
        }

        connection_status.sent = true;
        return true;
    }

    public Command getCommand() {
        if (commands.size() == 0) return null;

        return commands.get(0);
    }

    public Command popCommand() {
        if (commands.size() == 0) return null;

        Command cmd = commands.get(0);
        commands.remove(0);
        return cmd;
    }
    
    public int getNumberOfCommands() {
        return commands.size();
    }

    public AVAILABILITY isAvailable() {
        recive();

        if (!connection_status.received) return AVAILABILITY.DISCONNECTED;

        return commands.size() > 0 ? AVAILABILITY.YES : AVAILABILITY.NO;
    }

    private void recive() {
        boolean recived = false;
        int num_of_bytes;

        try {
            while ((num_of_bytes = socket.read(byte_buffer)) > 0) {
                try {
                    byte_buffer.flip();
                    buffer += new String(byte_buffer.array(), 0, num_of_bytes, "ASCII");
                    recived = true;
                } catch (UnsupportedEncodingException e) {
                    // Never throws en exception
                    // I hope
                }
            }

            if (num_of_bytes == -1 && !recived) throw new IOException();
        } catch (IOException e) {
            connection_status.received = false;
            connection_status.is_connected = false;
            return;
        }

        if (recived) findCommands();
    }

    public void findCommands() {
        NetworkDataParser network_data_parser = NetworkDataParser.getNetworkDataParser();

        commands.addAll(network_data_parser.getCommands(buffer));
        buffer = network_data_parser.removeCommand(buffer);

        if (buffer.length() > buffer_max_size) {
            //Reset buffer
            buffer = "";
        }

    }

}