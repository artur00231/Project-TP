package tp_project.Server;

import java.util.HashMap;
import java.util.Map;

import tp_project.Network.ICommand;

public class GameServiceInfo implements ICommand {
    public int max_players = 0;
    public String host_id = "";
    public Map<String, String> players = new HashMap<String, String>();
    public String ID = "";

    @Override
    public String toText() {
        String data =  ID + ";";
        data += Integer.toString(max_players) + ";";
        data += host_id + ";";
        data += players.size() + ";";

        for (String player_id : players.keySet()) {
            data += player_id + ";" + players.get(player_id) + ";";
        }

        return data;
    }

    @Override
    public void fromText(String text) throws IllegalArgumentException {
        String[] data = text.split(";");

        if (data.length < 5) throw new IllegalArgumentException();

        try {
            ID = data[0];
            max_players = Integer.valueOf(data[1]);
            host_id = data[2];

            int size = Integer.valueOf(data[3]);
            players.clear();

            for (int i = 0; i < size; i++) {
                players.put(data[4 + i * 2], data[5 + i * 2]);
            }

        } catch (Exception exception) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String getCommandType() {
        return "GameServiceInfo";
    }

}