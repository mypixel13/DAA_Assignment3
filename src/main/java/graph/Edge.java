package graph;

// One edge u -> v with a weight. For undirected graphs the same record is used,
// the graph just stores it in both adjacency lists.
public record Edge(int from, int to, int weight) {
}
