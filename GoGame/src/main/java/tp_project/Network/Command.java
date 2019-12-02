package tp_project.Network;

public class Command
{
    public enum Type{ Text };

    private String content;
    private Type type;
    private boolean is_valid = true;

    public Command(String type, String raw_command)
    {
        try {
            this.type = Type.valueOf(type);
        } catch (IllegalArgumentException exception)
        {
            is_valid = false;
        }

        content = raw_command;
    }

    public Type getType()
    {
        return type;
    }
    
    public ICommand getCommand()
    {
        if (!is_valid) return null;

        ICommand command = CommandFactory.crateCommand(type);

        try {
            command.fromText(content);
        } catch (IllegalArgumentException exception) {
            return null;
        }

        return command;
    }

    public boolean isValid() {
        return is_valid;
    }
}