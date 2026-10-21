package metrics;

// Operation counters. The algorithms increment these while they work,
// nothing here is computed from a formula afterwards.
public class Metrics {

    public long dsuOps;        // find calls + successful unions
    public long edgeScans;     // edges looked at
    public long comparisons;   // weights compared (sorting, priority queue, relaxation)
    public int maxDepth;       // deepest the explicit DFS stack ever got

    public void reset() {
        dsuOps = 0;
        edgeScans = 0;
        comparisons = 0;
        maxDepth = 0;
    }

    public void seeDepth(int depth) {
        if (depth > maxDepth) {
            maxDepth = depth;
        }
    }

    @Override
    public String toString() {
        return "dsuOps=" + dsuOps + " edgeScans=" + edgeScans
                + " comparisons=" + comparisons + " maxDepth=" + maxDepth;
    }
}
