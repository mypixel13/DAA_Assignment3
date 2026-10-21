package algorithms;

import graph.Graph;
import metrics.Metrics;

public class Kosaraju {

    public static SccResult run(Graph g, Metrics m) {
        int n = g.size();

        // pass 1: DFS on the original graph, remember the order in which vertices finish
        int[] finishOrder = new int[n];
        int[] filled = {0};
        IterativeDfs first = new IterativeDfs(g, m);
        for (int v = 0; v < n; v++) {
            if (!first.isSeen(v)) {
                first.run(v, u -> finishOrder[filled[0]++] = u);
            }
        }

        // pass 2: DFS on the reversed graph, starting from the vertex that finished last.
        // Every DFS tree we get there is one strongly connected component.
        Graph rev = g.reversed();
        int[] component = new int[n];
        int[] count = {0};
        IterativeDfs second = new IterativeDfs(rev, m);
        for (int i = n - 1; i >= 0; i--) {
            int v = finishOrder[i];
            if (!second.isSeen(v)) {
                second.run(v, u -> component[u] = count[0]);
                count[0]++;
            }
        }

        return new SccResult(component, count[0]);
    }
}
