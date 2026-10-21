package benchmark;

import algorithms.DagShortestPath;
import algorithms.Kahn;
import algorithms.Kosaraju;
import algorithms.Kruskal;
import algorithms.MstResult;
import algorithms.PathResult;
import algorithms.Prim;
import algorithms.SccResult;
import algorithms.Tarjan;
import algorithms.TopoResult;
import graph.Graph;
import metrics.Metrics;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Runs all algorithms on the ten generated graphs and writes results/results.csv.
// The run stops with an exception if anything is wrong: MST weights that differ,
// a bad topological order, a counter that breaks its formula, a reference value that doesn't match.
public class Benchmark {

    private static final int[] SIZES = {1000, 3162, 10000, 31623, 100000};
    private static final int RUNS = 5;

    // reference values from the assignment: {MST weight, SCC count, sum of DAG distances}
    private static final long[][] REFERENCE = {
            {147970, 27, 396274},        // 1000 sparse
            {30363, 1, 252411},          // 1000 dense
            {468911, 137, 1271911},      // 3162 sparse
            {97589, 1, 723157},          // 3162 dense
            {1486139, 373, 3941828},     // 10000 sparse
            {309550, 1, 2381342},        // 10000 dense
            {4715515, 1188, 12551891},   // 31623 sparse
            {968517, 1, 7376612},        // 31623 dense
            {15012094, 3923, 40096539},  // 100000 sparse
            {3056681, 1, 23512087},      // 100000 dense
    };

