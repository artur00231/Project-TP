package tp_project.GoGame;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GoGameServiceInfoTest {
    @Test
    public void test1() {
        GoGameServiceInfo inf = new GoGameServiceInfo();
        assertEquals(0, inf.getPlayersInfo().size());

        inf.addPlayer("1", true, 0);
        inf.addPlayer("2", true, 0);
        inf.addPlayer("3", false, 1);
        inf.addPlayer("4", true, 1);
        inf.addPlayer("5", false, 0);

        assertEquals("5;1,true,0;2,true,0;3,false,1;4,true,1;5,false,0;", inf.toText());
        assertEquals(inf.getCommandType(), "GoGameServiceInfo");

        inf.addPlayer("5", true, 8);
        assertEquals("5;1,true,0;2,true,0;3,false,1;4,true,1;5,false,0;", inf.toText());
    }

    @Test
    public void test2() {
        GoGameServiceInfo inf = new GoGameServiceInfo();

        inf.fromText("3;A,true,3;B,false,1;C,false,2;");

        assertEquals("A", inf.getPlayersInfo().get(0).ID);
        assertEquals(true, inf.getPlayersInfo().get(0).ready);
        assertEquals(3, inf.getPlayersInfo().get(0).colour);

        assertEquals("B", inf.getPlayersInfo().get(1).ID);
        assertEquals(false, inf.getPlayersInfo().get(1).ready);
        assertEquals(1, inf.getPlayersInfo().get(1).colour);

        assertEquals("C", inf.getPlayersInfo().get(2).ID);
        assertEquals(false, inf.getPlayersInfo().get(2).ready);
        assertEquals(2, inf.getPlayersInfo().get(2).colour);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test3() {
        GoGameServiceInfo inf = new GoGameServiceInfo();
        inf.fromText("20;A,false,8;");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test4() {
        GoGameServiceInfo inf = new GoGameServiceInfo();
        inf.fromText("dsadsad;4,true,5;");
    }

    @Test
    public void test5() {
        GoGameServiceInfo inf = new GoGameServiceInfo();
        inf.fromText("1;A,dd,3;");

        assertEquals("A", inf.getPlayersInfo().get(0).ID);
        assertEquals(false, inf.getPlayersInfo().get(0).ready);
        assertEquals(3, inf.getPlayersInfo().get(0).colour);
    }

    @Test
    public void test6() {
        GoGameServiceInfo inf = new GoGameServiceInfo();
        inf.fromText(inf.toText());
        assertEquals(0, inf.getPlayersInfo().size());
    }
}