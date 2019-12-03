package tp_project.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import tp_project.Network.SocketIO;
import tp_project.Network.TextCommand;

public class Server implements Runnable {
    private boolean is_runnig = false;
    private boolean is_valid = false;
    private boolean kill = false;
    private ServerSocketChannel socket_server;
    private Selector selector;
    private HashMap<SocketChannel, SocketIO> clients;
    private int port;

    public Server(int port) {
        this.port = port;
        clients = new HashMap<SocketChannel, SocketIO>();

        setup();
    }

    @Override
    public void finalize() {
        if (socket_server.isOpen())
        {
            try {
                socket_server.close();
            } catch (IOException e) {
                //Server is being deleted, so its ok
            }
        }

        if (selector.isOpen())
        {
            try {
                selector.close();
            } catch (IOException e) {
                //Server is being deleted, so its ok
            }
        }
    }

    public boolean isValid() {
        return is_valid;
    }

    public boolean isRunnig() {
        return is_runnig;
    }

    public void kill() {
        kill = true;
    }

    @Override
    public void run() {
        if (!is_valid) return;
        is_runnig = true;

        while (is_runnig) {
            if (kill) {
                is_runnig = false; 
                continue;
            }

            if (!checkSelector()) {
                is_runnig = false;
                is_valid = false;
                continue;
            }
        }
    }

    private boolean setup() {
        try {
            socket_server = ServerSocketChannel.open();
            socket_server.bind(new InetSocketAddress(port));
            socket_server.configureBlocking(false);

            selector = Selector.open();
            
            socket_server.register(selector, SelectionKey.OP_ACCEPT);
        } catch (Exception exception) {
            is_valid = false;
            return false;
        }

        is_valid = true;
        return true;
    }

    private boolean checkSelector() {
        int num_of_channels;
        try {
            num_of_channels = selector.select(100);
        } catch (IOException exception) {
            return false;
        } catch (ClosedSelectorException exception) {
            return false;
        }

        if(num_of_channels > 0) {

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while(keyIterator.hasNext()) {

                SelectionKey key = keyIterator.next();

                if(key.isAcceptable()) {
                    SocketIO new_socket = acceptNewConnection();

                    if (new_socket != null) {
                        new_socket.send(new TextCommand("HI FROM SERVER"));
                    }
                } else if (key.isConnectable()) {
                   
                } else if (key.isReadable()) {
                    SocketChannel incoming = ((SocketChannel) key.channel());
                    SocketIO client = clients.get(incoming);

                    if (client.isAvaiable() == SocketIO.AVAILABILITY.YES) {
                        String message = ((TextCommand)client.popCommand().getCommand()).toText();
                        if (message.equals("q")) {
                            client.send(new TextCommand("EXIT"));
                            kill = is_runnig = false;
                        }
                        else {
                            client.send(new TextCommand("FROM SERVER {" + message + "}\n"));

                            if (client.getSatus().sended != true) {
                                System.out.println("EER");
                            }
                        }
                    }
                }

                keyIterator.remove();
            }
        }

        return true;
    }

    private SocketIO acceptNewConnection() {
        SocketIO new_connection = null;

        try {
            SocketChannel sc = socket_server.accept();
            sc.configureBlocking(false);
            new_connection = new SocketIO(sc);
            
            clients.put(sc, new_connection);

            sc.register(selector, SelectionKey.OP_READ);
            
        } catch (IOException exception) {
            return null;
        }

        return new_connection;
    }
}