package tp_project.Server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import tp_project.GoGame.GoClient;
import tp_project.Network.ICommand;
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

}

public class ClientTest {
    public String IP = "127.0.0.1";
    public int PORT = 5004;

    @Test(timeout = 3000)
    public void test1() throws InterruptedException, IOException {
        Server server = new Server(PORT);
        assertTrue(server.isValid());
        Thread t = new Thread(server);
        t.start();

        Client client = GoClient.create(IP, PORT, "test").get();
        ClientAdapter clientAdapter = new ClientAdapter();
        client.setClientListener(clientAdapter);

        assertEquals(Client.POSITION.SERVER, client.getPosition());

        client.getGameServicesInfo();

        while(clientAdapter.last_command == null) client.update();

        assertEquals(0, ((GameServicesInfo)clientAdapter.last_command).game_services.size());

        server.kill();
        t.join();
    }

    @Test(timeout = 300000)
    public void test2() throws IOException, InterruptedException {
        Server server = new Server(PORT);
        assertTrue(server.isValid());
        Thread t = new Thread(server);
        t.start();

        Client c1 = GoClient.create(IP, PORT, "test").get();
        ClientAdapter a1 = new ClientAdapter();
        c1.setClientListener(a1);
        assertEquals(Client.POSITION.SERVER, c1.getPosition());
        Client c2 = GoClient.create(IP, PORT, "test").get();
        ClientAdapter a2 = new ClientAdapter();
        c2.setClientListener(a2);
        assertEquals(Client.POSITION.SERVER, c2.getPosition());


        c1.createGame();
        while(a1.pos_changed == false) c1.update();
        assertEquals(POSITION.GAMESERVICE, c1.getPosition());
        a1.pos_changed = false;

        c2.getGameServicesInfo();
        while(a2.last_command == null) c2.update();
        assertEquals(1, ((GameServicesInfo) a2.last_command).game_services.size());

        a2.pos_changed = false;
        c2.connect(((GameServicesInfo) a2.last_command).game_services.get(0).ID);
        while(a2.pos_changed == false) c2.update();
        assertEquals(POSITION.GAMESERVICE, c2.getPosition());

        c1.getGameServiceInfo();
        while(a1.last_command == null) c1.update();
        assertEquals(2, ((GameServiceInfo) a1.last_command).players.size());

        GameServiceInfo info = (GameServiceInfo) a1.last_command;
        String player_to_kick = null;
        for (String player : info.players.keySet()) {
            if (!player.equals(info.host_id)) {
                player_to_kick = player;
            }
        }

        a2.pos_changed = false;
        c1.kick(player_to_kick);
        while(a1.updated == false) c1.update();;

        while(a2.pos_changed == false) c2.update();
        assertEquals(Client.POSITION.SERVER, c2.getPosition());

        a2.last_command = null;
        c2.getGameServicesInfo();
        while(a2.last_command == null) c2.update();
        assertEquals(1, ((GameServicesInfo) a2.last_command).game_services.size());

        server.kill();
        t.join();
    }
}