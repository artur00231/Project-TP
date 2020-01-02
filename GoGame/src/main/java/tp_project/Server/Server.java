package tp_project.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import tp_project.Network.SocketIO;
import tp_project.Network.SocketIO.AVAILABILITY;

public class Server implements Runnable, GameServiceManager {
    private class Client {
        public String ID;
        public String game_ID;
        public SocketIO socketIO;

        public Client(String ID, SocketIO socketIO) {
            this.ID = ID;
            this.socketIO = socketIO;
        }
    }

    private class DelayedGameService implements Delayed {
        private String ID = "";
        private long time;
        public DelayedGameService(String id, long time) {
            ID = id;
            this.time = time;
        }
        @Override
        public int compareTo(Delayed o) {
            if (this.time < ((DelayedGameService)o).time) { 
                return -1; 
            } 
            if (this.time > ((DelayedGameService)o).time) { 
                return 1; 
            } 
            return 0; 
        }
        @Override
        public long getDelay(TimeUnit unit) {
            long diff = time - System.currentTimeMillis(); 
            return unit.convert(diff, TimeUnit.MILLISECONDS); 
        }
        @Override
        public String toString() {
            return ID;
        }
    }

    private boolean is_running = false;
    private boolean is_valid = false;
    private boolean kill = false;
    private boolean clean = false;
    private ServerSocketChannel socket_server;
    private Selector selector;
    private Object selector_sync = new Object();
    private HashMap<SocketChannel, Client> clients;
    private HashMap<String, GameService> game_services;
    private DelayQueue<DelayedGameService> game_services_to_delete;
    private int port;
    private Timer clean_timer;

    public Server(int port) {
        this.port = port;
        clients = new HashMap<>();
        game_services = new HashMap<>();
        game_services_to_delete = new DelayQueue<>();
        clean_timer = new Timer();

        setup();
    }

    @Override
    public void finalize() {
        if (socket_server.isOpen()) {
            try {
                socket_server.close();
            } catch (IOException e) {
                // Server is being deleted, so its ok
            }
        }

        if (selector.isOpen()) {
            try {
                selector.close();
            } catch (IOException e) {
                // Server is being deleted, so its ok
            }
        }

        if (clean) {
            cleanServer();
        }
    }

    public boolean isValid() {
        return is_valid;
    }

    public boolean isRunnig() {
        return is_running;
    }

    public void kill() {
        kill = true;
    }

