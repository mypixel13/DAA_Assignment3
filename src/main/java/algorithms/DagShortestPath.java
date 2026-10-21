package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.Arrays;

public class DagShortestPath {

    private static final long INF = Long.MAX_VALUE / 4;

    // order must be a full topological order of g (see Kahn).
    // Every vertex with in-degree 0 starts at distance 0 - that is the virtual super-source.
    public static PathResult run(Graph g, int[] order, Metrics m) {
        int n = g.size();
        if (order.length != n) {
            throw new IllegalArgumentException("need a full topological order, the graph has a cycle?");
        }

        long[] dist = new long[n];
        int[] parent = new int[n];
        Arrays.fill(dist, INF);
        Arrays.fill(parent, -1);

        int[] indeg = g.inDegrees();
        for (int v = 0; v < n; v++) {
            if (indeg[v] == 0) {
                dist[v] = 0;
            }
        }

        // one pass: when we get to v, all edges into v have already been relaxed
        for (int v : order) {
            for (Edge e : g.neighbors(v)) {
                m.edgeScans++;
                m.comparisons++;
                long candidate = dist[v] + e.weight();
                if (candidate < dist[e.to()]) {
                    dist[e.to()] = candidate;
                    parent[e.to()] = v;
                }
            }
        }

        return new PathResult(dist, parent);
    }
}
