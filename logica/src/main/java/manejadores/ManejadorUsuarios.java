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

    public static synchronized ManejadorUsuarios getInstance() {
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
            Long correosExistentes = em.createQuery(
                    """
                    SELECT COUNT(u)
                    FROM Usuario u
                    WHERE LOWER(u.correoElectronico) = LOWER(:correo)
                    """,
                    Long.class
            ).setParameter("correo", usuario.getCorreoElectronico()).getSingleResult();

            if (correosExistentes > 0) {
                tx.rollback();
                return false;
            }
            em.persist(usuario);
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ConflictosPersistencia.traducir(e, "Ya existe un usuario con ese nickname o correo electrónico.");
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
        return modificarAsistente(nickname, nombre, apellido, fechaNacimiento, institucion, null);
    }

    // Datos y contraseña opcional se confirman juntos; no llama a otra transacción.
    public boolean modificarAsistente(String nickname, String nombre, String apellido,
                                     LocalDate fechaNacimiento, Institucion institucion, String nuevaContrasenia) {
        EntityManager em = BaseDeDatos.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {

            tx.begin();
            Usuario usuario = em.find(Usuario.class, nickname);
            if (usuario == null) {
                tx.rollback();
                return false;
            }
            if (!(usuario instanceof Asistente asistente)) {
                throw new IllegalArgumentException("El usuario no es un asistente.");
            }
            Institucion institucionGestionada = null;
            if (institucion != null) {
                institucionGestionada = em.find(Institucion.class, institucion.getNombre());
                if (institucionGestionada == null) {
                    throw new IllegalArgumentException("La institución indicada no existe.");
                }
            }
            asistente.setNombre(nombre);
            asistente.setApellido(apellido);
            asistente.setFechaNacimiento(fechaNacimiento);
            asistente.setInstitucion(institucionGestionada);
            if (nuevaContrasenia != null && !nuevaContrasenia.isEmpty()) {
                asistente.setContrasenia(nuevaContrasenia);
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
        return modificarOrganizador(nickname, nombre, descripcion, sitioWeb, null);
    }

    public boolean modificarOrganizador(String nickname, String nombre, String descripcion,
                                       String sitioWeb, String nuevaContrasenia) {
        EntityManager em = BaseDeDatos.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Usuario usuario = em.find(Usuario.class, nickname);
            if (usuario == null) {
                tx.rollback();
                return false;
            }
            if (!(usuario instanceof Organizador organizador)) {
                throw new IllegalArgumentException("El usuario no es un organizador.");
            }

            organizador.setNombre(nombre);
            organizador.setDescripcion(descripcion);
            organizador.setSitioWeb(sitioWeb);
            if (nuevaContrasenia != null && !nuevaContrasenia.isEmpty()) {
                organizador.setContrasenia(nuevaContrasenia);
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
    /** Consulta heredada de una fila; no usar para autenticar identificadores ambiguos. */
    public Usuario obtenerPorNicknameOCorreo(String clave) {
        return buscarPorNicknameOCorreo(clave).stream().findFirst().orElse(null);
    }

    /** Todos los candidatos: nickname y correo pertenecen a espacios distintos. */
    public Collection<Usuario> buscarPorNicknameOCorreo(String clave) {
        EntityManager em = BaseDeDatos.getEntityManager();
        try {
            return em.createQuery(
                            """
                            SELECT u
                            FROM Usuario u
                            WHERE u.nickname = :clave
                               OR LOWER(u.correoElectronico) = LOWER(:clave)
                            """,
                            Usuario.class
                    )
                    .setParameter("clave", clave)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public boolean cambiarContrasenia(String nickname, String nueva) {
        EntityManager em = BaseDeDatos.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Usuario usuario = em.find(Usuario.class, nickname);

            if (usuario == null) {
                tx.rollback();
                return false;
            }

            usuario.setContrasenia(nueva);
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
}
