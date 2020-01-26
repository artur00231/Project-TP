package Server;

import java.util.HashMap;
import java.util.Map;

import Network.ICommand;

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
        StringBuilder text = new StringBuilder(code + ";");
        text.append(data.size()).append(";");

        for (String property : data.keySet()) {
            text.append(property).append(";").append(data.get(property)).append(";");
        }

        return text.toString();
    }

    @Override
    public void fromText(String text) {
        String[] raw_data = text.split(";");

        if (raw_data.length < 2) throw new IllegalArgumentException();

        try {
            code = Integer.parseInt(raw_data[0]);
            int size = Integer.parseInt(raw_data[1]);
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