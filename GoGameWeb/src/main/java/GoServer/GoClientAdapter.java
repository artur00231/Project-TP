package GoServer;

import java.util.ArrayList;

import Network.ICommand;
import Server.ClientListener;

public class GoClientAdapter implements ClientListener {
    private boolean is_updated = false;
    private boolean is_pos_changed = false;
    private boolean is_recived = false;
    private boolean is_error = false;

    static public class Command {
        public ICommand command;
        public String request;
        public Command(ICommand command, String request) {
            this.command = command;
            this.request = request;
        }
        public Command() {}
    }
    private ArrayList<Command> commands = new ArrayList<>();
    private String last_err = "";


    @Override
    public void updated() {
        is_updated = true;
    }

    @Override
    public void positionChanged() {
        is_pos_changed = true;
    }

    @Override
    public void recived(ICommand command, String request) {
        is_recived = true;

        synchronized (commands) {
            commands.add(new Command(command, request));
        }
    }

    @Override
    public void error(String request) {
        is_error = true;
        last_err = request;
    }

    public void reset() {
        is_updated = false;
        is_pos_changed = false;
        is_recived = false;
        is_error = false;
    }

    public boolean isUpdated() {
        return is_updated;
    }

    public boolean isPosChanged() {
        return is_pos_changed;
    }

    public boolean isRecived() {
        return is_recived;
    }

    public boolean isError() {
        return is_error;
    }

    public Command popCommand() {
        synchronized (commands) {
            if (commands.size() == 0) return null;

            Command cmd = commands.get(0);
            commands.remove(0);

            return cmd;
        }
    }

    public void clearCommands() {
        synchronized (commands) {
            commands.clear();
        }
    }

    public String getLastError() {
        return last_err;
    }

    public boolean stateChanged() {
        return is_updated || is_error || is_pos_changed || is_recived;
    }
}