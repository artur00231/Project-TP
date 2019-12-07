package tp_project.Server;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ServerCommandTest {
    @Test
    public void test1() {
        ServerCommand cmd = new ServerCommand();
        assertEquals(0, cmd.getCode());
        assertEquals(0, cmd.size());

        cmd.setCode(404);
        cmd.addValue("A1", "A");
        cmd.addValue("A2", "B");
        cmd.addValue("A3", "C");

        assertEquals("3;", cmd.toText().subSequence(0, 2));
        assertEquals(17, cmd.toText().length());
    }

    @Test
    public void test2() {
        ServerCommand cmd = new ServerCommand();
        cmd.fromText("3;A1;A;A2;B;A3;C;");

        assertEquals("A", cmd.getValue("A1"));
        assertEquals("B", cmd.getValue("A2"));
        assertEquals("C", cmd.getValue("A3"));
        assertEquals(null, cmd.getValue("A4"));
        assertEquals(3, cmd.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test3() {
        ServerCommand cmd = new ServerCommand();
        cmd.fromText("20;A;B;C;D;");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test4() {
        ServerCommand cmd = new ServerCommand();
        cmd.fromText("4;AA;");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test5() {
        ServerCommand cmd = new ServerCommand();;
        cmd.fromText("das;AA;3;A;B;C;");
    }
}