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
    private static EntityManagerFactory emf;

    private BaseDeDatos() {
    }

    public static Map<String, String> propiedadesJpa() {
        return propiedadesJpa(System.getenv());
    }

    static Map<String, String> propiedadesJpa(Map<String, String> entorno) {
        for (String clave : new String[]{"HOST", "PORT", "NAME", "USER", "PASSWORD"}) {
            String nombre = "EVENTOS_DB_" + clave;
            String valor = entorno.get(nombre);
            if (valor == null || (!clave.equals("PASSWORD") && valor.isBlank()))
                throw new IllegalStateException("Falta configurar " + nombre + ". No se utilizan valores de respaldo.");
        }
        String url;
        try {
            int puerto = Integer.parseInt(entorno.get("EVENTOS_DB_PORT"));
            if (puerto < 1 || puerto > 65535) throw new IllegalArgumentException();
            var uri = new java.net.URI("postgresql", null, entorno.get("EVENTOS_DB_HOST"), puerto,
                    "/" + entorno.get("EVENTOS_DB_NAME"), null, null);
            if (uri.getHost() == null) throw new IllegalArgumentException();
            url = "jdbc:" + uri.toASCIIString();
        } catch (Exception error) {
            throw new IllegalStateException("Revisar el host y puerto de la configuración externa de PostgreSQL.");
        }
        Map<String, String> props = new HashMap<>();
        props.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
        props.put("jakarta.persistence.jdbc.url", url);
        props.put("jakarta.persistence.jdbc.user", entorno.get("EVENTOS_DB_USER"));
        props.put("jakarta.persistence.jdbc.password", entorno.get("EVENTOS_DB_PASSWORD"));
        return props;
    }

    public static void asegurarDisponible() {

        Map<String, String> props = propiedadesJpa();
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("No se encontró el driver PostgreSQL en el proceso Central.");
        }
        DriverManager.setLoginTimeout(3);
        try (Connection conexion = DriverManager.getConnection(props.get("jakarta.persistence.jdbc.url"),
                props.get("jakarta.persistence.jdbc.user"), props.get("jakarta.persistence.jdbc.password"))) {
            System.out.println("[BD] PostgreSQL disponible mediante configuración externa.");
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo conectar a PostgreSQL. Revisar la configuración externa.");
        }
    }

    public static synchronized void inicializar() {
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

    /** Arranque del Central con conexión externa y sin generación de esquema. */
    public static synchronized void inicializar(Map<String, String> propiedades) {
        if (emf != null && emf.isOpen()) {
            throw new IllegalStateException("JPA ya está inicializado; no se puede sustituir la conexión.");
        }
        for (String clave : new String[]{"jakarta.persistence.jdbc.url", "jakarta.persistence.jdbc.user",
                "jakarta.persistence.jdbc.password"}) {
            if (!propiedades.containsKey(clave) || propiedades.get(clave) == null) {
                throw new IllegalArgumentException("Falta configuración externa de conexión.");
            }
        }
        Map<String, String> externas = new HashMap<>(propiedades);
        externas.put("hibernate.hbm2ddl.auto", "validate");
        emf = Persistence.createEntityManagerFactory("postgres", externas);
    }

    public static synchronized EntityManager getEntityManager() {
        if (emf == null || !emf.isOpen()) {
            inicializar();
        }
        return emf.createEntityManager();
    }

    public static synchronized void cerrar() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            System.out.println(
                    "[JPA] EntityManagerFactory cerrado."
            );
        }
    }
}
