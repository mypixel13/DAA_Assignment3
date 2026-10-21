package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.List;
import java.util.function.IntConsumer;

// DFS with our own stack instead of recursion, so 100 000 vertices in a row
// can't blow the JVM stack. One object is used for the whole pass over the graph:
// the "seen" array is shared between all start vertices.
class IterativeDfs {

    private final Graph g;
    private final Metrics m;
    private final boolean[] seen;
    private final int[] stack;      // every vertex is pushed at most once, so n is enough
    private final int[] nextEdge;   // how many edges of v we already looked at

    IterativeDfs(Graph g, Metrics m) {
        this.g = g;
        this.m = m;
        int n = g.size();
        seen = new boolean[n];
        stack = new int[n];
        nextEdge = new int[n];
    }

    boolean isSeen(int v) {
        return seen[v];
    }

    // explores everything reachable from start that was not seen yet,
    // onFinish is called for a vertex when all its edges are done
    void run(int start, IntConsumer onFinish) {
        int top = 0;
        stack[top++] = start;
        seen[start] = true;
        m.seeDepth(top);

        while (top > 0) {
            int v = stack[top - 1];
            List<Edge> out = g.neighbors(v);
            if (nextEdge[v] < out.size()) {
                Edge e = out.get(nextEdge[v]);
                nextEdge[v]++;
                m.edgeScans++;
                if (!seen[e.to()]) {
                    seen[e.to()] = true;
                    stack[top++] = e.to();
                    m.seeDepth(top);
                }
            } else {
                top--;
                onFinish.accept(v);
            }
        }
    }
}
