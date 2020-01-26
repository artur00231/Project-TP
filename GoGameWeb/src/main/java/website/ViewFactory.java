package website;
import Server.Client.POSITION;

public class ViewFactory {
    private ViewFactory() {}

    static IViewBuilder getViewBuilder(POSITION position) {
        if (position == null) {
            return new StartViewBuilder();
        }

        switch (position) {
            case SERVER:
                return new ServerViewBuilder();
            case GAMESERVICE:
                return new ServiceViewBuilder();
            case GAME:
                return new GameViewBuilder();
            default:
        }

        return null;
    }
}