package benchmark;

import estructuras.lista.*;
import estructuras.pilacola.*;

/**
 * Pruebas de correctitud de todas las estructuras.
 *
 * NO mide tiempos: solo verifica que cada metodo produce el resultado esperado
 * antes de correr el benchmark. Medir tiempos sobre estructuras con errores
 * daria conclusiones invalidas, asi que esta verificacion es un requisito
 * previo. Si alguna asercion falla, el programa termina con codigo de error.
 */
public class Correctitud {

    private static int pruebas = 0;
    private static int fallos = 0;

    private static void check(boolean cond, String descripcion) {
        pruebas++;
        if (!cond) {
            fallos++;
            System.out.println("  [FALLO] " + descripcion);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Pruebas de correctitud ===");

        probarLista("SinglyLinkedList", new SinglyLinkedList<>());
        probarLista("SinglyLinkedListTail", new SinglyLinkedListTail<>());
        probarLista("DoublyLinkedList", new DoublyLinkedList<>());
        probarLista("DoublyLinkedListTail", new DoublyLinkedListTail<>());

        probarStack();
        probarQueue();

        System.out.println("------------------------------");
        System.out.printf("Total: %d pruebas, %d fallos%n", pruebas, fallos);
        if (fallos > 0) {
            System.exit(1);
        }
        System.out.println("TODAS LAS PRUEBAS PASARON");
    }

    private static void probarLista(String nombre, MyList<Integer> l) {
        System.out.println("-- " + nombre);

        check(l.empty(), nombre + ": lista nueva vacia");
        check(l.size() == 0, nombre + ": size inicial 0");

        // pushFront: 3,2,1  -> lista [3,2,1]? pushFront(1),pushFront(2),pushFront(3) => 3,2,1
        l.pushFront(1);
        l.pushFront(2);
        l.pushFront(3);
        check(l.size() == 3, nombre + ": size 3 tras 3 pushFront");
        check(l.topFront() == 3, nombre + ": topFront==3");
        check(l.topBack() == 1, nombre + ": topBack==1");

        // pushBack: [3,2,1,10,20]
        l.pushBack(10);
        l.pushBack(20);
        check(l.topBack() == 20, nombre + ": topBack==20 tras pushBack");
        check(l.size() == 5, nombre + ": size 5");

        // find
        check(l.find(10) != null, nombre + ": find(10) encuentra");
        check(l.find(999) == null, nombre + ": find(999) no encuentra");

        // addAfter: despues de 2 -> [3,2,99,1,10,20]
        check(l.addAfter(2, 99), nombre + ": addAfter(2,99)");
        // addBefore: antes de 1 -> [3,2,99,88,1,10,20]
        check(l.addBefore(1, 88), nombre + ": addBefore(1,88)");
        check(l.size() == 7, nombre + ": size 7 tras inserciones");

        // Verificar orden esperado recorriendo con popFront
        int[] esperado = {3, 2, 99, 88, 1, 10, 20};
        boolean ordenOk = true;
        for (int e : esperado) {
            if (l.popFront() != e) { ordenOk = false; break; }
        }
        check(ordenOk, nombre + ": orden correcto tras inserciones");
        check(l.empty(), nombre + ": vacia tras vaciar con popFront");

        // popBack
        l.pushBack(1);
        l.pushBack(2);
        l.pushBack(3);
        check(l.popBack() == 3, nombre + ": popBack==3");
        check(l.popBack() == 2, nombre + ": popBack==2");
        check(l.size() == 1, nombre + ": size 1");

        // erase
        l.pushBack(5);
        l.pushBack(1);          // lista: [1,5,1]
        check(l.erase(1), nombre + ": erase(1) primera aparicion");
        check(l.topFront() == 5, nombre + ": tras erase topFront==5");
        check(l.size() == 2, nombre + ": size 2 tras erase");
        check(!l.erase(777), nombre + ": erase inexistente retorna false");

        // vaciar y probar excepciones
        while (!l.empty()) l.popFront();
        boolean lanzo = false;
        try { l.popFront(); } catch (ListaVaciaException ex) { lanzo = true; }
        check(lanzo, nombre + ": popFront vacia lanza excepcion");
    }

    private static void probarStack() {
        System.out.println("-- MyStackCircular");
        MyStack<Integer> s = new MyStackCircular<>(2); // capacidad pequena para forzar resize
        check(s.isEmpty(), "Stack: nuevo vacio");
        for (int i = 1; i <= 10; i++) s.push(i); // fuerza varios resize
        check(s.size() == 10, "Stack: size 10");
        check(s.peek() == 10, "Stack: peek==10 (LIFO)");
        check(s.pop() == 10, "Stack: pop==10");
        check(s.pop() == 9, "Stack: pop==9");
        check(s.size() == 8, "Stack: size 8");

        // delete: cima..fondo = 8,7,...,1 ; borra primera aparicion de 5
        check(s.delete(5), "Stack: delete(5)");
        check(!s.delete(999), "Stack: delete inexistente false");
        check(s.size() == 7, "Stack: size 7 tras delete");

        // Vaciar y verificar orden LIFO restante: 8,7,6,4,3,2,1
        int[] esperado = {8, 7, 6, 4, 3, 2, 1};
        boolean ok = true;
        for (int e : esperado) if (s.pop() != e) { ok = false; break; }
        check(ok, "Stack: orden LIFO correcto tras delete");

        boolean lanzo = false;
        try { s.pop(); } catch (EstructuraVaciaException ex) { lanzo = true; }
        check(lanzo, "Stack: pop vacio lanza excepcion");
    }

    private static void probarQueue() {
        System.out.println("-- MyQueueCircular");
        MyQueue<Integer> q = new MyQueueCircular<>(2);
        check(q.isEmpty(), "Queue: nueva vacia");
        for (int i = 1; i <= 10; i++) q.enqueue(i);
        check(q.size() == 10, "Queue: size 10");
        check(q.front() == 1, "Queue: front==1 (FIFO)");
        check(q.dequeue() == 1, "Queue: dequeue==1");
        check(q.dequeue() == 2, "Queue: dequeue==2");
        check(q.size() == 8, "Queue: size 8");

        // Probar el "dar la vuelta" del buffer: encolar mas tras varios dequeue
        q.enqueue(11);
        q.enqueue(12); // fuerza wrap-around en el buffer circular
        check(q.size() == 10, "Queue: size 10 tras wrap-around");

        // delete primera aparicion de 5 (frente..atras = 3,4,5,...)
        check(q.delete(5), "Queue: delete(5)");
        check(!q.delete(999), "Queue: delete inexistente false");

        // Orden FIFO esperado: 3,4,6,7,8,9,10,11,12
        int[] esperado = {3, 4, 6, 7, 8, 9, 10, 11, 12};
        boolean ok = true;
        for (int e : esperado) if (q.dequeue() != e) { ok = false; break; }
        check(ok, "Queue: orden FIFO correcto tras delete y wrap-around");

        boolean lanzo = false;
        try { q.dequeue(); } catch (EstructuraVaciaException ex) { lanzo = true; }
        check(lanzo, "Queue: dequeue vacia lanza excepcion");
    }
}
