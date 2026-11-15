package manejadores;

import clases.ConImagen;
import clases.Edicion;
import clases.Evento;
import clases.Imagen;
import clases.Institucion;
import clases.Usuario;
import datatypes.DtImagen;
import datatypes.TipoEntidadImagen;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import persistencia.BaseDeDatos;

public class ManejadorImagenes {

    private static ManejadorImagenes instancia = null;

    private ManejadorImagenes() {
    }

    public static synchronized ManejadorImagenes getInstance() {
        if (instancia == null) {
            instancia = new ManejadorImagenes();
        }
        return instancia;
    }

    private Class<? extends ConImagen> claseDe(TipoEntidadImagen tipo) {
        return switch (tipo) {
            case USUARIO -> Usuario.class;
            case EVENTO -> Evento.class;
            case EDICION -> Edicion.class;
            case INSTITUCION -> Institucion.class;
        };
    }

    // Devuelve false si la entidad no existe. Si ya tenía imagen, la reemplaza.
    public boolean asociar(TipoEntidadImagen tipo, String clave,
                           String tipoContenido, byte[] contenido) {
        EntityManager em = BaseDeDatos.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // Bloquear antes de hidratar evita reescribir campos ajenos con una copia obsoleta.
            String consulta = switch (tipo) {
                case USUARIO -> "SELECT nickname FROM usuario WHERE nickname = :clave FOR UPDATE";
                case EVENTO -> "SELECT nombre FROM evento WHERE nombre = :clave FOR UPDATE";
                case EDICION -> "SELECT idNombre FROM edicion WHERE idNombre = :clave FOR UPDATE";
                case INSTITUCION -> "SELECT nombre FROM institucion WHERE nombre = :clave FOR UPDATE";
            };
            if (em.createNativeQuery(consulta).setParameter("clave", clave).getResultList().isEmpty()) {
                tx.rollback();
                return false;
            }
            ConImagen entidad = em.find(claseDe(tipo), clave);

            if (entidad == null) {
                tx.rollback();
                return false;
            }

            entidad.setImagen(new Imagen(tipoContenido, contenido));

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

    // Devuelve null si la entidad no existe o no tiene imagen.
    public DtImagen obtener(TipoEntidadImagen tipo, String clave) {
        EntityManager em = BaseDeDatos.getEntityManager();
        try {
            ConImagen entidad = em.find(claseDe(tipo), clave);

            if (entidad == null || entidad.getImagen() == null) {
                return null;
            }

            Imagen imagen = entidad.getImagen();
            return new DtImagen(imagen.getTipoContenido(), imagen.getContenido());
        } finally {
            em.close();
        }
    }
}