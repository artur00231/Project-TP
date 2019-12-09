package tp_project.Server;

import tp_project.Network.SocketIO;

public class GameServiceFactory {
    static public GameService getGameService(String game_service_name, String ID, String sKey, String host, SocketIO host_socketIO, String host_id, GameServiceManager manager) {
        if (game_service_name.equals("GoGame")) {
            return new GoGameService(ID, sKey, host, host_socketIO, host_id, manager);
        }

        return null;
    }
}