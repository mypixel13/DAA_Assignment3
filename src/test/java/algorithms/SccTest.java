package algorithms;

import graph.Edge;
import graph.Graph;
import graph.GraphReader;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SccTest {

    // BFS from s, which vertices can we reach
    private static boolean[] reachable(Graph g, int s) {
        boolean[] seen = new boolean[g.size()];
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        seen[s] = true;
        queue.add(s);
        while (!queue.isEmpty()) {
            int v = queue.poll();
            for (Edge e : g.neighbors(v)) {
                if (!seen[e.to()]) {
                    seen[e.to()] = true;
                    queue.add(e.to());
                }
            }
        }
        return seen;
    }

    // u and v are in one component exactly when each can reach the other
    private static void compareWithBruteForce(Graph g, SccResult result, String what) {
        int n = g.size();
        Graph rev = g.reversed();
        boolean[][] forward = new boolean[n][];
        boolean[][] backward = new boolean[n][];
        for (int v = 0; v < n; v++) {
            forward[v] = reachable(g, v);
            backward[v] = reachable(rev, v);
        }

        int[] comp = result.component();
        for (int u = 0; u < n; u++) {
            for (int v = 0; v < n; v++) {
                boolean together = forward[u][v] && backward[u][v];
                assertEquals(together, comp[u] == comp[v], what + ": vertices " + u + " and " + v);
            }
        }
    }

    @Test
    void kosarajuMatchesBruteForceOnRandomGraphs() {
        Random rnd = new Random(5);
        for (int i = 0; i < 60; i++) {
            int n = 1 + rnd.nextInt(200);
            int m = rnd.nextInt(3 * n);
            Graph g = TestGraphs.randomDirected(rnd, n, m);
            compareWithBruteForce(g, Kosaraju.run(g, new Metrics()), "Kosaraju, n=" + n + " m=" + m);
        }
    }

    @Test
    void tarjanMatchesBruteForceAndKosaraju() {
        Random rnd = new Random(6);
        for (int i = 0; i < 60; i++) {
            int n = 1 + rnd.nextInt(200);
            int m = rnd.nextInt(3 * n);
            Graph g = TestGraphs.randomDirected(rnd, n, m);
            SccResult t = Tarjan.run(g, new Metrics());
            compareWithBruteForce(g, t, "Tarjan, n=" + n + " m=" + m);
            assertTrue(t.samePartitionAs(Kosaraju.run(g, new Metrics())));
        }
    }

    @Test
    void cyclicFileHasFourComponents() throws IOException {
        Graph g = GraphReader.read(Path.of("data/jobs_cyclic.txt"));
        SccResult k = Kosaraju.run(g, new Metrics());
        assertEquals(4, k.count());
        int[] c = k.component();
        assertEquals(c[0], c[1]);
        assertEquals(c[1], c[2]);
        assertEquals(c[3], c[4]);
        assertEquals(c[4], c[5]);
        assertTrue(c[0] != c[3] && c[3] != c[6] && c[6] != c[7]);
        assertTrue(Tarjan.run(g, new Metrics()).samePartitionAs(k));
    }

    @Test
    void dagHasOneComponentPerVertex() throws IOException {
        Graph g = GraphReader.read(Path.of("data/jobs_dag.txt"));
        assertEquals(g.size(), Kosaraju.run(g, new Metrics()).count());
    }

    @Test
    void emptyGraphAndIsolatedVertices() {
        assertEquals(0, Kosaraju.run(new Graph(0, true), new Metrics()).count());
        assertEquals(5, Kosaraju.run(new Graph(5, true), new Metrics()).count());
        assertEquals(5, Tarjan.run(new Graph(5, true), new Metrics()).count());
    }

    @Test
    void stackSafetyOnPathOf100000Vertices() {
        int n = 100_000;
        Graph g = TestGraphs.path(n);

        Metrics km = new Metrics();
        SccResult k = Kosaraju.run(g, km);   // a recursive DFS would throw StackOverflowError here
        assertEquals(n, k.count());
        assertEquals(n, km.maxDepth);        // the DFS really went all the way down
        assertEquals(2L * (n - 1), km.edgeScans);

        Metrics tm = new Metrics();
        assertEquals(n, Tarjan.run(g, tm).count());
        assertEquals(n, tm.maxDepth);
    }

    @Test
    void stackSafetyOnOneBigCycle() {
        int n = 100_000;
        Graph g = TestGraphs.path(n);
        g.addEdge(n - 1, 0, 1);
        assertEquals(1, Kosaraju.run(g, new Metrics()).count());
        assertEquals(1, Tarjan.run(g, new Metrics()).count());
    }

    @Test
    void edgeScansAreTwoMForKosarajuAndMForTarjan() {
        Random rnd = new Random(7);
        Graph g = TestGraphs.randomDirected(rnd, 500, 2000);
        Metrics km = new Metrics();
        Metrics tm = new Metrics();
        Kosaraju.run(g, km);
        Tarjan.run(g, tm);
        assertEquals(2L * g.edgeCount(), km.edgeScans);
        assertEquals(g.edgeCount(), tm.edgeScans);
    }
}
