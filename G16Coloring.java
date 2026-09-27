import java.util.*;

/**
 * G16 Triangle-Free 3-Edge-Coloring of K16
 *
 * Uses the Greenwood-Gleason (1955) construction via GF(16),
 * the finite field with 16 elements.
 *
 * Primitive polynomial: x^4 + x + 1  (0x13 in hex)
 * Primitive element:    alpha = x     (binary 0010 = 2)
 *
 * Coloring rule for edge {i, j} where i != j:
 *   d = i XOR j  (difference/addition in GF(2^4))
 *   Let e = discrete log of d (d = alpha^e)
 *   Color = e mod 3  =>  0=RED, 1=GREEN, 2=BLUE
 *
 * Each color class is a 5-regular Clebsch graph (triangle-free).
 * Three such graphs partition all 120 edges of K16.
 * This proves R(3,3,3) > 16.
 */
public class G16Coloring {

    static final int N = 16;

    // Color constants
    static final int RED   = 0;
    static final int GREEN = 1;
    static final int BLUE  = 2;
    static final String[] COLOR_NAMES = {"RED  ", "GREEN", "BLUE "};
    static final String[] ANSI = {"\u001B[31m", "\u001B[32m", "\u001B[34m"};
    static final String RESET = "\u001B[0m";
    static final String BOLD  = "\u001B[1m";

    // GF(16) tables
    static final int[] POW  = new int[15]; // pow[i] = alpha^i
    static final int[] LOG  = new int[16]; // log[x] = discrete log of x
    // adj[i][j] = color of edge {i,j}, -1 if i==j
    static final int[][] adj = new int[N][N];

    //  GF(16) arithmetic 

    /**
     * Multiply two elements in GF(16) = GF(2)[x] / (x^4 + x + 1).
     * Elements are 4-bit integers (polynomial coefficients in GF(2)).
     */
    static int gfMul(int a, int b) {
        int p = 0;
        for (int i = 0; i < 4; i++) {
            if ((b & 1) != 0) p ^= a;
            int highBit = a & 8;
            a = (a << 1) & 0xF;
            if (highBit != 0) a ^= 0x3; // reduce: x^4 = x + 1 => mod by x+1 = 0x3
            b >>= 1;
        }
        return p;
    }

    /** Build the POW and LOG tables for GF(16) using alpha=2. */
    static void buildTables() {
        int alpha = 2;
        POW[0] = 1;
        for (int i = 1; i < 15; i++) POW[i] = gfMul(POW[i - 1], alpha);
        for (int i = 0; i < 15; i++) LOG[POW[i]] = i;
        LOG[0] = -1; // log(0) undefined
    }

    /** Return the color (0/1/2) of edge {a, b}. */
    static int edgeColor(int a, int b) {
        int diff = a ^ b; // XOR = addition in GF(2^4)
        return LOG[diff] % 3;
    }

    /** Build the full adjacency/color matrix. */
    static void buildAdjacency() {
        for (int i = 0; i < N; i++) Arrays.fill(adj[i], -1);
        for (int i = 0; i < N; i++)
            for (int j = i + 1; j < N; j++) {
                int c = edgeColor(i, j);
                adj[i][j] = c;
                adj[j][i] = c;
            }
    }

    //  Verification 

    /** Return true if the coloring has no monochromatic triangle. */
    static boolean verifyNoMonochromaticTriangle() {
        for (int i = 0; i < N; i++)
            for (int j = i + 1; j < N; j++)
                for (int k = j + 1; k < N; k++) {
                    int c1 = adj[i][j], c2 = adj[j][k], c3 = adj[i][k];
                    if (c1 == c2 && c2 == c3) return false;
                }
        return true;
    }

    /** Count edges of each color. */
    static int[] countEdges() {
        int[] count = new int[3];
        for (int i = 0; i < N; i++)
            for (int j = i + 1; j < N; j++)
                count[adj[i][j]]++;
        return count;
    }

    /** Check that every vertex has exactly 5 neighbors of each color. */
    static boolean verifyRegularity() {
        for (int i = 0; i < N; i++) {
            int[] deg = new int[3];
            for (int j = 0; j < N; j++)
                if (adj[i][j] >= 0) deg[adj[i][j]]++;
            if (deg[RED] != 5 || deg[GREEN] != 5 || deg[BLUE] != 5) return false;
        }
        return true;
    }

    //  Display helpers 

    static String colorStr(int c) {
        return ANSI[c] + COLOR_NAMES[c] + RESET;
    }

    static void printSeparator(char ch, int len) {
        System.out.println(String.valueOf(ch).repeat(len));
    }

