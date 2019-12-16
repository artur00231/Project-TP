package tp_project.Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import tp_project.Network.Command;
import tp_project.Network.SocketIO;

public abstract class GameService implements GameManager {
    private static class Client {
        public String name;
        public SocketIO socketIO;
        public Client(String name, SocketIO socketIO) { this.name = name; this.socketIO = socketIO; };
    }
    private String host;
    private String host_id;
    private String sKey;
    private Map<String, Client> players;
    private String ID;
    private GameServiceManager manager;

    public abstract int getMaxPlayersCount();
    public abstract String getGameName();
    protected abstract boolean isPlayerReady(String player_id);
    protected abstract void setPlayerReady(String player_id, boolean ready);
    protected abstract boolean isGameReady();
    protected abstract void startGame();
    protected abstract boolean handleExtendetCommands(String client, ServerCommand command, SocketIO socketIO);

    public GameService(String ID, String sKey, String host, SocketIO host_socketIO, String host_id, GameServiceManager meanager) {
        this.ID = ID;
        this.host = host;
        this.host_id = host_id;
        manager = meanager;
        this.sKey = sKey;

        players = new HashMap<>();
        players.put(host_id, new Client(host, host_socketIO));
    }

    public String getID() {
        return ID;
    }

    public boolean checkSKey(String sKey) {
        return this.sKey.equals(sKey);
    }

    public GameServiceInfo getInfo() {
        GameServiceInfo info = new GameServiceInfo();
        info.ID = ID;
        info.host_id = host_id;
        info.max_players = getMaxPlayersCount();
        info.players = new HashMap<>();

        for (Map.Entry<String, Client> player: players.entrySet()) {
            info.players.put(player.getKey(), player.getValue().name);
        }
 
        return info;
    }

    public boolean addPlayer(String player_name, String player_id, SocketIO socketIO) {
        if (players.size() == getMaxPlayersCount()) return false;
        if (players.get(player_id) != null) {
            return false;
        }

        players.put(player_id, new Client(player_name, socketIO));
        updatePlayers();

        return true;
    }

    public boolean checkPlayer(String player_id) {
        return players.get(player_id) != null;
    }

    public boolean removePlayer(String player_id) {
        if (!checkPlayer(player_id)) return false;

        if (player_id.equals(host_id)) {
            manager.deleteLater(ID);

            ArrayList<String> to_kick = new ArrayList<>();

            for (String player : players.keySet()) {
                to_kick.add(player);
            }
            for (String player : to_kick) {
                kickPlayer(player);
            }
        }

        players.remove(player_id);
        manager.playerRemoved(player_id);
        return true;
    }

    public boolean kickPlayer(String player_id) {
        if (!checkPlayer(player_id)) return false;
        if (player_id.equals(host_id)) return false;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("kicked", host);
        cmd.setCode(301);
        if (players.get(player_id).socketIO != null) {
            players.get(player_id).socketIO.send(cmd);
        }

        return removePlayer(player_id);
    }

    public void setReady(String player_id, boolean ready) {
        setPlayerReady(player_id, ready);

        if (isGameReady()) {
            startGame();
        }
    }

    public void update(String player_id) {
        Client client = players.get(player_id);
        boolean updated = false;
        if (client == null) return;

        SocketIO socketIO = client.socketIO;
        Command command = socketIO.getCommand();

        if (command == null) return;
        if (!(command.getCommand().getCommandType().equals("ServerCommand"))) return;
        socketIO.popCommand();

        ServerCommand cmd = (ServerCommand)command.getCommand();
        if (cmd.getValue("ready") != null) {
            setReady(player_id, Boolean.parseBoolean(cmd.getValue("ready")));
            if (!isGameReady()) sendCode(200, socketIO);
            updated = true;
        } else if (cmd.getValue("exit") != null) {
            removePlayer(player_id);
            sendCode(200, socketIO);
            updated = true;
        } else if (cmd.getValue("kick") != null) {
            if (!checkSKey(cmd.getValue("sKey"))) { sendCode(400, socketIO); return; }
            if (kickPlayer(cmd.getValue("kick"))) {
                sendCode(200, socketIO);
                updated = true;
            } else {
                sendCode(400, socketIO);
            }
        } else if (cmd.getValue("getServiceInfo") != null) {
            socketIO.send(getInfo());
        } else if (cmd.getValue("add") != null) {
            addPlayer("BOT", "0000000000", null);
            setPlayerReady("0000000000", true);
        } else if (cmd.getValue("ping") != null) {
            ServerCommand ping = new ServerCommand();
            ping.setCode(1);
            ping.addValue("ping", "0");

            client.socketIO.send(ping);
        } else if (cmd.getValue("ready") != null) {
            setReady(player_id, Boolean.parseBoolean(cmd.getValue("ready")));
            updated = true;
        } else {
            updated = updated || handleExtendetCommands(player_id, cmd, socketIO);
        }

        if (updated) {
            updatePlayers();
        }
    }

    protected void sendCode(int code, SocketIO socketIO) {
        if (socketIO == null) return;
        ServerCommand cmd = new ServerCommand();
        cmd.setCode(code);
        socketIO.send(cmd);
    }

    protected void updatePlayers() {
        for (Map.Entry<String, Client> pair : players.entrySet()) {
            sendCode(302, pair.getValue().socketIO);
        }
    }

    @Override
    public void gameEnded()
    {
        for (Map.Entry<String, Client> pair : players.entrySet()) {
            manager.registerPlayer(pair.getKey());
        }

        for (Map.Entry<String, Client> pair : players.entrySet()) {
            sendCode(301, pair.getValue().socketIO);
        }
    }

    @Override
    public void gameStated()
    {
        for (Map.Entry<String, Client> pair : players.entrySet()) {
            manager.unregisterPlayer(pair.getKey());
        }

        for (Map.Entry<String, Client> pair : players.entrySet()) {
            sendCode(301, pair.getValue().socketIO);
        }
    }

    protected SocketIO getClientSocketIO(String client_id) {
        if (players.get(client_id) == null) {
            return null;
        }

        return players.get(client_id).socketIO;
    }
}