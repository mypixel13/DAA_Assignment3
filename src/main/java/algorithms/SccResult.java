package algorithms;

import java.util.HashMap;
import java.util.Map;

// component[v] is the number of the strongly connected component of v (0 .. count-1)
public record SccResult(int[] component, int count) {

    // Kosaraju and Tarjan number the components differently,
    // so to compare them we only check that they split the vertices the same way.
    public boolean samePartitionAs(SccResult other) {
        if (component.length != other.component.length || count != other.count) {
            return false;
        }
        Map<Integer, Integer> mine = new HashMap<>();
        Map<Integer, Integer> theirs = new HashMap<>();
        for (int v = 0; v < component.length; v++) {
            int a = component[v];
            int b = other.component[v];
            Integer seenB = mine.putIfAbsent(a, b);
            Integer seenA = theirs.putIfAbsent(b, a);
            if ((seenB != null && seenB != b) || (seenA != null && seenA != a)) {
                return false;
            }
        }
        return true;
    }
}
