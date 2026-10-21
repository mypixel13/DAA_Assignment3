package algorithms;

import graph.Edge;

import java.util.List;

// For a disconnected graph this is a minimum spanning forest,
// then edges.size() == n - components.
public record MstResult(long totalWeight, List<Edge> edges, int components) {
}
