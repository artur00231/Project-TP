package website;

import GoServer.GoServerClient;

public class StartViewBuilder implements IViewBuilder {
    private String err_msg = "";
    private boolean add_err = false;
    private String info_msg = "";
    private boolean add_info = false;

    @Override
    public void addErrorMessage(String message) {
        add_err = true;
        err_msg = message;
    }

    @Override
    public String buildWebsite(GoServerClient client) {
        StringBuilder site = new StringBuilder();
        ResourceManager manager = new ResourceManager();

        site.append("<!DOCTYPE html>\n<html>\n<head>\n<title>GoGame</title>\n</head>\n<body>\n");

        if (add_err || add_info) {
            site.append("<script>\n");
            site.append("function msg_foo() {\n");
            if (add_err)
                site.append("window.alert(\"" + err_msg + "\");\n");
            if (add_info)
                site.append("window.alert(\"" + info_msg + "\");\n");
            site.append("}\n");
            site.append("window.onload=msg_foo;\n");
            site.append("</script>\n");
        }


        site.append(manager.getResource("start_form"));
        site.append("\n</body>\n</html>");

        return site.toString();
    }

    @Override
    public boolean autoRefresh() {
        return false;
    }

    @Override
    public int autoRefreshTime() {
        return 9999;
    }

    @Override
    public void addInfoMessage(String message) {
        add_info = true;
        info_msg = message;
    }
}