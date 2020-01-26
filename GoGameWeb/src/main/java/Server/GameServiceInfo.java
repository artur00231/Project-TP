package Server;

import java.util.HashMap;
import java.util.Map;

import Network.ICommand;

public class GameServiceInfo implements ICommand {
    public int max_players = 0;
    public String host_id = "";
    public Map<String, String> players = new HashMap<>();
    public String ID = "";

    @Override
    public String toText() {
        StringBuilder data = new StringBuilder(ID + ";");
        data.append(max_players).append(";");
        data.append(host_id).append(";");
        data.append(players.size()).append(";");

        for (String player_id : players.keySet()) {
            data.append(player_id).append(";").append(players.get(player_id)).append(";");
        }

        return data.toString();
    }

    @Override
    public void fromText(String text) throws IllegalArgumentException {
        String[] data = text.split(";");

        if (data.length < 5) throw new IllegalArgumentException();

        try {
            ID = data[0];
            max_players = Integer.parseInt(data[1]);
            host_id = data[2];

            int size = Integer.parseInt(data[3]);
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