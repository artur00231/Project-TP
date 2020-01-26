package website;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import GoGame.GoStatus;
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

      client.setAutoUpdate(false);

      if (client.getGoClient().getPosition() == POSITION.GAME) {
        if (client.getGoPlayer() == null) {
          client.createGoPlayer();

          client.getGoPlayer().update();
          client.getGoPlayer().update();
        }

        if (!client.getGoPlayer().update()) {
          if (client.getGoPlayerAdapter().is_game_ended) {
            client.setRender(false);

            client.setMessage(getEndMessage(client.getGoPlayer().getLastStatus(), client.getGoClient().getID()));

            while (client.getGoClient().getPosition() == POSITION.GAME) {
              client.getGoClient().update();
            }

            client.removeGoPlayer();
          } else {
            client.getGoClient().disconnect();
          }
        }
        
      }

      if (client.getGoClient().getPosition() == POSITION.DISCONNECTED) {
        client.setGoClient(null);
      }
    }

    ActionExecutor exec = new ActionExecutor();
    boolean success = exec.execAction(request, client);

    if (client.getRender()) {
      IViewBuilder builder = ViewFactory.getViewBuilder(client.getGoClient() == null ? null : client.getGoClient().getPosition());
      if (!success) {
        builder.addErrorMessage(exec.err_message + "; E01");
      }

      String message = client.getMessage();
      if (!message.equals("")) {
        builder.addInfoMessage(message);
      }

      pw.println(builder.buildWebsite(client));

      if (builder.autoRefresh()) {
        response.setIntHeader("Refresh", builder.autoRefreshTime());
      }
    } else {
      client.setRender(true);

      response.setStatus(202);
      response.setIntHeader("Refresh", 1);
    }

    
    client.setAutoUpdate(true);
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

  private String getEndMessage(GoStatus status, String player_id) {
    boolean won = status.winner.equals(player_id);
    boolean draw = status.winner.equals("XX");
    boolean other_player_giveup = false;
    boolean giveup = false;
    int my_points = 0;
    int other_player_points = 0;

    if (status.player1.equals(player_id)) {
        other_player_giveup = status.player_2_giveup;
        giveup = status.player_1_giveup;
        my_points = status.player1_total_score;
        other_player_points = status.player2_total_score;
    } else {
        other_player_giveup = status.player_1_giveup;
        giveup = status.player_2_giveup;
        my_points = status.player2_total_score;
        other_player_points = status.player1_total_score;
    }

    if (other_player_giveup) {
        return "You won";
    } else if (giveup) {
        return "You lost";
    } else if (won) {
        return "You won. " + my_points + ":" + other_player_points;
    } else if (draw){
        return "No one won. " + my_points + ":" + other_player_points;
    } else {
        return "You lost. " + my_points + ":" + other_player_points;
    }
  }
}