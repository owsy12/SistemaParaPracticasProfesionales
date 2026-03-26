package Logic.Interface;

import Logic.DTOs.Solicitud;

import java.util.List;

public interface ISolicitudDAO {
    Solicitud findById(int id);
    List<Solicitud> findPending();
    boolean save(Solicitud solicitud);
    boolean updateState(int idSolicitud, String newState);
}