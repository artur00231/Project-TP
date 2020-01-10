package tp_project.GoGameDBObject;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Calendar;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import tp_project.GoGame.GoBoard;
import tp_project.Server.ServerCleanup;

public class DBGoManager {
    private static DBGoManager instance = null;
    private static final Object mutex = new Object();
    private Session session_main = null;
    private Session session_query = null;

    private DBGoManager() {
        session_main = HibernateUtil.getInstance().openSession();
        session_query = HibernateUtil.getInstance().openSession();

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
        if (session_main != null) {
            session_main.close();
            session_query.close();
        }
        session_main = null;
        session_query = null;
        instance = null;
    }

    public void addGame(DBGoGame game) {
        synchronized (session_main) {
            Transaction transaction = session_main.getTransaction();
            transaction.begin();
            session_main.persist(game);
            transaction.commit();
        }
    }

    public boolean updateGame(DBGoGame game) {
        synchronized (session_main) {
            try {
                Transaction transaction = session_main.getTransaction();
                transaction.begin();
                session_main.update(game);
                transaction.commit();
            } catch (Exception e) {
                return false;
            }
        }

        return true;
    }

    public synchronized void addBoard(DBGoBoard board, DBGoGame game) {
        synchronized (session_main) {
            board.game_id = game.getID();
            Transaction transaction = session_main.getTransaction();
            transaction.begin();
            session_main.save(board);
            transaction.commit();
        }
    }

    public synchronized void setStatus(DBGoStatus status, DBGoGame game) {
        synchronized (session_main) {
            status.game_id = game.getID();
            Transaction transaction = session_main.getTransaction();
            transaction.begin();
            session_main.save(status);
            transaction.commit();
        }
    }

    public synchronized DBGoGames getGames(Instant date) {
        DBGoGames games = new DBGoGames();
        List<DBGoGame> results;

        Timestamp low = Timestamp.from(date);
        Calendar cTs = Calendar.getInstance();
        cTs.setTimeInMillis(low.getTime());
        cTs.set(Calendar.HOUR_OF_DAY, 0);
        cTs.set(Calendar.MINUTE, 0);
        cTs.set(Calendar.SECOND, 0);
        cTs.set(Calendar.MILLISECOND, 0);
        low.setTime(cTs.getTimeInMillis());

        Timestamp high = new Timestamp(low.toInstant().toEpochMilli() + 1000 * 60 * 60 * 24);
        
        synchronized (session_query) {
            CriteriaBuilder cb = session_query.getCriteriaBuilder();
            CriteriaQuery<DBGoGame> cr = cb.createQuery(DBGoGame.class);
            Root<DBGoGame> root = cr.from(DBGoGame.class);

            cr.select(root).where(cb.between(root.get("game_date"), low, high), cb.equal(root.get("ended"), true));
            cr.select(root).orderBy(cb.asc(root.get("game_date")));
            cr.select(root);
            
            Query<DBGoGame> query = session_query.createQuery(cr);
            results = query.getResultList();
        }

        for (DBGoGame game : results) {
            games.games.add(game);
        }

        return games;
    }

	public DBGoStatus getStatus(Integer game_id) {
        List<DBGoStatus> results;

		synchronized (session_query) {
            CriteriaBuilder cb = session_query.getCriteriaBuilder();
            CriteriaQuery<DBGoStatus> cr = cb.createQuery(DBGoStatus.class);
            Root<DBGoStatus> root = cr.from(DBGoStatus.class);

            cr.select(root).where(cb.equal(root.get("game_id"), game_id));
            cr.select(root);
            
            Query<DBGoStatus> query = session_query.createQuery(cr);
            results = query.getResultList();
        }

        if (results.size() == 0) return null;
        return results.get(0);
	}

	public GoBoard getBoard(Integer game_id, Integer round) {
		List<DBGoBoard> results;

		synchronized (session_query) {
            CriteriaBuilder cb = session_query.getCriteriaBuilder();
            CriteriaQuery<DBGoBoard> cr = cb.createQuery(DBGoBoard.class);
            Root<DBGoBoard> root = cr.from(DBGoBoard.class);

            cr.select(root).where(cb.equal(root.get("game_id"), game_id), cb.equal(root.get("round_number"), round));
            cr.select(root);
            
            Query<DBGoBoard> query = session_query.createQuery(cr);
            results = query.getResultList();
        }

        if (results.size() == 0) return null;
        GoBoard board = new GoBoard(10);
        board.fromText(results.get(0).row_board);

        return board;
	}
}