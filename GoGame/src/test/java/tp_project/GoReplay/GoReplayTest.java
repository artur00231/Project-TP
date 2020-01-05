package tp_project.GoReplay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import tp_project.GoGame.GoClient;
import tp_project.Network.ICommand;
import tp_project.Server.ClientListener;
import tp_project.Server.GameServiceInfo;
import tp_project.Server.GameServicesInfo;
import tp_project.Server.Server;
import tp_project.Server.Client.POSITION;

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
public class GoReplayTest {
    public String IP = "127.0.0.1";
    public int PORT = 5004;

    @Test(timeout = 3000)
    public void test1() throws InterruptedException, IOException {
        Server server = new Server(PORT);
        assertTrue(server.isValid());
        Thread t = new Thread(server);
        t.start();

        GoReplayClient client1 = GoReplayClient.create(IP, PORT, "A").get();
        ClientAdapter adapter1 = new ClientAdapter();
        client1.setClientListener(adapter1);
        GoReplayClient client2 = GoReplayClient.create(IP, PORT, "B").get();
        ClientAdapter adapter2 = new ClientAdapter();
        client2.setClientListener(adapter2);
        GoReplayClient client3 = GoReplayClient.create(IP, PORT, "C").get();
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

        assertEquals(false, client3.getGameServiceInfo());
        client3.getGameServicesInfo();
        while(!adapter3.diff()) client3.update();
        GameServicesInfo info = (GameServicesInfo)adapter3.last_command;
        assertEquals(2, info.game_services.size());
        adapter3.reset();

        String to_connect = info.game_services.stream().filter(x -> x.host_id.equals(client1.getID())).findFirst().get().ID;

        client3.connect(to_connect);
        while(adapter3.error == false) client3.update();
        assertEquals(POSITION.SERVER, client3.getPosition());

        client1.getGameServiceInfo();
        while(!adapter1.diff()) client1.update();
        GameServiceInfo info2 = (GameServiceInfo) adapter1.last_command;
        assertEquals(1, info2.players.size());
        adapter1.reset();

        adapter3.reset();
        assertEquals(false, client3.getGameServiceInfo());
        client3.getGameServicesInfo();
        while(!adapter3.diff()) client3.update();
        info = (GameServicesInfo)adapter3.last_command;
        assertEquals(2, info.game_services.size());
        adapter3.reset();

        client1.getGoReplayServiceInfo();
        while(!adapter1.diff()) client1.update();
        GoReplayServiceInfo info3 = (GoReplayServiceInfo) adapter1.last_command;
        assertEquals(1, info3.getPlayersInfo().size());
        assertEquals(false, info3.getPlayersInfo().get(0).ready);
        adapter1.reset();

        server.kill();
        t.join();
    }

    @Test(timeout = 3000)
    public void test2() throws InterruptedException, IOException {
        Server server = new Server(PORT);
        assertTrue(server.isValid());
        Thread t = new Thread(server);
        t.start();

        GoReplayClient client1 = GoReplayClient.create(IP, PORT, "A").get();
        ClientAdapter adapter1 = new ClientAdapter();
        client1.setClientListener(adapter1);
        GoReplayClient client2 = GoReplayClient.create(IP, PORT, "B").get();
        ClientAdapter adapter2 = new ClientAdapter();
        client2.setClientListener(adapter2);

        GoClient client3 = GoClient.create(IP, PORT, "A").get();
        ClientAdapter adapter3 = new ClientAdapter();
        client3.setClientListener(adapter3);
        GoClient client4 = GoClient.create(IP, PORT, "B").get();
        ClientAdapter adapter4 = new ClientAdapter();
        client4.setClientListener(adapter4);

        assertEquals(POSITION.SERVER, client1.getPosition());
        assertEquals(POSITION.SERVER, client2.getPosition());
        assertEquals(POSITION.SERVER, client3.getPosition());
        assertEquals(POSITION.SERVER, client4.getPosition());

        client1.createGame();
        while(!adapter1.diff()) client1.update();
        assertEquals(POSITION.GAMESERVICE, client1.getPosition());
        adapter1.reset();

        client3.createGame();
        while(!adapter3.diff()) client3.update();
        assertEquals(POSITION.GAMESERVICE, client3.getPosition());
        adapter3.reset();

        assertEquals(false, client2.getGameServiceInfo());
        client2.getGameServicesInfo();
        while(!adapter2.diff()) client2.update();
        GameServicesInfo info = (GameServicesInfo)adapter2.last_command;
        assertEquals(1, info.game_services.size());
        adapter2.reset();

        assertEquals(false, client4.getGameServiceInfo());
        client4.getGameServicesInfo();
        while(!adapter4.diff()) client4.update();
        info = (GameServicesInfo)adapter4.last_command;
        assertEquals(1, info.game_services.size());
        adapter4.reset();

        server.kill();
        t.join();
    }
}