package Server;

import Network.ICommand;

public interface ClientListener {
    public void updated();

    public void positionChanged();

    public void recived(ICommand command, String request);

    public void error(String request);
}