package tp_project.Network;

public class CommandFactory {
    static ICommand crateCommand(Command.Type type)
    {
        if (type == Command.Type.Text)
        {
            return new TextCommand();
        }
        
        return null;
    }
}