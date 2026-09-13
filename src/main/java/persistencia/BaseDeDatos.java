package persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class BaseDeDatos {
    private static final String HOST = env("EVENTOS_DB_HOST", "localhost");
    private static final String PORT = env("EVENTOS_DB_PORT", "5432");
    private static final String NAME = env("EVENTOS_DB_NAME", "eventosuy");
    private static final String USER = env("EVENTOS_DB_USER", "postgres");
    private static final String PASSWORD = env("EVENTOS_DB_PASSWORD", "root");

    private static final String JDBC_URL =
            "jdbc:postgresql://" +
                    HOST + ":" +
                    PORT + "/" +
                    NAME;

    private static EntityManagerFactory emf;

    private BaseDeDatos() {
    }

    private static String env(
            String key,
            String valorPorDefecto
    ) {

        String valor = System.getenv(key);

        if (valor != null && !valor.isBlank()) {
            return valor;
        }
        return valorPorDefecto;
    }

    public static Map<String, String> propiedadesJpa() {

        Map<String, String> props = new HashMap<>();

        props.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
        props.put("jakarta.persistence.jdbc.url", JDBC_URL);
        props.put("jakarta.persistence.jdbc.user", USER);
        props.put("jakarta.persistence.jdbc.password", PASSWORD);

        return props;
    }

    public static void asegurarDisponible() {

        DriverManager.setLoginTimeout(3);

        try (
                Connection conexion =
                        DriverManager.getConnection(
                                JDBC_URL,
                                USER,
                                PASSWORD
                        )
        ) {

            System.out.println(
                    "[BD] PostgreSQL disponible en " +
                            HOST + ":" + PORT
            );

            System.out.println(
                    "[BD] Base de datos: " + NAME
            );

        } catch (SQLException e) {

            throw new RuntimeException(
                    "No se pudo conectar a PostgreSQL.\n" +
                            "URL: " + JDBC_URL + "\n" +
                            "Usuario: " + USER + "\n\n" +
                            "Verifique que PostgreSQL esté iniciado " +
                            "y que exista la base de datos '" +
                            NAME + "'.",
                    e
            );
        }
    }

    public static void inicializar() {
        if (emf != null && emf.isOpen()) {
            return;
        }

        asegurarDisponible();

        emf =
                Persistence.createEntityManagerFactory(
                        "postgres",
                        propiedadesJpa()
                );
        System.out.println(
                "[JPA] EntityManagerFactory inicializado."
        );
    }

    public static EntityManager getEntityManager() {
        if (emf == null || !emf.isOpen()) {
            inicializar();
        }
        return emf.createEntityManager();
    }

    public static void cerrar() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            System.out.println(
                    "[JPA] EntityManagerFactory cerrado."
            );
        }
    }
}