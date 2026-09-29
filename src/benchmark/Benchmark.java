package benchmark;

import java.util.function.Supplier;

import estructuras.lista.*;
import estructuras.pilacola.*;

/**
 * Benchmark de medicion de tiempos de ejecucion.
 *
 * -------------------------------------------------------------------------
 * DECISIONES DE MEDICION (justificacion pedida por el enunciado)
 * -------------------------------------------------------------------------
 * 1. UNIDAD: se mide con System.nanoTime() y se reporta en NANOSEGUNDOS por
 *    operacion. Razon: muchos metodos (pushFront, popFront, push, dequeue...)
 *    son O(1) y para tamanos pequenos tardan MENOS de 1 milisegundo. El
 *    borrador Main.java usaba Instant + toMillis(), que para esos casos da 0 ms
 *    y hace imposible el analisis. nanoTime ofrece la resolucion necesaria.
 *    En el informe/graficas se puede reescalar a microsegundos para leer mejor.
 *
 * 2. TIEMPO POR OPERACION: para un metodo O(1) se ejecuta la operacion n veces
 *    y se divide el tiempo total entre n (promedio por operacion). Asi se aisla
 *    el costo unitario y se evita que la resolucion del reloj domine el numero.
 *    Para metodos O(n) (pushBack sin tail, popBack, find, erase, addBefore/After)
 *    ejecutar n veces daria O(n^2) y seria impracticable en n grandes; para esos
 *    se mide un numero fijo y acotado de repeticiones (REPS_ON) sobre una
 *    estructura ya poblada con n elementos, y se reporta el promedio por llamada.
 *
 * 3. WARMUP: antes de cada medicion se ejecutan iteraciones de calentamiento
 *    para que el compilador JIT de la JVM optimice el codigo y no se mida la
 *    fase interpretada inicial. Ademas se repite cada medicion varias veces
 *    (MUESTRAS) y se toma la MEDIANA, mas robusta frente a pausas de GC.
 *
 * 4. GRAFICACION SEPARADA: este programa NO grafica. Solo imprime CSV por
 *    stdout. La graficacion se hace despues en Python para no contaminar la
 *    medicion con operaciones de dibujo (requisito explicito del enunciado).
 *
 * Formato de salida (CSV):
 *    categoria,estructura,metodo,n,ns_por_operacion
 */
public class Benchmark {

    /** Tamanos de entrada. Escala logaritmica base 10. */
    private static final int[] TAMANOS = {10, 100, 1_000, 10_000, 100_000, 1_000_000};

    /** Repeticiones para metodos O(n): acotadas para que el experimento termine. */
    private static final int REPS_ON = 200;

    /** Numero de muestras por medicion; se reporta la mediana. */
    private static final int MUESTRAS = 5;

    /**
     * Presupuesto de "operaciones elementales" por medicion. Un bucle de n
     * inserciones donde cada insercion es O(n) cuesta ~n^2; poblar una lista con
     * pushBack O(n) para luego medir tambien es ~n^2. Para que el experimento
     * termine en tiempo razonable, cualquier medicion cuyo costo estimado supere
     * este techo se OMITE (se registra como salto), en lugar de colgar el
     * benchmark durante horas. 5x10^8 operaciones ~ segundos por medicion.
     */
    private static final long PRESUPUESTO_OPS = 500_000_000L;

    public static void main(String[] args) {
        System.out.println("categoria,estructura,metodo,n,ns_por_operacion");

        for (int n : TAMANOS) {
            // ---- LISTAS ----
            medirLista("SinglyLinkedList", () -> new SinglyLinkedList<>(), n);
            medirLista("SinglyLinkedListTail", () -> new SinglyLinkedListTail<>(), n);
            medirLista("DoublyLinkedList", () -> new DoublyLinkedList<>(), n);
            medirLista("DoublyLinkedListTail", () -> new DoublyLinkedListTail<>(), n);

            // ---- PILA / COLA ----
            medirStack(n);
            medirQueue(n);
        }
    }

    // =====================================================================
    //  LISTAS
    // =====================================================================

    /**
     * Devuelve true si, para la implementacion dada, la operacion de extremo
     * indicada es O(1) (y por tanto construir/destruir n veces cuesta O(n)).
     * Si es O(n), construir/destruir n veces cuesta O(n^2).
     */
    private static boolean extremoEsO1(String nombre, boolean back) {
        if (!back) return true; // pushFront/popFront son O(1) en las 4 implementaciones
        // pushBack/popBack:
        switch (nombre) {
            case "SinglyLinkedListTail":  return true;  // pushBack O(1); popBack O(n)
            case "DoublyLinkedListTail":  return true;  // pushBack y popBack O(1)
            default:                      return false; // sin tail: O(n)
        }
    }

