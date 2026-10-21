package algorithms;

import graph.Graph;
import graph.GraphReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphTest {

    @Test
    void readsEveryFileInData() throws IOException {
        Graph a = GraphReader.read(Path.of("data/roads_connected.txt"));
        assertEquals(6, a.size());
        assertEquals(10, a.edgeCount());
        assertFalse(a.isDirected());

        Graph b = GraphReader.read(Path.of("data/roads_disconnected.txt"));
        assertEquals(8, b.size());
        assertEquals(6, b.edgeCount());

        Graph c = GraphReader.read(Path.of("data/jobs_dag.txt"));
        assertEquals(7, c.size());
        assertTrue(c.isDirected());

        Graph d = GraphReader.read(Path.of("data/jobs_cyclic.txt"));
        assertEquals(8, d.size());
        assertEquals(10, d.edgeCount());
    }

    @Test
    void undirectedEdgeIsStoredInBothLists() {
        Graph g = new Graph(3, false);
        g.addEdge(0, 1, 5);
        assertEquals(1, g.neighbors(0).size());
        assertEquals(1, g.neighbors(1).size());
        assertEquals(0, g.neighbors(2).size());
        assertEquals(1, g.edgeCount());
    }

    @Test
    void directedEdgeIsStoredOnce() {
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 5);
        assertEquals(1, g.neighbors(0).size());
        assertEquals(0, g.neighbors(1).size());
    }

    @Test
    void selfLoopsAndParallelEdgesAreAllowed() {
        Graph g = new Graph(2, false);
        g.addEdge(0, 0, 1);
        g.addEdge(0, 1, 2);
        g.addEdge(0, 1, 3);
        assertEquals(3, g.edgeCount());
        assertEquals(3, g.neighbors(0).size());
        assertEquals(2, g.neighbors(1).size());
    }

    @Test
    void inDegreesAndReverse() {
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 1);
        g.addEdge(0, 2, 1);
        g.addEdge(1, 2, 1);
        assertArrayEquals(new int[]{0, 1, 2}, g.inDegrees());
        assertArrayEquals(new int[]{2, 1, 0}, g.reversed().inDegrees());
    }

    @Test
    void badVertexIsRejected() {
        Graph g = new Graph(3, true);
        assertThrows(IllegalArgumentException.class, () -> g.addEdge(0, 3, 1));
        assertThrows(IllegalArgumentException.class, () -> g.addEdge(-1, 0, 1));
    }

    @Test
    void parserIgnoresCommentsAndBlankLines() throws IOException {
        Graph g = GraphReader.parse("# a comment\n\n3 2 directed\n0 1 4\n\n# another\n1 2 -7\n");
        assertEquals(3, g.size());
        assertEquals(2, g.edgeCount());
        assertEquals(-7, g.getEdges().get(1).weight());
    }

    @Test
    void parserComplainsAboutBrokenInput() {
        assertThrows(IllegalArgumentException.class, () -> GraphReader.parse("3 1 sideways\n0 1 1\n"));
        assertThrows(IllegalArgumentException.class, () -> GraphReader.parse("3 2 directed\n0 1 1\n"));
        assertThrows(IllegalArgumentException.class, () -> GraphReader.parse("3 1 directed\n0 1\n"));
        assertThrows(IllegalArgumentException.class, () -> GraphReader.parse(""));
    }
}
