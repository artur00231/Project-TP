package tp_project.GoGame;

import java.util.ArrayList;

import tp_project.Network.ICommand;

public class GoGameServiceInfo implements ICommand {
    static public class PlayerInfo {
        public String ID;
        public boolean ready;
        public int colour;
    }
    private ArrayList<PlayerInfo> players_info;

    public GoGameServiceInfo() {
        players_info = new ArrayList<>();
    }

    public void addPlayer(String player_id, boolean ready, int colour) {
        if (players_info.stream().anyMatch(x -> x.ID == player_id)) return;
        PlayerInfo player = new PlayerInfo();
        player.ID = player_id;
        player.ready = ready;
        player.colour = colour;

        players_info.add(player);
    }

    public ArrayList<PlayerInfo> getPlayersInfo() {
        return players_info;
    }

    @Override
    public String toText() {
        StringBuilder data = new StringBuilder();
        data.append(players_info.size()).append(";");

        for (PlayerInfo player_info : players_info) {
            data.append(player_info.ID).append(",").append(player_info.ready).append(",").append(player_info.colour).append(";");
        }

        return data.toString();
    }

    @Override
    public void fromText(String text) throws IllegalArgumentException {
        String[] data = text.split(";");

        if (data.length < 1) throw new IllegalArgumentException();

        try {
            int size = Integer.parseInt(data[0]);
            players_info.clear();

            for (int i = 0; i < size; i++) {
                PlayerInfo info = new PlayerInfo();
                String[] player_data = data[1 + i].split(",");
                if (player_data.length != 3) throw new IllegalArgumentException();
                info.ID = player_data[0];
                info.ready = Boolean.parseBoolean(player_data[1]);
                info.colour = Integer.parseInt(player_data[2]);
                players_info.add(info);
            }

        } catch (Exception exception) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String getCommandType() {
        return "GoGameServiceInfo";
    }

}