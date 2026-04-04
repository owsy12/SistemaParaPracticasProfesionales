package Logic.DTOs;

import java.time.LocalDate;

public class Project {

    private int       idProyecto;
    private int       idOrganizacion;
    private int       idTecnico;
    private int       idCoordinador;
    private String    nombre;
    private String    descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private int       cupoMaximo;
    private int       cupoDisponible;
    private String    estado;

    public Project() {}

    public Project(int idProyecto, int idOrganizacion, int idTecnico, int idCoordinador,
                   String nombre, String descripcion, LocalDate fechaInicio, LocalDate fechaFin,
                   int cupoMaximo, int cupoDisponible, String estado) {
        this.idProyecto     = idProyecto;
        this.idOrganizacion = idOrganizacion;
        this.idTecnico      = idTecnico;
        this.idCoordinador  = idCoordinador;
        this.nombre         = nombre;
        this.descripcion    = descripcion;
        this.fechaInicio    = fechaInicio;
        this.fechaFin       = fechaFin;
        this.cupoMaximo     = cupoMaximo;
        this.cupoDisponible = cupoDisponible;
        this.estado         = estado;
    }

    public int       getIdProyecto()                       { return idProyecto; }
    public void      setIdProyecto(int idProyecto)         { this.idProyecto = idProyecto; }

    public int       getIdOrganizacion()                           { return idOrganizacion; }
    public void      setIdOrganizacion(int idOrganizacion)         { this.idOrganizacion = idOrganizacion; }

    public int       getIdTecnico()                        { return idTecnico; }
    public void      setIdTecnico(int idTecnico)           { this.idTecnico = idTecnico; }

    public int       getIdCoordinador()                            { return idCoordinador; }
    public void      setIdCoordinador(int idCoordinador)           { this.idCoordinador = idCoordinador; }

    public String    getNombre()                 { return nombre; }
    public void      setNombre(String nombre)    { this.nombre = nombre; }

    public String    getDescripcion()                    { return descripcion; }
    public void      setDescripcion(String descripcion)  { this.descripcion = descripcion; }

    public LocalDate getFechaInicio()                        { return fechaInicio; }
    public void      setFechaInicio(LocalDate fechaInicio)   { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin()                     { return fechaFin; }
    public void      setFechaFin(LocalDate fechaFin)   { this.fechaFin = fechaFin; }

    public int       getCupoMaximo()                       { return cupoMaximo; }
    public void      setCupoMaximo(int cupoMaximo)         { this.cupoMaximo = cupoMaximo; }

    public int       getCupoDisponible()                           { return cupoDisponible; }
    public void      setCupoDisponible(int cupoDisponible)         { this.cupoDisponible = cupoDisponible; }

    public String    getEstado()                 { return estado; }
    public void      setEstado(String estado)    { this.estado = estado; }
}