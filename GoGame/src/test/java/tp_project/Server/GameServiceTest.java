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

    @Test(timeout = 300000)
    public void test1() throws IOException {
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
    }
}