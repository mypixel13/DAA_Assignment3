package benchmark;

import graph.Graph;

import java.util.Random;

// The exact generator from the assignment. Always a fresh Random(42),
// so the same (n, m) gives the same graph on every machine.
public class GraphGenerator {

    private static final long SEED = 42;

    // random spanning tree first (so the graph is connected), then random extra edges up to m
    public static Graph undirected(int n, int m) {
        Random rnd = new Random(SEED);
        int[] perm = shuffled(n, rnd);
        Graph g = new Graph(n, false);
        for (int i = 1; i < n; i++) {
            g.addEdge(perm[rnd.nextInt(i)], perm[i], weight(rnd));
        }
        while (g.edgeCount() < m) {
            int a = rnd.nextInt(n);
            int b = otherVertex(rnd, n, a);
            g.addEdge(a, b, weight(rnd));
        }
        return g;
    }

    // only random edges, no spanning tree, so there are many components
    public static Graph directed(int n, int m) {
        Random rnd = new Random(SEED);
        Graph g = new Graph(n, true);
        for (int i = 0; i < m; i++) {
            int a = rnd.nextInt(n);
            int b = otherVertex(rnd, n, a);
            g.addEdge(a, b, weight(rnd));
        }
        return g;
    }

    // random edges, but every edge is turned to point from the lower rank to the higher rank
    public static Graph dag(int n, int m) {
        Random rnd = new Random(SEED);
        int[] rank = shuffled(n, rnd);
        Graph g = new Graph(n, true);
        for (int i = 0; i < m; i++) {
            int a = rnd.nextInt(n);
            int b = otherVertex(rnd, n, a);
            if (rank[a] > rank[b]) {
                int t = a;
                a = b;
                b = t;
            }
            g.addEdge(a, b, weight(rnd));
        }
        return g;
    }

    private static int[] shuffled(int n, Random rnd) {
        int[] perm = new int[n];
        for (int i = 0; i < n; i++) {
            perm[i] = i;
        }
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = perm[i];
            perm[i] = perm[j];
            perm[j] = t;
        }
        return perm;
    }

    // a random vertex different from a
    private static int otherVertex(Random rnd, int n, int a) {
        int b = rnd.nextInt(n);
        while (b == a) {
            b = rnd.nextInt(n);
        }
        return b;
    }

    private static int weight(Random rnd) {
        return 1 + rnd.nextInt(1000);
    }
}
