package tp_project.Server;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ServerTest {
    @Test
    public void test1() {
        Server server = new Server(5004);
        assertTrue(server.isValid());

        server.run();

        System.out.println("END");
    }
}