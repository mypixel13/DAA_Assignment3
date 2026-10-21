package algorithms;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DsuTest {

    // the dumbest possible union-find: a label per element, a union relabels everything
    private static class SimpleSets {
        final int[] label;

        SimpleSets(int n) {
            label = new int[n];
            for (int i = 0; i < n; i++) {
                label[i] = i;
            }
        }

        void union(int a, int b) {
            int from = label[a];
            int to = label[b];
            for (int i = 0; i < label.length; i++) {
                if (label[i] == from) {
                    label[i] = to;
                }
            }
        }

        int countSets() {
            Set<Integer> distinct = new HashSet<>();
            for (int x : label) {
                distinct.add(x);
            }
            return distinct.size();
        }
    }

    @Test
    void findAgreesWithSimpleReferenceAfterRandomUnions() {
        Random rnd = new Random(1);
        for (int round = 0; round < 30; round++) {
            int n = 2 + rnd.nextInt(60);
            Dsu dsu = new Dsu(n);
            SimpleSets ref = new SimpleSets(n);

            int unions = rnd.nextInt(2 * n);
            for (int i = 0; i < unions; i++) {
                int a = rnd.nextInt(n);
                int b = rnd.nextInt(n);
                dsu.union(a, b);
                ref.union(a, b);
            }

            for (int a = 0; a < n; a++) {
                for (int b = 0; b < n; b++) {
                    boolean sameInDsu = dsu.find(a) == dsu.find(b);
                    boolean sameInRef = ref.label[a] == ref.label[b];
                    assertEquals(sameInRef, sameInDsu, "n=" + n + " a=" + a + " b=" + b);
                }
            }
        }
    }

    @Test
    void componentsMatchesNumberOfSets() {
        Random rnd = new Random(2);
        for (int round = 0; round < 30; round++) {
            int n = 1 + rnd.nextInt(80);
            Dsu dsu = new Dsu(n);
            SimpleSets ref = new SimpleSets(n);
            assertEquals(n, dsu.components());

            for (int i = 0; i < n; i++) {
                int a = rnd.nextInt(n);
                int b = rnd.nextInt(n);
                dsu.union(a, b);
                ref.union(a, b);
                assertEquals(ref.countSets(), dsu.components());
            }
        }
    }

    @Test
    void unionReportsWhetherAnythingMerged() {
        Dsu dsu = new Dsu(4);
        assertTrue(dsu.union(0, 1));
        assertFalse(dsu.union(1, 0));
        assertFalse(dsu.union(2, 2));
        assertTrue(dsu.union(2, 3));
        assertTrue(dsu.union(0, 3));
        assertEquals(1, dsu.components());
    }

    @Test
    void onlySuccessfulUnionsAreCounted() {
        Dsu dsu = new Dsu(5);
        dsu.union(0, 1);   // merges
        dsu.union(1, 0);   // already together
        dsu.union(3, 4);   // merges
        assertEquals(2, dsu.getUnionCalls());
        // every union does two finds
        assertEquals(6, dsu.getFindCalls());
        assertEquals(8, dsu.operations());
    }

    @Test
    void makeSetStartsOver() {
        Dsu dsu = new Dsu(3);
        dsu.union(0, 1);
        dsu.makeSet(6);
        assertEquals(6, dsu.components());
        assertEquals(0, dsu.operations());
        assertFalse(dsu.find(0) == dsu.find(1));
    }

    @Test
    void longChainDoesNotBreakFind() {
        // union by rank keeps the trees shallow and find is not recursive
        int n = 200_000;
        Dsu dsu = new Dsu(n);
        for (int i = 0; i + 1 < n; i++) {
            dsu.union(i, i + 1);
        }
        assertEquals(1, dsu.components());
        assertEquals(dsu.find(0), dsu.find(n - 1));
    }
}
