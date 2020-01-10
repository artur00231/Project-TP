package tp_project.GoGameDBObject;


import java.util.ArrayList;

import tp_project.Network.ICommand;

public class DBGoGames implements ICommand {
    public ArrayList<DBGoGame> games = new ArrayList<>();

    @Override
    public String toText() {
        StringBuilder data = new StringBuilder(games.size() + ",");

        for (DBGoGame game : games) {
            data.append(game.toText()).append(",");
        }

        return data.toString();
    }

    @Override
    public void fromText(String text) throws IllegalArgumentException {
        String[] data = text.split(",");

        if (data.length < 1) throw new IllegalArgumentException();

        try {
            int size = Integer.parseInt(data[0]);

            games.clear();

            for (int i = 0; i < size; i++) {
                DBGoGame temp = new DBGoGame();
                temp.fromText(data[1 + i]);
                games.add(temp);
            }

        } catch (Exception exception) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String getCommandType() {
        return "DBGoGames";
    }

}