    static void printHeader(String title) {
        printSeparator('=', 70);
        System.out.println(BOLD + "  " + title + RESET);
        printSeparator('=', 70);
    }

    //  Main output sections 

    static void printGF16Tables() {
        System.out.println(BOLD + "\nGF(16) Power Table  (alpha = x, poly = x^4+x+1)" + RESET);
        printSeparator('-', 50);
        System.out.printf("%-6s %-8s %-10s %-6s %-8s%n",
                "i", "alpha^i", "Binary", "Color", "Group");
        printSeparator('-', 50);
        for (int i = 0; i < 15; i++) {
            int v = POW[i];
            String bin = String.format("%4s", Integer.toBinaryString(v)).replace(' ', '0');
            int group = i % 3; // 0=R,1=G,2=B
            System.out.printf("%-6d %-8d %-10s %s  G%d%n",
                    i, v, bin, colorStr(group), group + 1);
        }
        printSeparator('-', 50);
        System.out.println("G1 (exp%3=0) -> RED    | G2 (exp%3=1) -> GREEN | G3 (exp%3=2) -> BLUE");
    }

    static void printEdgeList() {
        System.out.println(BOLD + "\nEdge Coloring (all 120 edges of K16)" + RESET);
        printSeparator('-', 55);
        System.out.printf("%-12s %-8s %-6s %-14s%n",
                "Edge", "Diff", "Log", "Color");
        printSeparator('-', 55);
        for (int i = 0; i < N; i++)
            for (int j = i + 1; j < N; j++) {
                int diff = i ^ j;
                int log  = LOG[diff];
                int c    = adj[i][j];
                System.out.printf("{%2d,%2d}   ->   diff=%2d  log=%2d   %s%n",
                        i, j, diff, log, colorStr(c));
            }
    }

    static void printAdjacencyMatrix() {
        System.out.println(BOLD + "\nAdjacency Color Matrix (R=Red, G=Green, B=Blue, -=self)" + RESET);
        printSeparator('-', 62);

        // Header row
        System.out.print("     ");
        for (int j = 0; j < N; j++) System.out.printf("%3d", j);
        System.out.println();
        printSeparator('-', 62);

        for (int i = 0; i < N; i++) {
            System.out.printf("%3d |", i);
            for (int j = 0; j < N; j++) {
                if (adj[i][j] < 0) System.out.print("  -");
                else System.out.print("  " + ANSI[adj[i][j]] + COLOR_NAMES[adj[i][j]].charAt(0) + RESET);
            }
            System.out.println();
        }
    }

    static void printNeighborLists() {
        System.out.println(BOLD + "\nNeighbor Lists per Vertex" + RESET);
        printSeparator('-', 65);
        System.out.printf("%-8s %-25s %-25s %-20s%n",
                "Vertex", "Red neighbors (5)", "Green neighbors (5)", "Blue neighbors (5)");
        printSeparator('-', 65);
        for (int i = 0; i < N; i++) {
            List<Integer> r = new ArrayList<>(), g = new ArrayList<>(), b = new ArrayList<>();
            for (int j = 0; j < N; j++) {
                if (adj[i][j] == RED)   r.add(j);
                if (adj[i][j] == GREEN) g.add(j);
                if (adj[i][j] == BLUE)  b.add(j);
            }
            System.out.printf("%-8d %-25s %-25s %-20s%n",
                    i, r.toString(), g.toString(), b.toString());
        }
    }

    static void printVerification() {
        System.out.println(BOLD + "\nVerification" + RESET);
        printSeparator('-', 55);

        // Monochromatic triangle check
        boolean noMono = verifyNoMonochromaticTriangle();
        System.out.printf("  No monochromatic triangle:  %s%n",
                noMono ? "\u001B[32m[PASS]\u001B[0m" : "\u001B[31m[FAIL]\u001B[0m");

        // Regularity check
        boolean regular = verifyRegularity();
        System.out.printf("  Each vertex: 5R + 5G + 5B:  %s%n",
                regular ? "\u001B[32m[PASS]\u001B[0m" : "\u001B[31m[FAIL]\u001B[0m");

        // Edge counts
        int[] ec = countEdges();
        System.out.printf("  Edge counts  R=%-3d G=%-3d B=%-3d total=%d  %s%n",
                ec[RED], ec[GREEN], ec[BLUE], ec[RED]+ec[GREEN]+ec[BLUE],
                (ec[RED]==40 && ec[GREEN]==40 && ec[BLUE]==40)
                        ? "\u001B[32m[PASS]\u001B[0m" : "\u001B[31m[FAIL]\u001B[0m");

        // Triangle count per color
        int[] tri = countTrianglesPerColor();
        System.out.printf("  Monochromatic triangles     R=%-3d G=%-3d B=%-3d  %s%n",
                tri[RED], tri[GREEN], tri[BLUE],
                (tri[RED]==0 && tri[GREEN]==0 && tri[BLUE]==0)
                        ? "\u001B[32m[PASS]\u001B[0m" : "\u001B[31m[FAIL]\u001B[0m");

        printSeparator('-', 55);
        System.out.println("  K16 is 3-colored with NO monochromatic triangle.");
        System.out.println("  This proves  R(3,3,3) > 16.");
        System.out.println("  (Greenwood & Gleason, 1955 - R(3,3,3) = 17)");
    }

