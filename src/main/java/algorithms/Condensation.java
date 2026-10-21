package algorithms;

import graph.Edge;
import graph.Graph;

// Bonus task B: every SCC becomes one vertex. Edges inside a component disappear,
// edges between components stay (parallel ones are kept too). The result is always a DAG.
public class Condensation {

    public static Graph build(Graph g, SccResult scc) {
        Graph dag = new Graph(scc.count(), true);
        for (Edge e : g.getEdges()) {
            int a = scc.component()[e.from()];
            int b = scc.component()[e.to()];
            if (a != b) {
                dag.addEdge(a, b, e.weight());
            }
        }
        return dag;
    }
}
