package tp_project.Server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import org.junit.Test;

import tp_project.Network.SocketIO;

public class GameServiceTest {
    public String IP = "127.0.0.1";
    public int PORT = 5004;

    private SocketIO getNewSocketIO() throws IOException {
        SocketChannel s = SocketChannel.open();
        s.connect(new InetSocketAddress(IP, PORT));
        SocketIO io = new SocketIO(s);
        assertTrue(io.getSatus().is_connected);

        while(io.isAvaiable() != SocketIO.AVAILABILITY.YES) continue;
        assertEquals("ServerCommand", io.popCommand().getType());

        return io;
    }

    @Test(timeout = 3000)
    public void test1() throws IOException, InterruptedException {
        Server server = new Server(PORT);
        assertTrue(server.isValid());
        Thread t = new Thread(server);
        t.start();

        SocketIO io1 = getNewSocketIO();
        SocketIO io2 = getNewSocketIO();

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("action", "create");
        cmd.addValue("name", "test");
        cmd.addValue("type", "GoGame");

        io1.send(cmd);
        while(io1.isAvaiable() != SocketIO.AVAILABILITY.YES) continue;
        assertEquals(201, ((ServerCommand)io1.popCommand().getCommand()).getCode());

        cmd = new ServerCommand();
        cmd.addValue("action", "getServicesInfo");
        io2.send(cmd);
        while(io2.isAvaiable() != SocketIO.AVAILABILITY.YES) continue;
        assertEquals(1, ((GameServicesInfo) io2.popCommand().getCommand()).game_services.size());

        cmd = new ServerCommand();
        cmd.addValue("exit", "true");
        io1.send(cmd);
        while(io1.isAvaiable() != SocketIO.AVAILABILITY.YES) continue;
        assertEquals(200, ((ServerCommand)io1.popCommand().getCommand()).getCode());

        cmd = new ServerCommand();
        cmd.addValue("action", "getServicesInfo");
        io2.send(cmd);
        while(io2.isAvaiable() != SocketIO.AVAILABILITY.YES) continue;
        assertEquals(0, ((GameServicesInfo) io2.popCommand().getCommand()).game_services.size());

        cmd = new ServerCommand();
        cmd.addValue("action", "getServicesInfo");
        io1.send(cmd);
        while(io1.isAvaiable() != SocketIO.AVAILABILITY.YES) continue;
        assertEquals(0, ((GameServicesInfo) io1.popCommand().getCommand()).game_services.size());

        server.kill();
        t.join();
    }

    @Test(timeout = 3000)
    public void test2() throws IOException, InterruptedException {
        Server server = new Server(PORT);
        assertTrue(server.isValid());
        Thread t = new Thread(server);
        t.start();

        SocketIO io1 = getNewSocketIO();
        SocketIO io2 = getNewSocketIO();
        SocketIO io3 = getNewSocketIO();

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("action", "create");
        cmd.addValue("name", "test");
        cmd.addValue("type", "GoGame");

        io1.send(cmd);
        while(io1.isAvaiable() != SocketIO.AVAILABILITY.YES) continue;
        assertEquals(201, ((ServerCommand)io1.getCommand().getCommand()).getCode());
        String game_id = ((ServerCommand)io1.popCommand().getCommand()).getValue("ID");

        cmd = new ServerCommand();
        cmd.addValue("action", "connect");
        cmd.addValue("name", "test");
        cmd.addValue("game", game_id);
        io2.send(cmd);
        while(io2.isAvaiable() != SocketIO.AVAILABILITY.YES) continue;
        assertEquals(200, ((ServerCommand)io2.popCommand().getCommand()).getCode());

        io3.send(cmd);
        while(io3.isAvaiable() != SocketIO.AVAILABILITY.YES) continue;
        assertEquals(400, ((ServerCommand)io3.popCommand().getCommand()).getCode());

        cmd = new ServerCommand();
        cmd.addValue("exit", "true");
        io2.send(cmd);
        while(io2.isAvaiable() != SocketIO.AVAILABILITY.YES) continue;
        assertEquals(200, ((ServerCommand)io2.popCommand().getCommand()).getCode());

        cmd = new ServerCommand();
        cmd.addValue("action", "connect");
        cmd.addValue("name", "test");
        cmd.addValue("game", game_id);
        io3.send(cmd);
        while(io3.isAvaiable() != SocketIO.AVAILABILITY.YES) continue;
        assertEquals(200, ((ServerCommand)io3.popCommand().getCommand()).getCode());

        cmd = new ServerCommand();
        cmd.addValue("action", "getServicesInfo");
        io2.send(cmd);
        while(io2.isAvaiable() != SocketIO.AVAILABILITY.YES) continue;
        assertEquals(2, ((GameServicesInfo) io2.getCommand().getCommand()).game_services.get(0).players.size());
        assertEquals(2, ((GameServicesInfo) io2.getCommand().getCommand()).game_services.get(0).max_players);

        server.kill();
        t.join();
    }

