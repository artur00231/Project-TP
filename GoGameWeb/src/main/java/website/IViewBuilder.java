package website;

import GoServer.GoServerClient;

public interface IViewBuilder {
    public void addErrorMessage(String message);

    public String buildWebsite(GoServerClient client);

    public boolean autoRefresh();
}