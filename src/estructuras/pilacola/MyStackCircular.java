package estructuras.pilacola;

/**
 * Pila (LIFO) implementada sobre un ARREGLO DINAMICO CIRCULAR.
 *
 * Se usa un buffer circular con indices head (frente logico) y una cuenta de
 * elementos. Aunque para una pila pura bastaria con un arreglo simple y un
 * indice de tope, se implementa de forma circular para mantener una base de
 * datos comun y directamente comparable con MyQueue (ambas comparten la misma
 * mecanica de buffer circular y de redimensionamiento).
 *
 * ---------------------------------------------------------------------------
 * ESTRATEGIA DE CRECIMIENTO DEL ARREGLO DINAMICO
 * ---------------------------------------------------------------------------
 * Cuando el buffer se llena, la capacidad se DUPLICA (crecimiento geometrico
 * x2). Esta es la eleccion clasica porque reparte el costo de las copias:
 *
 *   - Duplicar hace que las redimensiones sean cada vez mas espaciadas. Al
 *     insertar n elementos, el total de copias acumuladas es a lo sumo
 *     n + n/2 + n/4 + ... < 2n, es decir O(n) copias en total.
 *   - Por lo tanto el costo AMORTIZADO de push es O(1), aunque una operacion
 *     individual que dispare el resize cueste O(n).
 *
 * Se descarta el crecimiento aditivo (ej. +k fijo) porque provocaria O(n)
 * redimensiones y un costo amortizado O(n) por insercion (O(n^2) total).
 *
 * Analisis de complejidad:
 *   push     O(1) amortizado  (O(n) puntual cuando toca duplicar)
 *   pop      O(1)
 *   peek     O(1)
 *   isEmpty  O(1)
 *   size     O(1)
 *   delete   O(n)  (busqueda lineal + compactacion de los elementos restantes)
 */
public class MyStackCircular<T> implements MyStack<T> {

    private static final int CAPACIDAD_INICIAL = 8;

    private Object[] datos;
    private int head;   // indice del elemento "fondo" de la pila (el mas antiguo)
    private int count;  // numero de elementos

    public MyStackCircular() {
        this(CAPACIDAD_INICIAL);
    }

    public MyStackCircular(int capacidadInicial) {
        datos = new Object[Math.max(1, capacidadInicial)];
        head = 0;
        count = 0;
    }

    // -------------------------------------------------------------------- push
    /**
     * O(1) amortizado. La cima es la posicion logica count-1. Se inserta justo
     * despues del ultimo elemento en el buffer circular.
     */
    @Override
    public void push(T x) {
        if (count == datos.length) redimensionar(datos.length * 2);
        int pos = (head + count) % datos.length;
        datos[pos] = x;
        count++;
    }

    // --------------------------------------------------------------------- pop
    /** O(1). Retira el ultimo elemento insertado (la cima). */
    @Override
    @SuppressWarnings("unchecked")
    public T pop() {
        if (count == 0) throw new EstructuraVaciaException("pop sobre pila vacia");
        int pos = (head + count - 1) % datos.length;
        T x = (T) datos[pos];
        datos[pos] = null; // ayuda al GC
        count--;
        return x;
    }

    // -------------------------------------------------------------------- peek
    /** O(1). */
    @Override
    @SuppressWarnings("unchecked")
    public T peek() {
        if (count == 0) throw new EstructuraVaciaException("peek sobre pila vacia");
        int pos = (head + count - 1) % datos.length;
        return (T) datos[pos];
    }

    // ----------------------------------------------------------------- isEmpty
    @Override
    public boolean isEmpty() { return count == 0; }

    // -------------------------------------------------------------------- size
    @Override
    public int size() { return count; }

    // ------------------------------------------------------------------ delete
    /**
     * O(n). Busca la primera aparicion de n desde la cima hacia el fondo y la
     * elimina, desplazando los elementos que estaban por encima para no dejar
     * huecos.
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean delete(T n) {
        // Recorremos desde la cima (indice logico count-1) hacia el fondo (0).
        for (int i = count - 1; i >= 0; i--) {
            int pos = (head + i) % datos.length;
            if (iguales((T) datos[pos], n)) {
                // Desplazar los elementos por encima (indices logicos i+1..count-1)
                // una posicion hacia abajo.
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
    /** Copia el contenido logico a un nuevo arreglo, "desenrollando" el circulo. */
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