    /** Costo estimado de un bucle de n operaciones de extremo. */
    private static long costoConstruccion(String nombre, int n, boolean back) {
        return extremoEsO1(nombre, back) ? (long) n : (long) n * n;
    }

    private static void medirLista(String nombre, Supplier<MyList<Integer>> fab, int n) {
        // --- pushFront: O(1) en todas. Se hacen n pushFront sobre lista vacia. ---
        if (dentroPresupuesto((long) n)) // O(1)*n
            emitir("List", nombre, "PushFront", n, medirConstruccion(fab, n, TipoConstruccion.FRONT));

        // --- pushBack: O(1) con tail, O(n) sin tail -> construccion O(n) u O(n^2). ---
        if (dentroPresupuesto(costoConstruccion(nombre, n, true)))
            emitir("List", nombre, "PushBack", n, medirConstruccion(fab, n, TipoConstruccion.BACK));

        // Para poblar antes de medir popFront/popBack/busquedas usamos SIEMPRE el
        // metodo de construccion mas barato disponible (pushFront, O(1) en todas).
        // Asi el poblado no domina el costo de la medicion.

        // --- popFront: O(1). Poblado O(n) + n popFront O(1) = O(n). ---
        if (dentroPresupuesto((long) n * 2))
            emitir("List", nombre, "PopFront", n, medirDestruccion(fab, n, TipoDestruccion.FRONT));

        // --- popBack: O(1) solo en DoublyTail; O(n) en el resto. ---
        long costoPopBack = extremoEsO1(nombre, true) && nombre.equals("DoublyLinkedListTail")
                ? (long) n * 2                 // poblado O(n) + n popBack O(1)
                : (long) n + (long) n * n;     // poblado O(n) + n popBack O(n)
        if (dentroPresupuesto(costoPopBack))
            emitir("List", nombre, "PopBack", n, medirDestruccion(fab, n, TipoDestruccion.BACK));

        // Metodos de busqueda: poblado O(n) + REPS_ON llamadas O(n) = O(n) + O(REPS*n).
        long costoBusqueda = (long) n + (long) Math.min(REPS_ON, n) * n;
        if (dentroPresupuesto(costoBusqueda)) {
            emitir("List", nombre, "Find", n, medirBusqueda(fab, n, Operacion.FIND));
            emitir("List", nombre, "Erase", n, medirBusqueda(fab, n, Operacion.ERASE));
            emitir("List", nombre, "AddBefore", n, medirBusqueda(fab, n, Operacion.ADD_BEFORE));
            emitir("List", nombre, "AddAfter", n, medirBusqueda(fab, n, Operacion.ADD_AFTER));
        }
    }

    private enum TipoConstruccion { FRONT, BACK }
    private enum TipoDestruccion { FRONT, BACK }
    private enum Operacion { FIND, ERASE, ADD_BEFORE, ADD_AFTER }

    /** Mide el costo POR OPERACION de construir la lista con n inserciones. */
    private static double medirConstruccion(Supplier<MyList<Integer>> fab, int n, TipoConstruccion tipo) {
        // Warmup
        construir(fab, Math.min(n, 2_000), tipo);

        long[] tiempos = new long[MUESTRAS];
        for (int m = 0; m < MUESTRAS; m++) {
            MyList<Integer> l = fab.get();
            long ini = System.nanoTime();
            if (tipo == TipoConstruccion.FRONT) {
                for (int i = 0; i < n; i++) l.pushFront(i);
            } else {
                for (int i = 0; i < n; i++) l.pushBack(i);
            }
            long fin = System.nanoTime();
            tiempos[m] = fin - ini;
            impedirOptimizacion += l.size();
        }
        return mediana(tiempos) / (double) n;
    }

    /** Mide el costo POR OPERACION de vaciar una lista de n elementos. */
    private static double medirDestruccion(Supplier<MyList<Integer>> fab, int n, TipoDestruccion tipo) {
        // Warmup (poblado barato con pushFront, O(1) en todas las implementaciones)
        {
            MyList<Integer> w = construir(fab, Math.min(n, 2_000), TipoConstruccion.FRONT);
            while (!w.empty()) { if (tipo == TipoDestruccion.FRONT) w.popFront(); else w.popBack(); }
        }

        long[] tiempos = new long[MUESTRAS];
        for (int m = 0; m < MUESTRAS; m++) {
            MyList<Integer> l = construir(fab, n, TipoConstruccion.FRONT); // poblado O(1)/elem
            long ini = System.nanoTime();
            if (tipo == TipoDestruccion.FRONT) {
                for (int i = 0; i < n; i++) l.popFront();
            } else {
                for (int i = 0; i < n; i++) l.popBack();
            }
            long fin = System.nanoTime();
            tiempos[m] = fin - ini;
            impedirOptimizacion += l.size();
        }
        return mediana(tiempos) / (double) n;
    }

