package GoServer;

import java.util.concurrent.Semaphore;

import GoGame.GoClient;
import GoGame.GoRemotePlayer;

public class GoServerClient {
    private String ID;
    private GoClient go_client = null;
    private GoClientAdapter go_client_adapter = null;
    private GoRemotePlayer go_player = null;
    private GoPlayerAdapter go_player_adapter = null;
    private boolean auto_update = true;
    private boolean render_site = true;
    private String message = "";

    public Semaphore semaphore = new Semaphore(1);

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

    public boolean getAutoUpdate() {
        return auto_update;
    }

    public boolean getRender() {
        return render_site;
    }

    public String getMessage() {
        String msg_cpy = new String(message);
        message = "";
        return msg_cpy;
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

    public void removeGoPlayer() {
        go_player = null;
        go_player_adapter = null;
    }

    public void setAutoUpdate(boolean auto_update) {
        this.auto_update = auto_update;
    }

    public void setRender(boolean render) {
        this.render_site = render;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}