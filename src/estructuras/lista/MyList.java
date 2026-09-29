package estructuras.lista;

/**
 * Interfaz comun para las cuatro implementaciones de lista enlazada:
 *   - Sencillamente enlazada sin cola  (SinglyLinkedList)
 *   - Sencillamente enlazada con cola   (SinglyLinkedListTail)
 *   - Doblemente enlazada sin cola      (DoublyLinkedList)
 *   - Doblemente enlazada con cola      (DoublyLinkedListTail)
 *
 * Definir un contrato unico permite escribir el benchmark una sola vez y
 * ejecutarlo contra cualquier implementacion, garantizando que todas se
 * midan bajo exactamente las mismas condiciones.
 *
 * @param <T> tipo de dato almacenado en la lista.
 */
public interface MyList<T> {

    /** Inserta un elemento al inicio de la lista. */
    void pushFront(T value);

    /** Inserta un elemento al final de la lista. */
    void pushBack(T value);

    /** Elimina y retorna el primer elemento de la lista. */
    T popFront();

    /** Elimina y retorna el ultimo elemento de la lista. */
    T popBack();

    /** Retorna la referencia (nodo) que contiene el valor buscado, o null si no existe. */
    Object find(T value);

    /** Elimina la primera aparicion del valor indicado. Retorna true si se elimino. */
    boolean erase(T value);

    /** Inserta 'value' inmediatamente antes de la primera aparicion de 'reference'. */
    boolean addBefore(T reference, T value);

    /** Inserta 'value' inmediatamente despues de la primera aparicion de 'reference'. */
    boolean addAfter(T reference, T value);

    /** Retorna true si la lista no tiene elementos. */
    boolean empty();

    /** Retorna el primer elemento sin eliminarlo. */
    T topFront();

    /** Retorna el ultimo elemento sin eliminarlo. */
    T topBack();

    /** Retorna la cantidad de elementos almacenados. */
    int size();
}
