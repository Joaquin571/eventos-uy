package manejadores;

import clases.Edicion;
import clases.Institucion;
import clases.Patrocinio;
import clases.TipoRegistro;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import persistencia.BaseDeDatos;

import java.util.Collection;
import java.util.List;

public class ManejadorPatrocinios {

    private static ManejadorPatrocinios instancia = null;

    private ManejadorPatrocinios() {
    }

    public static ManejadorPatrocinios getInstance() {

        if (instancia == null) {
            instancia = new ManejadorPatrocinios();
        }

        return instancia;
    }

    // =====================================================
    // ALTA PATROCINIO
    // =====================================================

    public boolean addPatrocinio(
            Patrocinio patrocinio
    ) {

        EntityManager em =
                BaseDeDatos.getEntityManager();

        EntityTransaction tx =
                em.getTransaction();

        try {

            tx.begin();

            // =================================================
            // CÓDIGO DE PATROCINIO ÚNICO
            // =================================================

            if (em.find(
                    Patrocinio.class,
                    patrocinio.getCodigoPatrocinio()
            ) != null) {

                tx.rollback();
                return false;
            }

            // =================================================
            // INSTITUCIÓN MANAGED
            // =================================================

            Institucion institucionGestionada = null;

            if (patrocinio.getInstitucion() != null) {

                institucionGestionada =
                        em.find(
                                Institucion.class,
                                patrocinio
                                        .getInstitucion()
                                        .getNombre()
                        );

                if (institucionGestionada == null) {

                    tx.rollback();
                    return false;
                }
            }

            // =================================================
            // EDICIÓN MANAGED
            // =================================================

            Edicion edicionGestionada = null;

            if (patrocinio.getEdicion() != null) {

                edicionGestionada =
                        em.find(
                                Edicion.class,
                                patrocinio
                                        .getEdicion()
                                        .getIdNombre()
                        );

                if (edicionGestionada == null) {

                    tx.rollback();
                    return false;
                }
            }

            // =================================================
            // TIPO DE REGISTRO MANAGED
            // =================================================

            TipoRegistro tipoRegistroGestionado = null;

            if (patrocinio.getTipoRegistro() != null) {

                Long idTipoRegistro =
                        patrocinio
                                .getTipoRegistro()
                                .getId();

                if (idTipoRegistro == null) {

                    tx.rollback();
                    return false;
                }

                tipoRegistroGestionado =
                        em.find(
                                TipoRegistro.class,
                                idTipoRegistro
                        );

                if (tipoRegistroGestionado == null) {

                    tx.rollback();
                    return false;
                }
            }

            // =================================================
            // VALIDAR QUE EL TIPO PERTENEZCA A LA EDICIÓN
            // =================================================

            if (edicionGestionada != null
                    && tipoRegistroGestionado != null) {

                if (tipoRegistroGestionado.getEdicion() == null
                        || !tipoRegistroGestionado
                        .getEdicion()
                        .getIdNombre()
                        .equals(
                                edicionGestionada
                                        .getIdNombre()
                        )) {

                    tx.rollback();
                    return false;
                }
            }

            // =================================================
            // CREAR PATROCINIO CON ENTIDADES MANAGED
            // =================================================

            Patrocinio patrocinioGestionado =
                    new Patrocinio(
                            patrocinio.getFecha(),
                            patrocinio.getMontoAporte(),
                            patrocinio.getCantRegistrosGrat(),
                            patrocinio.getCodigoPatrocinio(),
                            patrocinio.getNivel(),
                            institucionGestionada
                    );

            patrocinioGestionado.setEdicion(
                    edicionGestionada
            );

            patrocinioGestionado.setTipoRegistro(
                    tipoRegistroGestionado
            );

            em.persist(
                    patrocinioGestionado
            );

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

    // =====================================================
    // EXISTE PATROCINIO
    // =====================================================

    public boolean existePatrocinio(
            String codigo
    ) {

        EntityManager em =
                BaseDeDatos.getEntityManager();

        try {

            return em.find(
                    Patrocinio.class,
                    codigo
            ) != null;

        } finally {

            em.close();
        }
    }

    // =====================================================
    // OBTENER PATROCINIO
    // =====================================================

    public Patrocinio obtenerPatrocinio(
            String codigo
    ) {

        EntityManager em =
                BaseDeDatos.getEntityManager();

        try {

            List<Patrocinio> resultado =
                    em.createQuery(
                                    """
                                    SELECT p
                                    FROM Patrocinio p
                                    LEFT JOIN FETCH p.institucion
                                    LEFT JOIN FETCH p.edicion
                                    LEFT JOIN FETCH p.tipoRegistro
                                    WHERE p.codigoPatrocinio = :codigo
                                    """,
                                    Patrocinio.class
                            )
                            .setParameter(
                                    "codigo",
                                    codigo
                            )
                            .getResultList();

            if (resultado.isEmpty()) {
                return null;
            }

            return resultado.getFirst();

        } finally {

            em.close();
        }
    }

    // =====================================================
    // LISTAR PATROCINIOS
    // =====================================================

    public Collection<Patrocinio>
    listarPatrocinios() {

        EntityManager em =
                BaseDeDatos.getEntityManager();

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
                    )
                    .getResultList();

        } finally {

            em.close();
        }
    }

    // =====================================================
    // PATROCINIOS DE UNA EDICIÓN
    // =====================================================

    public Collection<Patrocinio>
    obtenerPatrociniosEdicion(
            String nombreEdicion
    ) {

        EntityManager em =
                BaseDeDatos.getEntityManager();

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

    // =====================================================
    // INSTITUCIÓN YA PATROCINA ESA EDICIÓN
    // =====================================================

    public boolean existePatrocinioInstitucionEdicion(
            String nombreInstitucion,
            String nombreEdicion
    ) {

        EntityManager em =
                BaseDeDatos.getEntityManager();

        try {

            Long cantidad =
                    em.createQuery(
                                    """
                                    SELECT COUNT(p)
                                    FROM Patrocinio p
                                    WHERE LOWER(p.institucion.nombre)
                                          = LOWER(:institucion)
                                      AND LOWER(p.edicion.idNombre)
                                          = LOWER(:edicion)
                                    """,
                                    Long.class
                            )
                            .setParameter(
                                    "institucion",
                                    nombreInstitucion
                            )
                            .setParameter(
                                    "edicion",
                                    nombreEdicion
                            )
                            .getSingleResult();

            return cantidad > 0;

        } finally {

            em.close();
        }
    }
}