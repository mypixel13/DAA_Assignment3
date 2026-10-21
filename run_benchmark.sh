#!/bin/sh
# compile, run the self-checking benchmark, then draw the plots
set -e
mvn -q compile
java -Xmx4g -cp target/classes benchmark.Benchmark results/results.csv
python3 scripts/analyze.py > /dev/null
echo "done: results/results.csv, results/plots/*.png, results/analysis.md"
