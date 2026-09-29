package estructuras.lista;

/**
 * Excepcion lanzada cuando se intenta operar (popFront, popBack, topFront,
 * topBack) sobre una lista vacia.
 */
public class ListaVaciaException extends RuntimeException {
    public ListaVaciaException(String mensaje) {
        super(mensaje);
    }
}
