package Network;

public interface ICommand
{
    public String toText();

    public void fromText(String text) throws IllegalArgumentException;

    public String getCommandType();
}