package Network;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkDataParser {
    private static NetworkDataParser network_data_parser = null;
    private Pattern pattern;
    private String raw_data_pattern = "[A-Za-z0-9\\s=,\\\\.\\[\\]{}()-;_]";
    private String raw_pattern = "TBEGIN>>>\"([a-zA-Z]*)\"<<<\\[("+ raw_data_pattern +"*)\\]>>>\"[a-zA-Z]*\"<<<TEND";

    private NetworkDataParser() {
        pattern = Pattern.compile(raw_pattern);
    }

    public static NetworkDataParser getNetworkDataParser() {
        if (network_data_parser == null) {
			synchronized (NetworkDataParser.class) {
				if (network_data_parser == null) {
					network_data_parser = new NetworkDataParser();
				}
			}
        }
        
        return network_data_parser;
    }

    public List<Command> getCommands(String data) {
        ArrayList<Command> commands = new ArrayList<>();

        Matcher matcher = pattern.matcher(data);

        while (matcher.find())
        {
            Command cmd = new Command(matcher.group(1), matcher.group(2));
            commands.add(cmd);
        }

        return commands;
    }

    public String removeCommand(String data) {
        return data.replaceAll(raw_pattern, "");
    }

    public byte[] getNetworkData(String type, String data) {
        return ("TBEGIN>>>\"" + type + "\"<<<[" + data + "]>>>\"" + type + "\"<<<TEND").getBytes();
    }

    public boolean isValid(String data) {
        return data.matches(raw_data_pattern + "+");
    }
}