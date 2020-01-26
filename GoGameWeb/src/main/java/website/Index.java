package website;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import GoServer.GoServerClient;
import GoServer.GoServerClients;
import Server.Client.POSITION;


public class Index extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static int num_of_index_servlets = 0;

  @Override
  public void init() throws ServletException {
    super.init();

    synchronized (HttpServlet.class) {
      num_of_index_servlets++;
      System.out.println("Servlets:" + num_of_index_servlets);
    }
  }

  @Override
  public void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

    response.setContentType("text/html");
    PrintWriter pw = response.getWriter();

    GoServerClients clients = GoServerClients.getInstance();
    if (!clients.getClient(request.getSession().getId()).isPresent()) {
      clients.addClient(request.getSession().getId());
    }

    GoServerClient client = clients.getClient(request.getSession().getId()).get();
    if (client.getGoClient() != null) {
      if (client.getGoClient().getPosition() == POSITION.GAME) {
        if (!client.getGoPlayer().update()) {
          //TODO
        }
      } else {
        client.getGoClient().update();
      }

      if (client.getGoClient().getPosition() == POSITION.DISCONNECTED) {
        client.setGoClient(null);
      }
    }

    ActionExecutor exec = new ActionExecutor();
    boolean success = exec.execAction(request, client);

    IViewBuilder builder = ViewFactory.getViewBuilder(client.getGoClient() == null ? null : client.getGoClient().getPosition());
    if (!success) {
      builder.addErrorMessage(exec.err_message + "; E01");
    }

    pw.println(builder.buildWebsite(client));

    if (builder.autoRefresh()) {
      response.setIntHeader("Refresh", 2);
    }
          
    pw.close();
  }

  @Override
  public void destroy() {
    synchronized (HttpServlet.class) {
      num_of_index_servlets--;
      System.out.println("Servlets:" + num_of_index_servlets);

      if (num_of_index_servlets == 0) {
        GoServerClients.destroy();
      }
    }
  }
}