package GoServer;

import GoGame.GoClient;
import GoGame.GoRemotePlayer;

public class GoServerClient {
    private String ID;
    private GoClient go_client = null;
    private GoClientAdapter go_client_adapter = null;
    private GoRemotePlayer go_player = null;
    private GoPlayerAdapter go_player_adapter = null;


    public GoServerClient(String ID) {
        this.ID = ID;
    }

    public String getID() {
        return ID;
    }
    
    public GoClient getGoClient() {
        return go_client;
    }

    public GoClientAdapter getGoClientAdapter() {
        return go_client_adapter;
    }

    public GoRemotePlayer getGoPlayer() {
        return go_player;
    }

    public GoPlayerAdapter getGoPlayerAdapter() {
        return go_player_adapter;
    }


    public void setGoClient(GoClient client) {
        go_client = client;
        
        if (go_client == null) return;

        go_client_adapter = new GoClientAdapter();
        go_client.setClientListener(go_client_adapter);
    }

    public void createGoPlayer() {
        go_player = getGoClient().getPlayer();
        
        if (go_player == null) return;

        go_player_adapter = new GoPlayerAdapter();
        go_player.setListener(go_player_adapter);
    }
}