package manejadores;

import clases.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import persistencia.BaseDeDatos;

import java.util.Collection;
import java.util.List;

public class ManejadorEventos {

    private static ManejadorEventos instancia = null;

    private ManejadorEventos() {
    }

    public static ManejadorEventos getInstance() {

        if (instancia == null) {
            instancia = new ManejadorEventos();
        }

        return instancia;
    }

    // =====================================================
    // EVENTOS
    // =====================================================

    public boolean addEvento(Evento evento) {

        EntityManager em = BaseDeDatos.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {

            tx.begin();

            if (em.find(Evento.class, evento.getNombre()) != null) {
                tx.rollback();
                return false;
            }

            em.persist(evento);

            tx.commit();
            return true;

        } catch (Exception e) {

            if (tx.isActive()) {
                tx.rollback();
            }

            throw e;

        } finally {
            em.close();
        }
    }

    public Evento obtenerEvento(String nombre) {

        EntityManager em = BaseDeDatos.getEntityManager();

        try {

            List<Evento> resultado =
                    em.createQuery(
                                    """
                                    SELECT DISTINCT e
                                    FROM Evento e
                                    LEFT JOIN FETCH e.categorias
                                    WHERE e.nombre = :nombre
                                    """,
                                    Evento.class
                            )
                            .setParameter("nombre", nombre)
                            .getResultList();

            if (resultado.isEmpty()) {
                return null;
            }

            return resultado.getFirst();

        } finally {
            em.close();
        }
    }

    public boolean existeEvento(String nombre) {

        EntityManager em = BaseDeDatos.getEntityManager();

        try {

            return em.find(
                    Evento.class,
                    nombre
            ) != null;

        } finally {
            em.close();
        }
    }

    public Collection<Evento> obtenerEventos() {

        EntityManager em = BaseDeDatos.getEntityManager();

        try {

            return em.createQuery(
                    """
                    SELECT DISTINCT e
                    FROM Evento e
                    LEFT JOIN FETCH e.categorias
                    ORDER BY e.nombre
                    """,
                    Evento.class
            ).getResultList();

        } finally {
            em.close();
        }
    }

    // =====================================================
    // EDICIONES
    // =====================================================

    public boolean addEdicion(Edicion edicion) {

        EntityManager em = BaseDeDatos.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {

            tx.begin();

            if (em.find(
                    Edicion.class,
                    edicion.getIdNombre()
            ) != null) {

                tx.rollback();
                return false;
            }

            em.persist(edicion);

            tx.commit();
            return true;

        } catch (Exception e) {

            if (tx.isActive()) {
                tx.rollback();
            }

            throw e;

        } finally {
            em.close();
        }
    }

    public Edicion obtenerEdicion(String nombre) {

        EntityManager em = BaseDeDatos.getEntityManager();

        try {

            List<Edicion> resultado =
                    em.createQuery(
                                    """
                                    SELECT DISTINCT e
                                    FROM Edicion e
                                    LEFT JOIN FETCH e.tiposRegistros
                                    LEFT JOIN FETCH e.organizador
                                    WHERE e.idNombre = :nombre
                                    """,
                                    Edicion.class
                            )
                            .setParameter("nombre", nombre)
                            .getResultList();

            if (resultado.isEmpty()) {
                return null;
            }

            return resultado.getFirst();

        } finally {
            em.close();
        }
    }

    public boolean existeEdicion(String nombre) {

        EntityManager em = BaseDeDatos.getEntityManager();

        try {

            return em.find(
                    Edicion.class,
                    nombre
            ) != null;

        } finally {
            em.close();
        }
    }

    public Collection<Edicion> obtenerEdiciones() {

        EntityManager em = BaseDeDatos.getEntityManager();

        try {

            return em.createQuery(
                    """
                    SELECT DISTINCT e
                    FROM Edicion e
                    LEFT JOIN FETCH e.organizador
                    ORDER BY e.idNombre
                    """,
                    Edicion.class
            ).getResultList();

        } finally {
            em.close();
        }
    }

    public Collection<Edicion> obtenerEdicionesEvento(
            String nombreEvento
    ) {

        EntityManager em = BaseDeDatos.getEntityManager();

        try {

            return em.createQuery(
                            """
                            SELECT DISTINCT e
                            FROM Edicion e
                            LEFT JOIN FETCH e.organizador
                            WHERE e.evento.nombre = :nombreEvento
                            ORDER BY e.idNombre
                            """,
                            Edicion.class
                    )
                    .setParameter(
                            "nombreEvento",
                            nombreEvento
                    )
                    .getResultList();

        } finally {
            em.close();
        }
    }

