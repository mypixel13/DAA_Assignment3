package algorithms;

// Disjoint set union with union by rank and path compression.
public class Dsu {

    private int[] parent;
    private int[] rank;
    private int sets;

    private long findCalls;
    private long unionCalls;   // only the successful ones, where two sets really merged

    public Dsu(int n) {
        makeSet(n);
    }

    // (re)starts with n separate one-element sets 0 .. n-1
    public void makeSet(int n) {
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
        sets = n;
        findCalls = 0;
        unionCalls = 0;
    }

    public int find(int x) {
        findCalls++;
        int root = x;
        while (parent[root] != root) {
            root = parent[root];
        }
        // second walk: hang everything on the path directly under the root
        while (parent[x] != root) {
            int next = parent[x];
            parent[x] = root;
            x = next;
        }
        return root;
    }

    // returns true if x and y were in different sets and got merged
    public boolean union(int x, int y) {
        int rx = find(x);
        int ry = find(y);
        if (rx == ry) {
            return false;
        }
        if (rank[rx] < rank[ry]) {
            parent[rx] = ry;
        } else if (rank[rx] > rank[ry]) {
            parent[ry] = rx;
        } else {
            parent[ry] = rx;
            rank[rx]++;
        }
        sets--;
        unionCalls++;
        return true;
    }

    public int components() {
        return sets;
    }

    public long getFindCalls() {
        return findCalls;
    }

    public long getUnionCalls() {
        return unionCalls;
    }

    // what goes into Metrics.dsuOps
    public long operations() {
        return findCalls + unionCalls;
    }
}
