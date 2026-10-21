package algorithms;

import graph.Graph;
import graph.GraphReader;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MstTest {

    @Test
    void kruskalAndPrimGiveTheSameWeightOnRandomConnectedGraphs() {
        Random rnd = new Random(3);
        for (int i = 0; i < 100; i++) {
            int n = 2 + rnd.nextInt(40);
            Graph g = TestGraphs.randomConnected(rnd, n, rnd.nextInt(4 * n));

            MstResult k = Kruskal.run(g, new Metrics());
            MstResult p = Prim.run(g, new Metrics());

            assertEquals(k.totalWeight(), p.totalWeight(), "graph #" + i + " with n=" + n);
            assertEquals(n - 1, k.edges().size(), "Kruskal tree size, graph #" + i);
            assertEquals(n - 1, p.edges().size(), "Prim tree size, graph #" + i);
            assertEquals(1, k.components());
            assertEquals(1, p.components());
        }
    }

    @Test
    void reportedWeightIsTheSumOfTheTreeEdges() {
        Random rnd = new Random(4);
        Graph g = TestGraphs.randomConnected(rnd, 30, 60);
        for (MstResult r : new MstResult[]{Kruskal.run(g, new Metrics()), Prim.run(g, new Metrics())}) {
            long sum = 0;
            for (var e : r.edges()) {
                sum += e.weight();
            }
            assertEquals(sum, r.totalWeight());
        }
    }

    @Test
    void connectedFileFromData() throws IOException {
        Graph g = GraphReader.read(Path.of("data/roads_connected.txt"));
        MstResult k = Kruskal.run(g, new Metrics());
        MstResult p = Prim.run(g, new Metrics());
        assertEquals(11, k.totalWeight());
        assertEquals(11, p.totalWeight());
        assertEquals(5, k.edges().size());
        assertEquals(5, p.edges().size());
    }

    @Test
    void disconnectedGraphGivesAForestAndDoesNotThrow() throws IOException {
        Graph g = GraphReader.read(Path.of("data/roads_disconnected.txt"));
        MstResult k = Kruskal.run(g, new Metrics());
        MstResult p = Prim.run(g, new Metrics());

        assertEquals(9, k.totalWeight());
        assertEquals(9, p.totalWeight());
        assertEquals(5, k.components());
        assertEquals(5, p.components());
        // forest: n - components edges
        assertEquals(3, k.edges().size());
        assertEquals(3, p.edges().size());
    }

    @Test
    void graphWithoutEdgesAndSingleVertex() {
        Graph empty = new Graph(4, false);
        assertEquals(4, Kruskal.run(empty, new Metrics()).components());
        assertEquals(4, Prim.run(empty, new Metrics()).components());
        assertEquals(0, Kruskal.run(empty, new Metrics()).totalWeight());

        Graph one = new Graph(1, false);
        one.addEdge(0, 0, 7);
        assertEquals(0, Kruskal.run(one, new Metrics()).totalWeight());
        assertEquals(0, Prim.run(one, new Metrics()).totalWeight());
        assertEquals(1, Prim.run(one, new Metrics()).components());
    }

    @Test
    void kruskalStopsWhenTheTreeIsReady() {
        // path 0-1-2 with cheap edges and a long list of expensive extra edges
        Graph g = new Graph(3, false);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        for (int i = 0; i < 50; i++) {
            g.addEdge(0, 2, 100 + i);
        }
        Metrics m = new Metrics();
        Kruskal.run(g, m);
        assertEquals(2, m.edgeScans);
        assertTrue(m.dsuOps > 0);
        assertTrue(m.comparisons > 0);
    }
}
