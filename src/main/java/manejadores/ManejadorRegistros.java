package manejadores;

import clases.Asistente;
import clases.Edicion;
import clases.Registro;
import clases.TipoRegistro;
import java.util.Collection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import persistencia.BaseDeDatos;

public class ManejadorRegistros {

    private static ManejadorRegistros instancia = null;
    private ManejadorRegistros() {}
    public static ManejadorRegistros getInstance() {
        if (instancia == null) {
            instancia = new ManejadorRegistros();
        }
        return instancia;
    }

    public boolean existeRegistroAsistenteEdicion(String nickname, String nombreEdicion) {
        EntityManager em = BaseDeDatos.getEntityManager();
        try {
            Long cantidad = em.createQuery(
                                    """
                                    SELECT COUNT(r)
                                    FROM Registro r
                                    WHERE r.asistente.nickname = :nickname
                                      AND r.edicion.idNombre = :edicion
                                    """,
                                    Long.class
                            ).setParameter(
                                    "nickname",
                                    nickname
                            ).setParameter(
                                    "edicion",
                                    nombreEdicion
                            ).getSingleResult();
            return cantidad > 0;
        } finally {
            em.close();
        }
    }


    public long contarRegistrosTipo(String nombreEdicion, String nombreTipoRegistro) {
        EntityManager em = BaseDeDatos.getEntityManager();
        try {
            return em.createQuery(
                            """
                            SELECT COUNT(r)
                            FROM Registro r
                            WHERE r.edicion.idNombre = :edicion
                              AND LOWER(r.tipoRegistro.idNombre)
                                  = LOWER(:tipo)
                            """,
                            Long.class
                    ).setParameter(
                            "edicion",
                            nombreEdicion
                    ).setParameter(
                            "tipo",
                            nombreTipoRegistro
                    ).getSingleResult();
        } finally {
            em.close();
        }
    }

    public boolean addRegistro(Registro registro) {
        EntityManager em = BaseDeDatos.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Asistente asistenteGestionado = em.find(Asistente.class, registro.getAsistente().getNickname());
            Edicion edicionGestionada = em.find(Edicion.class, registro.getEdicion().getIdNombre());

            TipoRegistro tipoGestionado = null;
            if (registro.getTipoRegistro() != null) {
                Long idTipoRegistro = registro.getTipoRegistro().getId();

                if (idTipoRegistro != null) {
                    tipoGestionado = em.find(TipoRegistro.class, idTipoRegistro);
                }
            }

            if (asistenteGestionado == null || edicionGestionada == null || tipoGestionado == null) {
                tx.rollback();
                return false;
            }

            if (tipoGestionado.getEdicion() == null || !tipoGestionado.getEdicion().getIdNombre().equals(edicionGestionada.getIdNombre())) {
                tx.rollback();
                return false;
            }

            registro.setAsistente(asistenteGestionado);
            registro.setEdicion(edicionGestionada);
            registro.setTipoRegistro(tipoGestionado);

            em.persist(registro);
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

    public Collection<Registro> obtenerRegistrosAsistente(
            String nickname
    ) {
        EntityManager em = BaseDeDatos.getEntityManager();

        try {
            return em.createQuery(
                            """
                            SELECT r
                            FROM Registro r
                            LEFT JOIN FETCH r.tipoRegistro
                            LEFT JOIN FETCH r.edicion
                            WHERE r.asistente.nickname = :nickname
                            ORDER BY r.fechaRegistro
                            """,
                            Registro.class
                    ).setParameter(
                            "nickname",
                            nickname
                    ).getResultList();

        } finally {
            em.close();
        }
    }

    public Registro obtenerRegistro(String nickname, String nombreEdicion
    ) {
        EntityManager em = BaseDeDatos.getEntityManager();

        try {
            return em.createQuery(
                            """
                            SELECT r
                            FROM Registro r
                            LEFT JOIN FETCH r.tipoRegistro
                            LEFT JOIN FETCH r.edicion
                            WHERE r.asistente.nickname = :nickname
                              AND r.edicion.idNombre = :edicion
                            """,
                            Registro.class
                    ).setParameter(
                            "nickname",
                            nickname
                    ).setParameter(
                            "edicion",
                            nombreEdicion
                    ).getSingleResult();

        } catch (jakarta.persistence.NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }


    public Collection<Registro> obtenerRegistrosEdicion(
            String nombreEdicion
    ) {
        EntityManager em = BaseDeDatos.getEntityManager();
        try {
            return em.createQuery(
                            """
                            SELECT r
                            FROM Registro r
                            LEFT JOIN FETCH r.tipoRegistro
                            LEFT JOIN FETCH r.edicion
                            WHERE r.edicion.idNombre = :edicion
                            ORDER BY r.fechaRegistro
                            """,
                            Registro.class
                    ).setParameter(
                            "edicion",
                            nombreEdicion
                    ).getResultList();
        } finally {
            em.close();
        }
    }
}