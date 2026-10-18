package manejadores;

import clases.Institucion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import persistencia.BaseDeDatos;

import java.util.Collection;
import java.util.List;

public class ManejadorInstituciones {

    private static ManejadorInstituciones instancia = null;
    private ManejadorInstituciones() {}
    public static ManejadorInstituciones getInstance() {
        if (instancia == null) {instancia = new ManejadorInstituciones();}
        return instancia;
    }

    public boolean addInstitucion(Institucion institucion) {
        EntityManager em = BaseDeDatos.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            if (em.find(Institucion.class, institucion.getNombre()) != null) {
                tx.rollback();
                return false;
            }

            em.persist(institucion);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) {tx.rollback();}
            throw e;
        } finally {
            em.close();
        }
    }

    public Institucion obtenerInstitucion(String nombre) {
        EntityManager em = BaseDeDatos.getEntityManager();
        try {
            return em.find(Institucion.class, nombre);
        } finally {
            em.close();
        }
    }

    public boolean existeInstitucion(String nombre) {
        EntityManager em = BaseDeDatos.getEntityManager();
        try {
            return em.find(Institucion.class, nombre) != null;
        } finally {
            em.close();
        }
    }

    public Collection<Institucion> listarInstituciones() {
        EntityManager em = BaseDeDatos.getEntityManager();
        try {
            List<Institucion> instituciones = em.createQuery(
                            "SELECT i FROM Institucion i ORDER BY i.nombre",
                            Institucion.class
                    ).getResultList();
            return instituciones;

        } finally {
            em.close();
        }
    }
}