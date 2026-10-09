
package manejadores;

import clases.Asistente;
import clases.Edicion;
import clases.Patrocinio;
import clases.Registro;
import clases.TipoRegistro;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;

import persistencia.BaseDeDatos;

import java.time.LocalDate;
import java.util.Collection;

public class ManejadorRegistros {

    private static ManejadorRegistros instancia = null;

    private ManejadorRegistros() {}

    public static ManejadorRegistros getInstance() {
        if (instancia == null) {
            instancia = new ManejadorRegistros();
        }
        return instancia;
    }

    public boolean existeRegistroAsistenteEdicion(
            String nickname,
            String nombreEdicion
    ) {
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
                    )
                    .setParameter("nickname", nickname)
                    .setParameter("edicion", nombreEdicion)
                    .getSingleResult();

            return cantidad > 0;
        } finally {
            em.close();
        }
    }

    public long contarRegistrosTipo(
            String nombreEdicion,
            String nombreTipoRegistro
    ) {
        EntityManager em = BaseDeDatos.getEntityManager();

        try {
            return em.createQuery(
                            """
                            SELECT COUNT(r)
                            FROM Registro r
                            WHERE r.edicion.idNombre = :edicion
                              AND LOWER(r.tipoRegistro.idNombre) = LOWER(:tipo)
                            """,
                            Long.class
                    )
                    .setParameter("edicion", nombreEdicion)
                    .setParameter("tipo", nombreTipoRegistro)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    public boolean addRegistro(Registro registro) {
        EntityManager em = BaseDeDatos.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Asistente asistenteGestionado = em.find(
                    Asistente.class,
                    registro.getAsistente().getNickname()
            );

            Edicion edicionGestionada = em.find(
                    Edicion.class,
                    registro.getEdicion().getIdNombre()
            );

            TipoRegistro tipoGestionado = null;

            if (registro.getTipoRegistro() != null) {
                Long idTipoRegistro = registro.getTipoRegistro().getId();

                if (idTipoRegistro != null) {
                    tipoGestionado = em.find(
                            TipoRegistro.class,
                            idTipoRegistro
                    );
                }
            }

            if (asistenteGestionado == null
                    || edicionGestionada == null
                    || tipoGestionado == null) {
                tx.rollback();
                return false;
            }

            if (tipoGestionado.getEdicion() == null
                    || !tipoGestionado.getEdicion().getIdNombre()
                    .equals(edicionGestionada.getIdNombre())) {
                tx.rollback();
                return false;
            }

            registro.setAsistente(asistenteGestionado);
            registro.setEdicion(edicionGestionada);
            registro.setTipoRegistro(tipoGestionado);

            if (registro.getPatrocinio() != null) {
                Patrocinio patrocinioGestionado = em.find(
                        Patrocinio.class,
                        registro.getPatrocinio().getCodigoPatrocinio()
                );

                if (patrocinioGestionado == null) {
                    tx.rollback();
                    return false;
                }

                registro.setPatrocinio(patrocinioGestionado);
            }

            em.persist(registro);
            tx.commit();
            return true;

        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public boolean registrar(
            String nickname,
            String nombreEdicion,
            String nombreTipoRegistro,
            LocalDate fecha,
            String codigoPatrocinio
    ) {
        EntityManager em = BaseDeDatos.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Asistente asistente = em.find(
                    Asistente.class,
                    nickname
            );

            Edicion edicion = em.find(
                    Edicion.class,
                    nombreEdicion
            );

            if (asistente == null || edicion == null) {
                throw new IllegalArgumentException(
                        "El asistente o la edición no existen."
                );
            }

            TipoRegistro tipo = em.createQuery(
                            """
                            SELECT t
                            FROM TipoRegistro t
                            WHERE t.edicion.idNombre = :edicion
                              AND t.idNombre = :nombre
                            """,
                            TipoRegistro.class
                    )
                    .setParameter("edicion", nombreEdicion)
                    .setParameter("nombre", nombreTipoRegistro)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (tipo == null) {
                throw new IllegalArgumentException(
                        "El tipo de registro no existe."
                );
            }

            // Bloquea el tipo de registro mientras se comprueba su cupo.
            em.lock(tipo, LockModeType.PESSIMISTIC_WRITE);

            String codigo = codigoPatrocinio == null
                    ? ""
                    : codigoPatrocinio.trim();

            Patrocinio patrocinio = null;

            if (!codigo.isEmpty()) {
                patrocinio = em.find(
                        Patrocinio.class,
                        codigo,
                        LockModeType.PESSIMISTIC_WRITE
                );

                if (patrocinio == null) {
                    throw new IllegalArgumentException(
                            "El código de patrocinio no existe."
                    );
                }

                if (asistente.getInstitucion() == null
                        || patrocinio.getInstitucion() == null
                        || !asistente.getInstitucion().getNombre()
                        .equals(patrocinio.getInstitucion().getNombre())) {

                    throw new IllegalArgumentException(
                            "El patrocinio no corresponde a la institución del asistente."
                    );
                }

                if (patrocinio.getEdicion() == null
                        || !patrocinio.getEdicion().getIdNombre()
                        .equals(nombreEdicion)) {

                    throw new IllegalArgumentException(
                            "El patrocinio no corresponde a esta edición."
                    );
                }

                if (patrocinio.getTipoRegistro() == null
                        || !patrocinio.getTipoRegistro().getId()
                        .equals(tipo.getId())) {

                    throw new IllegalArgumentException(
                            "El patrocinio no corresponde al tipo de registro seleccionado."
                    );
                }

                Long utilizados = em.createQuery(
                                """
                                SELECT COUNT(r)
                                FROM Registro r
                                WHERE r.patrocinio.codigoPatrocinio = :codigo
                                """,
                                Long.class
                        )
                        .setParameter("codigo", codigo)
                        .getSingleResult();

                if (utilizados >= patrocinio.getCantRegistrosGrat()) {
                    throw new IllegalArgumentException(
                            "Se agotaron los registros gratuitos de este patrocinio."
                    );
                }
            }

            Long existentes = em.createQuery(
                            """
                            SELECT COUNT(r)
                            FROM Registro r
                            WHERE r.asistente.nickname = :nickname
                              AND r.edicion.idNombre = :edicion
                            """,
                            Long.class
                    )
                    .setParameter("nickname", nickname)
                    .setParameter("edicion", nombreEdicion)
                    .getSingleResult();

            if (existentes > 0) {
                throw new IllegalArgumentException(
                        "El asistente ya está registrado en esta edición."
                );
            }

            Long registrados = em.createQuery(
                            """
                            SELECT COUNT(r)
                            FROM Registro r
                            WHERE r.tipoRegistro.id = :tipoId
                            """,
                            Long.class
                    )
                    .setParameter("tipoId", tipo.getId())
                    .getSingleResult();

            if (registrados >= tipo.getCupo()) {
                throw new IllegalArgumentException(
                        "Se alcanzó el cupo máximo del tipo de registro."
                );
            }

            float costo = patrocinio == null
                    ? tipo.getCosto()
                    : 0;

            Registro registro = new Registro(
                    fecha,
                    costo,
                    tipo,
                    edicion
            );

            registro.setAsistente(asistente);
            registro.setPatrocinio(patrocinio);

            em.persist(registro);
            tx.commit();

            return true;

        } catch (RuntimeException e) {
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
                    )
                    .setParameter("nickname", nickname)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Registro obtenerRegistro(
            String nickname,
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
                            WHERE r.asistente.nickname = :nickname
                              AND r.edicion.idNombre = :edicion
                            """,
                            Registro.class
                    )
                    .setParameter("nickname", nickname)
                    .setParameter("edicion", nombreEdicion)
                    .getSingleResult();

        } catch (NoResultException e) {
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
                    )
                    .setParameter("edicion", nombreEdicion)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
