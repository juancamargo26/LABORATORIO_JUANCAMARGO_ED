package estructuras.pilacola;

/**
 * Excepcion lanzada al operar (pop, peek, dequeue, front) sobre una pila o
 * cola vacia.
 */
public class EstructuraVaciaException extends RuntimeException {
    public EstructuraVaciaException(String mensaje) {
        super(mensaje);
    }
}