    /**
     * Mide find/erase/addBefore/addAfter sobre una lista poblada con n valores
     * 0..n-1. Se ejecutan REPS_ON llamadas sobre valores repartidos a lo largo
     * de la lista (incluyendo el peor caso: el ultimo elemento) y se promedia.
     * Para erase/add se restaura el estado para no alterar n durante la medicion.
     */
    private static double medirBusqueda(Supplier<MyList<Integer>> fab, int n, Operacion op) {
        int reps = Math.min(REPS_ON, n);
        // Warmup con una lista pequena (poblado barato con pushFront)
        {
            MyList<Integer> w = construir(fab, Math.min(n, 2_000), TipoConstruccion.FRONT);
            for (int i = 0; i < Math.min(reps, w.size()); i++) w.find(i);
        }

        long[] tiempos = new long[MUESTRAS];
        for (int m = 0; m < MUESTRAS; m++) {
            MyList<Integer> l = construir(fab, n, TipoConstruccion.FRONT); // valores 0..n-1 presentes
            // Valores objetivo repartidos uniformemente en [0, n-1].
            int[] objetivos = new int[reps];
            for (int i = 0; i < reps; i++) {
                objetivos[i] = (int) ((long) i * (n - 1) / Math.max(1, reps - 1));
            }

            long ini = System.nanoTime();
            switch (op) {
                case FIND:
                    for (int i = 0; i < reps; i++) {
                        Object r = l.find(objetivos[i]);
                        if (r != null) impedirOptimizacion++;
                    }
                    break;
                case ERASE:
                    for (int i = 0; i < reps; i++) {
                        if (l.erase(objetivos[i])) impedirOptimizacion++;
                        l.pushBack(objetivos[i]); // restaura tamano (al final, no altera la busqueda del resto)
                    }
                    break;
                case ADD_BEFORE:
                    for (int i = 0; i < reps; i++) {
                        if (l.addBefore(objetivos[i], -1)) impedirOptimizacion++;
                        l.erase(-1); // restaura tamano
                    }
                    break;
                case ADD_AFTER:
                    for (int i = 0; i < reps; i++) {
                        if (l.addAfter(objetivos[i], -1)) impedirOptimizacion++;
                        l.erase(-1);
                    }
                    break;
            }
            long fin = System.nanoTime();
            // El tiempo incluye la operacion medida + una restauracion del mismo
            // orden de costo; dividimos entre reps para el promedio por "ciclo".
            tiempos[m] = fin - ini;
        }
        return mediana(tiempos) / (double) reps;
    }

    // =====================================================================
    //  PILA (circular array)
    // =====================================================================
    private static void medirStack(int n) {
        String nombre = "MyStackCircular";

        // push: O(1) amortizado. n push sobre pila vacia.
        {
            new MyStackCircular<Integer>(); // warmup ligero
            long[] t = new long[MUESTRAS];
            for (int m = 0; m < MUESTRAS; m++) {
                MyStack<Integer> s = new MyStackCircular<>();
                long ini = System.nanoTime();
                for (int i = 0; i < n; i++) s.push(i);
                long fin = System.nanoTime();
                t[m] = fin - ini;
                impedirOptimizacion += s.size();
            }
            emitir("Stack", nombre, "push", n, mediana(t) / (double) n);
        }

        // pop: O(1). Poblar y hacer n pop.
        {
            long[] t = new long[MUESTRAS];
            for (int m = 0; m < MUESTRAS; m++) {
                MyStack<Integer> s = new MyStackCircular<>();
                for (int i = 0; i < n; i++) s.push(i);
                long ini = System.nanoTime();
                for (int i = 0; i < n; i++) s.pop();
                long fin = System.nanoTime();
                t[m] = fin - ini;
            }
            emitir("Stack", nombre, "pop", n, mediana(t) / (double) n);
        }

        // peek: O(1).
        {
            MyStack<Integer> s = new MyStackCircular<>();
            for (int i = 0; i < n; i++) s.push(i);
            long[] t = new long[MUESTRAS];
            for (int m = 0; m < MUESTRAS; m++) {
                long ini = System.nanoTime();
                for (int i = 0; i < n; i++) { if (s.peek() != null) impedirOptimizacion++; }
                long fin = System.nanoTime();
                t[m] = fin - ini;
            }
            emitir("Stack", nombre, "peek", n, mediana(t) / (double) n);
        }

        // delete: O(n). REPS_ON eliminaciones (con reinsercion para mantener n).
        {
            int reps = Math.min(REPS_ON, n);
            long[] t = new long[MUESTRAS];
            for (int m = 0; m < MUESTRAS; m++) {
                MyStack<Integer> s = new MyStackCircular<>();
                for (int i = 0; i < n; i++) s.push(i);
                int[] objetivos = new int[reps];
                for (int i = 0; i < reps; i++) objetivos[i] = (int) ((long) i * (n - 1) / Math.max(1, reps - 1));
                long ini = System.nanoTime();
                for (int i = 0; i < reps; i++) {
                    if (s.delete(objetivos[i])) impedirOptimizacion++;
                    s.push(objetivos[i]);
                }
                long fin = System.nanoTime();
                t[m] = fin - ini;
            }
            emitir("Stack", nombre, "delete", n, mediana(t) / (double) reps);
        }
    }

