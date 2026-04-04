package Logic.Interface;

import Logic.DTOs.LinkedOrganization;
import java.util.List;

public interface ILinkedOrganizationDAO {

    boolean saveLinkedOrganization(LinkedOrganization linkedOrganization);

    LinkedOrganization findById(int idOrganizacion);
    List<LinkedOrganization> findAll();
    List<LinkedOrganization> findAllActive();
    boolean update(LinkedOrganization linkedOrganization);
    boolean deactivateLinkedOrganization(int idOrganizacion);
}