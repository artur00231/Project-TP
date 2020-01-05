package tp_project.GoGameDBObject;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import tp_project.Server.ServerCleanup;

public class HibernateUtil {
    private static StandardServiceRegistry registry = null;
    private static SessionFactory sessionFactory = null;

    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = getInstance();
        }

        return sessionFactory;
    }

    public static SessionFactory getInstance() {
        if (sessionFactory == null) {
            try {
                registry = new StandardServiceRegistryBuilder().configure().build();
                MetadataSources sources = new MetadataSources(registry);
                Metadata metadata = sources.getMetadataBuilder().build();
                sessionFactory = metadata.getSessionFactoryBuilder().build();

            } catch (Exception e) {
                if (registry != null) {
                    StandardServiceRegistryBuilder.destroy(registry);
                }

                throw e;
            }
        }

        ServerCleanup cleanup = new ServerCleanup();
        cleanup.addCleaningFuncion(() -> shutdown());

        return sessionFactory;
    }

    public static void shutdown() {
        if (registry != null) {
            StandardServiceRegistryBuilder.destroy(registry);
            registry = null;
        }

        if (sessionFactory != null) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }
}