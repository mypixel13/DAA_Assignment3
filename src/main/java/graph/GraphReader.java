package graph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

// Reads the text format from the assignment:
//   n m directed|undirected
//   u v w        (m times)
// Empty lines and lines starting with # are ignored.
public class GraphReader {

    public static Graph read(Path file) throws IOException {
        try (BufferedReader in = Files.newBufferedReader(file)) {
            return read(in);
        }
    }

    public static Graph parse(String text) throws IOException {
        return read(new BufferedReader(new StringReader(text)));
    }

    private static Graph read(BufferedReader in) throws IOException {
        String header = nextLine(in);
        if (header == null) {
            throw new IllegalArgumentException("empty graph file");
        }
        String[] h = header.split("\\s+");
        if (h.length != 3) {
            throw new IllegalArgumentException("first line must be: n m directed|undirected, got: " + header);
        }
        int n = Integer.parseInt(h[0]);
        int m = Integer.parseInt(h[1]);
        boolean directed;
        if (h[2].equals("directed")) {
            directed = true;
        } else if (h[2].equals("undirected")) {
            directed = false;
        } else {
            throw new IllegalArgumentException("expected directed or undirected, got: " + h[2]);
        }

        Graph g = new Graph(n, directed);
        for (int i = 0; i < m; i++) {
            String line = nextLine(in);
            if (line == null) {
                throw new IllegalArgumentException("file ended after " + i + " edges, expected " + m);
            }
            String[] p = line.split("\\s+");
            if (p.length != 3) {
                throw new IllegalArgumentException("bad edge line: " + line);
            }
            g.addEdge(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
        }
        return g;
    }

    // next line that is not empty and not a comment, or null at the end of the file
    private static String nextLine(BufferedReader in) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
                return line;
            }
        }
        return null;
    }
}
