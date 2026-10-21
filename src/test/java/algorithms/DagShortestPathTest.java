package algorithms;

import graph.Edge;
import graph.Graph;
import graph.GraphReader;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DagShortestPathTest {

    private static final long INF = Long.MAX_VALUE / 4;

    // slow reference: Bellman-Ford with the same start rule (in-degree 0 -> distance 0)
    private static long[] bellmanFord(Graph g) {
        int n = g.size();
        long[] dist = new long[n];
        Arrays.fill(dist, INF);
        int[] indeg = g.inDegrees();
        for (int v = 0; v < n; v++) {
            if (indeg[v] == 0) {
                dist[v] = 0;
            }
        }
        for (int round = 0; round < n - 1; round++) {
            boolean changed = false;
            for (Edge e : g.getEdges()) {
                if (dist[e.from()] != INF && dist[e.from()] + e.weight() < dist[e.to()]) {
                    dist[e.to()] = dist[e.from()] + e.weight();
                    changed = true;
                }
            }
            if (!changed) {
                break;
            }
        }
        return dist;
    }

    private static PathResult shortestPaths(Graph g) {
        TopoResult topo = Kahn.run(g, new Metrics());
        return DagShortestPath.run(g, topo.order(), new Metrics());
    }

    @Test
    void matchesBellmanFordOnRandomDagsWithNegativeWeights() {
        Random rnd = new Random(11);
        for (int i = 0; i < 100; i++) {
            int n = 2 + rnd.nextInt(40);
            Graph g = TestGraphs.randomDag(rnd, n, rnd.nextInt(4 * n), -10, 10);
            assertArrayEquals(bellmanFord(g), shortestPaths(g).distances(), "graph #" + i + ", n=" + n);
        }
    }

    @Test
    void matchesBellmanFordOnlyPositiveWeights() {
        Random rnd = new Random(12);
        for (int i = 0; i < 30; i++) {
            Graph g = TestGraphs.randomDag(rnd, 30, 90, 1, 100);
            assertArrayEquals(bellmanFord(g), shortestPaths(g).distances());
        }
    }

    @Test
    void fileWithNegativeEdge() throws IOException {
        Graph g = GraphReader.read(Path.of("data/jobs_dag.txt"));
        PathResult r = shortestPaths(g);
        assertArrayEquals(new long[]{0, 2, 4, -1, 4, 8, 7}, r.distances());
        assertEquals(24, r.sumOfDistances());
    }

    @Test
    void pathIsRestoredFromParents() throws IOException {
        Graph g = GraphReader.read(Path.of("data/jobs_dag.txt"));
        PathResult r = shortestPaths(g);
        assertEquals(List.of(0, 1, 3, 4), r.pathTo(4));
        assertEquals(List.of(0), r.pathTo(0));
        assertEquals(List.of(0, 2, 6), r.pathTo(6));
    }

    @Test
    void pathWeightEqualsDistanceOnRandomDags() {
        Random rnd = new Random(13);
        for (int i = 0; i < 30; i++) {
            Graph g = TestGraphs.randomDag(rnd, 40, 120, -5, 10);
            PathResult r = shortestPaths(g);
            for (int v = 0; v < g.size(); v++) {
                List<Integer> path = r.pathTo(v);
                long sum = 0;
                for (int k = 0; k + 1 < path.size(); k++) {
                    sum += cheapestEdge(g, path.get(k), path.get(k + 1));
                }
                assertEquals(r.distanceTo(v), sum, "vertex " + v);
            }
        }
    }

    // parallel edges are possible, the algorithm uses the cheapest one
    private static int cheapestEdge(Graph g, int from, int to) {
        int best = Integer.MAX_VALUE;
        for (Edge e : g.neighbors(from)) {
            if (e.to() == to) {
                best = Math.min(best, e.weight());
            }
        }
        return best;
    }

    @Test
    void severalSourcesAllStartAtZero() {
        // two sources 0 and 1, both lead to 2
        Graph g = new Graph(3, true);
        g.addEdge(0, 2, 5);
        g.addEdge(1, 2, -4);
        PathResult r = shortestPaths(g);
        assertArrayEquals(new long[]{0, 0, -4}, r.distances());
        assertEquals(List.of(1, 2), r.pathTo(2));
    }

    @Test
    void isolatedVerticesGetDistanceZero() {
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 3);
        PathResult r = shortestPaths(g);
        assertArrayEquals(new long[]{0, 3, 0, 0}, r.distances());
    }

    @Test
    void relaxationScansEachEdgeOnce() {
        Random rnd = new Random(14);
        Graph g = TestGraphs.randomDag(rnd, 200, 900, -3, 9);
        Metrics m = new Metrics();
        DagShortestPath.run(g, Kahn.run(g, new Metrics()).order(), m);
        assertEquals(g.edgeCount(), m.edgeScans);
        assertEquals(g.edgeCount(), m.comparisons);
    }

    @Test
    void incompleteOrderIsRejected() throws IOException {
        Graph g = GraphReader.read(Path.of("data/jobs_cyclic.txt"));
        TopoResult t = Kahn.run(g, new Metrics());
        assertTrue(t.hasCycle());
        assertThrows(IllegalArgumentException.class, () -> DagShortestPath.run(g, t.order(), new Metrics()));
    }

    @Test
    void criticalPathOfCyclicFile() throws IOException {
        Graph g = GraphReader.read(Path.of("data/jobs_cyclic.txt"));
        CriticalPath.Result r = CriticalPath.run(g);
        assertEquals(3, r.length());
        assertEquals(4, r.components().size());   // A -> B -> C -> D
    }

    @Test
    void condensationIsAlwaysADag() {
        Random rnd = new Random(15);
        for (int i = 0; i < 30; i++) {
            int n = 2 + rnd.nextInt(100);
            Graph g = TestGraphs.randomDirected(rnd, n, 2 * n);
            SccResult scc = Kosaraju.run(g, new Metrics());
            Graph dag = Condensation.build(g, scc);
            assertEquals(scc.count(), dag.size());
            assertFalse(Kahn.run(dag, new Metrics()).hasCycle(), "condensation must not have a cycle");
        }
    }
}
