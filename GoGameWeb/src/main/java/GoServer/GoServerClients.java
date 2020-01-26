package GoServer;

import java.util.HashMap;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import Server.Client.POSITION;

public class GoServerClients {
    private static GoServerClients instance = null;

    private HashMap<String, GoServerClient> clients;
    private Timer auto_update_timer;
    
    private class DelayedClient implements Delayed {
        private String ID = "";
        private long time;
        public DelayedClient(String id, long time) {
            ID = id;
            this.time = time + System.currentTimeMillis();
        }
        @Override
        public int compareTo(Delayed o) {
            if (this.time < ((DelayedClient)o).time) { 
                return -1; 
            } 
            if (this.time > ((DelayedClient)o).time) { 
                return 1; 
            } 
            return 0; 
        }
        @Override
        public long getDelay(TimeUnit unit) {
            long diff = time - System.currentTimeMillis(); 
            return unit.convert(diff, TimeUnit.MILLISECONDS); 
        }
        @Override
        public String toString() {
            return ID;
        }
    }
    private DelayQueue<DelayedClient> clients_to_delete;

    private GoServerClients() {
        clients = new HashMap<>();
        clients_to_delete = new DelayQueue<>();
        auto_update_timer = new Timer();
        auto_update_timer.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run() {
                synchronized (GoServerClients.class) {
                    for (GoServerClient client : clients.values()) {
                        if (client.getAutoUpdate() && client.getGoClient() != null) {
                            if (client.getGoClient().getPosition() == POSITION.GAME) {
                                if (client.getGoPlayer() == null) {
                                    client.createGoPlayer();
                                }

                                client.getGoPlayer().update();
                            } else {
                                client.getGoClient().update();
                            }
                        }
                    }
                }
            }
        }, 100, 50);
    }

    public static GoServerClients getInstance() {
        if (instance == null) {
            synchronized (GoServerClients.class) {
                if (instance == null) {
                    instance = new GoServerClients();
                }
            }
        }

        return instance;
    }

    public static void destroy() {
        if (instance != null) {
            for (Entry<String, GoServerClient> pair : instance.clients.entrySet()) {
                if (pair.getValue().getGoClient() != null) {
                    pair.getValue().getGoClient().disconnect();
                }
            }

            instance.auto_update_timer.cancel();
        }

        instance = null;
    }

    public Optional<GoServerClient> getClient(String ID) {

        synchronized (GoServerClients.class) {
            clients_to_delete.removeIf(x -> x.ID.equals(ID));
            clients_to_delete.add(new DelayedClient(ID, 20000));

            DelayedClient delayed_client = clients_to_delete.poll();
            while (delayed_client != null) {
                GoServerClient client = clients.get(delayed_client.ID);
                if (client != null && client.getGoClient() != null) {
                    client.getGoClient().disconnect();
                }

                clients.remove(delayed_client.ID);
                delayed_client = clients_to_delete.poll();
            }

        }

        return Optional.ofNullable(clients.get(ID));
    }

    public GoServerClient addClient(String ID) {
        GoServerClient client = new GoServerClient(ID);

        synchronized (GoServerClients.class) {
            clients.put(ID, client);
            clients_to_delete.add(new DelayedClient(ID, 20000));
        }

        return client;
    }
}