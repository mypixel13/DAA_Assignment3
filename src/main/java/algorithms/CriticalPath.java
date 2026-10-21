package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.List;

// Longest path in the condensation of a graph (bonus task B).
// Trick: flip the sign of every weight and look for the shortest path with the DAG algorithm.
public class CriticalPath {

    public record Result(long length, List<Integer> components) {
    }

    public static Result run(Graph g) {
        Metrics ignored = new Metrics();
        SccResult scc = Kosaraju.run(g, ignored);
        Graph dag = Condensation.build(g, scc);

        // same DAG with negated weights
        Graph negated = new Graph(dag.size(), true);
        for (Edge e : dag.getEdges()) {
            negated.addEdge(e.from(), e.to(), -e.weight());
        }

        TopoResult topo = Kahn.run(negated, ignored);
        PathResult paths = DagShortestPath.run(negated, topo.order(), ignored);

        if (negated.size() == 0) {
            return new Result(0, List.of());
        }

        // the smallest (most negative) distance is the longest path
        int end = 0;
        for (int v = 1; v < negated.size(); v++) {
            if (paths.distanceTo(v) < paths.distanceTo(end)) {
                end = v;
            }
        }
        return new Result(-paths.distanceTo(end), paths.pathTo(end));
    }
}
