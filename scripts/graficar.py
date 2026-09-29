#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Generacion de graficas a partir de resultados/resultados.csv

La graficacion esta separada de la medicion (requisito del enunciado): este
script solo LEE el CSV producido por el benchmark en Java y dibuja. No mide
tiempos.

Produce tres bloques de graficas:
  1. Por cada metodo de List, una grafica comparando las 4 implementaciones.
  2. Una grafica por estructura para Stack y otra para Queue (todos sus metodos).
  3. Comparativa de metodos equivalentes List vs Stack/Queue.

Escala: log-log (tamano n en X, ns por operacion en Y). La escala log en ambos
ejes permite distinguir O(1) (recta horizontal) de O(n) (recta con pendiente 1).
"""

import csv
import os
from collections import defaultdict

import matplotlib
matplotlib.use("Agg")  # backend sin pantalla
import matplotlib.pyplot as plt
import matplotlib.ticker as mticker

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CSV_PATH = os.path.join(BASE, "resultados", "resultados.csv")
OUT_DIR = os.path.join(BASE, "graficas")
os.makedirs(OUT_DIR, exist_ok=True)

# ---------------------------------------------------------------- estilo comun
plt.rcParams.update({
    "figure.figsize": (9, 5.5),
    "figure.dpi": 130,
    "font.size": 11,
    "axes.grid": True,
    "grid.alpha": 0.3,
    "axes.axisbelow": True,
})

# Paleta consistente por implementacion / estructura
COLORES = {
    "SinglyLinkedList":     "#1f77b4",
    "SinglyLinkedListTail": "#ff7f0e",
    "DoublyLinkedList":     "#2ca02c",
    "DoublyLinkedListTail": "#d62728",
    "MyStackCircular":      "#9467bd",
    "MyQueueCircular":      "#8c564b",
}
ETIQUETA = {
    "SinglyLinkedList":     "Simple sin cola",
    "SinglyLinkedListTail": "Simple con cola",
    "DoublyLinkedList":     "Doble sin cola",
    "DoublyLinkedListTail": "Doble con cola",
    "MyStackCircular":      "Stack (circular)",
    "MyQueueCircular":      "Queue (circular)",
}


def cargar():
    """Devuelve datos[categoria][metodo][estructura] = [(n, ns), ...] ordenado por n."""
    datos = defaultdict(lambda: defaultdict(lambda: defaultdict(list)))
    with open(CSV_PATH, newline="", encoding="utf-8") as f:
        for fila in csv.DictReader(f):
            cat = fila["categoria"]
            met = fila["metodo"]
            est = fila["estructura"]
            n = int(fila["n"])
            ns = float(fila["ns_por_operacion"])
            datos[cat][met][est].append((n, ns))
    for cat in datos:
        for met in datos[cat]:
            for est in datos[cat][met]:
                datos[cat][met][est].sort()
    return datos


def eje_log(ax):
    ax.set_xscale("log")
    ax.set_yscale("log")
    ax.set_xlabel("Tamaño de entrada n (escala log)")
    ax.set_ylabel("Tiempo por operación (ns, escala log)")
    ax.xaxis.set_major_formatter(mticker.LogFormatterMathtext())


# ============================================================================
# BLOQUE 1: por cada metodo de List, comparar las 4 implementaciones
# ============================================================================
ORDEN_LISTA = ["SinglyLinkedList", "SinglyLinkedListTail",
               "DoublyLinkedList", "DoublyLinkedListTail"]

METODOS_LISTA = ["PushFront", "PushBack", "PopFront", "PopBack",
                 "Find", "Erase", "AddBefore", "AddAfter"]


def graficar_bloque1(datos):
    generadas = []
    for met in METODOS_LISTA:
        if met not in datos["List"]:
            continue
        fig, ax = plt.subplots()
        for est in ORDEN_LISTA:
            serie = datos["List"][met].get(est, [])
            if not serie:
                continue
            xs = [p[0] for p in serie]
            ys = [p[1] for p in serie]
            ax.plot(xs, ys, marker="o", markersize=4, linewidth=1.8,
                    color=COLORES[est], label=ETIQUETA[est])
        eje_log(ax)
        ax.set_title(f"List — {met}: comparación de las 4 implementaciones")
        ax.legend(fontsize=9)
        fig.tight_layout()
        ruta = os.path.join(OUT_DIR, f"list_{met.lower()}.png")
        fig.savefig(ruta)
        plt.close(fig)
        generadas.append(ruta)
    return generadas


# ============================================================================
# BLOQUE 2: Stack y Queue (cada uno con todos sus metodos)
# ============================================================================
def graficar_bloque2(datos):
    generadas = []
    for cat, est in [("Stack", "MyStackCircular"), ("Queue", "MyQueueCircular")]:
        fig, ax = plt.subplots()
        for met in sorted(datos[cat].keys()):
            serie = datos[cat][met].get(est, [])
            if not serie:
                continue
            xs = [p[0] for p in serie]
            ys = [p[1] for p in serie]
            ax.plot(xs, ys, marker="o", markersize=4, linewidth=1.8, label=met)
        eje_log(ax)
        ax.set_title(f"{cat} sobre arreglo circular — todos los métodos")
        ax.legend(fontsize=9)
        fig.tight_layout()
        ruta = os.path.join(OUT_DIR, f"{cat.lower()}_metodos.png")
        fig.savefig(ruta)
        plt.close(fig)
        generadas.append(ruta)
    return generadas


# ============================================================================
# BLOQUE 3: metodos equivalentes List vs Stack/Queue
# ============================================================================
# Se elige, para cada metodo del Stack/Queue, la implementacion de List MAS
# OPTIMA para ese metodo equivalente (la de menor complejidad), tal como pide
# el enunciado.
#
#   push (Stack, cima)         ~ PushFront de List        -> cualquiera (O(1))
#   pop (Stack, cima)          ~ PopFront de List         -> cualquiera (O(1))
#   enqueue (Queue, al final)  ~ PushBack de List         -> con cola (O(1))
#   dequeue (Queue, del frente)~ PopFront de List         -> cualquiera (O(1))
#   delete                     ~ Erase de List            -> Doble con cola
EQUIVALENCIAS = [
    # (titulo, (cat_pc, met_pc, est_pc), (met_list, est_list, justificacion))
    ("push (Stack) vs PushFront (List)",
     ("Stack", "push", "MyStackCircular"),
     ("PushFront", "SinglyLinkedList",
      "PushFront es O(1) en todas; se usa Simple sin cola por ser la más liviana.")),
    ("pop (Stack) vs PopFront (List)",
     ("Stack", "pop", "MyStackCircular"),
     ("PopFront", "SinglyLinkedList",
      "PopFront es O(1) en todas; Simple sin cola es la más liviana.")),
    ("enqueue (Queue) vs PushBack (List)",
     ("Queue", "enqueue", "MyQueueCircular"),
     ("PushBack", "DoublyLinkedListTail",
      "PushBack sólo es O(1) con cola; se elige Doble con cola.")),
    ("dequeue (Queue) vs PopFront (List)",
     ("Queue", "dequeue", "MyQueueCircular"),
     ("PopFront", "DoublyLinkedListTail",
      "PopFront es O(1) en todas; se usa Doble con cola para emparejar con enqueue.")),
    ("delete (Stack) vs Erase (List)",
     ("Stack", "delete", "MyStackCircular"),
     ("Erase", "DoublyLinkedListTail",
      "Erase es O(n) por la búsqueda; Doble con cola tiene el desenganche O(1).")),
    ("delete (Queue) vs Erase (List)",
     ("Queue", "delete", "MyQueueCircular"),
     ("Erase", "DoublyLinkedListTail",
      "Erase es O(n) por la búsqueda; Doble con cola tiene el desenganche O(1).")),
]


def graficar_bloque3(datos):
    generadas = []
    for i, (titulo, (cat_pc, met_pc, est_pc), (met_l, est_l, just)) in enumerate(EQUIVALENCIAS, 1):
        serie_pc = datos[cat_pc][met_pc].get(est_pc, [])
        serie_l = datos["List"][met_l].get(est_l, [])
        if not serie_pc or not serie_l:
            continue
        fig, ax = plt.subplots()
        xs = [p[0] for p in serie_pc]; ys = [p[1] for p in serie_pc]
        ax.plot(xs, ys, marker="s", markersize=4, linewidth=1.8,
                color=COLORES[est_pc], label=f"{est_pc}.{met_pc}")
        xs = [p[0] for p in serie_l]; ys = [p[1] for p in serie_l]
        ax.plot(xs, ys, marker="o", markersize=4, linewidth=1.8,
                color=COLORES[est_l], label=f"{ETIQUETA[est_l]}.{met_l}")
        eje_log(ax)
        ax.set_title(titulo, fontsize=11)
        ax.legend(fontsize=9)
        # Nota de justificacion al pie
        fig.text(0.5, -0.02, just, ha="center", va="top", fontsize=8, style="italic", wrap=True)
        fig.tight_layout()
        ruta = os.path.join(OUT_DIR, f"equiv_{i}_{met_pc}_vs_{met_l.lower()}.png")
        fig.savefig(ruta, bbox_inches="tight")
        plt.close(fig)
        generadas.append(ruta)
    return generadas


def main():
    datos = cargar()
    g1 = graficar_bloque1(datos)
    g2 = graficar_bloque2(datos)
    g3 = graficar_bloque3(datos)
    print(f"Bloque 1 (List por metodo): {len(g1)} graficas")
    print(f"Bloque 2 (Stack/Queue):     {len(g2)} graficas")
    print(f"Bloque 3 (equivalentes):    {len(g3)} graficas")
    print(f"Total: {len(g1)+len(g2)+len(g3)} graficas en {OUT_DIR}")


if __name__ == "__main__":
    main()
