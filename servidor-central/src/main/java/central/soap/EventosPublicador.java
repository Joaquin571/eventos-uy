package central.soap;

import implementacion.Fabrica;
import interfaces.ISistema;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebService(name = "EventosPublicos", serviceName = "EventosService", portName = "EventosPort",
        targetNamespace = "https://eventos.uy/soap/eventos")
public class EventosPublicador {
    private static final Logger LOG = Logger.getLogger(EventosPublicador.class.getName());
    private final ISistema sistema;

    public EventosPublicador() { this(Fabrica.getInstance().getISistema()); }
    public EventosPublicador(ISistema sistema) { this.sistema = Objects.requireNonNull(sistema); }

    @WebMethod
    public List<EventoSoap> listarEventos() throws ConsultaException {
        try {
            List<EventoSoap> resultado = new ArrayList<>();
            for (var evento : sistema.listarEventos()) {
                EventoSoap dato = new EventoSoap();
                dato.setNombre(evento.getNombre());
                dato.setSigla(evento.getSigla());
                dato.setDescripcion(evento.getDescripcion());
                dato.setFechaAlta(evento.getFechaAlta() == null ? null : evento.getFechaAlta().toString());
                dato.setCategorias(evento.getCategorias() == null ? new ArrayList<>() : new ArrayList<>(evento.getCategorias()));
                resultado.add(dato);
            }
            return resultado;
        } catch (RuntimeException error) {
            LOG.log(Level.SEVERE, "No se pudo consultar el listado de eventos.", error);
            throw new ConsultaException("No se pudo consultar el listado de eventos. Intentá nuevamente más tarde.");
        }
    }
}
