package persistencia;

import jakarta.persistence.Persistence;
import java.sql.DriverManager;
import java.util.HashMap;

/** Paso explícito de instalación para una base PostgreSQL nueva y vacía. */
public final class PrepararEsquemaNuevo {
    private PrepararEsquemaNuevo() { }

    public static void main(String[] args) throws Exception {
        if (args.length != 1 && args.length != 3 || !"--base-vacia".equals(args[0]))
            throw new IllegalArgumentException("Uso: PrepararEsquemaNuevo --base-vacia [--schema nombre]");
        var propiedades = BaseDeDatos.propiedadesJpa();
        if (args.length == 3) {
            if (!"--schema".equals(args[1]) || !args[2].matches("[a-z][a-z0-9_]{0,62}"))
                throw new IllegalArgumentException("El nombre del esquema debe ser un identificador PostgreSQL simple.");
            propiedades.put("jakarta.persistence.jdbc.url",
                    propiedades.get("jakarta.persistence.jdbc.url") + "?currentSchema=" + args[2] + ",public");
        }
        preparar(propiedades);
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
