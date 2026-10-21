package algorithms;

// order holds the vertices that left the queue. If the graph has a cycle
// it is shorter than n and hasCycle is true, so it must not be used as a real order.
public record TopoResult(int[] order, boolean hasCycle) {
}
