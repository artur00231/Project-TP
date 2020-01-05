package tp_project.Network;

import java.util.ArrayList;

import tp_project.GoGame.GoBoard;
import tp_project.GoGame.GoGameServiceInfo;
import tp_project.GoGame.GoMove;
import tp_project.GoGame.GoStatus;
import tp_project.GoGame.GoMove.TYPE;
import tp_project.GoReplay.GoReplayServiceInfo;
import tp_project.Server.GameServiceInfo;
import tp_project.Server.GameServicesInfo;
import tp_project.Server.ServerCommand;

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
        } else if (type.equals("GoReplayServiceInfo")) {
            return new GoReplayServiceInfo();
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
        types_names.add("GoReplayServiceInfo");
        return types_names.contains(type);
    }
}