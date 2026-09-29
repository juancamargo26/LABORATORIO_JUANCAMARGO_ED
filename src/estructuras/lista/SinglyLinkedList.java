package estructuras.lista;

/**
 * Lista SENCILLAMENTE enlazada SIN referencia a la cola (Singly Linked List - No Tail).
 *
 * Solo se mantiene una referencia al primer nodo (head). Cada nodo conoce
 * unicamente a su sucesor. No existe puntero al ultimo nodo, por lo que
 * cualquier operacion que necesite el final de la lista debe recorrerla
 * completa.
 *
 * Analisis de complejidad (n = numero de elementos):
 *   pushFront  O(1)   -> solo se crea un nodo y se reengancha head.
 *   pushBack   O(n)   -> hay que recorrer hasta el ultimo nodo (no hay tail).
 *   popFront   O(1)   -> se avanza head al siguiente.
 *   popBack    O(n)   -> hay que llegar al penultimo nodo para desengancharlo.
 *   find       O(n)   -> busqueda lineal.
 *   erase      O(n)   -> busqueda lineal + reenganche O(1).
 *   addBefore  O(n)   -> hay que ubicar el nodo anterior a la referencia.
 *   addAfter   O(n)   -> busqueda lineal de la referencia + reenganche O(1).
 *   topFront   O(1)   -> acceso directo a head.
 *   topBack    O(n)   -> recorrido hasta el final.
 *   empty/size O(1)   -> size cacheado en un contador.
 */
public class SinglyLinkedList<T> implements MyList<T> {

    /** Nodo de una lista sencillamente enlazada: dato + referencia al siguiente. */
    static class Node<T> {
        T value;
        Node<T> next;
        Node(T value) { this.value = value; }
    }

    private Node<T> head;
    private int size;

    // ---------------------------------------------------------------- pushFront
    /** O(1): crea nodo y lo coloca como nuevo head. */
    @Override
    public void pushFront(T value) {
        Node<T> nuevo = new Node<>(value);
        nuevo.next = head;
        head = nuevo;
        size++;
    }

    // ----------------------------------------------------------------- pushBack
    /** O(n): sin tail, se recorre hasta el final para enganchar el nuevo nodo. */
    @Override
    public void pushBack(T value) {
        Node<T> nuevo = new Node<>(value);
        if (head == null) {
            head = nuevo;
        } else {
            Node<T> actual = head;
            while (actual.next != null) {
                actual = actual.next;
            }
            actual.next = nuevo;
        }
        size++;
    }

    // ------------------------------------------------------------------ popFront
    /** O(1): avanza head. */
    @Override
    public T popFront() {
        if (head == null) throw new ListaVaciaException("popFront sobre lista vacia");
        T value = head.value;
        head = head.next;
        size--;
        return value;
    }

    // ------------------------------------------------------------------- popBack
    /** O(n): hay que ubicar el penultimo nodo para poder cortar el ultimo. */
    @Override
    public T popBack() {
        if (head == null) throw new ListaVaciaException("popBack sobre lista vacia");
        if (head.next == null) { // un solo elemento
            T value = head.value;
            head = null;
            size--;
            return value;
        }
        Node<T> actual = head;
        while (actual.next.next != null) {
            actual = actual.next;
        }
        T value = actual.next.value;
        actual.next = null;
        size--;
        return value;
    }

    // ---------------------------------------------------------------------- find
    /** O(n): busqueda lineal, retorna el nodo o null. */
    @Override
    public Node<T> find(T value) {
        Node<T> actual = head;
        while (actual != null) {
            if (iguales(actual.value, value)) return actual;
            actual = actual.next;
        }
        return null;
    }

    // --------------------------------------------------------------------- erase
    /** O(n): ubica el nodo previo al objetivo y lo desengancha. */
    @Override
    public boolean erase(T value) {
        if (head == null) return false;
        if (iguales(head.value, value)) {
            head = head.next;
            size--;
            return true;
        }
        Node<T> actual = head;
        while (actual.next != null) {
            if (iguales(actual.next.value, value)) {
                actual.next = actual.next.next;
                size--;
                return true;
            }
            actual = actual.next;
        }
        return false;
    }

    // ----------------------------------------------------------------- addBefore
    /** O(n): localiza el nodo previo a la referencia para insertar delante de ella. */
    @Override
    public boolean addBefore(T reference, T value) {
        if (head == null) return false;
        if (iguales(head.value, reference)) {
            pushFront(value);
            return true;
        }
        Node<T> actual = head;
        while (actual.next != null) {
            if (iguales(actual.next.value, reference)) {
                Node<T> nuevo = new Node<>(value);
                nuevo.next = actual.next;
                actual.next = nuevo;
                size++;
                return true;
            }
            actual = actual.next;
        }
        return false;
    }

    // ------------------------------------------------------------------ addAfter
    /** O(n): localiza la referencia y engancha el nuevo nodo despues de ella. */
    @Override
    public boolean addAfter(T reference, T value) {
        Node<T> ref = find(reference);
        if (ref == null) return false;
        Node<T> nuevo = new Node<>(value);
        nuevo.next = ref.next;
        ref.next = nuevo;
        size++;
        return true;
    }

    // --------------------------------------------------------------- utilitarios
    /** O(1). */
    @Override
    public boolean empty() { return head == null; }

    /** O(1). */
    @Override
    public T topFront() {
        if (head == null) throw new ListaVaciaException("topFront sobre lista vacia");
        return head.value;
    }

    /** O(n): sin tail hay que recorrer hasta el final. */
    @Override
    public T topBack() {
        if (head == null) throw new ListaVaciaException("topBack sobre lista vacia");
        Node<T> actual = head;
        while (actual.next != null) actual = actual.next;
        return actual.value;
    }

    /** O(1). */
    @Override
    public int size() { return size; }

    private boolean iguales(T a, T b) {
        return (a == null) ? b == null : a.equals(b);
    }
}
