package Server;

import java.time.Instant;
import java.util.ArrayList;

import Network.ICommand;
import Network.SocketIO;
import Network.SocketIO.AVAILABILITY;

public abstract class Client {
    public enum POSITION {
        SERVER, GAMESERVICE, GAME, DISCONNECTED
    };
    protected enum RESPONSETYPE {
        CODE, SERVERCOMMAND, OBJECT, EXTENDENT 
    };

    static public class Request {
        public ICommand command;
        public RESPONSETYPE type;
        public String request_name;

        public Request(ICommand cmd, String name, RESPONSETYPE type) {
            command = cmd;
            request_name = name;
            this.type = type;
        }
    }

    protected ClientListener client_listener = new ClientListener() {
        @Override
        public void updated() {}
        @Override
        public void positionChanged() {}
        @Override
        public void recived(ICommand command, String request) {}
        @Override
        public void error(String request) {}
    };
    protected SocketIO socketIO;
    private String sKey = "XX";
    private String ID = "XX";
    private String name = "XX";
    private POSITION position;
    private RESPONSETYPE response_type;
    private boolean wait_for_response = false;
    private String request = "";
    private ArrayList<Request> requests;


    protected Client(SocketIO socketIO, String name) {
        this.socketIO = socketIO;
        this.name = name;
        requests = new ArrayList<>();

        connectToServer();
    }

    public void setClientListener(ClientListener new_client_listener) {
        client_listener = new_client_listener;
    }

    public String getName() {
        return name;
    }

    public String getID() {
        return ID;
    }

    protected String getSKey() {
        return sKey;
    }

    public void update() {
        if (position == POSITION.DISCONNECTED) return;
        SocketIO.AVAILABILITY status = socketIO.isAvailable();

        if (status == AVAILABILITY.DISCONNECTED) {
            position = POSITION.DISCONNECTED;

            return;
        }

        do {
            while (socketIO.getCommand() != null) {
                if (socketIO.getCommand().getType().equals("ServerCommand")) {
                    if (((ServerCommand) socketIO.getCommand().getCommand()).getCode() == 301) {
                        wait_for_response = false;
                        socketIO.isAvailable();
                        while (socketIO.popCommand() != null) socketIO.isAvailable();
                        requests.clear();
                        getLocation();
                        return;
                    } else if (((ServerCommand) socketIO.getCommand().getCommand()).getCode() == 302) {
                        client_listener.updated();
                    } else {
                        break;
                    }
                } else {
                    break;
                }
                socketIO.popCommand();
            }

            if (socketIO.getCommand() == null) break;

            if (wait_for_response)
            {
                if (response_type == RESPONSETYPE.SERVERCOMMAND || response_type == RESPONSETYPE.CODE) {
                    while (socketIO.getCommand() != null && !socketIO.getCommand().getType().equals("ServerCommand")) {
                        socketIO.popCommand();
                    }
                }

                if (socketIO.getCommand() == null) return;

                wait_for_response = false;

                switch (response_type) {
                    case CODE: {
                        handleCodeResponse();
                        break;
                    }
                    case SERVERCOMMAND: {
                        handleCommandResponse();
                        break;
                    }
                    case OBJECT: {
                        client_listener.recived(socketIO.popCommand().getCommand(), request);
                        wait_for_response = false;
                        break;
                    }
                    case EXTENDENT: {
                        handleExtendentCommand(socketIO.popCommand().getCommand(), request);
                        break;
                    }
                }
            } else {
                socketIO.popCommand();
            }   

        } while (socketIO.getCommand() != null);

        if (!wait_for_response) {
            sendRequest();
        }
    }

    protected abstract void handleExtendentCommand(ICommand command, String request);
    public abstract String getGameName();
    public abstract String getGameFiltr();

    public POSITION getPosition() {
        return position;
    }

    public boolean getGameServicesInfo() {
        if (position != POSITION.SERVER) return false;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("action", "getServicesInfo");
        cmd.addValue("filter", getGameFiltr());
        pushRequest(new Request(cmd, "getServicesInfo", RESPONSETYPE.OBJECT));
        sendRequest();
        
        return true;
    }

    public boolean getGameServiceInfo() {
        if (position != POSITION.GAMESERVICE) return false;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("getServiceInfo", "true");
        pushRequest(new Request(cmd, "getServiceInfo", RESPONSETYPE.OBJECT));
        sendRequest();

        return true;
    }

    public boolean kick(String player_id) {
        if (position != POSITION.GAMESERVICE) return false;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("kick", player_id);
        cmd.addValue("sKey", sKey.equals("") ? "XX" : sKey);
        pushRequest(new Request(cmd, "kick", RESPONSETYPE.CODE));
        sendRequest();

        return true;
    }

    public boolean createGame() {
        if (position != POSITION.SERVER) return false;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("action", "create");
        cmd.addValue("type", getGameName());
        cmd.addValue("name", name);
        pushRequest(new Request(cmd, "create", RESPONSETYPE.SERVERCOMMAND));
        sendRequest();

        return true;
    }

    public boolean setReady(boolean ready) {
        if (position != POSITION.GAMESERVICE) return false;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("ready", Boolean.toString(ready));
        pushRequest(new Request(cmd, "ready", RESPONSETYPE.CODE));
        sendRequest();

        return true;
    }

