package Network;

public class Command
{
    private String content;
    private String type;
    private boolean is_valid = true;

    public Command(String type, String raw_command)
    {
        is_valid = CommandFactory.isValidType(type);
        this.type = type;

        content = raw_command;
    }

    public String getType()
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