package tp_project.Server;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GameServicesInfoTest {
    @Test
    public void test1() {
        GameServiceInfo inf1 = new GameServiceInfo();
        inf1.host = "h1";
        inf1.ID = "id1";
        inf1.max_players = 4;
        inf1.players.put("A1", "A1");
        inf1.players.put("B1", "B1");
        inf1.players.put("C1", "C1");

        GameServiceInfo inf2 = new GameServiceInfo();
        inf2.host = "h2";
        inf2.ID = "id2";
        inf2.max_players = 4;
        inf2.players.put("A2", "A2");
        inf2.players.put("B2", "B2");
        inf2.players.put("C2", "C2");

        GameServiceInfo inf3 = new GameServiceInfo();
        inf3.host = "h3";
        inf3.ID = "id3";
        inf3.max_players = 4;
        inf3.players.put("A3", "A3");
        inf3.players.put("B3", "B3");
        inf3.players.put("C3", "C3");

        GameServicesInfo game_services_info = new GameServicesInfo();
        game_services_info.game_services.add(inf1);
        game_services_info.game_services.add(inf2);
        game_services_info.game_services.add(inf3);

        String text = game_services_info.toText();
        assertEquals("3,", text.substring(0, 2));

        GameServicesInfo game_services_info2 = new GameServicesInfo();
        game_services_info2.fromText(text);

        assertEquals(3, game_services_info2.game_services.size());
        assertEquals("GameServicesInfo", game_services_info2.getCommandType());

        assertEquals("h1", game_services_info2.game_services.get(0).host);
        assertEquals("id1", game_services_info2.game_services.get(0).ID);
        assertEquals(4, game_services_info2.game_services.get(0).max_players);
        assertEquals(3, game_services_info2.game_services.get(0).players.size());
        assertEquals("A1", game_services_info2.game_services.get(0).players.get("A1"));
        assertEquals("B1", game_services_info2.game_services.get(0).players.get("B1"));
        assertEquals("C1", game_services_info2.game_services.get(0).players.get("C1"));

        assertEquals("h2", game_services_info2.game_services.get(1).host);
        assertEquals("id2", game_services_info2.game_services.get(1).ID);
        assertEquals(4, game_services_info2.game_services.get(1).max_players);
        assertEquals(3, game_services_info2.game_services.get(1).players.size());
        assertEquals("A2", game_services_info2.game_services.get(1).players.get("A2"));
        assertEquals("B2", game_services_info2.game_services.get(1).players.get("B2"));
        assertEquals("C2", game_services_info2.game_services.get(1).players.get("C2"));

        assertEquals("h3", game_services_info2.game_services.get(2).host);
        assertEquals("id3", game_services_info2.game_services.get(2).ID);
        assertEquals(4, game_services_info2.game_services.get(2).max_players);
        assertEquals(3, game_services_info2.game_services.get(2).players.size());
        assertEquals("A3", game_services_info2.game_services.get(2).players.get("A3"));
        assertEquals("B3", game_services_info2.game_services.get(2).players.get("B3"));
        assertEquals("C3", game_services_info2.game_services.get(2).players.get("C3"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test2() {
        GameServicesInfo inf = new GameServicesInfo();
        inf.fromText("20,");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test3() {
        GameServicesInfo inf = new GameServicesInfo();
        inf.fromText("1,ads;dasd;ads;da");
    }

    @Test
    public void test4() {
        GameServicesInfo inf = new GameServicesInfo();
        inf.fromText(inf.toText());
        assertEquals(0, inf.game_services.size());
    }

}