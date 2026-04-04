package Logic.DTOs;

public class TechnicalResponsible {

    private int    idTecnico;
    private int    idOrganizacion;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String correoResponsable;
    private String cargo;

    public TechnicalResponsible() {}

    public TechnicalResponsible(int idTecnico, int idOrganizacion, String nombre,
                                String apellidoPaterno, String apellidoMaterno,
                                String correoResponsable, String cargo) {
        this.idTecnico        = idTecnico;
        this.idOrganizacion   = idOrganizacion;
        this.nombre           = nombre;
        this.apellidoPaterno  = apellidoPaterno;
        this.apellidoMaterno  = apellidoMaterno;
        this.correoResponsable = correoResponsable;
        this.cargo            = cargo;
    }

    public int    getIdTecnico()                     { return idTecnico; }
    public void   setIdTecnico(int idTecnico)        { this.idTecnico = idTecnico; }

    public int    getIdOrganizacion()                        { return idOrganizacion; }
    public void   setIdOrganizacion(int idOrganizacion)      { this.idOrganizacion = idOrganizacion; }

    public String getNombre()                { return nombre; }
    public void   setNombre(String nombre)   { this.nombre = nombre; }

    public String getApellidoPaterno()                       { return apellidoPaterno; }
    public void   setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }

    public String getApellidoMaterno()                       { return apellidoMaterno; }
    public void   setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }

    public String getCorreoResponsable()                           { return correoResponsable; }
    public void   setCorreoResponsable(String correoResponsable)   { this.correoResponsable = correoResponsable; }

    public String getCargo()               { return cargo; }
    public void   setCargo(String cargo)   { this.cargo = cargo; }

    public String getFullName() {
        return nombre + " " + apellidoPaterno + " " + apellidoMaterno;
    }
}