package estructuras.lista;

/**
 * Lista SENCILLAMENTE enlazada CON referencia a la cola (Singly Linked List - With Tail).
 *
 * Ademas de head se mantiene un puntero tail al ultimo nodo. Esto convierte
 * pushBack en O(1). Sin embargo, como los nodos solo apuntan hacia adelante,
 * popBack sigue siendo O(n): para borrar el ultimo nodo hay que localizar el
 * penultimo recorriendo la lista, ya que no existe puntero "prev".
 *
 * Analisis de complejidad (n = numero de elementos):
 *   pushFront  O(1)
 *   pushBack   O(1)   <- MEJORA respecto a la version sin cola gracias a tail.
 *   popFront   O(1)
 *   popBack    O(n)   <- sigue siendo lineal: no hay puntero al anterior.
 *   find       O(n)
 *   erase      O(n)
 *   addBefore  O(n)
 *   addAfter   O(n)   busqueda O(n); si la referencia es tail, el reenganche
 *                     actualiza tail en O(1).
 *   topFront   O(1)
 *   topBack    O(1)   <- MEJORA: acceso directo a tail.
 *   empty/size O(1)
 */
public class SinglyLinkedListTail<T> implements MyList<T> {

    static class Node<T> {
        T value;
        Node<T> next;
        Node(T value) { this.value = value; }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    // ---------------------------------------------------------------- pushFront
    /** O(1). Si la lista estaba vacia, el nuevo nodo tambien es tail. */
    @Override
    public void pushFront(T value) {
        Node<T> nuevo = new Node<>(value);
        if (head == null) {
            head = tail = nuevo;
        } else {
            nuevo.next = head;
            head = nuevo;
        }
        size++;
    }

    // ----------------------------------------------------------------- pushBack
    /** O(1): con tail el nuevo nodo se engancha directamente al final. */
    @Override
    public void pushBack(T value) {
        Node<T> nuevo = new Node<>(value);
        if (head == null) {
            head = tail = nuevo;
        } else {
            tail.next = nuevo;
            tail = nuevo;
        }
        size++;
    }

    // ------------------------------------------------------------------ popFront
    /** O(1). Actualiza tail a null si la lista queda vacia. */
    @Override
    public T popFront() {
        if (head == null) throw new ListaVaciaException("popFront sobre lista vacia");
        T value = head.value;
        head = head.next;
        if (head == null) tail = null;
        size--;
        return value;
    }

    // ------------------------------------------------------------------- popBack
    /** O(n): aunque tenemos tail, hay que recorrer para hallar el penultimo. */
    @Override
    public T popBack() {
        if (head == null) throw new ListaVaciaException("popBack sobre lista vacia");
        if (head.next == null) {
            T value = head.value;
            head = tail = null;
            size--;
            return value;
        }
        Node<T> actual = head;
        while (actual.next.next != null) {
            actual = actual.next;
        }
        T value = actual.next.value;
        actual.next = null;
        tail = actual;
        size--;
        return value;
    }

    // ---------------------------------------------------------------------- find
    /** O(n). */
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
    /** O(n). Mantiene tail coherente si se borra el ultimo nodo. */
    @Override
    public boolean erase(T value) {
        if (head == null) return false;
        if (iguales(head.value, value)) {
            head = head.next;
            if (head == null) tail = null;
            size--;
            return true;
        }
        Node<T> actual = head;
        while (actual.next != null) {
            if (iguales(actual.next.value, value)) {
                if (actual.next == tail) tail = actual;
                actual.next = actual.next.next;
                size--;
                return true;
            }
            actual = actual.next;
        }
        return false;
    }

    // ----------------------------------------------------------------- addBefore
    /** O(n). */
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
    /** O(n) por la busqueda; el reenganche es O(1) y actualiza tail si aplica. */
    @Override
    public boolean addAfter(T reference, T value) {
        Node<T> ref = find(reference);
        if (ref == null) return false;
        Node<T> nuevo = new Node<>(value);
        nuevo.next = ref.next;
        ref.next = nuevo;
        if (ref == tail) tail = nuevo;
        size++;
        return true;
    }

    // --------------------------------------------------------------- utilitarios
    @Override
    public boolean empty() { return head == null; }

    /** O(1). */
    @Override
    public T topFront() {
        if (head == null) throw new ListaVaciaException("topFront sobre lista vacia");
        return head.value;
    }

    /** O(1): acceso directo a tail. */
    @Override
    public T topBack() {
        if (tail == null) throw new ListaVaciaException("topBack sobre lista vacia");
        return tail.value;
    }

    @Override
    public int size() { return size; }

    private boolean iguales(T a, T b) {
        return (a == null) ? b == null : a.equals(b);
    }
}
