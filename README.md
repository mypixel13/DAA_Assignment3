# DAA Assignment 3 – Graphs: DSU, MST, SCC and Shortest Paths

Own graph class (adjacency list, directed or undirected), DSU, Kruskal, Prim, Kosaraju, Kahn and
shortest paths in a DAG, with operation counters, a self-checking benchmark and JUnit 5 tests.
Bonus: Tarjan's SCC, condensation and critical path.

GitHub: `(https://github.com/mypixel13/DAA_Assignment3.git)` (branch `main`, tag `v1.0`)

## Layout

```
src/main/java/graph/        Graph, Edge, GraphReader
src/main/java/algorithms/   Dsu, Kruskal, Prim, Kosaraju, Kahn, DagShortestPath,
                            Tarjan, Condensation, CriticalPath (+ result classes, IterativeDfs)
src/main/java/metrics/      Metrics (dsuOps, edgeScans, comparisons, maxDepth)
src/main/java/benchmark/    GraphGenerator, Benchmark, Demo
src/test/java/algorithms/   JUnit 5 tests
data/                       small input graphs (connected, disconnected, DAG, cyclic)
results/results.csv         50 rows: 5 algorithms x 10 graphs
results/plots/              the three PNG plots
scripts/analyze.py          makes the plots and the tables used in REPORT.md
REPORT.md                   report
```

## Build and test

Needs JDK 17 or newer and Maven.

```
mvn test
```

The 100 000 vertex tests need some memory, the pom gives the test JVM `-Xmx2g`.

## Run the benchmark

One command (compiles, runs all ten graphs, checks everything, writes the CSV, draws the plots):

```
./run_benchmark.sh
```

or step by step:

```
mvn -q compile
java -Xmx4g -cp target/classes benchmark.Benchmark      # writes results/results.csv
python3 scripts/analyze.py                              # needs matplotlib and numpy
```

The benchmark stops with an exception if Kruskal and Prim give different weights, if a topological
order is invalid, if Kosaraju does not scan exactly 2m edges (Kahn and the DAG relaxation exactly m),
or if a result differs from the reference table in the assignment. It runs for about 40 seconds.
Wall-clock time is the median of 5 runs, but the conclusions in the report use the counters.

Small demo on the files in `data/`:

```
java -cp target/classes benchmark.Demo
```

## Input format

```
n m directed|undirected
u v w        (m lines)
```

Empty lines and lines starting with `#` are ignored.

## Notes on the counters

* `dsuOps` = `find` calls + successful `union` calls. `union` calls `find` twice inside, these are counted.
* `edgeScans` = edges looked at: Kruskal counts the sorted edges it examines (it stops when the tree
  has n-1 edges), Prim every adjacency entry, Kosaraju both DFS passes (2m), Kahn and the DAG relaxation m.
  Computing in-degrees and building the reversed graph is preparation and is not counted.
* `comparisons` = weight comparisons in the sort (Kruskal), in the priority queue (Prim) and in the relaxation.
* `maxDepth` = deepest the explicit DFS stack got (Kosaraju; Tarjan in the bonus file).

## AI use acknowledgement

<!-- edit this paragraph so that it is true for you -->
I used an AI assistant (Claude) while working on this assignment: it generated the first version of the
code, the tests and the report text. I read and ran the code, and I can explain every class at the defense.