    public boolean exit() {
        if (position == POSITION.DISCONNECTED) return false;

        ServerCommand cmd = new ServerCommand();
        
        switch (position) {
            case SERVER:
                cmd.addValue("action", "exit");
                position = POSITION.DISCONNECTED;
                break;
            case GAMESERVICE:
                cmd.addValue("exit", "true");
                pushRequest(new Request(cmd, "exit",RESPONSETYPE.CODE));
                sendRequest();
                break;
            case GAME:
                return false;
        
            default:
                break;
        }

        return true;
    }

    public boolean connect(String game_service_id) {
        if (position != POSITION.SERVER) return false;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("action", "connect");
        cmd.addValue("game", game_service_id);
        cmd.addValue("name", name);
        pushRequest(new Request(cmd, "connect", RESPONSETYPE.SERVERCOMMAND));
        sendRequest();

        return true;
    }

    public boolean addBot() {
        if (position != POSITION.GAMESERVICE) return false;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("add", "bot");
        pushRequest(new Request(cmd, "addBot", RESPONSETYPE.CODE));
        sendRequest();

        return true;
    }

    public boolean getLocation() {
        if (position == POSITION.DISCONNECTED) return false;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("ping", "true");
        pushRequest(new Request(cmd, "ping", RESPONSETYPE.SERVERCOMMAND));
        sendRequest();

        return true;
    }

    public void disconnect() {
        position = POSITION.DISCONNECTED;
        socketIO.finalize();
    }

    protected void pushRequest(Request request) {
        requests.add(request);
    }

    protected boolean sendRequest() {
        if (requests.isEmpty()) return true;
        if (wait_for_response) return true;

        Request request = requests.get(0);
        requests.remove(0);

        return send(request.command, request.type, request.request_name);
    }

    private boolean send(ICommand command, RESPONSETYPE type, String request) {
        if (!socketIO.send(command)) return false;

        wait_for_response = true;
        response_type = type;
        this.request = request;

        return true;
    }

    protected boolean isBuisy() {
        return wait_for_response || (requests.size() > 0);
    }

    private void connectToServer() {
        long start = Instant.now().toEpochMilli();

        while (Instant.now().toEpochMilli() - start < 2000) { //timeout after 2s
            AVAILABILITY availability = socketIO.isAvailable();

            if (availability == AVAILABILITY.DISCONNECTED) {
                position = POSITION.DISCONNECTED;
                return;
            }

            if (availability == AVAILABILITY.YES) {
                if (!socketIO.getCommand().getType().equals("ServerCommand")) {
                    position = POSITION.DISCONNECTED;
                    return;
                } else {
                    ServerCommand cmd = (ServerCommand)socketIO.popCommand().getCommand();

                    if (cmd.getCode() != 202) {
                        position = POSITION.DISCONNECTED;
                        return;
                    } else {
                        position = POSITION.SERVER;
                        ID = cmd.getValue("ID");
                        return;
                    }
                }
            }
        }

        position = POSITION.DISCONNECTED;
        return;
    }

    private void handleCodeResponse() {
        if (socketIO.isAvailable() != AVAILABILITY.YES) return;
        if (socketIO.getCommand() == null || !socketIO.getCommand().getType().equals("ServerCommand")) return;

        ServerCommand cmd = (ServerCommand)socketIO.popCommand().getCommand();

        switch (request) {
            case "exit": {
                if (cmd.getCode() == 200) {
                    getLocation();
                }
                break;
            }
            case "kick": {
                if (cmd.getCode() != 200) {
                    client_listener.error(request);
                }
                break;
            }
            case "addBot": {
                if (cmd.getCode() != 200) {
                    client_listener.error(request);
                }
                break;
            }
        }
    }

    private void handleCommandResponse() {
        if (socketIO.isAvailable() != AVAILABILITY.YES) return;
        if (socketIO.getCommand() == null || !socketIO.getCommand().getType().equals("ServerCommand")) return;

        ServerCommand cmd = (ServerCommand)socketIO.popCommand().getCommand();

        switch (request) {
            case "ping": {
                if (cmd.getValue("ping") == null) return;
                POSITION new_pos = position;
                if (cmd.getCode() == 0) {
                    new_pos = POSITION.SERVER;
                } else if (cmd.getCode() == 1) {
                    new_pos = POSITION.GAMESERVICE;
                } else if (cmd.getCode() == 2) {
                    new_pos = POSITION.GAME;
                }

                if (new_pos != position) {
                    position = new_pos;
                    client_listener.positionChanged();
                }

                break;
            }
            case "connect": {
                if (cmd.getCode() == 200) {
                    position = POSITION.GAMESERVICE;
                    client_listener.positionChanged();
                } else {
                    client_listener.error(request);
                }
                break;
            }
            case "create": {
                if (cmd.getCode() == 201) {
                    position = POSITION.GAMESERVICE;
                    sKey = cmd.getValue("sKey");
                    client_listener.positionChanged();
                } else {
                    client_listener.error(request);
                }
                break;
            }
            default: {
                //Invalid command
                break;
            }
        }
    }
}