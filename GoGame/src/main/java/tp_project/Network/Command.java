package tp_project.Network;

public class Command
{
    public enum Type{ ServerRequest, ServerInfo };

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
        return null;
    }
}