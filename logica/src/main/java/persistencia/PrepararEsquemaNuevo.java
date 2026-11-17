package persistencia;

import jakarta.persistence.Persistence;
import java.sql.DriverManager;
import java.util.HashMap;

/** Paso explícito de instalación para una base PostgreSQL nueva y vacía. */
public final class PrepararEsquemaNuevo {
    private PrepararEsquemaNuevo() { }

    public static void main(String[] args) throws Exception {
        if (args.length != 1 || !"--base-vacia".equals(args[0]))
            throw new IllegalArgumentException("Uso: PrepararEsquemaNuevo --base-vacia");
        preparar(BaseDeDatos.propiedadesJpa());
    }

    static void preparar(java.util.Map<String, String> propiedades) throws Exception {
        try (var conexion = DriverManager.getConnection(propiedades.get("jakarta.persistence.jdbc.url"),
                propiedades.get("jakarta.persistence.jdbc.user"), propiedades.get("jakarta.persistence.jdbc.password"))) {
            try (var consulta = conexion.prepareStatement("""
                    SELECT count(*) FROM information_schema.tables
                    WHERE table_schema = current_schema() AND table_type = 'BASE TABLE'
                    """); var resultado = consulta.executeQuery()) {
                resultado.next();
                if (resultado.getLong(1) != 0)
                    throw new IllegalStateException("El esquema ya contiene tablas. No se modifica una instalación existente.");
            }
        }
        var instalacion = new HashMap<>(propiedades);
        instalacion.put("hibernate.hbm2ddl.auto", "update");
        try (var factory = Persistence.createEntityManagerFactory("postgres", instalacion)) {
            // Hibernate crea exclusivamente las tablas del modelo en este paso explícito.
        }
        System.out.println("Tablas iniciales creadas. Aplicar los SQL de instalación y arrancar Central con validate.");
    }
}