    @Test(timeout = 3000)
    public void test3() throws IOException, InterruptedException {
        Server server = new Server(PORT);
        assertTrue(server.isValid());
        Thread t = new Thread(server);
        t.start();

        SocketIO io1 = getNewSocketIO();
        SocketIO io2 = getNewSocketIO();

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("action", "create");
        cmd.addValue("name", "test");
        cmd.addValue("type", "GoGame");

        io1.send(cmd);
        while(io1.isAvaiable() != SocketIO.AVAILABILITY.YES) continue;
        assertEquals(201, ((ServerCommand)io1.popCommand().getCommand()).getCode());

        cmd = new ServerCommand();
        cmd.addValue("action", "getServicesInfo");
        io2.send(cmd);
        while(io2.isAvaiable() != SocketIO.AVAILABILITY.YES) continue;
        assertEquals(1, ((GameServicesInfo) io2.getCommand().getCommand()).game_services.size());

        cmd = new ServerCommand();
        cmd.addValue("action", "connect");
        cmd.addValue("name", "test");
        cmd.addValue("game", ((GameServicesInfo) io2.popCommand().getCommand()).game_services.get(0).ID);
        io2.send(cmd);
        while(io2.isAvaiable() != SocketIO.AVAILABILITY.YES) continue;
        assertEquals(200, ((ServerCommand)io2.popCommand().getCommand()).getCode());

        cmd = new ServerCommand();
        cmd.addValue("exit", "true");
        io1.send(cmd);
        while(io1.isAvaiable() != SocketIO.AVAILABILITY.YES) continue;
        assertEquals(200, ((ServerCommand)io1.popCommand().getCommand()).getCode());

        cmd = new ServerCommand();
        cmd.addValue("action", "getServicesInfo");
        io2.send(cmd);
        while(io2.isAvaiable() != SocketIO.AVAILABILITY.YES) continue;
        assertEquals(301, ((ServerCommand)io2.popCommand().getCommand()).getCode());
        while(io2.isAvaiable() != SocketIO.AVAILABILITY.YES) continue;
        assertEquals(0, ((GameServicesInfo) io2.popCommand().getCommand()).game_services.size());

        server.kill();
        t.join();
    }

    @Test(timeout = 3000)
    public void test4() throws IOException, InterruptedException {
        Server server = new Server(PORT);
        assertTrue(server.isValid());
        Thread t = new Thread(server);
        t.start();

        SocketIO io1 = getNewSocketIO();
        SocketIO io2 = getNewSocketIO();

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("action", "create");
        cmd.addValue("name", "test");
        cmd.addValue("type", "GoGame");

        io1.send(cmd);
        while(io1.isAvaiable() != SocketIO.AVAILABILITY.YES) continue;
        assertEquals(201, ((ServerCommand)io1.getCommand().getCommand()).getCode());
        String sKey = ((ServerCommand)io1.popCommand().getCommand()).getValue("sKey");

        cmd = new ServerCommand();
        cmd.addValue("action", "getServicesInfo");
        io2.send(cmd);
        while(io2.isAvaiable() != SocketIO.AVAILABILITY.YES) continue;
        assertEquals(1, ((GameServicesInfo) io2.getCommand().getCommand()).game_services.size());

        cmd = new ServerCommand();
        cmd.addValue("action", "connect");
        cmd.addValue("name", "test");
        cmd.addValue("game", ((GameServicesInfo) io2.popCommand().getCommand()).game_services.get(0).ID);

        io2.send(cmd);
        while(io2.isAvaiable() != SocketIO.AVAILABILITY.YES) continue;
        assertEquals(200, ((ServerCommand)io2.popCommand().getCommand()).getCode());

        cmd = new ServerCommand();
        cmd.addValue("getServiceInfo", "true");
        io1.send(cmd);
        while(io1.isAvaiable() != SocketIO.AVAILABILITY.YES) continue;
        assertEquals(2, ((GameServiceInfo) io1.getCommand().getCommand()).players.size());

        GameServiceInfo info = ((GameServiceInfo) io1.popCommand().getCommand());
        String player_to_kick = null;
        for (String player : info.players.keySet()) {
            if (!player.equals(info.host_id)) {
                player_to_kick = player;
            }
        }

        cmd = new ServerCommand();
        cmd.addValue("kick", player_to_kick);
        cmd.addValue("sKey", sKey);
        io1.send(cmd);
        while(io1.isAvaiable() != SocketIO.AVAILABILITY.YES) continue;
        assertEquals(200, ((ServerCommand)io1.getCommand().getCommand()).getCode());

        while(io2.isAvaiable() != SocketIO.AVAILABILITY.YES) continue;
        assertEquals(301, ((ServerCommand)io2.popCommand().getCommand()).getCode());

        cmd = new ServerCommand();
        cmd.addValue("action", "getServicesInfo");
        io2.send(cmd);
        while(io2.isAvaiable() != SocketIO.AVAILABILITY.YES) continue;
        assertEquals(1, ((GameServicesInfo) io2.popCommand().getCommand()).game_services.size());

        server.kill();
        t.join();
    }
}