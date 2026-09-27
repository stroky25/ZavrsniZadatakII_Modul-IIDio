package org.example.util;

import org.example.model.Polaznik;
import org.example.model.ProgramObrazovanja;
import org.example.model.Upis;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;


public final class HibernateUtil {

    private static final String JDBC_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=JavaAdv;encrypt=false;trustServerCertificate=true";

    private static final String JDBC_USER = "sa";

    // Ovdje upiši svoju lozinku za SQL Server korisnika sa.
    private static final String JDBC_PASSWORD =
            System.getenv().getOrDefault("JAVAADV_DB_PASSWORD", "SQL");

    private static final SessionFactory FACTORY = buildSessionFactory();

    private HibernateUtil() {
    }

    private static SessionFactory buildSessionFactory() {
        try {
            Configuration configuration = new Configuration()
                    .setProperty("hibernate.connection.driver_class", "com.microsoft.sqlserver.jdbc.SQLServerDriver")
                    .setProperty("hibernate.connection.url", JDBC_URL)
                    .setProperty("hibernate.connection.username", JDBC_USER)
                    .setProperty("hibernate.connection.password", JDBC_PASSWORD)
                    .setProperty("hibernate.hbm2ddl.auto", "validate")
                    .setProperty("hibernate.show_sql", "true")
                    .setProperty("hibernate.format_sql", "true")
                    .setProperty("hibernate.connection.pool_size", "5")
                    .addAnnotatedClass(Polaznik.class)
                    .addAnnotatedClass(ProgramObrazovanja.class)
                    .addAnnotatedClass(Upis.class);

            return configuration.buildSessionFactory();
        } catch (Throwable e) {
            System.err.println("Nije moguće inicijalizirati Hibernate.");
            System.err.println("Provjeri SQL Server Authentication, korisnika/lozinku, bazu JavaAdv i JDBC URL.");
            System.err.println("Korisnik: " + JDBC_USER);
            System.err.println("Ako koristiš sa, postavi lozinku u varijablu okruženja JAVAADV_DB_PASSWORD ili promijeni JDBC_PASSWORD.");
            throw new ExceptionInInitializerError(e);
        }
    }

    public static SessionFactory getFactory() {
        return FACTORY;
    }

    public static void shutdown() {
        if (FACTORY != null && !FACTORY.isClosed()) {
            FACTORY.close();
        }
    }
}
