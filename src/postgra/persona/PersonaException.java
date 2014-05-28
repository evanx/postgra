/*
 */
package postgra.persona;

import vellum.exception.DisplayException;

/**
 *
 * @author evan.summers
 */
public class PersonaException extends DisplayException {

    public PersonaException(String message) {
        super(message);
    }

    public PersonaException(String status, String reason) {
        this(String.format("%s: %s", status, reason));
    }
}
