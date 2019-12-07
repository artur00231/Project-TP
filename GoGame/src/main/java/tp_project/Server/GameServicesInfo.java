package tp_project.Server;


import java.util.ArrayList;

import tp_project.Network.ICommand;

public class GameServicesInfo implements ICommand {
    public ArrayList<GameServiceInfo> game_services = new ArrayList<GameServiceInfo>();

    @Override
    public String toText() {
        String data =  Integer.toString(game_services.size()) + ",";

        for (GameServiceInfo game_service : game_services) {
            data += game_service.toText() + ",";
        }

        return data;
    }

    @Override
    public void fromText(String text) throws IllegalArgumentException {
        String[] data = text.split(",");

        if (data.length < 1) throw new IllegalArgumentException();

        try {
            int size = Integer.valueOf(data[0]);

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