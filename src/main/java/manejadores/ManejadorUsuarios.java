package manejadores;

import clases.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import persistencia.BaseDeDatos;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ManejadorUsuarios {

    private static ManejadorUsuarios instancia = null;
    private ManejadorUsuarios() {}

    public static ManejadorUsuarios getInstance() {
        if (instancia == null) {instancia = new ManejadorUsuarios();}
        return instancia;
    }

    public boolean addUsuario(Usuario usuario) {
        EntityManager em = BaseDeDatos.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (em.find(Usuario.class, usuario.getNickname()) != null) {
                tx.rollback();
                return false;
            }

            if (usuario instanceof Asistente asistente && asistente.getInstitucion() != null) {
                Institucion institucionGestionada = em.find(Institucion.class, asistente.getInstitucion().getNombre());
                if (institucionGestionada == null) {
                    tx.rollback();
                    return false;
                }
                asistente.setInstitucion(institucionGestionada);
            }
            em.persist(usuario);
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
    public Usuario obtenerUsuario(String nickname) {
        EntityManager em = BaseDeDatos.getEntityManager();
        try {
            return em.find(Usuario.class, nickname);
        } finally {
            em.close();
        }
    }

    public boolean existeUsuario(String nickname) {
        EntityManager em = BaseDeDatos.getEntityManager();
        try {
            return em.find(Usuario.class, nickname) != null;
        } finally {
            em.close();
        }
    }

    public boolean existeCorreo(String correoElectronico) {
        EntityManager em = BaseDeDatos.getEntityManager();
        try {
            Long cantidad = em.createQuery(
                                    """
                                    SELECT COUNT(u)
                                    FROM Usuario u
                                    WHERE LOWER(u.correoElectronico) = LOWER(:correo)
                                    """,
                                    Long.class
                            ).setParameter(
                                    "correo",
                                    correoElectronico
                            ).getSingleResult();

            return cantidad > 0;
        } finally {
            em.close();
        }
    }

    public Collection<Usuario> listarUsuarios() {
        EntityManager em = BaseDeDatos.getEntityManager();
        try {
            List<Usuario> usuarios = em.createQuery(
                            "SELECT u FROM Usuario u ORDER BY u.nickname",
                            Usuario.class
                    ).getResultList();
            return usuarios;
        } finally {
            em.close();
        }
    }

    public boolean modificarAsistente(String nickname, String nombre, String apellido, LocalDate fechaNacimiento, Institucion institucion) {
        EntityManager em = BaseDeDatos.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {

            tx.begin();
            Asistente asistente =
                    em.find(Asistente.class, nickname);
            if (asistente == null) {
                tx.rollback();
                return false;
            }
            asistente.setNombre(nombre);
            asistente.setApellido(apellido);
            asistente.setFechaNacimiento(fechaNacimiento);

            if (institucion != null) {
                Institucion institucionGestionada = em.find(Institucion.class, institucion.getNombre());
                asistente.setInstitucion(institucionGestionada);
            } else {
                asistente.setInstitucion(null);
            }
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

    public boolean modificarOrganizador(String nickname, String nombre, String descripcion, String sitioWeb) {
        EntityManager em = BaseDeDatos.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Organizador organizador = em.find(Organizador.class, nickname);

            if (organizador == null) {
                tx.rollback();
                return false;
            }

            organizador.setNombre(nombre);
            organizador.setDescripcion(descripcion);
            organizador.setSitioWeb(sitioWeb);

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
    public Organizador obtenerOrganizador(String nickname) {
        EntityManager em = BaseDeDatos.getEntityManager();
        try {
            return em.find(
                    Organizador.class,
                    nickname
            );
        } finally {
            em.close();
        }
    }
}