package estructuras.pilacola;

/**
 * Interfaz de una pila (LIFO - Last In, First Out).
 *
 * @param <T> tipo de dato almacenado.
 */
public interface MyStack<T> {

    /** Inserta un elemento en la cima. */
    void push(T x);

    /** Elimina y retorna el elemento en la cima. */
    T pop();

    /** Retorna el elemento en la cima sin eliminarlo. */
    T peek();

    /** Verifica si la pila esta vacia. */
    boolean isEmpty();

    /** Retorna el numero de elementos. */
    int size();

    /** Elimina el primer valor n que encuentra (desde la cima hacia el fondo). */
    boolean delete(T n);
}
