package estructuras.pilacola;

/**
 * Cola (FIFO) implementada sobre un ARREGLO DINAMICO CIRCULAR.
 *
 * El buffer circular es la estructura natural para una cola sobre arreglo:
 * permite encolar por un extremo y desencolar por el otro en O(1), sin
 * necesidad de desplazar todos los elementos como ocurriria en un arreglo
 * lineal simple donde dequeue seria O(n).
 *
 * Se mantienen:
 *   - head : indice del primer elemento (frente de la cola).
 *   - count: numero de elementos.
 * La posicion de insercion (cola) se calcula como (head + count) % capacidad.
 * Cuando head+count supera el final del arreglo, el indice "da la vuelta"
 * (modulo), reutilizando las celdas liberadas por dequeue.
 *
 * ---------------------------------------------------------------------------
 * ESTRATEGIA DE CRECIMIENTO: duplicar la capacidad (x2).
 * Igual que en MyStackCircular: costo amortizado O(1) por enqueue, total de
 * copias O(n) al insertar n elementos. Se descarta el crecimiento aditivo por
 * degradar el amortizado a O(n).
 * ---------------------------------------------------------------------------
 *
 * Analisis de complejidad:
 *   enqueue  O(1) amortizado  (O(n) puntual cuando toca duplicar)
 *   dequeue  O(1)   <- clave del buffer circular: solo se avanza head.
 *   front    O(1)
 *   isEmpty  O(1)
 *   size     O(1)
 *   delete   O(n)   (busqueda lineal + compactacion)
 */
public class MyQueueCircular<T> implements MyQueue<T> {

    private static final int CAPACIDAD_INICIAL = 8;

    private Object[] datos;
    private int head;   // indice del frente
    private int count;  // numero de elementos

    public MyQueueCircular() {
        this(CAPACIDAD_INICIAL);
    }

    public MyQueueCircular(int capacidadInicial) {
        datos = new Object[Math.max(1, capacidadInicial)];
        head = 0;
        count = 0;
    }

    // ----------------------------------------------------------------- enqueue
    /** O(1) amortizado. Inserta al final logico usando aritmetica modular. */
    @Override
    public void enqueue(T x) {
        if (count == datos.length) redimensionar(datos.length * 2);
        int pos = (head + count) % datos.length;
        datos[pos] = x;
        count++;
    }

    // ----------------------------------------------------------------- dequeue
    /** O(1). Solo avanza head; no desplaza elementos (ventaja del buffer circular). */
    @Override
    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (count == 0) throw new EstructuraVaciaException("dequeue sobre cola vacia");
        T x = (T) datos[head];
        datos[head] = null; // ayuda al GC
        head = (head + 1) % datos.length;
        count--;
        return x;
    }

    // ------------------------------------------------------------------- front
    /** O(1). */
    @Override
    @SuppressWarnings("unchecked")
    public T front() {
        if (count == 0) throw new EstructuraVaciaException("front sobre cola vacia");
        return (T) datos[head];
    }

    // ----------------------------------------------------------------- isEmpty
    @Override
    public boolean isEmpty() { return count == 0; }

    // -------------------------------------------------------------------- size
    @Override
    public int size() { return count; }

    // ------------------------------------------------------------------ delete
    /**
     * O(n). Busca la primera aparicion de n desde el frente y la elimina,
     * compactando los elementos posteriores una posicion hacia el frente.
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean delete(T n) {
        for (int i = 0; i < count; i++) {
            int pos = (head + i) % datos.length;
            if (iguales((T) datos[pos], n)) {
                // Desplazar los elementos posteriores (logicos i+1..count-1) hacia atras.
                for (int j = i; j < count - 1; j++) {
                    int destino = (head + j) % datos.length;
                    int origen = (head + j + 1) % datos.length;
                    datos[destino] = datos[origen];
                }
                int ultimo = (head + count - 1) % datos.length;
                datos[ultimo] = null;
                count--;
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------------- redimensionar
    /** Copia el contenido logico a un nuevo arreglo, dejando head en 0. */
    private void redimensionar(int nuevaCapacidad) {
        Object[] nuevo = new Object[nuevaCapacidad];
        for (int i = 0; i < count; i++) {
            nuevo[i] = datos[(head + i) % datos.length];
        }
        datos = nuevo;
        head = 0;
    }

    private boolean iguales(T a, T b) {
        return (a == null) ? b == null : a.equals(b);
    }
}
