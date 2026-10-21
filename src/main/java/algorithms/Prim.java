package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Prim {

    public static MstResult run(Graph g, Metrics m) {
        int n = g.size();
        boolean[] inTree = new boolean[n];
        PriorityQueue<Edge> pq = new PriorityQueue<>((a, b) -> {
            m.comparisons++;
            return Integer.compare(a.weight(), b.weight());
        });

        List<Edge> tree = new ArrayList<>();
        long total = 0;
        int components = 0;

        // start from vertex 0; if something is left over the graph is disconnected,
        // so we start again from the first vertex that is not in the tree yet (forest)
        for (int start = 0; start < n; start++) {
            if (inTree[start]) {
                continue;
            }
            components++;
            inTree[start] = true;
            addEdges(g, start, inTree, pq, m);

            while (!pq.isEmpty()) {
                Edge e = pq.poll();
                if (inTree[e.to()]) {
                    continue;   // the other end is already in the tree
                }
                inTree[e.to()] = true;
                tree.add(e);
                total += e.weight();
                addEdges(g, e.to(), inTree, pq, m);
            }
        }

        return new MstResult(total, tree, components);
    }

    private static void addEdges(Graph g, int v, boolean[] inTree, PriorityQueue<Edge> pq, Metrics m) {
        for (Edge e : g.neighbors(v)) {
            m.edgeScans++;
            if (!inTree[e.to()]) {
                pq.add(e);
            }
        }
    }
}
