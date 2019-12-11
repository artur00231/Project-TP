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

    private ClientListener client_listener = new ClientListener() {
        @Override
        public void updated() {}
        @Override
        public void positionChanged() {}
        @Override
        public void recived(ICommand command, String request) {}
        @Override
        public void recived(int code, String request) {}
    };
    private SocketIO socketIO;
    private String sKey = "";
    private String name = "name";
    private POSITION position;
    private enum RESPONSETYPE { CODE, SERVERCOMMAND, OBJECT, EXTENDENT };
    protected RESPONSETYPE response_type;
    protected boolean wait_for_response = false;
    protected String request = "";


    protected Client(SocketIO socketIO) {
        this.socketIO = socketIO;

        connectToServer();
    }

    public void setClientListener(ClientListener new_client_listener) {
        client_listener = new_client_listener;
    }

    public void update() {
        if (position == POSITION.DISCONNECTED) return;
        SocketIO.AVAILABILITY status = socketIO.isAvailable();

        if (status == AVAILABILITY.DISCONNECTED) {
            position = POSITION.DISCONNECTED;

            return;
        }

        if (status == AVAILABILITY.NO) return;

        if (!wait_for_response) {
            while (socketIO.getCommand() != null) {
                if (socketIO.getCommand().getType().equals("ServerCommand")) {
                    if (((ServerCommand) socketIO.getCommand().getCommand()).getCode() == 301) {
                        getLocation(); 
                    }
                }

                socketIO.popCommand();
            }
            return;
        }

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
                break;
            }
        }
        
    }

    protected abstract void handleExtendentCommand();
    public abstract String getGameName();
    public abstract String getGameFiltr();

    public POSITION getPosition() {
        return position;
    }

    public STATUS getGameServicesInfo() {
        if (position != POSITION.SERVER) return STATUS.WPOS;
        if (wait_for_response != false) return STATUS.BUSY;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("action", "getServicesInfo");
        socketIO.send(cmd);

        wait_for_response = true;
        response_type = RESPONSETYPE.OBJECT;
        request = "getServicesInfo";
        
        return STATUS.OK;
    }

    public STATUS getGameServiceInfo() {
        if (position != POSITION.GAMESERVICE) return STATUS.WPOS;
        if (wait_for_response != false) return STATUS.BUSY;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("getServiceInfo", "true");
        socketIO.send(cmd);

        wait_for_response = true;
        response_type = RESPONSETYPE.OBJECT;
        request = "getServiceInfo";

        return STATUS.OK;
    }

    public STATUS kick(String player_id) {
        if (position != POSITION.GAMESERVICE) return STATUS.WPOS;
        if (wait_for_response != false) return STATUS.BUSY;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("kick", player_id);
        cmd.addValue("sKey", sKey.equals("") ? "XX" : sKey);
        socketIO.send(cmd);

        wait_for_response = true;
        response_type = RESPONSETYPE.CODE;
        request = "kick";

        return STATUS.OK;
    }

    public STATUS createGame() {
        if (position != POSITION.SERVER) return STATUS.WPOS;
        if (wait_for_response != false) return STATUS.BUSY;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("action", "create");
        cmd.addValue("type", getGameName());
        cmd.addValue("name", name);
        socketIO.send(cmd);

        wait_for_response = true;
        response_type = RESPONSETYPE.SERVERCOMMAND;
        request = "create";

        return STATUS.OK;
    }

    public STATUS setReady(boolean ready) {
        /*if (position != POSITION.GAMESERVICE) return STATUS.WPOS;
        if (wait_for_response != false) return STATUS.BUSY;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("action", "connect");
        cmd.addValue("game", game_service_id);
        cmd.addValue("name", name);

        wait_for_response = true;
        response_type = RESPONSETYPE.SERVERCOMMAND;
        request = "connect";
        */
        return STATUS.OK;
    }

    public STATUS exit() {
        if (position == POSITION.DISCONNECTED) return STATUS.WPOS;
        if (wait_for_response != false) return STATUS.BUSY;

        ServerCommand cmd = new ServerCommand();
        
        switch (position) {
            case SERVER:
                cmd.addValue("action", "exit");
                position = POSITION.DISCONNECTED;
                socketIO.send(cmd);
                break;
            case GAMESERVICE:
                cmd.addValue("exit", "true");
                wait_for_response = true;
                response_type = RESPONSETYPE.CODE;
                request = "exit";
                socketIO.send(cmd);
                break;
            case GAME:
                //TODO
                break;
        
            default:
                break;
        }

        return STATUS.OK;
    }

    public STATUS connect(String game_service_id) {
        if (position != POSITION.SERVER) return STATUS.WPOS;
        if (wait_for_response != false) return STATUS.BUSY;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("action", "connect");
        cmd.addValue("game", game_service_id);
        cmd.addValue("name", name);
        socketIO.send(cmd);

        wait_for_response = true;
        response_type = RESPONSETYPE.SERVERCOMMAND;
        request = "connect";

        return STATUS.OK;
    }

    //TODO public STATUS addBot(boolean ready);

    public STATUS getLocation() {
        if (position == POSITION.DISCONNECTED) return STATUS.WPOS;
        if (wait_for_response != false) return STATUS.BUSY;

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("ping", "true");
        socketIO.send(cmd);

        wait_for_response = true;
        response_type = RESPONSETYPE.SERVERCOMMAND;
        request = "ping";

        return STATUS.OK;
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
                        ID = cmd.getValue("ID");
                        position = POSITION.SERVER;
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
        while (socketIO.getCommand() != null && !socketIO.getCommand().getType().equals("ServerCommand")) {
            socketIO.popCommand();
        }
        if (socketIO.getCommand() == null) return;

        ServerCommand cmd = (ServerCommand)socketIO.popCommand().getCommand();

        switch (request) {
            case "exit": {
                if (cmd.getCode() == 200) {
                    getLocation();
                }
            }
            default: {
               client_listener.recived(cmd.getCode(), request);
           }
        }
    }

    private void handleCommandResponse() {
        if (socketIO.isAvailable() != AVAILABILITY.YES) return;
        while (socketIO.getCommand() != null && !socketIO.getCommand().getType().equals("ServerCommand")) {
            socketIO.popCommand();
        }
        if (socketIO.getCommand() == null) return;

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
                    client_listener.recived(cmd.getCode(), request);
                }
                break;
            }
            case "create": {
                if (cmd.getCode() == 201) {
                    position = POSITION.GAMESERVICE;
                    sKey = cmd.getValue("sKey");
                    client_listener.positionChanged();
                } else {
                    client_listener.recived(cmd.getCode(), request);
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