    static int[] countTrianglesPerColor() {
        int[] count = new int[3];
        for (int i = 0; i < N; i++)
            for (int j = i + 1; j < N; j++)
                for (int k = j + 1; k < N; k++) {
                    int c1 = adj[i][j], c2 = adj[j][k], c3 = adj[i][k];
                    if (c1 == c2 && c2 == c3) count[c1]++;
                }
        return count;
    }

    //  Interactive mode 

    static void interactiveMode(Scanner sc) {
        System.out.println(BOLD + "\n Interactive Query Mode " + RESET);
        System.out.println("Commands:  edge <i> <j>  |  vertex <i>  |  quit");
        while (true) {
            System.out.print("\n> ");
            String line = sc.nextLine().trim().toLowerCase();
            if (line.equals("quit") || line.equals("q") || line.equals("exit")) break;

            String[] parts = line.split("\\s+");
            if (parts.length == 0) continue;

            if (parts[0].equals("edge") && parts.length == 3) {
                try {
                    int i = Integer.parseInt(parts[1]);
                    int j = Integer.parseInt(parts[2]);
                    if (i < 0 || i >= N || j < 0 || j >= N) {
                        System.out.println("  Vertices must be 0-15.");
                    } else if (i == j) {
                        System.out.println("  No self-loops.");
                    } else {
                        int c = adj[i][j];
                        System.out.printf("  Edge {%d,%d}:  diff = %d  |  log = %d  |  color = %s%n",
                                i, j, i^j, LOG[i^j], colorStr(c));
                    }
                } catch (NumberFormatException e) {
                    System.out.println("  Usage: edge <i> <j>");
                }
            } else if (parts[0].equals("vertex") && parts.length == 2) {
                try {
                    int v = Integer.parseInt(parts[1]);
                    if (v < 0 || v >= N) {
                        System.out.println("  Vertex must be 0-15.");
                    } else {
                        List<Integer> r = new ArrayList<>(), g = new ArrayList<>(), b = new ArrayList<>();
                        for (int j = 0; j < N; j++) {
                            if (adj[v][j] == RED)   r.add(j);
                            if (adj[v][j] == GREEN) g.add(j);
                            if (adj[v][j] == BLUE)  b.add(j);
                        }
                        System.out.printf("  Vertex %d neighbors:%n", v);
                        System.out.printf("    %sRED  %s: %s%n", ANSI[RED],  RESET, r);
                        System.out.printf("    %sGREEN%s: %s%n", ANSI[GREEN],RESET, g);
                        System.out.printf("    %sBLUE %s: %s%n", ANSI[BLUE], RESET, b);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("  Usage: vertex <i>");
                }
            } else {
                System.out.println("  Unknown command. Try: edge 0 5  |  vertex 3  |  quit");
            }
        }
    }

    //  Entry point 

    public static void main(String[] args) {
        buildTables();
        buildAdjacency();

        printHeader("G16 Triangle-Free 3-Edge-Coloring  (Greenwood-Gleason 1955)");
        System.out.println();
        System.out.println("  Problem : Color the edges of K16 (complete graph, 16 vertices)");
        System.out.println("            with 3 colors so NO triangle is monochromatic.");
        System.out.println("  Method  : Finite field GF(16) - label vertices with field elements,");
        System.out.println("            color edge {i,j} by  log(i XOR j)  mod 3.");
        System.out.println("  Result  : Each color class = Clebsch graph (5-regular, triangle-free).");
        System.out.println("            Proves R(3,3,3) > 16  =>  R(3,3,3) = 17.");

        printGF16Tables();
        printNeighborLists();
        printAdjacencyMatrix();
        printVerification();

        // Check for interactive flag or stdin available
        Scanner sc = new Scanner(System.in);
        boolean hasArg = args.length > 0 && args[0].equals("--interactive");
        if (hasArg || System.console() != null) {
            interactiveMode(sc);
        } else {
            System.out.println("\n  (Run with --interactive to query individual edges/vertices)");
        }

        printSeparator('=', 70);
        System.out.println();
    }
}
