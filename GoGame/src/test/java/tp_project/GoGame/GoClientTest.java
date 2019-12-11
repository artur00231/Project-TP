package tp_project.GoGame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import tp_project.Network.ICommand;
import tp_project.Server.ClientListener;
import tp_project.Server.GameServiceInfo;
import tp_project.Server.GameServicesInfo;
import tp_project.Server.Server;
import tp_project.Server.Client.POSITION;
import tp_project.Server.Client.STATUS;

class ClientAdapter implements ClientListener {
    public ICommand last_command = null;
    public boolean error = false;
    public String last_request = "";
    public boolean updated = false;
    public boolean pos_changed = false;

    @Override
    public void updated() {
        updated = true;
        return;
    }

    @Override
    public void positionChanged() {
        pos_changed = true;
        return;
    }

    @Override
    public void recived(ICommand command, String request) {
        last_command = command;
        last_request = request;
        return;
    }

    @Override
    public void error(String request) {
        error = true;
        last_request = request;
        return;
    }

    public void reset() {
        last_command = null;
        error = false;
        last_request = "";
        updated = false;
        pos_changed = false;
    }

    public boolean diff() {
        return last_command != null ||
        error != false ||
        last_request != "" ||
        updated != false ||
        pos_changed != false;
    }
}
public class GoClientTest {
    public String IP = "127.0.0.1";
    public int PORT = 5004;

