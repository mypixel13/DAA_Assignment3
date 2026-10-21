package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.Arrays;

public class Kahn {

    public static TopoResult run(Graph g, Metrics m) {
        int n = g.size();
        int[] indeg = g.inDegrees();

        // plain array as a queue: every vertex enters it at most once
        int[] queue = new int[n];
        int head = 0;
        int tail = 0;
        for (int v = 0; v < n; v++) {
            if (indeg[v] == 0) {
                queue[tail++] = v;
            }
        }

        while (head < tail) {
            int v = queue[head++];
            for (Edge e : g.neighbors(v)) {
                m.edgeScans++;
                indeg[e.to()]--;
                if (indeg[e.to()] == 0) {
                    queue[tail++] = e.to();
                }
            }
        }

        // fewer than n vertices came out -> the rest sit on a cycle
        boolean cycle = head < n;
        return new TopoResult(Arrays.copyOf(queue, head), cycle);
    }

    // pos[u] < pos[v] must hold for every edge u -> v, and every vertex appears exactly once
    public static boolean isValidOrder(Graph g, int[] order) {
        int n = g.size();
        if (order.length != n) {
            return false;
        }
        int[] pos = new int[n];
        Arrays.fill(pos, -1);
        for (int i = 0; i < n; i++) {
            if (order[i] < 0 || order[i] >= n || pos[order[i]] != -1) {
                return false;
            }
            pos[order[i]] = i;
        }
        for (Edge e : g.getEdges()) {
            if (pos[e.from()] >= pos[e.to()]) {
                return false;
            }
        }
        return true;
    }
}
