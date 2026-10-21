package graph;

import java.util.ArrayList;
import java.util.List;

// Adjacency list graph, vertices are 0 .. n-1. Works as directed or undirected.
// Self-loops, parallel edges and isolated vertices are allowed.
public class Graph {

    private final int n;
    private final boolean directed;
    private final List<List<Edge>> adj;
    // every edge exactly once, in the order it was added (Kruskal needs this)
    private final List<Edge> edges = new ArrayList<>();

    public Graph(int n, boolean directed) {
        if (n < 0) {
            throw new IllegalArgumentException("n can't be negative: " + n);
        }
        this.n = n;
        this.directed = directed;
        adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
    }

    public void addEdge(int u, int v, int w) {
        checkVertex(u);
        checkVertex(v);
        Edge e = new Edge(u, v, w);
        edges.add(e);
        adj.get(u).add(e);
        // undirected: also v -> u, but a self-loop only needs one entry
        if (!directed && u != v) {
            adj.get(v).add(new Edge(v, u, w));
        }
    }

    public int size() {
        return n;
    }

    public int edgeCount() {
        return edges.size();
    }

    public boolean isDirected() {
        return directed;
    }

    public List<Edge> neighbors(int v) {
        return adj.get(v);
    }

    public List<Edge> getEdges() {
        return edges;
    }

    // in-degree of every vertex, based on the adjacency lists
    public int[] inDegrees() {
        int[] deg = new int[n];
        for (int v = 0; v < n; v++) {
            for (Edge e : adj.get(v)) {
                deg[e.to()]++;
            }
        }
        return deg;
    }

    // same graph with all edges turned around (needed for Kosaraju's second pass)
    public Graph reversed() {
        Graph r = new Graph(n, directed);
        for (Edge e : edges) {
            r.addEdge(e.to(), e.from(), e.weight());
        }
        return r;
    }

    private void checkVertex(int v) {
        if (v < 0 || v >= n) {
            throw new IllegalArgumentException("vertex " + v + " is out of range 0.." + (n - 1));
        }
    }
}
