package Logic.Interface;

import Logic.DTOs.Solicitud;
import java.util.List;

public interface ISolicitudDAO {
    boolean saveSolicitud(Solicitud solicitud);
    Solicitud findById(int idSolicitud);
    Solicitud findByIntern(int idPracticante);
    List<Solicitud> findAll();
    List<Solicitud> findByStatus(String estado);
    boolean updateStatus(int idSolicitud, String estado);
}