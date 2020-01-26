package website;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

public class ResourceManager {
    private HashMap<String, String> resources;

    public ResourceManager() {
        resources = new HashMap<>();
        setResources();
    }

    public String getResource(String name) {
        String path = resources.get(name);

        if (path == null) return "";

        return load(path);
    }

    private String load(String path) throws IllegalArgumentException {
        File file = new File(path);
        String data = "";

        try {
            data = new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }

        return data;
    }

    private void setResources() {
        resources.put("start_form", "src\\main\\resources\\start_form.txt");
        resources.put("game_service", "src\\main\\resources\\service.txt");
        resources.put("exit_button", "src\\main\\resources\\exit_button.txt");
        resources.put("create_button", "src\\main\\resources\\create_form.txt");
        resources.put("player_info", "src\\main\\resources\\player_info.txt");
        resources.put("ready_button", "src\\main\\resources\\ready_button.txt");
        resources.put("kick_button", "src\\main\\resources\\kick_button.txt");
        resources.put("refresh_button", "src\\main\\resources\\refresh_button.txt");
        resources.put("add_bot_button", "src\\main\\resources\\add_bot_button.txt");
        resources.put("give_up_button", "src\\main\\resources\\give_up_button.txt");
        resources.put("pass_button", "src\\main\\resources\\pass_button.txt");
        resources.put("style", "src\\main\\resources\\style.css");
    }
}