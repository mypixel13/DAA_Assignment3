### Counter / formula, sparse

| n | Kruskal dsuOps/E | Kruskal dsuOps/scans | Kruskal cmp/(E log2 E) | Prim scans/2E | Prim cmp/(E log2 E) | Kosaraju scans/(V+E) | Kahn scans/(V+E) | DAG scans/(V+E) |
|---|---|---|---|---|---|---|---|---|
| 1000 | 1.791 | 2.324 | 0.891 | 1.000 | 1.829 | 1.600 | 0.800 | 0.800 |
| 3162 | 2.019 | 2.283 | 0.907 | 1.000 | 1.865 | 1.600 | 0.800 | 0.800 |
| 10000 | 2.182 | 2.259 | 0.869 | 1.000 | 1.895 | 1.600 | 0.800 | 0.800 |
| 31623 | 2.232 | 2.252 | 0.809 | 1.000 | 1.921 | 1.600 | 0.800 | 0.800 |
| 100000 | 2.248 | 2.250 | 0.745 | 1.000 | 1.934 | 1.600 | 0.800 | 0.800 |

### Counter / formula, dense

| n | Kruskal dsuOps/E | Kruskal dsuOps/scans | Kruskal cmp/(E log2 E) | Prim scans/2E | Prim cmp/(E log2 E) | Kosaraju scans/(V+E) | Kahn scans/(V+E) | DAG scans/(V+E) |
|---|---|---|---|---|---|---|---|---|
| 1000 | 0.480 | 2.232 | 0.903 | 1.000 | 1.825 | 1.905 | 0.952 | 0.952 |
| 3162 | 0.506 | 2.219 | 0.846 | 1.000 | 1.847 | 1.905 | 0.952 | 0.952 |
| 10000 | 0.465 | 2.241 | 0.784 | 1.000 | 1.866 | 1.905 | 0.952 | 0.952 |
| 31623 | 0.654 | 2.166 | 0.720 | 1.000 | 1.878 | 1.905 | 0.952 | 0.952 |
| 100000 | 0.794 | 2.134 | 0.666 | 1.000 | 1.890 | 1.905 | 0.952 | 0.952 |

### Log-log slopes

| algorithm | density | slope of edgeScans | slope of total operations |
|---|---|---|---|
| Kruskal | sparse | 1.055 | 1.054 |
| Kruskal | dense | 1.119 | 1.022 |
| Prim | sparse | 1.000 | 1.101 |
| Prim | dense | 1.000 | 1.085 |
| Kosaraju | sparse | 1.000 | 1.000 |
| Kosaraju | dense | 1.000 | 1.000 |
| Kahn | sparse | 1.000 | 1.000 |
| Kahn | dense | 1.000 | 1.000 |
| DagShortestPath | sparse | 1.000 | 1.000 |
| DagShortestPath | dense | 1.000 | 1.000 |

### Share of edges Kruskal examines

| n | sparse | dense |
|---|---|---|
| 1000 | 0.770 | 0.215 |
| 3162 | 0.885 | 0.228 |
| 10000 | 0.966 | 0.207 |
| 31623 | 0.991 | 0.302 |
| 100000 | 0.999 | 0.372 |

### Kosaraju maxDepth

| density | n | maxDepth | maxDepth / n |
|---|---|---|---|
| sparse | 1000 | 626 | 0.626 |
| sparse | 3162 | 1871 | 0.592 |
| sparse | 10000 | 6002 | 0.600 |
| sparse | 31623 | 18822 | 0.595 |
| sparse | 100000 | 59668 | 0.597 |
| dense | 1000 | 925 | 0.925 |
| dense | 3162 | 2937 | 0.929 |
| dense | 10000 | 9217 | 0.922 |
| dense | 31623 | 29073 | 0.919 |
| dense | 100000 | 91920 | 0.919 |

### Median time (ms) at n = 100 000

| algorithm | sparse | dense |
|---|---|---|
| Kruskal | 131.4 | 665.1 |
| Prim | 341.6 | 2701.0 |
| Kosaraju | 159.8 | 920.2 |
| Kahn | 28.4 | 117.3 |
| DagShortestPath | 34.8 | 127.0 |

### Tarjan vs Kosaraju

| graph | m | Kosaraju scans | Tarjan scans | Kosaraju maxDepth | Tarjan maxDepth |
|---|---|---|---|---|---|
| 1000_sparse | 4000 | 8000 | 4000 | 626 | 626 |
| 1000_dense | 20000 | 40000 | 20000 | 925 | 916 |
| 3162_sparse | 12648 | 25296 | 12648 | 1871 | 1871 |
| 3162_dense | 63240 | 126480 | 63240 | 2937 | 2920 |
| 10000_sparse | 40000 | 80000 | 40000 | 6002 | 5925 |
| 10000_dense | 200000 | 400000 | 200000 | 9217 | 9217 |
| 31623_sparse | 126492 | 252984 | 126492 | 18822 | 18671 |
| 31623_dense | 632460 | 1264920 | 632460 | 29073 | 29073 |
| 100000_sparse | 400000 | 800000 | 400000 | 59668 | 59394 |
| 100000_dense | 2000000 | 4000000 | 2000000 | 91920 | 91920 |
