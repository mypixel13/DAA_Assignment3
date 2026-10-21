package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.Arrays;
import java.util.List;

// Bonus task A: strongly connected components in one DFS pass.
// Iterative as well, the recursion would die on a long path.
public class Tarjan {

    public static SccResult run(Graph g, Metrics m) {
        int n = g.size();
        int[] index = new int[n];
        int[] low = new int[n];
        int[] component = new int[n];
        boolean[] onStack = new boolean[n];
        Arrays.fill(index, -1);

        int[] sccStack = new int[n];   // vertices that are not assigned to a component yet
        int sccTop = 0;
        int[] callStack = new int[n];  // the DFS path
        int[] nextEdge = new int[n];

        int counter = 0;
        int count = 0;

        for (int s = 0; s < n; s++) {
            if (index[s] != -1) {
                continue;
            }
            int top = 0;
            callStack[top++] = s;
            index[s] = low[s] = counter++;
            sccStack[sccTop++] = s;
            onStack[s] = true;
            m.seeDepth(top);

            while (top > 0) {
                int v = callStack[top - 1];
                List<Edge> out = g.neighbors(v);

                if (nextEdge[v] < out.size()) {
                    int w = out.get(nextEdge[v]).to();
                    nextEdge[v]++;
                    m.edgeScans++;
                    if (index[w] == -1) {
                        index[w] = low[w] = counter++;
                        sccStack[sccTop++] = w;
                        onStack[w] = true;
                        callStack[top++] = w;
                        m.seeDepth(top);
                    } else if (onStack[w]) {
                        low[v] = Math.min(low[v], index[w]);
                    }
                } else {
                    // v is done. If it is the root of a component, pop the component off.
                    if (low[v] == index[v]) {
                        int w;
                        do {
                            w = sccStack[--sccTop];
                            onStack[w] = false;
                            component[w] = count;
                        } while (w != v);
                        count++;
                    }
                    top--;
                    if (top > 0) {
                        int parent = callStack[top - 1];
                        low[parent] = Math.min(low[parent], low[v]);
                    }
                }
            }
        }

        return new SccResult(component, count);
    }
}
