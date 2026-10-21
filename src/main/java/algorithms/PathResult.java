package algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Result of the shortest path run in a DAG.
// parent[v] is the previous vertex on the best path to v, -1 for a start vertex.
public class PathResult {

    private final long[] dist;
    private final int[] parent;

    public PathResult(long[] dist, int[] parent) {
        this.dist = dist;
        this.parent = parent;
    }

    public long[] distances() {
        return dist;
    }

    public int[] parents() {
        return parent;
    }

    public long distanceTo(int v) {
        return dist[v];
    }

    public long sumOfDistances() {
        long sum = 0;
        for (long d : dist) {
            sum += d;
        }
        return sum;
    }

    // the actual path, from a start vertex to v
    public List<Integer> pathTo(int v) {
        List<Integer> path = new ArrayList<>();
        for (int cur = v; cur != -1; cur = parent[cur]) {
            path.add(cur);
        }
        Collections.reverse(path);
        return path;
    }
}
