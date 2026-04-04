package Logic.Interface;

import Logic.DTOs.SolicitudProject;
import java.util.List;

public interface ISolicitudProjectDAO {
    boolean saveSolicitudProject(SolicitudProject solicitudProject);
    List<SolicitudProject> findBySolicitud(int idSolicitud);
    SolicitudProject findById(int idSolicitudProyecto);
    boolean delete(int idSolicitudProyecto);
}