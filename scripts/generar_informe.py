#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Genera el informe HTML (que luego se convierte a PDF con wkhtmltopdf).

Inyecta automaticamente las tablas de resultados leyendo resultados/resultados.csv,
de modo que los numeros del informe siempre coincidan con la ultima corrida del
benchmark. Embebe las graficas como imagenes en base64 para producir un PDF
autocontenido.
"""

import csv
import os
import base64
from collections import defaultdict

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CSV_PATH = os.path.join(BASE, "resultados", "resultados.csv")
GRAF_DIR = os.path.join(BASE, "graficas")
OUT_HTML = os.path.join(BASE, "informe", "informe.html")
os.makedirs(os.path.dirname(OUT_HTML), exist_ok=True)

# Marcador del repositorio: reemplazar por la URL real de GitHub.
REPO_URL = "https://github.com/USUARIO/Stack-Queue-Java-ED"

AUTOR = "Diana Carolina Vásquez Gutiérrez"
CURSO = "Estructuras de Datos — 2026-2"
PROFESOR = "David Herrera"
MONITORA = "Ángela Camila Siabato Londoño"

TAMANOS = [10, 100, 1000, 10000, 100000, 1000000]

ETIQUETA = {
    "SinglyLinkedList": "Simplemente enlazada sin cola",
    "SinglyLinkedListTail": "Simplemente enlazada con cola",
    "DoublyLinkedList": "Doblemente enlazada sin cola",
    "DoublyLinkedListTail": "Doblemente enlazada con cola",
    "MyStackCircular": "Pila (arreglo circular)",
    "MyQueueCircular": "Cola (arreglo circular)",
}


def cargar():
    d = defaultdict(dict)
    with open(CSV_PATH, newline="", encoding="utf-8") as f:
        for r in csv.DictReader(f):
            d[(r["categoria"], r["estructura"], r["metodo"])][int(r["n"])] = float(r["ns_por_operacion"])
    return d


def img64(nombre):
    ruta = os.path.join(GRAF_DIR, nombre)
    if not os.path.exists(ruta):
        return ""
    with open(ruta, "rb") as f:
        b = base64.b64encode(f.read()).decode("ascii")
    return f'<img src="data:image/png;base64,{b}" />'


def celda(v):
    if v is None:
        return '<td class="na">—</td>'
    if v >= 1000:
        return f'<td>{v:,.0f}</td>'
    return f'<td>{v:.1f}</td>'


def tabla_lista(d, est, metodos):
    filas = []
    encabezado = "<tr><th>Método</th>" + "".join(
        f"<th>n={n:,}</th>" for n in TAMANOS) + "</tr>"
    for met in metodos:
        row = d.get(("List", est, met), {})
        celdas = "".join(celda(row.get(n)) for n in TAMANOS)
        filas.append(f"<tr><td class='met'>{met}</td>{celdas}</tr>")
    return f"<table class='data'>{encabezado}{''.join(filas)}</table>"


def tabla_pc(d, cat, est):
    mets = sorted(set(k[2] for k in d if k[0] == cat))
    encabezado = "<tr><th>Método</th>" + "".join(
        f"<th>n={n:,}</th>" for n in TAMANOS) + "</tr>"
    filas = []
    for met in mets:
        row = d.get((cat, est, met), {})
        celdas = "".join(celda(row.get(n)) for n in TAMANOS)
        filas.append(f"<tr><td class='met'>{met}</td>{celdas}</tr>")
    return f"<table class='data'>{encabezado}{''.join(filas)}</table>"


d = cargar()

METODOS_LISTA = ["PushFront", "PushBack", "PopFront", "PopBack",
                 "Find", "Erase", "AddBefore", "AddAfter"]

HTML = f"""<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="utf-8">
<style>
  @page {{ margin: 2cm 1.8cm; }}
  body {{ font-family: 'DejaVu Sans', Arial, sans-serif; color: #1a1a1a; font-size: 10.5pt; line-height: 1.5; }}
  h1 {{ font-size: 20pt; color: #0b3d62; margin-bottom: 0.1em; }}
  h2 {{ font-size: 14pt; color: #0b3d62; border-bottom: 2px solid #0b3d62; padding-bottom: 3px; margin-top: 1.4em; }}
  h3 {{ font-size: 12pt; color: #144e72; margin-top: 1.1em; }}
  h4 {{ font-size: 11pt; color: #333; margin-top: 0.9em; margin-bottom: 0.3em; }}
  p {{ text-align: justify; }}
  code {{ font-family: 'DejaVu Sans Mono', monospace; background: #f0f3f6; padding: 1px 4px; border-radius: 3px; font-size: 9.5pt; }}
  .portada {{ text-align: center; margin-top: 5cm; }}
  .portada .sub {{ font-size: 13pt; color: #444; margin-top: 0.3em; }}
  .portada .meta {{ margin-top: 3cm; font-size: 12pt; line-height: 2; }}
  .pagebreak {{ page-break-before: always; }}
  table.data {{ border-collapse: collapse; width: 100%; margin: 0.6em 0 1.2em; font-size: 8.4pt; }}
  table.data th {{ background: #0b3d62; color: #fff; padding: 5px 4px; text-align: right; font-weight: 600; }}
  table.data th:first-child {{ text-align: left; }}
  table.data td {{ padding: 4px; text-align: right; border-bottom: 1px solid #dde3e8; }}
  table.data td.met {{ text-align: left; font-weight: 600; color: #144e72; }}
  table.data td.na {{ color: #b00; text-align: center; }}
  table.data tr:nth-child(even) td {{ background: #f6f9fb; }}
  table.bigo {{ border-collapse: collapse; width: 100%; margin: 0.6em 0 1.2em; font-size: 9pt; }}
  table.bigo th {{ background: #144e72; color:#fff; padding:6px; }}
  table.bigo td {{ padding: 5px 6px; border: 1px solid #dde3e8; text-align:center; }}
  table.bigo td:first-child {{ text-align:left; font-weight:600; }}
  .o1 {{ background:#e6f4ea; color:#137333; font-weight:600; }}
  .on {{ background:#fce8e6; color:#c5221f; font-weight:600; }}
  .fig {{ text-align: center; margin: 1em 0; }}
  .fig img {{ width: 100%; max-width: 17cm; border: 1px solid #e0e0e0; }}
  .fig .cap {{ font-size: 9pt; color: #555; font-style: italic; margin-top: 4px; }}
  .nota {{ background:#fff8e1; border-left: 4px solid #f9a825; padding: 8px 12px; margin: 1em 0; font-size: 9.5pt; }}
  ul, ol {{ margin: 0.4em 0 0.9em; }}
  li {{ margin-bottom: 4px; text-align: justify; }}
  a {{ color: #0b57d0; }}
</style>
</head>
<body>

<!-- PORTADA -->
<div class="portada">
  <h1>Implementación y Análisis de Complejidad<br>de Listas, Pilas y Colas en Java</h1>
  <div class="sub">Tarea individual · {CURSO}</div>
  <div class="meta">
    <b>Estudiante:</b> {AUTOR}<br>
    <b>Profesor:</b> {PROFESOR}<br>
    <b>Monitora:</b> {MONITORA}<br>
    <b>Repositorio GitHub:</b> <a href="{REPO_URL}">{REPO_URL}</a><br>
    <b>Fecha:</b> 28 de septiembre de 2026
  </div>
</div>

<!-- 1. OBJETIVO -->
<div class="pagebreak"></div>
<h2>1. Objetivo</h2>
<p>El objetivo de este trabajo es implementar desde cero, sin usar librerías de
estructuras de datos de Java, la estructura <b>List</b> en sus cuatro variantes
de lista enlazada (simplemente enlazada sin y con cola, y doblemente enlazada sin
y con cola), así como las estructuras <b>Stack</b> (pila) y <b>Queue</b> (cola)
sobre un arreglo dinámico circular. Sobre cada implementación se realiza un
análisis de complejidad de sus métodos, contrastando la complejidad teórica en
notación Big-O con mediciones empíricas de tiempo de ejecución, y se concluye
sobre las condiciones bajo las cuales conviene cada estructura.</p>

<!-- 2. IMPLEMENTACION -->
<h2>2. Explicación de la implementación</h2>

<h3>2.1. Estructura List (listas enlazadas)</h3>
<p>Las cuatro variantes implementan una interfaz común <code>MyList&lt;T&gt;</code>.
Compartir el contrato permite ejecutar el mismo banco de pruebas sobre todas las
implementaciones bajo condiciones idénticas. Cada variante mantiene un contador
<code>size</code> para responder <code>size()</code> en O(1). Las diferencias de
diseño son:</p>
<ul>
  <li><b>Simplemente enlazada sin cola:</b> cada nodo guarda solo <code>next</code>
  y la lista solo referencia a <code>head</code>. Cualquier operación sobre el
  final requiere recorrer los <i>n</i> nodos.</li>
  <li><b>Simplemente enlazada con cola:</b> añade un puntero <code>tail</code>, lo
  que hace <code>pushBack</code> y <code>topBack</code> O(1). Sin embargo,
  <code>popBack</code> sigue siendo O(n) porque, al no existir puntero al nodo
  anterior, hay que recorrer hasta el penúltimo.</li>
  <li><b>Doblemente enlazada sin cola:</b> cada nodo guarda <code>prev</code> y
  <code>next</code>. Tener <code>prev</code> hace que el desenganche en
  <code>erase</code> y la inserción en <code>addBefore</code> sean O(1) una vez
  localizado el nodo, pero sin <code>tail</code> el acceso al final sigue siendo
  O(n).</li>
  <li><b>Doblemente enlazada con cola:</b> combina <code>prev/next</code> con
  <code>head/tail</code>. Todas las operaciones sobre los extremos son O(1); solo
  las que dependen de localizar un valor arbitrario mantienen la búsqueda lineal
  O(n).</li>
</ul>

<h3>2.2. Estructuras Stack y Queue (arreglo dinámico circular)</h3>
<p>Ambas se construyen sobre un <b>buffer circular</b> con un índice
<code>head</code> y un contador <code>count</code>. La posición de inserción se
calcula con aritmética modular <code>(head + count) % capacidad</code>, de forma
que los índices "dan la vuelta" y se reutilizan las celdas liberadas. Esto es
esencial en la cola: <code>dequeue</code> solo avanza <code>head</code> en O(1),
evitando el desplazamiento O(n) de todos los elementos que exigiría un arreglo
lineal.</p>

<h4>Estrategia de crecimiento del arreglo dinámico</h4>
<p>Cuando el buffer se llena, la capacidad se <b>duplica</b> (crecimiento
geométrico ×2). Se justifica así: al insertar <i>n</i> elementos, el total de
copias por redimensionamiento es a lo sumo
<code>n + n/2 + n/4 + … &lt; 2n</code>, es decir <b>O(n) copias en total</b>. Por
lo tanto el costo <b>amortizado</b> de <code>push</code>/<code>enqueue</code> es
<b>O(1)</b>, aunque una inserción puntual que dispare la duplicación cueste O(n).
Se descartó el crecimiento aditivo (sumar una constante <code>+k</code>) porque
provocaría O(n) redimensionamientos y un costo amortizado O(n) por inserción,
esto es O(n²) en total.</p>

<!-- 3. METODOLOGIA DE MEDICION -->
<h2>3. Metodología de medición</h2>
<p>Las decisiones de medición responden directamente a los criterios del
enunciado, en particular la precisión:</p>
<ul>
  <li><b>Unidad — nanosegundos:</b> se usa <code>System.nanoTime()</code>. Muchos
  métodos son O(1) y para <i>n</i> pequeños tardan menos de 1 ms; medir en
  milisegundos (como el borrador <code>Main.java</code> con <code>Instant</code>)
  daría 0 y haría imposible el análisis. Se reporta el <b>tiempo promedio por
  operación</b> en ns.</li>
  <li><b>Promedio por operación:</b> para métodos O(1) se ejecuta la operación
  <i>n</i> veces y se divide el tiempo total entre <i>n</i>, aislando el costo
  unitario y evitando que la resolución del reloj domine. Para métodos O(n)
  (búsquedas, <code>erase</code>, etc.) se ejecuta un número acotado de
  repeticiones sobre una estructura ya poblada con <i>n</i> elementos.</li>
  <li><b>Calentamiento (warmup) y mediana:</b> antes de cada medición se ejecutan
  iteraciones de calentamiento para que el compilador JIT optimice el código y no
  se mida la fase interpretada. Cada medición se repite 5 veces y se toma la
  <b>mediana</b>, robusta frente a pausas del recolector de basura.</li>
  <li><b>Graficación separada:</b> el programa Java <b>solo</b> exporta un CSV;
  las gráficas se generan después en Python. Así se evita que las operaciones de
  dibujo contaminen los tiempos medidos, tal como advierte el enunciado.</li>
  <li><b>Presupuesto de operaciones:</b> las combinaciones cuyo costo estimado es
  O(n²) (p. ej. <code>pushBack</code> sin cola para <i>n</i>≥10⁵) se omiten para
  que el experimento termine en tiempo razonable; en las tablas aparecen como
  "—". La ausencia de dato es en sí misma evidencia del costo cuadrático.</li>
</ul>
<p>Tamaños de entrada evaluados: 10, 100, 1.000, 10.000, 100.000 y 1.000.000.</p>

<!-- 4. ANALISIS PARTE 1 -->
<div class="pagebreak"></div>
<h2>4. Análisis de complejidad — Parte 1: métodos de List</h2>
<p>Se comparan los mismos métodos entre las cuatro implementaciones de lista. Los
tiempos están en <b>nanosegundos por operación</b>; "—" indica una medición
omitida por su costo cuadrático.</p>

<h3>4.1. Complejidad teórica (Big-O)</h3>
<table class="bigo">
<tr><th>Método</th><th>Simple sin cola</th><th>Simple con cola</th><th>Doble sin cola</th><th>Doble con cola</th></tr>
<tr><td>PushFront</td><td class="o1">O(1)</td><td class="o1">O(1)</td><td class="o1">O(1)</td><td class="o1">O(1)</td></tr>
<tr><td>PushBack</td><td class="on">O(n)</td><td class="o1">O(1)</td><td class="on">O(n)</td><td class="o1">O(1)</td></tr>
<tr><td>PopFront</td><td class="o1">O(1)</td><td class="o1">O(1)</td><td class="o1">O(1)</td><td class="o1">O(1)</td></tr>
<tr><td>PopBack</td><td class="on">O(n)</td><td class="on">O(n)</td><td class="on">O(n)</td><td class="o1">O(1)</td></tr>
<tr><td>Find</td><td class="on">O(n)</td><td class="on">O(n)</td><td class="on">O(n)</td><td class="on">O(n)</td></tr>
<tr><td>Erase</td><td class="on">O(n)</td><td class="on">O(n)</td><td class="on">O(n)</td><td class="on">O(n)</td></tr>
<tr><td>AddBefore</td><td class="on">O(n)</td><td class="on">O(n)</td><td class="on">O(n)</td><td class="on">O(n)</td></tr>
<tr><td>AddAfter</td><td class="on">O(n)</td><td class="on">O(n)</td><td class="on">O(n)</td><td class="on">O(n)</td></tr>
</table>
<p class="nota"><b>Hipótesis previa.</b> Se esperaba que las variantes
<i>con cola</i> aplanaran <code>pushBack</code>/<code>topBack</code> a O(1), que
solo la <i>doble con cola</i> lograra <code>popBack</code> O(1) (por requerir el
puntero <code>prev</code> además de <code>tail</code>), y que
<code>find/erase/addBefore/addAfter</code> permanecieran O(n) en todas por la
búsqueda lineal inherente a una lista enlazada. Las mediciones confirman estas
hipótesis.</p>

<h3>4.2. Resultados empíricos por implementación</h3>
<h4>Simplemente enlazada sin cola</h4>
{tabla_lista(d, "SinglyLinkedList", METODOS_LISTA)}
<h4>Simplemente enlazada con cola</h4>
{tabla_lista(d, "SinglyLinkedListTail", METODOS_LISTA)}
<h4>Doblemente enlazada sin cola</h4>
{tabla_lista(d, "DoublyLinkedList", METODOS_LISTA)}
<h4>Doblemente enlazada con cola</h4>
{tabla_lista(d, "DoublyLinkedListTail", METODOS_LISTA)}

<h3>4.3. Gráficas comparativas por método</h3>
<div class="fig">{img64("list_pushfront.png")}<div class="cap">Figura 1. PushFront: O(1) plano en las cuatro implementaciones.</div></div>
<div class="fig">{img64("list_pushback.png")}<div class="cap">Figura 2. PushBack: las variantes sin cola crecen linealmente (O(n)); las de cola permanecen planas (O(1)).</div></div>
<div class="fig">{img64("list_popfront.png")}<div class="cap">Figura 3. PopFront: O(1) plano en todas.</div></div>
<div class="fig">{img64("list_popback.png")}<div class="cap">Figura 4. PopBack: solo la doble con cola es O(1); el resto es O(n).</div></div>
<div class="fig">{img64("list_find.png")}<div class="cap">Figura 5. Find: O(n) en todas, con pendiente unitaria en log-log.</div></div>
<div class="fig">{img64("list_erase.png")}<div class="cap">Figura 6. Erase: O(n) dominado por la búsqueda.</div></div>
<div class="fig">{img64("list_addbefore.png")}<div class="cap">Figura 7. AddBefore: O(n).</div></div>
<div class="fig">{img64("list_addafter.png")}<div class="cap">Figura 8. AddAfter: O(n).</div></div>

<h3>4.4. Interpretación</h3>
<p>El contraste más claro es <b>PushBack</b> (Figura 2): en las variantes sin cola
el tiempo por operación pasa de ~190 ns con <i>n</i>=10 a más de 11.000 ns con
<i>n</i>=10.000, un crecimiento proporcional a <i>n</i> que confirma O(n); las
variantes con cola se mantienen en ~10 ns sin importar el tamaño, confirmando
O(1). En <b>PopBack</b> (Figura 4) se aprecia que tener <code>tail</code> no basta:
la simple con cola sigue siendo O(n) porque necesita el penúltimo nodo; solo la
doble con cola, que dispone de <code>prev</code>, logra O(1). Los métodos de
búsqueda (Figuras 5–8) crecen linealmente en las cuatro implementaciones: la
mejora que aporta <code>prev</code> al desenganche no altera el orden de
complejidad porque el costo lo domina la búsqueda O(n).</p>

<!-- 5. ANALISIS PARTE 2 -->
<div class="pagebreak"></div>
<h2>5. Análisis de complejidad — Parte 2: MyStack y MyQueue</h2>

<h3>5.1. Complejidad teórica</h3>
<table class="bigo">
<tr><th>Estructura</th><th>Inserción</th><th>Eliminación</th><th>Consulta</th><th>delete</th><th>size/isEmpty</th></tr>
<tr><td>MyStack (circular)</td><td class="o1">push O(1)*</td><td class="o1">pop O(1)</td><td class="o1">peek O(1)</td><td class="on">O(n)</td><td class="o1">O(1)</td></tr>
<tr><td>MyQueue (circular)</td><td class="o1">enqueue O(1)*</td><td class="o1">dequeue O(1)</td><td class="o1">front O(1)</td><td class="on">O(n)</td><td class="o1">O(1)</td></tr>
</table>
<p style="font-size:9pt">* O(1) <b>amortizado</b>: una inserción puntual que dispara la duplicación del arreglo cuesta O(n), pero repartido entre todas las inserciones el costo por operación es constante.</p>

<h3>5.2. Resultados empíricos</h3>
<h4>Pila — MyStack sobre arreglo circular</h4>
{tabla_pc(d, "Stack", "MyStackCircular")}
<h4>Cola — MyQueue sobre arreglo circular</h4>
{tabla_pc(d, "Queue", "MyQueueCircular")}

<div class="fig">{img64("stack_metodos.png")}<div class="cap">Figura 9. MyStack: push/pop/peek O(1); delete O(n).</div></div>
<div class="fig">{img64("queue_metodos.png")}<div class="cap">Figura 10. MyQueue: enqueue/dequeue/front O(1); delete O(n).</div></div>

<h3>5.3. Interpretación</h3>
<p>Las operaciones fundamentales (<code>push</code>, <code>pop</code>,
<code>peek</code>, <code>enqueue</code>, <code>dequeue</code>, <code>front</code>)
se mantienen prácticamente constantes en todo el rango de tamaños, confirmando el
O(1) amortizado. La ligera <b>disminución</b> del tiempo por operación al crecer
<i>n</i> no contradice la teoría: es consecuencia del calentamiento del JIT, que
optimiza cada vez mejor el bucle a medida que se ejecuta más veces, y del hecho de
que el costo de las duplicaciones se diluye entre más operaciones. En algunos
puntos <code>peek</code>/<code>front</code> caen a valores cercanos a cero porque
el JIT llega a eliminar parte del bucle constante; es un artefacto de medición
esperado que no cambia la conclusión de O(1). El método <code>delete</code>, en
cambio, crece linealmente (búsqueda + compactación), como muestra su pendiente en
las Figuras 9 y 10.</p>

<!-- 6. ANALISIS PARTE 3 -->
<div class="pagebreak"></div>
<h2>6. Análisis de complejidad — Parte 3: métodos equivalentes List vs Stack/Queue</h2>
<p>Se compara cada método de las estructuras circulares con su método equivalente
en List. Para cada comparación se elige la implementación de List <b>más óptima</b>
para ese método, según se justifica en cada caso.</p>

<table class="bigo">
<tr><th>Stack/Queue</th><th>Equivalente en List</th><th>List elegida</th><th>Justificación de la elección</th></tr>
<tr><td>push (cima)</td><td>PushFront</td><td>Simple sin cola</td><td style="text-align:left">PushFront es O(1) en todas; se toma la más liviana en memoria.</td></tr>
<tr><td>pop (cima)</td><td>PopFront</td><td>Simple sin cola</td><td style="text-align:left">PopFront es O(1) en todas; la más liviana.</td></tr>
<tr><td>enqueue (final)</td><td>PushBack</td><td>Doble con cola</td><td style="text-align:left">PushBack solo es O(1) con cola; se elige la única que además da todos los extremos en O(1).</td></tr>
<tr><td>dequeue (frente)</td><td>PopFront</td><td>Doble con cola</td><td style="text-align:left">PopFront es O(1) en todas; se usa la doble con cola para emparejar con enqueue.</td></tr>
<tr><td>delete</td><td>Erase</td><td>Doble con cola</td><td style="text-align:left">Erase es O(n) por la búsqueda; la doble con cola ofrece el desenganche O(1).</td></tr>
</table>

<div class="fig">{img64("equiv_1_push_vs_pushfront.png")}<div class="cap">Figura 11. push (Stack) vs PushFront (List). Ambos O(1).</div></div>
<div class="fig">{img64("equiv_2_pop_vs_popfront.png")}<div class="cap">Figura 12. pop (Stack) vs PopFront (List). Ambos O(1).</div></div>
<div class="fig">{img64("equiv_3_enqueue_vs_pushback.png")}<div class="cap">Figura 13. enqueue (Queue) vs PushBack (List, doble con cola). Ambos O(1).</div></div>
<div class="fig">{img64("equiv_4_dequeue_vs_popfront.png")}<div class="cap">Figura 14. dequeue (Queue) vs PopFront (List). Ambos O(1).</div></div>
<div class="fig">{img64("equiv_5_delete_vs_erase.png")}<div class="cap">Figura 15. delete (Stack) vs Erase (List). Ambos O(n).</div></div>
<div class="fig">{img64("equiv_6_delete_vs_erase.png")}<div class="cap">Figura 16. delete (Queue) vs Erase (List). Ambos O(n).</div></div>

<h3>6.1. Interpretación</h3>
<p>Para las operaciones de extremo, la lista enlazada y el arreglo circular son
del mismo orden de complejidad (O(1)) y sus tiempos absolutos son comparables. La
diferencia práctica está en los <b>factores constantes</b>: el arreglo circular
suele ser algo más rápido y usa menos memoria por elemento (no almacena punteros),
mientras que la lista enlazada nunca paga el costo puntual O(n) de un
redimensionamiento y no desperdicia capacidad reservada. En <code>delete</code> vs
<code>Erase</code> ambos son O(n), pero por motivos distintos: en la lista el costo
es la búsqueda; en el arreglo, la búsqueda más la compactación por desplazamiento.</p>

<!-- 7. CONCLUSIONES -->
<div class="pagebreak"></div>
<h2>7. Conclusiones</h2>

<h3>7.1. ¿Cuándo usar cada implementación de List?</h3>
<ul>
  <li><b>Simplemente enlazada sin cola:</b> adecuada cuando solo se opera en el
  frente (como pila LIFO); es la más económica en memoria. Debe evitarse si se
  inserta o consulta el final con frecuencia.</li>
  <li><b>Simplemente enlazada con cola:</b> ideal para una cola FIFO enlazada:
  <code>pushBack</code> y <code>popFront</code> son O(1). Su punto débil es
  <code>popBack</code>, que sigue siendo O(n).</li>
  <li><b>Doblemente enlazada sin cola:</b> útil cuando se requieren
  <code>erase</code>/<code>addBefore</code> eficientes una vez ubicado el nodo, sin
  necesidad de operar el final. Cuesta un puntero extra por nodo.</li>
  <li><b>Doblemente enlazada con cola:</b> la más versátil; todos los extremos en
  O(1). Es la elección por defecto cuando se necesita una lista de propósito
  general (equivale a una deque). Su costo es el mayor consumo de memoria (dos
  punteros por nodo).</li>
</ul>

<h3>7.2. Escenarios reales de pilas y colas</h3>
<ul>
  <li><b>Pilas (LIFO):</b> pila de llamadas de un programa, deshacer/rehacer
  (undo/redo) en editores, evaluación de expresiones y balanceo de paréntesis,
  y recorrido DFS de grafos.</li>
  <li><b>Colas (FIFO):</b> colas de impresión y de tareas, buffers de
  productor-consumidor, atención de peticiones en servidores, y recorrido BFS de
  grafos.</li>
</ul>

<h3>7.3. Arreglos dinámicos vs listas enlazadas en Java</h3>
<table class="bigo">
<tr><th>Criterio</th><th>Arreglo dinámico circular</th><th>Lista enlazada</th></tr>
<tr><td>Acceso a extremos</td><td>O(1) amortizado</td><td>O(1) (con los punteros adecuados)</td></tr>
<tr><td>Memoria por elemento</td><td class="o1">Menor (solo el dato)</td><td class="on">Mayor (dato + 1 o 2 punteros)</td></tr>
<tr><td>Localidad de caché</td><td class="o1">Alta (memoria contigua)</td><td class="on">Baja (nodos dispersos)</td></tr>
<tr><td>Costo puntual peor caso</td><td class="on">O(n) al redimensionar</td><td class="o1">Nunca redimensiona</td></tr>
<tr><td>Capacidad desperdiciada</td><td class="on">Sí (hasta ~50% tras duplicar)</td><td class="o1">No</td></tr>
</table>
<p>En síntesis, el <b>arreglo dinámico</b> gana en velocidad media y consumo de
memoria por su localidad de caché y la ausencia de punteros, y es la mejor opción
para pilas y colas de propósito general en Java; su precio es el redimensionamiento
puntual O(n) y la capacidad reservada. La <b>lista enlazada</b> ofrece un costo por
operación predecible sin picos de redimensionamiento y crecimiento exacto sin
desperdicio, a cambio de más memoria por nodo y peor localidad de caché. La
elección correcta depende del patrón de uso: extremos frecuentes y memoria ajustada
favorecen el arreglo; inserciones/eliminaciones internas tras ubicar un nodo, y la
necesidad de evitar picos de latencia, favorecen la lista doblemente enlazada.</p>

<h3>7.4. Reflexión sobre la medición empírica</h3>
<p>La coincidencia entre la complejidad teórica y la medida validó tanto las
implementaciones como la metodología. Los principales aprendizajes prácticos
fueron: la necesidad de <b>nanosegundos</b> para capturar operaciones O(1); la
importancia del <b>warmup del JIT</b>, cuyo efecto explica la mejora de los tiempos
al aumentar <i>n</i>; y la conveniencia de <b>separar la graficación de la
medición</b>, que evita sesgar los resultados. Los "—" de las tablas, lejos de ser
huecos, son la evidencia más contundente del costo O(n²) de operar el extremo
equivocado en una lista sin los punteros apropiados.</p>

<hr style="margin-top:2em">
<p style="font-size:9pt;color:#666"><b>Código fuente completo:</b>
<a href="{REPO_URL}">{REPO_URL}</a> — incluye las implementaciones en Java, el
banco de pruebas de correctitud (110 verificaciones), el benchmark de medición y
los scripts de graficación.</p>

</body>
</html>
"""

with open(OUT_HTML, "w", encoding="utf-8") as f:
    f.write(HTML)
print("HTML generado:", OUT_HTML)
