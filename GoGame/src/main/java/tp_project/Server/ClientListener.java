package tp_project.Server;

import tp_project.Network.ICommand;

public interface ClientListener {
    public void updated();

    public void positionChanged();

    public void recived(ICommand command, String request);

    public void recived(int code, String request);
}