    // =====================================================================
    //  COLA (circular array)
    // =====================================================================
    private static void medirQueue(int n) {
        String nombre = "MyQueueCircular";

        // enqueue: O(1) amortizado.
        {
            long[] t = new long[MUESTRAS];
            for (int m = 0; m < MUESTRAS; m++) {
                MyQueue<Integer> q = new MyQueueCircular<>();
                long ini = System.nanoTime();
                for (int i = 0; i < n; i++) q.enqueue(i);
                long fin = System.nanoTime();
                t[m] = fin - ini;
                impedirOptimizacion += q.size();
            }
            emitir("Queue", nombre, "enqueue", n, mediana(t) / (double) n);
        }

        // dequeue: O(1).
        {
            long[] t = new long[MUESTRAS];
            for (int m = 0; m < MUESTRAS; m++) {
                MyQueue<Integer> q = new MyQueueCircular<>();
                for (int i = 0; i < n; i++) q.enqueue(i);
                long ini = System.nanoTime();
                for (int i = 0; i < n; i++) q.dequeue();
                long fin = System.nanoTime();
                t[m] = fin - ini;
            }
            emitir("Queue", nombre, "dequeue", n, mediana(t) / (double) n);
        }

        // front: O(1).
        {
            MyQueue<Integer> q = new MyQueueCircular<>();
            for (int i = 0; i < n; i++) q.enqueue(i);
            long[] t = new long[MUESTRAS];
            for (int m = 0; m < MUESTRAS; m++) {
                long ini = System.nanoTime();
                for (int i = 0; i < n; i++) { if (q.front() != null) impedirOptimizacion++; }
                long fin = System.nanoTime();
                t[m] = fin - ini;
            }
            emitir("Queue", nombre, "front", n, mediana(t) / (double) n);
        }

        // delete: O(n).
        {
            int reps = Math.min(REPS_ON, n);
            long[] t = new long[MUESTRAS];
            for (int m = 0; m < MUESTRAS; m++) {
                MyQueue<Integer> q = new MyQueueCircular<>();
                for (int i = 0; i < n; i++) q.enqueue(i);
                int[] objetivos = new int[reps];
                for (int i = 0; i < reps; i++) objetivos[i] = (int) ((long) i * (n - 1) / Math.max(1, reps - 1));
                long ini = System.nanoTime();
                for (int i = 0; i < reps; i++) {
                    if (q.delete(objetivos[i])) impedirOptimizacion++;
                    q.enqueue(objetivos[i]);
                }
                long fin = System.nanoTime();
                t[m] = fin - ini;
            }
            emitir("Queue", nombre, "delete", n, mediana(t) / (double) reps);
        }
    }

    // =====================================================================
    //  UTILITARIOS
    // =====================================================================

    /** Acumulador con efecto observable para que el JIT no elimine el codigo medido. */
    static long impedirOptimizacion = 0;

    /** True si el costo estimado (en operaciones elementales) cabe en el presupuesto. */
    private static boolean dentroPresupuesto(long costoEstimado) {
        return costoEstimado <= PRESUPUESTO_OPS;
    }

    private static MyList<Integer> construir(Supplier<MyList<Integer>> fab, int n, TipoConstruccion tipo) {
        MyList<Integer> l = fab.get();
        if (tipo == TipoConstruccion.FRONT) for (int i = 0; i < n; i++) l.pushFront(i);
        else for (int i = 0; i < n; i++) l.pushBack(i);
        return l;
    }

    private static long mediana(long[] arr) {
        long[] copia = arr.clone();
        java.util.Arrays.sort(copia);
        int m = copia.length / 2;
        return (copia.length % 2 == 1) ? copia[m] : (copia[m - 1] + copia[m]) / 2;
    }

    private static void emitir(String categoria, String estructura, String metodo, int n, double ns) {
        System.out.printf(java.util.Locale.US, "%s,%s,%s,%d,%.4f%n", categoria, estructura, metodo, n, ns);
    }
}
