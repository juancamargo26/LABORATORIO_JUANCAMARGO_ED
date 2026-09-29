#!/usr/bin/env bash
# Compila todo, ejecuta las pruebas de correctitud, corre el benchmark y
# regenera las graficas. Uso: ./ejecutar.sh
set -e
cd "$(dirname "$0")"

echo "==> Compilando..."
mkdir -p bin
javac -d bin src/estructuras/lista/*.java src/estructuras/pilacola/*.java src/benchmark/*.java

echo "==> Pruebas de correctitud..."
java -cp bin benchmark.Correctitud

echo "==> Benchmark (esto puede tardar ~1 minuto)..."
mkdir -p resultados
java -Xmx3g -cp bin benchmark.Benchmark > resultados/resultados.csv
echo "    CSV -> resultados/resultados.csv ($(wc -l < resultados/resultados.csv) filas)"

if command -v python3 >/dev/null 2>&1; then
  echo "==> Generando graficas..."
  python3 scripts/graficar.py || echo "    (matplotlib no disponible; se omiten las graficas)"
else
  echo "==> Python no disponible; se omiten las graficas."
fi

echo "==> Listo."
