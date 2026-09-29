# Implementación y Análisis de Complejidad de Listas, Pilas y Colas en Java

Tarea individual — **Estructuras de Datos 2026-2**
Profesor: David Herrera · Monitora: Ángela Camila Siabato Londoño
Estudiante: **Juan Diego Camargo**

Implementación desde cero (sin librerías de estructuras de datos) de:

- La estructura **List** en sus cuatro variantes de lista enlazada.
- Las estructuras **Stack** (pila) y **Queue** (cola) sobre **arreglo dinámico circular**.

Con análisis de complejidad teórico (Big-O) contrastado con mediciones empíricas
de tiempo, gráficas y conclusiones. El informe completo está en
[`informe/`](informe/).

---

## Estructura del repositorio

```
Stack-Queue-Java-ED/
├── src/
│   ├── estructuras/
│   │   ├── lista/                     # Estructura List (4 implementaciones)
│   │   │   ├── MyList.java            # Interfaz común
│   │   │   ├── SinglyLinkedList.java          # Simple sin cola
│   │   │   ├── SinglyLinkedListTail.java      # Simple con cola
│   │   │   ├── DoublyLinkedList.java          # Doble sin cola
│   │   │   ├── DoublyLinkedListTail.java      # Doble con cola
│   │   │   └── ListaVaciaException.java
│   │   └── pilacola/                  # Stack y Queue (arreglo circular)
│   │       ├── MyStack.java           # Interfaz de pila
│   │       ├── MyQueue.java           # Interfaz de cola
│   │       ├── MyStackCircular.java   # Pila sobre buffer circular
│   │       ├── MyQueueCircular.java   # Cola sobre buffer circular
│   │       └── EstructuraVaciaException.java
│   └── benchmark/
│       ├── Correctitud.java           # 110 pruebas de correctitud
│       └── Benchmark.java             # Medición de tiempos -> CSV
├── scripts/
│   ├── graficar.py                    # Genera las gráficas desde el CSV
│   └── generar_informe.py            # Genera el informe HTML
├── resultados/
│   └── resultados.csv                 # Salida del benchmark
├── graficas/                          # Gráficas PNG generadas
├── informe/
│   └── Stack-Queue-Java-ED-Diana Vasquez.pdf   # Informe final
├── Main.java                          # Borrador de referencia del profesor
├── ejecutar.sh                        # Compila y corre todo
└── README.md
```

## Cómo compilar y ejecutar

Requisitos: **JDK 17+** (se probó con JDK 21) y, opcionalmente, **Python 3** con
`matplotlib` para regenerar las gráficas.

### Opción rápida (script)

```bash
./ejecutar.sh
```

Compila todo, corre las pruebas de correctitud, ejecuta el benchmark y regenera
las gráficas.

### Manual

```bash
# 1. Compilar
javac -d bin src/estructuras/lista/*.java src/estructuras/pilacola/*.java src/benchmark/*.java

# 2. Verificar correctitud (110 pruebas)
java -cp bin benchmark.Correctitud

# 3. Ejecutar el benchmark (exporta el CSV)
java -Xmx3g -cp bin benchmark.Benchmark > resultados/resultados.csv

# 4. Generar las gráficas
python3 scripts/graficar.py
```

## Notas de diseño

- **Interfaz común** `MyList<T>` para las cuatro listas: permite medir todas bajo
  condiciones idénticas.
- **Buffer circular** con crecimiento **×2** en Stack y Queue: costo amortizado
  O(1) por inserción (justificación en el informe).
- **Medición en nanosegundos** con `System.nanoTime()`, warmup del JIT y mediana
  de 5 muestras. La **graficación está separada** de la medición (Java solo
  exporta CSV; Python grafica) para no sesgar los tiempos.

## Resumen de complejidad

| Método    | Simple s/cola | Simple c/cola | Doble s/cola | Doble c/cola |
|-----------|:---:|:---:|:---:|:---:|
| PushFront | O(1) | O(1) | O(1) | O(1) |
| PushBack  | O(n) | O(1) | O(n) | O(1) |
| PopFront  | O(1) | O(1) | O(1) | O(1) |
| PopBack   | O(n) | O(n) | O(n) | O(1) |
| Find      | O(n) | O(n) | O(n) | O(n) |
| Erase     | O(n) | O(n) | O(n) | O(n) |
| AddBefore | O(n) | O(n) | O(n) | O(n) |
| AddAfter  | O(n) | O(n) | O(n) | O(n) |

| Estructura | Inserción | Eliminación | Consulta | delete |
|------------|:---:|:---:|:---:|:---:|
| MyStack (circular) | push O(1)* | pop O(1) | peek O(1) | O(n) |
| MyQueue (circular) | enqueue O(1)* | dequeue O(1) | front O(1) | O(n) |

\* O(1) amortizado.
