package estructuras.pilacola;

/**
 * Interfaz de una cola (FIFO - First In, First Out).
 *
 * @param <T> tipo de dato almacenado.
 */
public interface MyQueue<T> {

    /** Inserta un elemento al final. */
    void enqueue(T x);

    /** Elimina y retorna el primer elemento. */
    T dequeue();

    /** Retorna el primer elemento sin eliminarlo. */
    T front();

    /** Verifica si la cola esta vacia. */
    boolean isEmpty();

    /** Retorna el numero de elementos. */
    int size();

    /** Elimina el primer valor n que encuentra (desde el frente hacia atras). */
    boolean delete(T n);
}
