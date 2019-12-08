package tp_project.Server;

import java.util.HashMap;
import java.util.Map;

import tp_project.Network.Command;
import tp_project.Network.SocketIO;

public abstract class GameService {
    private class Client {
        public String name;
        public SocketIO socketIO;
        public Client(String name, SocketIO socketIO) { this.name = name; this.socketIO = socketIO; };
    }
    private String host;
    private String host_id;
    private String sKey;
    private Map<String, Client> players;
    private String ID;
    private GameServiceMenager menager;

    public abstract int getMaxPlayersCount();
    public abstract String getGameName();
    protected abstract boolean isPlayerReady(String player_id);
    protected abstract void setPlayerReady(String player_id, boolean ready);
    protected abstract boolean isGameRedy();
    protected abstract void startGame();

    public GameService(String ID, String sKey, String host, SocketIO host_socketIO, String host_id, GameServiceMenager meanager) {
        this.ID = ID;
        this.host = host;
        this.host_id = host_id;
        menager = meanager;
        this.sKey = sKey;

        players = new HashMap<String, Client>();
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

        return true;
    }

    public boolean checkPlayer(String player_id) {
        return players.get(player_id) != null;
    }

    public boolean removePlayer(String player_id) {
        if (!checkPlayer(player_id)) return false;

        if (player_id.equals(host_id)) {
            menager.deleteLater(ID);

            for (String player : players.keySet()) {
                kickPlayer(player);
            }
        }

        players.remove(player_id);
        menager.playerRemoved(player_id);
        return true;
    }

    public boolean kickPlayer(String player_id) {
        if (!checkPlayer(player_id)) return false;
        if (player_id.equals(host_id)) return false;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("kicked", host);
        cmd.setCode(301);
        players.get(player_id).socketIO.send(cmd);

        return removePlayer(player_id);
    }

    public void setReady(String player_id, boolean ready, String sKey) {
        if (player_id == host_id && checkSKey(sKey)) {
            setPlayerReady(player_id, ready);
        } else if (player_id != host_id) {
            setPlayerReady(player_id, ready);
        }

        if (isGameRedy()) {
            startGame();
        }
    }

    public void update(String player_id) {
        Client client = players.get(player_id);
        if (client == null) return;

        SocketIO socketIO = client.socketIO;
        Command command = socketIO.getCommand();

        if (command == null) return;
        if (!(command.getCommand().getCommandType().equals("ServerCommand"))) return;
        socketIO.popCommand();

        ServerCommand cmd = (ServerCommand)command.getCommand();
        if (cmd.getValue("ready") != null) {
            setPlayerReady(player_id, Boolean.valueOf(cmd.getValue("ready")));
            sendCode(200, socketIO);
        } else if (cmd.getValue("exit") != null) {
            removePlayer(player_id);
            sendCode(200, socketIO);
        } else if (cmd.getValue("kick") != null) {
            if (!checkSKey(cmd.getValue("sKey"))) { sendCode(403, socketIO); }
            if (kickPlayer(cmd.getValue("kick"))) {
                sendCode(200, socketIO);
            } else {
                sendCode(400, socketIO);
            }
        } else if (cmd.getValue("getServiceInfo") != null) {
            socketIO.send(getInfo());
        }
    }

    protected void sendCode(int code, SocketIO socketIO) {
        ServerCommand cmd = new ServerCommand();
        cmd.setCode(code);
        socketIO.send(cmd);
    }
}