    @Override
    public void run() {
        clean_timer.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run() {
                clean = true;
            }
        }, 2000, 2000);
        if (!is_valid)
            return;
        is_running = true;

        while (is_running) {
            if (kill) {
                is_running = false;
                continue;
            }

            synchronized (selector_sync) {
                if (!checkSelector()) {
                    is_running = false;
                    is_valid = false;
                    continue;
                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }

            DelayedGameService delayed_game_services = game_services_to_delete.poll();
            while (delayed_game_services != null) {
                String game_service_id = delayed_game_services.toString();
                GameService game_service = game_services.get(game_service_id);
                if (game_service.isGameRunnig()) {
                    game_services_to_delete.add(new DelayedGameService(game_service_id, 1000));
                } else {
                    removeGameService(game_service_id);
                }
                
                delayed_game_services = game_services_to_delete.poll();
            }
        }

        if (socket_server.isOpen()) {
            try {
                socket_server.close();
            } catch (IOException e) {
                // Server is being deleted, so its ok
            }
        }

        if (selector.isOpen()) {
            try {
                selector.close();
            } catch (IOException e) {
                // Server is being deleted, so its ok
            }
        }

        for (SocketChannel client : clients.keySet()) {
            if (client.isOpen()) {
                try {
                    client.close();
                } catch (IOException e) {}
            }
        }
    }

    private void removeGameService(String ID) {
        game_services.remove(ID);
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
        } catch (IOException | ClosedSelectorException exception) {
            return false;
        }

        if (num_of_channels > 0) {

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext()) {

                SelectionKey key = keyIterator.next();

                if (key.isAcceptable()) {
                    acceptNewConnection();
                } else if (key.isReadable()) {
                    SocketChannel incoming = ((SocketChannel) key.channel());
                    Client client = clients.get(incoming);

                    AVAILABILITY data_status = client.socketIO.isAvailable();
                    if (data_status == SocketIO.AVAILABILITY.YES) {
                        if (client.game_ID == null) {
                            handleIncomingCommand(client);
                        } else {
                            game_services.get(client.game_ID).update(client.ID);
                        }
                    } else if (data_status == AVAILABILITY.DISCONNECTED) {
                        removeClient(incoming);
                    }
                }

                keyIterator.remove();
            }
        }

        return true;
    }

    private boolean acceptNewConnection() {
        SocketChannel sc;

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

        } catch (IOException exception) {
            return false;
        }

        return true;
    }

    private boolean removeClient(SocketChannel sc) {
        Client client = clients.get(sc);
        if (client == null)
            return false;
        if (client.game_ID != null) {
            GameService gs = game_services.get(client.game_ID);
            if (gs != null) {
                gs.removePlayer(client.ID);
            }
        }
        sc.keyFor(selector).cancel();
        clients.remove(sc);

        return true;
    }

    private boolean removeClient(String client_id) {
        for (Map.Entry<SocketChannel, Client> pair : clients.entrySet()) {
            if (pair.getValue().ID.equals(client_id)) {
                return removeClient(pair.getKey());
            }
        }

        return false;
    }

    private void handleIncomingCommand(Client client) {
        if (client.socketIO.getCommand() == null)
            return;
        if (!(client.socketIO.getCommand().getType().equals("ServerCommand")))
            return;

        ServerCommand cmd = (ServerCommand) client.socketIO.getCommand().getCommand();
        if (cmd.getValue("ping") != null) {
            client.socketIO.popCommand();;
            ServerCommand ping = new ServerCommand();
            ping.setCode(0);
            ping.addValue("ping", "0");

            client.socketIO.send(ping);
            return;
        }

        if (cmd.getValue("action") == null) sendCode(client.socketIO, 400);

        switch (cmd.getValue("action")) {
            case "getServicesInfo":
                client.socketIO.popCommand();
                client.socketIO.send(getGameServcesInfo(cmd.getValue("filter")));
                break;
            case "create": {
                cmd = (ServerCommand) client.socketIO.popCommand().getCommand();
                if (cmd.getValue("type") == null) {
                    sendCode(client.socketIO, 400);
                    return;
                }
                if (cmd.getValue("name") == null) {
                    sendCode(client.socketIO, 400);
                    return;
                }
                String game_service_id = UUID.randomUUID().toString();
                String sKey = UUID.randomUUID().toString();

                GameService new_game_service = GameServiceFactory.getGameService(cmd.getValue("type"), game_service_id, sKey, cmd.getValue("name"), client.socketIO, client.ID, this);
                if (new_game_service == null) {
                    sendCode(client.socketIO, 400);
                    return;
                }

                game_services.put(game_service_id, new_game_service);
                client.game_ID = game_service_id;

                ServerCommand message = new ServerCommand();
                message.setCode(201);
                message.addValue("ID", game_service_id);
                message.addValue("sKey", sKey);
                client.socketIO.send(message);
                break;
            }
            case "connect": {
                cmd = (ServerCommand) client.socketIO.popCommand().getCommand();
                if (cmd.getValue("game") == null) {
                    sendCode(client.socketIO, 400);
                    return;
                }
                if (cmd.getValue("name") == null) {
                    sendCode(client.socketIO, 400);
                    return;
                }
                if (game_services.get(cmd.getValue("game")) == null) {
                    sendCode(client.socketIO, 400);
                    return;
                }

                GameService service = game_services.get(cmd.getValue("game"));
                if (!service.addPlayer(cmd.getValue("name"), client.ID, client.socketIO)) {
                    sendCode(client.socketIO, 400);
                    return;
                }
                client.game_ID = cmd.getValue("game");

                ServerCommand message = new ServerCommand();
                message.setCode(200);
                client.socketIO.send(message);

                break;
            }
            case "exit": {
                removeClient(client.ID);
            }
            default:
                client.socketIO.popCommand();
                break;
        }

    }

    private void sendCode(SocketIO socketIO, int code) {
        ServerCommand message = new ServerCommand();
        message.setCode(400);
        socketIO.send(message);
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

    private void cleanServer() {
        for (Map.Entry<SocketChannel, Client> pair : clients.entrySet()) {
            Client client = pair.getValue();
            AVAILABILITY data_status = client.socketIO.isAvailable();
            if (data_status == SocketIO.AVAILABILITY.YES) {
                if (client.game_ID == null) {
                        handleIncomingCommand(client);
                    } else {
                        game_services.get(client.game_ID).update(client.ID);
                    }
            } else if (data_status == AVAILABILITY.DISCONNECTED) {
                removeClient(pair.getKey());
            }
        }
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

    @Override
    public void unregisterPlayer(String ID) {
        synchronized (selector_sync) {
            for (Map.Entry<SocketChannel, Client> pair : clients.entrySet()) {
                if (pair.getValue().ID.equals(ID)) {
                    if (pair.getKey().isRegistered()) {
                        pair.getKey().keyFor(selector).cancel();
                    }
                }
            }
        }
    }

    @Override
    public void registerPlayer(String ID) {
        synchronized (selector_sync) {
            for (Map.Entry<SocketChannel, Client> pair : clients.entrySet()) {
                if (pair.getValue().ID.equals(ID)) {
                    if (!pair.getKey().isRegistered()) {
                        try {
                            pair.getKey().register(selector, SelectionKey.OP_READ);
                        } catch (ClosedChannelException e) {
                            //Chanel is cloased
                            //Ignore it
                        }
                    }
                }
            }
        }
    }

    @Override
    public void deleteLater(String game_service_id) {
        game_services_to_delete.add(new DelayedGameService(game_service_id, 10));
    }
}