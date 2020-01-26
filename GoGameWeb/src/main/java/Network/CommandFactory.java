package Network;

import java.util.ArrayList;

import GoGame.GoBoard;
import GoGame.GoGameServiceInfo;
import GoGame.GoMove;
import GoGame.GoStatus;
import GoGame.GoMove.TYPE;
import Server.GameServiceInfo;
import Server.GameServicesInfo;
import Server.ServerCommand;

public class CommandFactory {
    public static ICommand crateCommand(String type)
    {
        if (type.equals("Text")) {
            return new TextCommand();
        } else if (type.equals("GameServiceInfo")) {
            return new GameServiceInfo();
        } else if (type.equals("GameServicesInfo")) {
            return new GameServicesInfo();
        } else if (type.equals("ServerCommand")) {
            return new ServerCommand();
        } else if (type.equals("GoGameServiceInfo")) {
            return new GoGameServiceInfo();
        } else if (type.equals("GoMove")) {
            return new GoMove(TYPE.PASS);
        } else if (type.equals("GoStatus")) {
            return new GoStatus();
        } else if (type.equals("GoBoard")) {
            return new GoBoard(1);
        }
        
        return null;
    }

    public static boolean isValidType(String type) {
        ArrayList<String> types_names = new ArrayList<>();
        types_names.add("Text");
        types_names.add("ServerCommand");
        types_names.add("GameServiceInfo");
        types_names.add("GameServicesInfo");
        types_names.add("GoGameServiceInfo");
        types_names.add("GoMove");
        types_names.add("GoStatus");
        types_names.add("GoBoard");
        
        return types_names.contains(type);
    }
}