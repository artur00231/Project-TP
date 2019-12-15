package tp_project.Server;

import java.time.Instant;

import tp_project.Network.ICommand;
import tp_project.Network.SocketIO;
import tp_project.Network.SocketIO.AVAILABILITY;

public abstract class Client {
    public enum POSITION {
        SERVER, GAMESERVICE, GAME, DISCONNECTED
    };

    public enum STATUS {
        OK, WPOS, BUSY
    };

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
    protected enum RESPONSETYPE { CODE, SERVERCOMMAND, OBJECT, EXTENDENT };
    private RESPONSETYPE response_type;
    private boolean wait_for_response = false;
    private String request = "";


    protected Client(SocketIO socketIO, String name) {
        this.socketIO = socketIO;
        this.name = name;

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
                        while (socketIO.popCommand() != null) continue;
                        getLocation();
                        socketIO.popCommand();

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

            if (socketIO.getCommand() == null) return;

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
    }

    protected abstract void handleExtendentCommand(ICommand command, String request);
    public abstract String getGameName();
    public abstract String getGameFiltr();

    public POSITION getPosition() {
        return position;
    }

    public STATUS getGameServicesInfo() {
        if (position != POSITION.SERVER) return STATUS.WPOS;
        if (isWaiting()) return STATUS.BUSY;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("action", "getServicesInfo");
        send(cmd, RESPONSETYPE.OBJECT, "getServicesInfo");
        
        return STATUS.OK;
    }

    public STATUS getGameServiceInfo() {
        if (position != POSITION.GAMESERVICE) return STATUS.WPOS;
        if (isWaiting()) return STATUS.BUSY;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("getServiceInfo", "true");
        send(cmd, RESPONSETYPE.OBJECT, "getServiceInfo");

        return STATUS.OK;
    }

    public STATUS kick(String player_id) {
        if (position != POSITION.GAMESERVICE) return STATUS.WPOS;
        if (isWaiting()) return STATUS.BUSY;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("kick", player_id);
        cmd.addValue("sKey", sKey.equals("") ? "XX" : sKey);
        send(cmd, RESPONSETYPE.CODE, "kick");

        return STATUS.OK;
    }

    public STATUS createGame() {
        if (position != POSITION.SERVER) return STATUS.WPOS;
        if (isWaiting()) return STATUS.BUSY;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("action", "create");
        cmd.addValue("type", getGameName());
        cmd.addValue("name", name);
        send(cmd, RESPONSETYPE.SERVERCOMMAND, "create");

        return STATUS.OK;
    }

    public STATUS setReady(boolean ready) {
        if (position != POSITION.GAMESERVICE) return STATUS.WPOS;
        if (isWaiting()) return STATUS.BUSY;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("ready", Boolean.toString(ready));
        send(cmd, RESPONSETYPE.SERVERCOMMAND, "create");

        return STATUS.OK;
    }

    public STATUS exit() {
        if (position == POSITION.DISCONNECTED) return STATUS.WPOS;
        if (isWaiting()) return STATUS.BUSY;

        ServerCommand cmd = new ServerCommand();
        
        switch (position) {
            case SERVER:
                cmd.addValue("action", "exit");
                position = POSITION.DISCONNECTED;
                socketIO.send(cmd);
                break;
            case GAMESERVICE:
                cmd.addValue("exit", "true");
                send(cmd, RESPONSETYPE.CODE, "exit");
                break;
            case GAME:
                return STATUS.WPOS;
        
            default:
                break;
        }

        return STATUS.OK;
    }

    public STATUS connect(String game_service_id) {
        if (position != POSITION.SERVER) return STATUS.WPOS;
        if (isWaiting() != false) return STATUS.BUSY;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("action", "connect");
        cmd.addValue("game", game_service_id);
        cmd.addValue("name", name);
        send(cmd, RESPONSETYPE.SERVERCOMMAND, "connect");

        return STATUS.OK;
    }

    //TODO public STATUS addBot(boolean ready);

    public STATUS getLocation() {
        if (position == POSITION.DISCONNECTED) return STATUS.WPOS;
        if (isWaiting()) return STATUS.BUSY;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("ping", "true");
        send(cmd, RESPONSETYPE.SERVERCOMMAND, "ping");

        return STATUS.OK;
    }

    protected boolean send(ICommand command, RESPONSETYPE type, String request) {
        if (!socketIO.send(command)) return false;

        wait_for_response = true;
        response_type = type;
        this.request = request;

        return true;
    }

    protected boolean isWaiting() {
        return wait_for_response;
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