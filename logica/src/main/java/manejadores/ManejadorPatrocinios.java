package manejadores;

import clases.Edicion;
import clases.Institucion;
import clases.Patrocinio;
import clases.TipoRegistro;
import datatypes.DtPatrocinio;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;

import persistencia.BaseDeDatos;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ManejadorPatrocinios {

    private static ManejadorPatrocinios instancia = null;

    private ManejadorPatrocinios() {
    }

    public static synchronized ManejadorPatrocinios getInstance() {
        if (instancia == null) {instancia = new ManejadorPatrocinios();}
        return instancia;
    }

    /** Unidad de escritura interna: Sistema aporta la política, este manejador las referencias y la transacción. */
    public boolean addPatrocinio(DtPatrocinio datos, Consumer<Patrocinio> validar) {
        Objects.requireNonNull(validar);
        EntityManager em = BaseDeDatos.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Mismo orden de bloqueo que altas de tipos y registros: edición antes de tipo.
            Edicion edicion = em.find(Edicion.class, datos.getNombreEdicion(),
                    LockModeType.PESSIMISTIC_WRITE);
            Institucion institucion = em.find(Institucion.class, datos.getNombreInstituto());
            List<TipoRegistro> tipos = edicion == null ? List.of() : em.createQuery(
                    "SELECT t FROM TipoRegistro t WHERE t.edicion.idNombre = :edicion AND LOWER(t.idNombre) = LOWER(:tipo)",
                    TipoRegistro.class).setParameter("edicion", datos.getNombreEdicion())
                    .setParameter("tipo", datos.getNombreTipoRegistro())
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
            Patrocinio nuevo = new Patrocinio(datos.getFecha(), datos.getMontoAporte(), datos.getCantRegistrosGrat(),
                    datos.getCodigoPatrocinio(), datos.getNivel(), institucion);
            nuevo.setEdicion(edicion);
            nuevo.setTipoRegistro(tipos.isEmpty() ? null : tipos.getFirst());
            validar.accept(nuevo); // Incluye autorización antes de consultas de duplicados y persistencia.
            em.persist(nuevo);
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw ConflictosPersistencia.traducir(e, "Ya existe un patrocinio con ese código o para esa institución y edición.");
        } finally {
            em.close();
        }
    }

    public Patrocinio obtenerPatrocinio(String nombreEdicion, String nombreInstitucion) {
        EntityManager em = BaseDeDatos.getEntityManager();
        try {
            List<Patrocinio> resultado = em.createQuery(
                            """
                            SELECT p
                            FROM Patrocinio p
                            LEFT JOIN FETCH p.institucion
                            LEFT JOIN FETCH p.edicion
                            LEFT JOIN FETCH p.tipoRegistro
                            WHERE p.edicion.idNombre = :edicion
                              AND p.institucion.nombre = :institucion
                            """,
                            Patrocinio.class
                    )
                    .setParameter("edicion", nombreEdicion)
                    .setParameter("institucion", nombreInstitucion)
                    .getResultList();

            if (resultado.isEmpty()) {
                return null;
            }
            return resultado.getFirst();
        } finally {
            em.close();
        }
    }

    public boolean existePatrocinio(String codigo) {
        EntityManager em = BaseDeDatos.getEntityManager();
        try {
            return em.find(Patrocinio.class, codigo) != null;
        } finally {
            em.close();
        }
    }

    public Patrocinio obtenerPatrocinio(String codigo) {
        EntityManager em = BaseDeDatos.getEntityManager();

        try {
            List<Patrocinio> resultado = em.createQuery(
                                    """
                                    SELECT p
                                    FROM Patrocinio p
                                    LEFT JOIN FETCH p.institucion
                                    LEFT JOIN FETCH p.edicion
                                    LEFT JOIN FETCH p.tipoRegistro
                                    WHERE p.codigoPatrocinio = :codigo
                                    """,
                                    Patrocinio.class
                            ).setParameter(
                                    "codigo",
                                    codigo
                            ).getResultList();
            if (resultado.isEmpty()) {
                return null;
            }
            return resultado.getFirst();
        } finally {
            em.close();
        }
    }

    public Collection<Patrocinio> listarPatrocinios() {
        EntityManager em = BaseDeDatos.getEntityManager();
        try {
            return em.createQuery(
                            """
                            SELECT p
                            FROM Patrocinio p
                            LEFT JOIN FETCH p.institucion
                            LEFT JOIN FETCH p.edicion
                            LEFT JOIN FETCH p.tipoRegistro
                            ORDER BY p.codigoPatrocinio
                            """,
                            Patrocinio.class
                    ).getResultList();
        } finally {
            em.close();
        }
    }

    public Collection<Patrocinio>
    obtenerPatrociniosEdicion(String nombreEdicion
    ) {
        EntityManager em = BaseDeDatos.getEntityManager();

        try {
            return em.createQuery(
                            """
                            SELECT p
                            FROM Patrocinio p
                            LEFT JOIN FETCH p.institucion
                            LEFT JOIN FETCH p.edicion
                            LEFT JOIN FETCH p.tipoRegistro
                            WHERE p.edicion.idNombre = :nombreEdicion
                            ORDER BY p.codigoPatrocinio
                            """,
                            Patrocinio.class
                    ).setParameter(
                            "nombreEdicion",
                            nombreEdicion
                    ).getResultList();

        } finally {
            em.close();
        }
    }

    public boolean existePatrocinioInstitucionEdicion(
            String nombreInstitucion,
            String nombreEdicion
    ) {
        EntityManager em = BaseDeDatos.getEntityManager();

        try {
            Long cantidad = em.createQuery(
                                    """
                                    SELECT COUNT(p)
                                    FROM Patrocinio p
                                    WHERE p.institucion.nombre = :institucion
                                      AND p.edicion.idNombre = :edicion
                                    """,
                                    Long.class
                            ).setParameter(
                                    "institucion",
                                    nombreInstitucion
                            ).setParameter(
                                    "edicion",
                                    nombreEdicion
                            ).getSingleResult();

            return cantidad > 0;
        } finally {
            em.close();
        }
    }
}