    // =====================================================
    // CATEGORÍAS
    // =====================================================

    public boolean addCategoria(Categoria categoria) {

        EntityManager em = BaseDeDatos.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {

            tx.begin();

            if (em.find(
                    Categoria.class,
                    categoria.getNombre()
            ) != null) {

                tx.rollback();
                return false;
            }

            em.persist(categoria);

            tx.commit();
            return true;

        } catch (Exception e) {

            if (tx.isActive()) {
                tx.rollback();
            }

            throw e;

        } finally {
            em.close();
        }
    }

    public Categoria obtenerCategoria(String nombre) {

        EntityManager em = BaseDeDatos.getEntityManager();

        try {

            List<Categoria> resultado =
                    em.createQuery(
                                    """
                                    SELECT DISTINCT c
                                    FROM Categoria c
                                    LEFT JOIN FETCH c.subcategorias
                                    LEFT JOIN FETCH c.padre
                                    WHERE c.nombre = :nombre
                                    """,
                                    Categoria.class
                            )
                            .setParameter("nombre", nombre)
                            .getResultList();

            if (resultado.isEmpty()) {
                return null;
            }

            return resultado.getFirst();

        } finally {
            em.close();
        }
    }

    public boolean existeCategoria(String nombre) {

        EntityManager em = BaseDeDatos.getEntityManager();

        try {

            return em.find(
                    Categoria.class,
                    nombre
            ) != null;

        } finally {
            em.close();
        }
    }

    public Collection<Categoria> obtenerCategorias() {

        EntityManager em = BaseDeDatos.getEntityManager();

        try {

            List<Categoria> categorias =
                    em.createQuery(
                            """
                            SELECT DISTINCT c
                            FROM Categoria c
                            LEFT JOIN FETCH c.subcategorias
                            LEFT JOIN FETCH c.padre
                            ORDER BY c.nombre
                            """,
                            Categoria.class
                    ).getResultList();

            // Inicializamos el árbol mientras el EntityManager sigue abierto.
            for (Categoria categoria : categorias) {
                inicializarSubcategorias(categoria);
            }

            return categorias;

        } finally {
            em.close();
        }
    }

    private void inicializarSubcategorias(
            Categoria categoria
    ) {

        categoria.getSubcategorias().size();

        for (Categoria hija :
                categoria.getSubcategorias()) {

            inicializarSubcategorias(hija);
        }
    }

    // =====================================================
    // TIPOS DE REGISTRO
    // =====================================================

    public boolean addTipoRegistro(
            TipoRegistro tipoRegistro
    ) {

        EntityManager em = BaseDeDatos.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {

            tx.begin();

            if (em.find(
                    TipoRegistro.class,
                    tipoRegistro.getIdNombre()
            ) != null) {

                tx.rollback();
                return false;
            }

            em.persist(tipoRegistro);

            tx.commit();
            return true;

        } catch (Exception e) {

            if (tx.isActive()) {
                tx.rollback();
            }

            throw e;

        } finally {
            em.close();
        }
    }

    public TipoRegistro obtenerTipoRegistro(
            String nombre
    ) {

        EntityManager em = BaseDeDatos.getEntityManager();

        try {

            return em.find(
                    TipoRegistro.class,
                    nombre
            );

        } finally {
            em.close();
        }
    }

    public boolean existeTipoRegistro(
            String nombre
    ) {

        EntityManager em = BaseDeDatos.getEntityManager();

        try {

            return em.find(
                    TipoRegistro.class,
                    nombre
            ) != null;

        } finally {
            em.close();
        }
    }

    public Collection<TipoRegistro>
    obtenerTiposRegistro() {

        EntityManager em = BaseDeDatos.getEntityManager();

        try {

            return em.createQuery(
                    """
                    SELECT t
                    FROM TipoRegistro t
                    ORDER BY t.idNombre
                    """,
                    TipoRegistro.class
            ).getResultList();

        } finally {
            em.close();
        }
    }

    public Collection<TipoRegistro>
    obtenerTiposRegistroEdicion(
            String nombreEdicion
    ) {

        EntityManager em = BaseDeDatos.getEntityManager();

        try {

            return em.createQuery(
                            """
                            SELECT t
                            FROM TipoRegistro t
                            WHERE t.edicion.idNombre = :nombreEdicion
                            ORDER BY t.idNombre
                            """,
                            TipoRegistro.class
                    )
                    .setParameter(
                            "nombreEdicion",
                            nombreEdicion
                    )
                    .getResultList();

        } finally {
            em.close();
        }
    }
}