package tp_project.Server;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GameServiceInfoTest {
    @Test
    public void test1() {
        GameServiceInfo inf = new GameServiceInfo();
        assertEquals("", inf.host);
        assertEquals("", inf.ID);
        assertEquals(0, inf.max_players);
        assertEquals(0, inf.players.size());

        inf.host = "AA";
        inf.ID = "uid";
        inf.max_players = 4;
        inf.players.put("Aid", "A");
        inf.players.put("Bid", "B");
        inf.players.put("Cid", "C");

        assertEquals(inf.toText().subSequence(0, 10), "uid;4;AA;3");
        assertEquals(inf.toText().length(), 29);
        assertEquals(inf.getCommandType(), "GameServiceInfo");
    }

    @Test
    public void test2() {
        GameServiceInfo inf = new GameServiceInfo();

        inf.fromText("uid;4;AA;3;Aid;A;Bid;B;Cid;C;");

        assertEquals("AA", inf.host);
        assertEquals("uid", inf.ID);
        assertEquals(4, inf.max_players);
        assertEquals(3, inf.players.size());
        assertEquals("A", inf.players.get("Aid"));
        assertEquals("B", inf.players.get("Bid"));
        assertEquals("C", inf.players.get("Cid"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test3() {
        GameServiceInfo inf = new GameServiceInfo();
        inf.fromText("uid;4;AA;20;A;B;C;");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test4() {
        GameServiceInfo inf = new GameServiceInfo();
        inf.fromText("uid;4;AA;");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test5() {
        GameServiceInfo inf = new GameServiceInfo();
        inf.fromText("uid;das;AA;3;A;B;C;");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test6() {
        GameServiceInfo inf = new GameServiceInfo();
        inf.fromText(inf.toText());
    }
}