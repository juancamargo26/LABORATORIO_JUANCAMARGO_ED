package estructuras.lista;

/**
 * Lista DOBLEMENTE enlazada SIN referencia a la cola (Doubly Linked List - No Tail).
 *
 * Cada nodo mantiene punteros prev y next, pero solo se guarda una referencia
 * al primer nodo (head). Tener prev simplifica addBefore y erase (una vez
 * hallado el nodo, el reenganche usa el puntero al anterior sin recorridos
 * extra). Sin embargo, al no tener tail, todo lo que involucre el final de la
 * lista (pushBack, popBack, topBack) exige recorrer los n nodos.
 *
 * Analisis de complejidad (n = numero de elementos):
 *   pushFront  O(1)
 *   pushBack   O(n)   <- sin tail hay que llegar al final.
 *   popFront   O(1)
 *   popBack    O(n)   <- sin tail hay que llegar al final (aunque prev
 *                        haria el desenganche O(1) una vez alli).
 *   find       O(n)
 *   erase      O(n)   busqueda O(n); desenganche O(1) gracias a prev/next.
 *   addBefore  O(n)   busqueda O(n); insercion O(1) gracias a prev.
 *   addAfter   O(n)   busqueda O(n); insercion O(1).
 *   topFront   O(1)
 *   topBack    O(n)
 *   empty/size O(1)
 */
public class DoublyLinkedList<T> implements MyList<T> {

    /** Nodo doblemente enlazado: dato + referencias al anterior y al siguiente. */
    static class Node<T> {
        T value;
        Node<T> prev;
        Node<T> next;
        Node(T value) { this.value = value; }
    }

    private Node<T> head;
    private int size;

    // ---------------------------------------------------------------- pushFront
    /** O(1). */
    @Override
    public void pushFront(T value) {
        Node<T> nuevo = new Node<>(value);
        if (head != null) {
            nuevo.next = head;
            head.prev = nuevo;
        }
        head = nuevo;
        size++;
    }

    // ----------------------------------------------------------------- pushBack
    /** O(n): sin tail se recorre hasta el ultimo nodo. */
    @Override
    public void pushBack(T value) {
        Node<T> nuevo = new Node<>(value);
        if (head == null) {
            head = nuevo;
        } else {
            Node<T> actual = head;
            while (actual.next != null) actual = actual.next;
            actual.next = nuevo;
            nuevo.prev = actual;
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
        size--;
        return value;
    }

    // ------------------------------------------------------------------- popBack
    /** O(n): sin tail hay que recorrer hasta el ultimo nodo. */
    @Override
    public T popBack() {
        if (head == null) throw new ListaVaciaException("popBack sobre lista vacia");
        if (head.next == null) {
            T value = head.value;
            head = null;
            size--;
            return value;
        }
        Node<T> actual = head;
        while (actual.next != null) actual = actual.next;
        actual.prev.next = null;
        size--;
        return actual.value;
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
    /** O(n) por la busqueda; el desenganche es O(1) usando prev y next. */
    @Override
    public boolean erase(T value) {
        Node<T> objetivo = find(value);
        if (objetivo == null) return false;
        desenganchar(objetivo);
        size--;
        return true;
    }

    // ----------------------------------------------------------------- addBefore
    /** O(n) por la busqueda; la insercion es O(1) gracias a prev. */
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
    /** O(n) por la busqueda; la insercion es O(1). */
    @Override
    public boolean addAfter(T reference, T value) {
        Node<T> ref = find(reference);
        if (ref == null) return false;
        Node<T> nuevo = new Node<>(value);
        nuevo.prev = ref;
        nuevo.next = ref.next;
        if (ref.next != null) ref.next.prev = nuevo;
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

    /** O(n): sin tail hay que recorrer hasta el final. */
    @Override
    public T topBack() {
        if (head == null) throw new ListaVaciaException("topBack sobre lista vacia");
        Node<T> actual = head;
        while (actual.next != null) actual = actual.next;
        return actual.value;
    }

    @Override
    public int size() { return size; }

    /** Desengancha un nodo cualquiera en O(1) usando sus punteros prev/next. */
    private void desenganchar(Node<T> nodo) {
        if (nodo.prev != null) nodo.prev.next = nodo.next;
        else head = nodo.next; // era el head
        if (nodo.next != null) nodo.next.prev = nodo.prev;
    }

    private boolean iguales(T a, T b) {
        return (a == null) ? b == null : a.equals(b);
    }
}
