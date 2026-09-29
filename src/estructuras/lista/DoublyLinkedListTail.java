package estructuras.lista;

/**
 * Lista DOBLEMENTE enlazada CON referencia a la cola (Doubly Linked List - With Tail).
 *
 * Combina lo mejor de las variantes anteriores: cada nodo tiene prev/next y la
 * lista guarda head y tail. Es la implementacion mas versatil: todas las
 * operaciones sobre los extremos son O(1). Solo las operaciones que dependen
 * de encontrar un valor arbitrario (find, erase, addBefore, addAfter) siguen
 * siendo O(n) por la busqueda lineal inherente a una lista enlazada.
 *
 * Analisis de complejidad (n = numero de elementos):
 *   pushFront  O(1)
 *   pushBack   O(1)   <- tail permite enganchar al final directamente.
 *   popFront   O(1)
 *   popBack    O(1)   <- tail + prev permiten desenganchar el ultimo en O(1).
 *   find       O(n)
 *   erase      O(n)   busqueda O(n); desenganche O(1).
 *   addBefore  O(n)   busqueda O(n); insercion O(1).
 *   addAfter   O(n)   busqueda O(n); insercion O(1).
 *   topFront   O(1)
 *   topBack    O(1)
 *   empty/size O(1)
 */
public class DoublyLinkedListTail<T> implements MyList<T> {

    static class Node<T> {
        T value;
        Node<T> prev;
        Node<T> next;
        Node(T value) { this.value = value; }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    // ---------------------------------------------------------------- pushFront
    /** O(1). */
    @Override
    public void pushFront(T value) {
        Node<T> nuevo = new Node<>(value);
        if (head == null) {
            head = tail = nuevo;
        } else {
            nuevo.next = head;
            head.prev = nuevo;
            head = nuevo;
        }
        size++;
    }

    // ----------------------------------------------------------------- pushBack
    /** O(1): con tail se engancha al final directamente. */
    @Override
    public void pushBack(T value) {
        Node<T> nuevo = new Node<>(value);
        if (tail == null) {
            head = tail = nuevo;
        } else {
            nuevo.prev = tail;
            tail.next = nuevo;
            tail = nuevo;
        }
        size++;
    }

    // ------------------------------------------------------------------ popFront
    /** O(1). */
    @Override
    public T popFront() {
        if (head == null) throw new ListaVaciaException("popFront sobre lista vacia");
        T value = head.value;
        head = head.next;
        if (head != null) head.prev = null;
        else tail = null;
        size--;
        return value;
    }

    // ------------------------------------------------------------------- popBack
    /** O(1): tail + prev permiten desenganchar el ultimo sin recorrer. */
    @Override
    public T popBack() {
        if (tail == null) throw new ListaVaciaException("popBack sobre lista vacia");
        T value = tail.value;
        tail = tail.prev;
        if (tail != null) tail.next = null;
        else head = null;
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
    /** O(n) por la busqueda; desenganche O(1). */
    @Override
    public boolean erase(T value) {
        Node<T> objetivo = find(value);
        if (objetivo == null) return false;
        desenganchar(objetivo);
        size--;
        return true;
    }

    // ----------------------------------------------------------------- addBefore
    /** O(n) por la busqueda; insercion O(1). */
    @Override
    public boolean addBefore(T reference, T value) {
        Node<T> ref = find(reference);
        if (ref == null) return false;
        if (ref == head) {
            pushFront(value);
            return true;
        }
        Node<T> nuevo = new Node<>(value);
        nuevo.prev = ref.prev;
        nuevo.next = ref;
        ref.prev.next = nuevo;
        ref.prev = nuevo;
        size++;
        return true;
    }

    // ------------------------------------------------------------------ addAfter
    /** O(n) por la busqueda; insercion O(1). Actualiza tail si aplica. */
    @Override
    public boolean addAfter(T reference, T value) {
        Node<T> ref = find(reference);
        if (ref == null) return false;
        if (ref == tail) {
            pushBack(value);
            return true;
        }
        Node<T> nuevo = new Node<>(value);
        nuevo.prev = ref;
        nuevo.next = ref.next;
        ref.next.prev = nuevo;
        ref.next = nuevo;
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

    /** O(1). */
    @Override
    public T topBack() {
        if (tail == null) throw new ListaVaciaException("topBack sobre lista vacia");
        return tail.value;
    }

    @Override
    public int size() { return size; }

    /** Desengancha un nodo cualquiera en O(1), manteniendo head y tail coherentes. */
    private void desenganchar(Node<T> nodo) {
        if (nodo.prev != null) nodo.prev.next = nodo.next;
        else head = nodo.next;
        if (nodo.next != null) nodo.next.prev = nodo.prev;
        else tail = nodo.prev;
    }

    private boolean iguales(T a, T b) {
        return (a == null) ? b == null : a.equals(b);
    }
}
