package tp_project.Server;

import java.util.HashMap;
import java.util.Map;

import tp_project.Network.ICommand;

public class ServerCommand implements ICommand
{
    private Map<String, String> data;
    private int code = 0;

    public ServerCommand() {
        data = new HashMap<>();
    }

    public void addValue(String property, String value) {
        data.put(property, value);
    }

    public String getValue(String property) {
        return data.get(property);
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public int size() {
        return data.size();
    }

    @Override
    public String toText() {
        String text = Integer.toString(code) + ";";
        text += Integer.toString(data.size()) + ";";

        for (String property : data.keySet()) {
            text += property + ";" + data.get(property) + ";";
        }

        return text;
    }

    @Override
    public void fromText(String text) {
        String[] raw_data = text.split(";");

        if (raw_data.length < 2) throw new IllegalArgumentException();

        try {
            code = Integer.valueOf(raw_data[0]);
            int size = Integer.valueOf(raw_data[1]);
            data.clear();

            for (int i = 0; i < size; i++) {
                data.put(raw_data[2 + i * 2], raw_data[3 + i * 2]);
            }

        } catch (Exception exception) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String getCommandType() {
        return "ServerCommand";
    }
}