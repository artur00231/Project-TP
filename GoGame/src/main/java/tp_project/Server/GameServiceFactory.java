package tp_project.Server;

import tp_project.Network.SocketIO;

public class GameServiceFactory {
    static public GameService getGameService(String game_serice_name, String ID, String sKey, String host, SocketIO host_socketIO, String host_id, GameServiceMenager meanager) {
        if (game_serice_name.equals("GoGame")) {
            return new GoGameService(ID, sKey, host, host_socketIO, host_id, meanager);
        }

        return null;
    }
}