    @Test(timeout = 3000)
    public void test1() throws InterruptedException, IOException {
        Server server = new Server(PORT);
        assertTrue(server.isValid());
        Thread t = new Thread(server);
        t.start();

        GoClient client1 = GoClient.create(IP, PORT, "A").get();
        ClientAdapter adapter1 = new ClientAdapter();
        client1.setClientListener(adapter1);
        GoClient client2 = GoClient.create(IP, PORT, "B").get();
        ClientAdapter adapter2 = new ClientAdapter();
        client2.setClientListener(adapter2);
        GoClient client3 = GoClient.create(IP, PORT, "C").get();
        ClientAdapter adapter3 = new ClientAdapter();
        client3.setClientListener(adapter3);

        assertEquals(POSITION.SERVER, client1.getPosition());
        assertEquals(POSITION.SERVER, client2.getPosition());
        assertEquals(POSITION.SERVER, client3.getPosition());

        client1.createGame();
        while(!adapter1.diff()) client1.update();
        assertEquals(POSITION.GAMESERVICE, client1.getPosition());
        adapter1.reset();

        client2.createGame();
        while(!adapter2.diff()) client2.update();
        assertEquals(POSITION.GAMESERVICE, client2.getPosition());
        adapter2.reset();

        assertEquals(STATUS.WPOS, client3.getGameServiceInfo());
        client3.getGameServicesInfo();
        while(!adapter3.diff()) client3.update();
        GameServicesInfo info = (GameServicesInfo)adapter3.last_command;
        assertEquals(2, info.game_services.size());
        adapter3.reset();

        String to_connect = info.game_services.stream().filter(x -> x.host_id.equals(client1.getID())).findFirst().get().ID;

        client3.connect(to_connect);
        while(adapter3.pos_changed == false) client3.update();
        assertEquals(POSITION.GAMESERVICE, client3.getPosition());

        while(!adapter1.diff()) client1.update();
        adapter3.reset();
        adapter1.reset();

        client3.setReady(true);
        while(!adapter1.diff()) client1.update();
        while(!adapter3.diff()) client3.update();
        adapter3.reset();
        adapter1.reset();

        client1.getGameServiceInfo();
        while(!adapter1.diff()) client1.update();
        GameServiceInfo info2 = (GameServiceInfo) adapter1.last_command;
        String player_to_kick = null;
        for (String player : info2.players.keySet()) {
            if (!player.equals(info2.host_id)) {
                player_to_kick = player;
            }
        }
        adapter1.reset();

        client3.kick(player_to_kick);
        while(!adapter3.diff()) client3.update();
        assertEquals(true, adapter3.error);
        adapter3.reset();

        client1.kick(player_to_kick);
        while(!adapter1.diff()) client1.update();
        assertEquals(true, adapter1.updated);
        adapter1.reset();
        
        while(!adapter3.diff()) client3.update();
        assertEquals(POSITION.SERVER, client3.getPosition());

        client1.getGameServiceInfo();
        while(!adapter1.diff()) client1.update();
        info2 = (GameServiceInfo) adapter1.last_command;
        assertEquals(1, info2.players.size());
        adapter1.reset();

        adapter3.reset();
        assertEquals(STATUS.WPOS, client3.getGameServiceInfo());
        client3.getGameServicesInfo();
        while(!adapter3.diff()) client3.update();
        info = (GameServicesInfo)adapter3.last_command;
        assertEquals(2, info.game_services.size());
        adapter3.reset();

        to_connect = info.game_services.stream().filter(x -> x.host_id.equals(client2.getID())).findFirst().get().ID;

        client3.connect(to_connect);
        while(adapter3.pos_changed == false) client3.update();
        assertEquals(POSITION.GAMESERVICE, client3.getPosition());

        while(!adapter2.diff()) client2.update();
        adapter3.reset();
        adapter2.reset();

        client3.setReady(true);
        while(!adapter2.diff()) client2.update();
        while(!adapter3.diff()) client3.update();
        adapter3.reset();
        adapter2.reset();

        client2.exit();
        while(!adapter2.diff()) client2.update();
        client2.exit();
        while(!adapter2.diff()) client2.update();
        assertEquals(POSITION.DISCONNECTED, client2.getPosition());

        while(!adapter3.diff()) client3.update();
        assertEquals(POSITION.SERVER, client3.getPosition());

        adapter3.reset();
        client3.getGameServicesInfo();
        while(!adapter3.diff()) client3.update();
        info = (GameServicesInfo)adapter3.last_command;
        assertEquals(1, info.game_services.size());
        adapter3.reset();

        to_connect = info.game_services.stream().filter(x -> x.host_id.equals(client1.getID())).findFirst().get().ID;

        client3.connect(to_connect);
        while(adapter3.pos_changed == false) client3.update();
        assertEquals(POSITION.GAMESERVICE, client3.getPosition());

        while(!adapter1.diff()) client1.update();
        adapter3.reset();
        adapter1.reset();

        adapter3.reset();
        client3.getGoGameServiceInfo();
        while(!adapter3.diff()) client3.update();
        GoGameServiceInfo info3 = (GoGameServiceInfo)adapter3.last_command;
        assertEquals(2, info3.getPlayersInfo().size());
        assertEquals(2, info3.getPlayersInfo().stream().filter(x -> x.ready == false).count());
        assertEquals(1, info3.getPlayersInfo().stream().filter(x -> x.colour == 0).count());
        assertEquals(1, info3.getPlayersInfo().stream().filter(x -> x.colour == 1).count());
        int index = info3.getPlayersInfo().indexOf(info3.getPlayersInfo().stream().filter(x -> x.ID.equals(client3.getID())).findFirst().get());
        assertEquals(1, info3.getPlayersInfo().get(index).colour);
        adapter3.reset();

        adapter3.reset();
        client3.flipColours();
        while(!adapter3.diff()) client3.update();
        assertEquals(true, adapter3.error);
        adapter3.reset();

        adapter1.reset();
        client1.flipColours();
        while(!adapter1.diff()) client1.update();
        while(!adapter3.diff()) client3.update();

        adapter3.reset();
        adapter1.reset();
        client3.setReady(true);
        while(!adapter3.diff()) client3.update();
        while(!adapter1.diff()) client1.update();

        adapter3.reset();
        client3.getGoGameServiceInfo();
        while(!adapter3.diff()) client3.update();
        info3 = (GoGameServiceInfo)adapter3.last_command;
        assertEquals(2, info3.getPlayersInfo().size());
        assertEquals(1, info3.getPlayersInfo().stream().filter(x -> x.ready == false).count());
        assertEquals(1, info3.getPlayersInfo().stream().filter(x -> x.colour == 0).count());
        assertEquals(1, info3.getPlayersInfo().stream().filter(x -> x.colour == 1).count());
        index = info3.getPlayersInfo().indexOf(info3.getPlayersInfo().stream().filter(x -> x.ID.equals(client3.getID())).findFirst().get());
        assertEquals(0, info3.getPlayersInfo().get(index).colour);
        index = info3.getPlayersInfo().indexOf(info3.getPlayersInfo().stream().filter(x -> x.ready == true).findFirst().get());
        assertEquals(client3.getID(), info3.getPlayersInfo().get(index).ID);
        adapter3.reset();
        
        server.kill();
        t.join();
    }
}