package Logic.Exceptions;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Excepción personalizada para errores ocurridos durante operaciones de base de datos.
 * Encapsula la causa original (normalmente un SQLException) y agrega un mensaje
 * descriptivo sin revelar el nombre de la capa ni detalles internos de implementación.
 */
public class ServiceException extends Exception {

    private static final Logger LOGGER = Logger.getLogger(ServiceException.class.getName());

    /**
     * @param message Descripción del error ocurrido.
     * @param cause   Excepción original que provocó el fallo.
     */
    public ServiceException(String message, SQLException cause) {
        super(message, cause);
        LOGGER.log(Level.SEVERE, "Error en base de datos: {0} | Causa: {1}",
                new Object[]{message, cause.getMessage()});
    }

    /**
     * Constructor genérico que acepta cualquier Throwable como causa.
     *
     * @param message Descripción del error.
     * @param cause   Causa subyacente.
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        LOGGER.log(Level.SEVERE, "Error en base de datos: {0} | Causa: {1}",
                new Object[]{message, cause.getMessage()});
    }
}
