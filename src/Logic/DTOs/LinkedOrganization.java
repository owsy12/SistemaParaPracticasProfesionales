package Logic.DTOs;

public class LinkedOrganization {

    private int    idOrganizacion;
    private String nombreOrganizacion;
    private String correoOrganizacion;
    private String direccion;
    private String sector;
    private String estado;

    public LinkedOrganization() {}

    public LinkedOrganization(int idOrganizacion, String nombreOrganizacion,
                              String correoOrganizacion, String direccion,
                              String sector, String estado) {
        this.idOrganizacion    = idOrganizacion;
        this.nombreOrganizacion = nombreOrganizacion;
        this.correoOrganizacion = correoOrganizacion;
        this.direccion         = direccion;
        this.sector            = sector;
        this.estado            = estado;
    }

    public int    getIdOrganizacion()                        { return idOrganizacion; }
    public void   setIdOrganizacion(int idOrganizacion)      { this.idOrganizacion = idOrganizacion; }

    public String getNombreOrganizacion()                              { return nombreOrganizacion; }
    public void   setNombreOrganizacion(String nombreOrganizacion)     { this.nombreOrganizacion = nombreOrganizacion; }

    public String getCorreoOrganizacion()                              { return correoOrganizacion; }
    public void   setCorreoOrganizacion(String correoOrganizacion)     { this.correoOrganizacion = correoOrganizacion; }

    public String getDireccion()                   { return direccion; }
    public void   setDireccion(String direccion)   { this.direccion = direccion; }

    public String getSector()                { return sector; }
    public void   setSector(String sector)   { this.sector = sector; }

    public String getEstado()                { return estado; }
    public void   setEstado(String estado)   { this.estado = estado; }
}