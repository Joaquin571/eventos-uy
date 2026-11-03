package soporte;

import persistencia.BaseDeDatos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

/** Barrera exclusiva de pruebas: no inicializa JPA ni modifica su configuración. */
public final class ProteccionBasePruebas {
    private static final String BASE_PERMITIDA = "eventosuy_test";

    private ProteccionBasePruebas() { }

    @FunctionalInterface
    interface AbrirConexion {
        Connection abrir(String url, String usuario, String clave) throws SQLException;
    }

    public static void verificar() {
        verificar(System.getenv("EVENTOS_DB_NAME"), BaseDeDatos.propiedadesJpa(),
                DriverManager::getConnection);
        System.out.println("[PRUEBAS] Base efectiva confirmada por JDBC: eventosuy_test.");
    }

    static void verificar(String nombreConfigurado, Map<String, String> propiedades,
                          AbrirConexion abrirConexion) {
        if (!BASE_PERMITIDA.equals(nombreConfigurado)) {
            throw new IllegalStateException("Pruebas bloqueadas: definir EVENTOS_DB_NAME=eventosuy_test.");
        }
        String url = propiedades.get("jakarta.persistence.jdbc.url");
        if (url == null || !url.startsWith("jdbc:postgresql://")
                || !url.endsWith("/" + BASE_PERMITIDA)) {
            throw new IllegalStateException("Pruebas bloqueadas: la URL efectiva no corresponde a eventosuy_test.");
        }
        try (Connection conexion = abrirConexion.abrir(url,
                propiedades.get("jakarta.persistence.jdbc.user"),
                propiedades.get("jakarta.persistence.jdbc.password"))) {
            conexion.setReadOnly(true);
            try (var consulta = conexion.createStatement()) {
                consulta.setQueryTimeout(5);
                try (var resultado = consulta.executeQuery("SELECT current_database()")) {
                    if (!resultado.next() || !BASE_PERMITIDA.equals(resultado.getString(1))) {
                        throw new IllegalStateException("Pruebas bloqueadas: la base JDBC real no es eventosuy_test.");
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Pruebas bloqueadas: no se pudo verificar la base por JDBC.", e);
        }
    }

    /** Comprobación previa ejecutable sin iniciar JPA ni ejecutar la suite. */
    public static void main(String[] args) {
        verificar();
    }
}
