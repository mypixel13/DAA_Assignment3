package algorithms;

import graph.Edge;
import graph.Graph;
import graph.GraphReader;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TopoOrderTest {

    @Test
    void validOrderOnRandomDags() {
        Random rnd = new Random(8);
        for (int i = 0; i < 50; i++) {
            int n = 1 + rnd.nextInt(100);
            Graph g = TestGraphs.randomDag(rnd, n, rnd.nextInt(4 * n), 1, 10);

            TopoResult t = Kahn.run(g, new Metrics());
            assertFalse(t.hasCycle(), "a DAG must not report a cycle");
            assertEquals(n, t.order().length);
            assertTrue(Kahn.isValidOrder(g, t.order()), "pos[u] < pos[v] must hold for every edge, n=" + n);
        }
    }

    @Test
    void cycleIsDetectedInsteadOfReturningAWrongOrder() throws IOException {
        Graph g = GraphReader.read(Path.of("data/jobs_cyclic.txt"));
        TopoResult t = Kahn.run(g, new Metrics());
        assertTrue(t.hasCycle());
        assertTrue(t.order().length < g.size());
        assertFalse(Kahn.isValidOrder(g, t.order()));
    }

    @Test
    void selfLoopIsACycle() {
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 1, 1);
        g.addEdge(1, 2, 1);
        TopoResult t = Kahn.run(g, new Metrics());
        assertTrue(t.hasCycle());
        assertEquals(1, t.order().length);   // only vertex 0 can leave the queue
    }

    @Test
    void cycleAddedToARandomDagIsFound() {
        Random rnd = new Random(9);
        for (int i = 0; i < 30; i++) {
            int n = 5 + rnd.nextInt(50);
            Graph g = TestGraphs.randomDag(rnd, n, 3 * n, 1, 10);
            // an edge u -> v plus the edge v -> u is a cycle for sure
            Edge first = g.getEdges().get(0);
            g.addEdge(first.to(), first.from(), 1);

            TopoResult t = Kahn.run(g, new Metrics());
            assertTrue(t.hasCycle(), "n=" + n);
            assertTrue(t.order().length < n);
        }
    }

    @Test
    void validatorRejectsBadOrders() {
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        assertTrue(Kahn.isValidOrder(g, new int[]{0, 1, 2}));
        assertFalse(Kahn.isValidOrder(g, new int[]{1, 0, 2}));   // edge 0 -> 1 goes backwards
        assertFalse(Kahn.isValidOrder(g, new int[]{0, 1}));      // too short
        assertFalse(Kahn.isValidOrder(g, new int[]{0, 1, 1}));   // vertex twice
    }

    @Test
    void edgeScansAreExactlyM() {
        Random rnd = new Random(10);
        Graph g = TestGraphs.randomDag(rnd, 300, 1500, 1, 10);
        Metrics m = new Metrics();
        Kahn.run(g, m);
        assertEquals(g.edgeCount(), m.edgeScans);
    }

    @Test
    void emptyGraph() {
        TopoResult t = Kahn.run(new Graph(0, true), new Metrics());
        assertFalse(t.hasCycle());
        assertEquals(0, t.order().length);
    }
}
