package tp_project.GoGameDBObject;

import org.hibernate.Session;
import org.hibernate.Transaction;

import tp_project.Server.ServerCleanup;

public class DBGoManager {
    private static DBGoManager instance = null;
    private static final Object mutex = new Object();
    private Session session = null;

    private DBGoManager() {
        session = HibernateUtil.getInstance().openSession();

        ServerCleanup cleanup = new ServerCleanup();
        cleanup.addCleaningFuncion(() -> this.destroy());
    }

    public static DBGoManager getInstance() {
        if (instance == null) {
            synchronized (mutex) {
                if (instance == null) {
                    instance = new DBGoManager();
                }
            }
        }

        return instance;
    }

    public void destroy() {
        session.close();
        session = null;
        instance = null;
    }

    public synchronized void addGame(DBGoGame game) {
        Transaction transaction = session.getTransaction();
        transaction.begin();
        session.persist(game);
        transaction.commit();
    }

    public synchronized boolean updateGame(DBGoGame game) {
        try {
            Transaction transaction = session.getTransaction();
            transaction.begin();
            session.update(game);
            transaction.commit();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public synchronized void addBoard(DBGoBoard board, DBGoGame game) {
        board.game_id = game.getID();
        Transaction transaction = session.getTransaction();
        transaction.begin();
        session.save(board);
        transaction.commit();
    }

    public synchronized void setStatus(DBGoStatus status, DBGoGame game) {
        status.game_id = game.getID();
        Transaction transaction = session.getTransaction();
        transaction.begin();
        session.save(status);
        transaction.commit();
    }
}