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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import tp_project.Network.SocketIO;
import tp_project.Network.SocketIO.AVAILABILITY;

public class Server implements Runnable, GameServiceMenager {
    private class Client {
        public String ID;
        public String game_ID;
        public SocketIO socketIO;
        public Client(String ID, SocketIO socketIO) { this.ID = ID; this.socketIO = socketIO; }
    }
    private boolean is_runnig = false;
    private boolean is_valid = false;
    private boolean kill = false;
    private ServerSocketChannel socket_server;
    private Selector selector;
    private HashMap<SocketChannel, Client> clients;
    private HashMap<String, GameService> game_services;
    private int port;

    public Server(int port) {
        this.port = port;
        clients = new HashMap<SocketChannel, Client>();
        game_services = new HashMap<String, GameService>();

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
                    acceptNewConnection();
                } else if (key.isConnectable()) {
                   
                } else if (key.isReadable()) {
                    SocketChannel incoming = ((SocketChannel) key.channel());
                    Client client = clients.get(incoming);

                    AVAILABILITY data_status = client.socketIO.isAvaiable();
                    if (data_status == SocketIO.AVAILABILITY.YES) {
                        if (client.game_ID == null) {
                            handleIncomingCommand(client);
                        } else {
                            //GameService.update();
                        }
                    } else if (data_status == AVAILABILITY.DISCONNECTED) {
                        removeClient(incoming);
                        System.err.println("Disconnected:" + client.ID);
                    }
                }

                keyIterator.remove();
            }
        }

        return true;
    }

    private boolean acceptNewConnection() {
        SocketChannel sc = null;

        try {
            sc = socket_server.accept();
            sc.configureBlocking(false);
            SocketIO new_connection = new SocketIO(sc);
            String ID = UUID.randomUUID().toString();
            clients.put(sc, new Client(ID, new_connection));

            sc.register(selector, SelectionKey.OP_READ);

            ServerCommand server_command = new ServerCommand();
            server_command.setCode(202);
            server_command.addValue("ID", ID);
            new_connection.send(server_command);

            System.err.println("Connected:" + ID);
            
        } catch (IOException exception) {
            return false;
        }

        return true;
    }

    private boolean removeClient(SocketChannel sc) {
        Client client = clients.get(sc);
        if (client == null) return false;
        if (client.game_ID != null) {
            GameService gs = game_services.get(client.game_ID);
            if (gs != null && gs.getInfo().host.equals(client.ID)) {
                //TODO remove game_service
            }
        }
        sc.keyFor(selector).cancel();
        clients.remove(sc);

        return true;
    }

    private void handleIncomingCommand(Client client) {
        if (client.socketIO.getCommand() == null) return;
        if (!(client.socketIO.getCommand().getType().equals("ServerCommand"))) return;

        ServerCommand cmd = (ServerCommand)client.socketIO.getCommand().getCommand();

        if (cmd.getValue("action").equals("getServicesInfo")) {
            client.socketIO.popCommand();
            client.socketIO.send(getGameServcesInfo(cmd.getValue("filter")));
        }
    }

    private GameServicesInfo getGameServcesInfo(String filter) {
        GameServicesInfo info = new GameServicesInfo();

        for (Map.Entry<String, GameService> val : game_services.entrySet()) {
            if (filter != null) {
                if (val.getValue().getGameName().contains(filter)) {
                    info.game_services.add(val.getValue().getInfo());
                }
            } else {
                info.game_services.add(val.getValue().getInfo());
            }
        }

        return info;
    }

    @Override
    public void playerRemoved(String ID) {
        for (Client client : clients.values()) {
            if (client.ID.equals(ID)) {
                client.game_ID = null;

                break;
            }
        }
    }
}