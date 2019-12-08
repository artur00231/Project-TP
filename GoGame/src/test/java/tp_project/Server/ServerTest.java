package tp_project.Server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import org.junit.Test;

import tp_project.Network.SocketIO;
import tp_project.Network.SocketIO.AVAILABILITY;

public class ServerTest {
    public String IP = "127.0.0.1";
    public int PORT = 5004;

    @Test(timeout = 1000)
    public void test1() throws InterruptedException {
        Server server = new Server(PORT);
        assertTrue(server.isValid());
        Thread t = new Thread(server);
        t.start();

        server.kill();
        t.join();
    }

    @Test(timeout = 2000)
    public void test2() throws InterruptedException, IOException {
        Server server = new Server(PORT);
        assertTrue(server.isValid());
        Thread t = new Thread(server);
        t.start();

        SocketChannel s = SocketChannel.open();
        s.connect(new InetSocketAddress(IP, PORT));
        SocketIO io = new SocketIO(s);
        assertTrue(io.getSatus().is_connected);

        Thread.sleep(1000);
        assertEquals(AVAILABILITY.YES, io.isAvaiable());
        assertEquals("ServerCommand", io.getCommand().getType());

        server.kill();
        t.join();
    }

    @Test(timeout = 3000)
    public void test3() throws InterruptedException, IOException {
        Server server = new Server(PORT);
        assertTrue(server.isValid());
        Thread t = new Thread(server);
        t.start();

        SocketChannel s = SocketChannel.open();
        s.connect(new InetSocketAddress(IP, PORT));
        SocketIO io = new SocketIO(s);
        assertTrue(io.getSatus().is_connected);

        while(io.isAvaiable() != AVAILABILITY.YES) continue;
        assertEquals("ServerCommand", io.popCommand().getType());

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("action", "getServicesInfo");
        io.send(cmd);

        while(io.isAvaiable() != AVAILABILITY.YES) continue;
        assertEquals("GameServicesInfo", io.getCommand().getType());
        assertEquals(0, ((GameServicesInfo) io.popCommand().getCommand()).game_services.size());

        server.kill();
        t.join();
    }

    @Test(timeout = 3000)
    public void test4() throws InterruptedException, IOException {
        Server server = new Server(PORT);
        assertTrue(server.isValid());
        Thread t = new Thread(server);
        t.start();

        SocketChannel s = SocketChannel.open();
        s.connect(new InetSocketAddress(IP, PORT));
        SocketIO io = new SocketIO(s);
        assertTrue(io.getSatus().is_connected);

        while(io.isAvaiable() != AVAILABILITY.YES) continue;
        assertEquals("ServerCommand", io.popCommand().getType());

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("action", "getServicesInfo");
        io.send(cmd);

        while(io.isAvaiable() != AVAILABILITY.YES) continue;
        assertEquals("GameServicesInfo", io.getCommand().getType());
        assertEquals(0, ((GameServicesInfo) io.popCommand().getCommand()).game_services.size());

        cmd = new ServerCommand();
        cmd.addValue("action", "getServicesInfo");
        cmd.addValue("filter", "xx");
        io.send(cmd);

        while(io.isAvaiable() != AVAILABILITY.YES) continue;
        assertEquals("GameServicesInfo", io.getCommand().getType());
        assertEquals(0, ((GameServicesInfo) io.popCommand().getCommand()).game_services.size());

        server.kill();
        t.join();
    }

    @Test(timeout = 3000)
    public void test5() throws InterruptedException, IOException {
        Server server = new Server(PORT);
        assertTrue(server.isValid());
        Thread t = new Thread(server);
        t.start();

        SocketChannel s = SocketChannel.open();
        s.connect(new InetSocketAddress(IP, PORT));
        SocketIO io = new SocketIO(s);
        assertTrue(io.getSatus().is_connected);

        while(io.isAvaiable() != AVAILABILITY.YES) continue;
        assertEquals("ServerCommand", io.popCommand().getType());

        ServerCommand cmd = new ServerCommand();
        cmd.addValue("action", "create");
        io.send(cmd);
        while(io.isAvaiable() != AVAILABILITY.YES) continue;
        assertEquals("ServerCommand", io.getCommand().getType());
        assertEquals(400, ((ServerCommand) io.popCommand().getCommand()).getCode());

        cmd.addValue("type", "GoGame");
        io.send(cmd);
        while(io.isAvaiable() != AVAILABILITY.YES) continue;
        assertEquals("ServerCommand", io.getCommand().getType());
        assertEquals(400, ((ServerCommand) io.popCommand().getCommand()).getCode());

        cmd.addValue("name", "Art");
        io.send(cmd);
        while(io.isAvaiable() != AVAILABILITY.YES) continue;
        assertEquals("ServerCommand", io.getCommand().getType());
        assertEquals(201, ((ServerCommand) io.popCommand().getCommand()).getCode());


        SocketChannel s2 = SocketChannel.open();
        s2.connect(new InetSocketAddress(IP, PORT));
        SocketIO io2 = new SocketIO(s2);
        assertTrue(io2.getSatus().is_connected);

        while(io2.isAvaiable() != AVAILABILITY.YES) continue;
        assertEquals("ServerCommand", io2.popCommand().getType());

        cmd = new ServerCommand();
        cmd.addValue("action", "getServicesInfo");
        io2.send(cmd);

        while(io2.isAvaiable() != AVAILABILITY.YES) continue;
        assertEquals("GameServicesInfo", io2.getCommand().getType());
        assertEquals(1, ((GameServicesInfo) io2.popCommand().getCommand()).game_services.size());

        cmd = new ServerCommand();
        cmd.addValue("action", "getServicesInfo");
        cmd.addValue("filter", "xx");
        io2.send(cmd);

        while(io2.isAvaiable() != AVAILABILITY.YES) continue;
        assertEquals("GameServicesInfo", io2.getCommand().getType());
        assertEquals(0, ((GameServicesInfo) io2.popCommand().getCommand()).game_services.size());

        server.kill();
        t.join();
    }
}
