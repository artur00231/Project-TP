package tp_project.Server;


import java.util.ArrayList;

import tp_project.Network.ICommand;

public class GameServicesInfo implements ICommand {
    public ArrayList<GameServiceInfo> game_services = new ArrayList<>();

    @Override
    public String toText() {
        StringBuilder data = new StringBuilder(game_services.size() + ",");

        for (GameServiceInfo game_service : game_services) {
            data.append(game_service.toText()).append(",");
        }

        return data.toString();
    }

    @Override
    public void fromText(String text) throws IllegalArgumentException {
        String[] data = text.split(",");

        if (data.length < 1) throw new IllegalArgumentException();

        try {
            int size = Integer.parseInt(data[0]);

            game_services.clear();

            for (int i = 0; i < size; i++) {
                GameServiceInfo temp = new GameServiceInfo();
                temp.fromText(data[1 + i]);
                game_services.add(temp);
            }

        } catch (Exception exception) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String getCommandType() {
        return "GameServicesInfo";
    }

}