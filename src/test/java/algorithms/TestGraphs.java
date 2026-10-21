package algorithms;

import graph.Graph;

import java.util.Random;

// random graphs for the tests
class TestGraphs {

    // connected undirected graph: a random tree plus extra edges.
    // The extra edges may be self-loops or parallel edges, on purpose.
    static Graph randomConnected(Random rnd, int n, int extraEdges) {
        Graph g = new Graph(n, false);
        for (int v = 1; v < n; v++) {
            g.addEdge(rnd.nextInt(v), v, 1 + rnd.nextInt(20));
        }
        for (int i = 0; i < extraEdges; i++) {
            g.addEdge(rnd.nextInt(n), rnd.nextInt(n), 1 + rnd.nextInt(20));
        }
        return g;
    }

    // directed graph with random edges, self-loops and parallel edges allowed
    static Graph randomDirected(Random rnd, int n, int m) {
        Graph g = new Graph(n, true);
        for (int i = 0; i < m; i++) {
            g.addEdge(rnd.nextInt(n), rnd.nextInt(n), 1 + rnd.nextInt(10));
        }
        return g;
    }

    // random DAG: shuffle a rank for every vertex and point every edge from lower to higher rank
    static Graph randomDag(Random rnd, int n, int m, int minWeight, int maxWeight) {
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) {
            rank[i] = i;
        }
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = rank[i];
            rank[i] = rank[j];
            rank[j] = t;
        }
        Graph g = new Graph(n, true);
        int added = 0;
        while (added < m && n > 1) {
            int a = rnd.nextInt(n);
            int b = rnd.nextInt(n);
            if (a == b) {
                continue;
            }
            if (rank[a] > rank[b]) {
                int t = a;
                a = b;
                b = t;
            }
            g.addEdge(a, b, minWeight + rnd.nextInt(maxWeight - minWeight + 1));
            added++;
        }
        return g;
    }

    // 0 -> 1 -> 2 -> ... -> n-1
    static Graph path(int n) {
        Graph g = new Graph(n, true);
        for (int i = 0; i + 1 < n; i++) {
            g.addEdge(i, i + 1, 1);
        }
        return g;
    }
}