    private final List<String> rows = new ArrayList<>();
    private final List<String> tarjanRows = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        Path out = Path.of(args.length > 0 ? args[0] : "results/results.csv");
        Benchmark b = new Benchmark();
        b.runAll();
        b.write(out);
    }

    private void runAll() {
        System.out.printf("%-10s %-12s %9s %9s %10s %12s %12s %9s %12s%n",
                "graph", "algorithm", "n", "m", "time_ms", "dsu_ops", "edge_scans", "max_depth", "result");
        int refIndex = 0;
        for (int n : SIZES) {
            for (String density : new String[]{"sparse", "dense"}) {
                int m = density.equals("sparse") ? 4 * n : 20 * n;
                String name = n + "_" + density;
                long[] ref = REFERENCE[refIndex++];

                long mstWeight = runMst(name, n, m);
                long sccCount = runScc(name, n, m);
                long dagSum = runDag(name, n, m);

                check(name + " MST weight", mstWeight, ref[0]);
                check(name + " SCC count", sccCount, ref[1]);
                check(name + " sum of DAG distances", dagSum, ref[2]);
            }
        }
        System.out.println();
        System.out.println("All checks passed: MST weights agree, orders are valid, counters match their formulas, "
                + "reference values reproduced.");
    }

    private long runMst(String name, int n, int m) {
        Graph g = GraphGenerator.undirected(n, m);

        Metrics km = new Metrics();
        MstResult[] kr = new MstResult[1];
        double kTime = timeIt(() -> {
            km.reset();
            kr[0] = Kruskal.run(g, km);
        });

        Metrics pm = new Metrics();
        MstResult[] pr = new MstResult[1];
        double pTime = timeIt(() -> {
            pm.reset();
            pr[0] = Prim.run(g, pm);
        });

        // the two algorithms must agree, otherwise one of them has a bug
        if (kr[0].totalWeight() != pr[0].totalWeight()) {
            throw new IllegalStateException(name + ": Kruskal and Prim disagree: "
                    + kr[0].totalWeight() + " vs " + pr[0].totalWeight());
        }
        if (kr[0].edges().size() != n - kr[0].components() || pr[0].edges().size() != n - pr[0].components()) {
            throw new IllegalStateException(name + ": a spanning forest must have n - components edges");
        }

        addRow("Kruskal", name, n, g.edgeCount(), kTime, km, kr[0].totalWeight());
        addRow("Prim", name, n, g.edgeCount(), pTime, pm, pr[0].totalWeight());
        return kr[0].totalWeight();
    }

    private long runScc(String name, int n, int m) {
        Graph g = GraphGenerator.directed(n, m);

        Metrics sm = new Metrics();
        SccResult[] sr = new SccResult[1];
        double time = timeIt(() -> {
            sm.reset();
            sr[0] = Kosaraju.run(g, sm);
        });
        check(name + " Kosaraju edge scans (2m)", sm.edgeScans, 2L * g.edgeCount());

        // bonus: Tarjan on the same graph, must find the same components
        Metrics tm = new Metrics();
        SccResult tr = Tarjan.run(g, tm);
        if (!tr.samePartitionAs(sr[0])) {
            throw new IllegalStateException(name + ": Tarjan and Kosaraju found different components");
        }
        check(name + " Tarjan edge scans (m)", tm.edgeScans, g.edgeCount());
        tarjanRows.add(String.join(",", name, String.valueOf(n), String.valueOf(g.edgeCount()),
                String.valueOf(sm.edgeScans), String.valueOf(tm.edgeScans),
                String.valueOf(sm.maxDepth), String.valueOf(tm.maxDepth), String.valueOf(sr[0].count())));

        addRow("Kosaraju", name, n, g.edgeCount(), time, sm, sr[0].count());
        return sr[0].count();
    }

    private long runDag(String name, int n, int m) {
        Graph g = GraphGenerator.dag(n, m);

        Metrics km = new Metrics();
        TopoResult[] tr = new TopoResult[1];
        double kTime = timeIt(() -> {
            km.reset();
            tr[0] = Kahn.run(g, km);
        });
        if (tr[0].hasCycle()) {
            throw new IllegalStateException(name + ": Kahn found a cycle in a DAG");
        }
        if (!Kahn.isValidOrder(g, tr[0].order())) {
            throw new IllegalStateException(name + ": Kahn returned an invalid topological order");
        }
        check(name + " Kahn edge scans (m)", km.edgeScans, g.edgeCount());

        Metrics dm = new Metrics();
        PathResult[] pr = new PathResult[1];
        double dTime = timeIt(() -> {
            dm.reset();
            pr[0] = DagShortestPath.run(g, tr[0].order(), dm);
        });
        check(name + " DAG relaxation scans (m)", dm.edgeScans, g.edgeCount());

        addRow("Kahn", name, n, g.edgeCount(), kTime, km, tr[0].order().length);
        addRow("DagShortestPath", name, n, g.edgeCount(), dTime, dm, pr[0].sumOfDistances());
        return pr[0].sumOfDistances();
    }

    // median of RUNS runs, in milliseconds
    private static double timeIt(Runnable task) {
        double[] times = new double[RUNS];
        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();
            task.run();
            times[i] = (System.nanoTime() - start) / 1e6;
        }
        Arrays.sort(times);
        return times[RUNS / 2];
    }

    private void addRow(String algorithm, String graph, int n, int m, double timeMs, Metrics x, long result) {
        rows.add(String.format(java.util.Locale.ROOT, "%s,%s,%d,%d,%.3f,%d,%d,%d,%d,%d",
                algorithm, graph, n, m, timeMs, x.dsuOps, x.edgeScans, x.comparisons, x.maxDepth, result));
        System.out.printf(java.util.Locale.ROOT, "%-10s %-12s %9d %9d %10.2f %12d %12d %9d %12d%n",
                graph, algorithm, n, m, timeMs, x.dsuOps, x.edgeScans, x.maxDepth, result);
    }

    private static void check(String what, long actual, long expected) {
        if (actual != expected) {
            throw new IllegalStateException("CHECK FAILED: " + what + ": got " + actual + ", expected " + expected);
        }
    }

    private void write(Path csv) throws IOException {
        if (rows.size() != 50) {
            throw new IllegalStateException("expected 50 rows, have " + rows.size());
        }
        Files.createDirectories(csv.toAbsolutePath().getParent());
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(csv))) {
            w.println("algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result");
            rows.forEach(w::println);
        }
        Path tarjanCsv = csv.toAbsolutePath().getParent().resolve("tarjan_vs_kosaraju.csv");
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(tarjanCsv))) {
            w.println("graph,n,m,kosaraju_edge_scans,tarjan_edge_scans,kosaraju_max_depth,tarjan_max_depth,scc_count");
            tarjanRows.forEach(w::println);
        }
        System.out.println("Wrote " + csv + " and " + tarjanCsv);
    }
}
