package tp_project.Network;

import java.util.ArrayList;

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
        }
        
        return null;
    }

    public static boolean isValidType(String type) {
        ArrayList<String> types_names = new ArrayList<>();
        types_names.add("Text");
        types_names.add("ServerCommand");
        types_names.add("GameServiceInfo");
        types_names.add("GameServicesInfo");
        return types_names.contains(type);
    }
}