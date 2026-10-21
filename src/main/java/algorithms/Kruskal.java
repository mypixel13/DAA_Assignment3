package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.ArrayList;
import java.util.List;

public class Kruskal {

    public static MstResult run(Graph g, Metrics m) {
        int n = g.size();

        // sort a copy of the edges by weight, every comparison is counted
        List<Edge> sorted = new ArrayList<>(g.getEdges());
        sorted.sort((a, b) -> {
            m.comparisons++;
            return Integer.compare(a.weight(), b.weight());
        });

        Dsu dsu = new Dsu(n);
        List<Edge> tree = new ArrayList<>();
        long total = 0;

        for (Edge e : sorted) {
            // a spanning tree is ready, the rest of the edges can't be useful
            if (tree.size() == n - 1) {
                break;
            }
            m.edgeScans++;
            // union does find(u), find(v) inside and only merges if they differ
            if (dsu.union(e.from(), e.to())) {
                tree.add(e);
                total += e.weight();
            }
        }

        m.dsuOps = dsu.operations();
        return new MstResult(total, tree, dsu.components());
    }
}
