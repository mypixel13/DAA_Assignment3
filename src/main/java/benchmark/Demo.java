package benchmark;

import algorithms.CriticalPath;
import algorithms.DagShortestPath;
import algorithms.Kahn;
import algorithms.Kosaraju;
import algorithms.Kruskal;
import algorithms.MstResult;
import algorithms.PathResult;
import algorithms.Prim;
import algorithms.SccResult;
import algorithms.TopoResult;
import graph.Graph;
import graph.GraphReader;
import metrics.Metrics;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

// Small demo on the files from data/: run every algorithm and print what it found.
public class Demo {

    public static void main(String[] args) throws IOException {
        mst("data/roads_connected.txt");
        mst("data/roads_disconnected.txt");
        jobs("data/jobs_dag.txt");
        jobs("data/jobs_cyclic.txt");
    }

    private static void mst(String file) throws IOException {
        Graph g = GraphReader.read(Path.of(file));
        System.out.println("== " + file + " (n=" + g.size() + ", m=" + g.edgeCount() + ")");

        Metrics km = new Metrics();
        MstResult k = Kruskal.run(g, km);
        System.out.println("Kruskal: weight " + k.totalWeight() + ", " + k.edges().size()
                + " edges, " + k.components() + " component(s)   " + km);

        Metrics pm = new Metrics();
        MstResult p = Prim.run(g, pm);
        System.out.println("Prim:    weight " + p.totalWeight() + ", " + p.edges().size()
                + " edges, " + p.components() + " component(s)   " + pm);
        System.out.println();
    }

    private static void jobs(String file) throws IOException {
        Graph g = GraphReader.read(Path.of(file));
        System.out.println("== " + file + " (n=" + g.size() + ", m=" + g.edgeCount() + ")");

        Metrics sm = new Metrics();
        SccResult scc = Kosaraju.run(g, sm);
        System.out.println("Kosaraju: " + scc.count() + " components, component[] = "
                + Arrays.toString(scc.component()) + "   " + sm);

        Metrics km = new Metrics();
        TopoResult topo = Kahn.run(g, km);
        if (topo.hasCycle()) {
            System.out.println("Kahn: cycle found, only " + topo.order().length + " of " + g.size()
                    + " vertices could be ordered");
            CriticalPath.Result cp = CriticalPath.run(g);
            System.out.println("Critical path in the condensation: length " + cp.length()
                    + ", components " + cp.components());
        } else {
            System.out.println("Kahn: order " + Arrays.toString(topo.order()) + ", valid = "
                    + Kahn.isValidOrder(g, topo.order()));
            Metrics dm = new Metrics();
            PathResult paths = DagShortestPath.run(g, topo.order(), dm);
            System.out.println("Distances: " + Arrays.toString(paths.distances()) + "   " + dm);
            for (int v = 0; v < g.size(); v++) {
                System.out.println("  path to " + v + ": " + paths.pathTo(v) + " (length " + paths.distanceTo(v) + ")");
            }
        }
        System.out.println();
    }
}
