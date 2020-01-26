package Network;

public class TextCommand implements ICommand
{
    private String data = "";

    public TextCommand() {
    }

    public TextCommand(String text) {
        data = text;
    }

    public void setText(String text) {
        data = text;
    }

    public String getText() {
        return data;
    }

    @Override
    public String toText() {
        return data;
    }

    @Override
    public void fromText(String text) {
        data = text;
    }

    @Override
    public String getCommandType() {
        return "Text";
    }
}