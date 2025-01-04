package Client;

import java.util.*;

public class SpanningTreeGenerator {

    public static List<String> generateSpanningTrees(int n, List<Edge> edges) {
        List<String> spanningTrees = new ArrayList<>();
        List<List<Edge>> combinations = getCombinations(edges, n - 1);

        for (int root = 1; root <= n; root++) { // Try each node as root
            for (List<Edge> edgeSubset : combinations) {
                if (isValidDirectedSpanningTree(n, root, edgeSubset)) {
                    Map<Integer, List<Integer>> tree = buildDirectedTree(edgeSubset);
                    spanningTrees.add(treeToString(tree, root));
                }
            }
        }

        return spanningTrees;
    }

    private static boolean isValidDirectedSpanningTree(int n, int root, List<Edge> edgeSubset) {
        if (edgeSubset.size() != n - 1) {
            return false; // A spanning tree must have exactly n-1 edges
        }

        // Build adjacency list and check reachability from root
        Map<Integer, List<Integer>> graph = new HashMap<>();
        for (int i = 1; i <= n; i++) {
            graph.put(i, new ArrayList<>());
        }
        for (Edge edge : edgeSubset) {
            graph.get(edge.u).add(edge.v);
        }

        // Check if all nodes are reachable from the root
        Set<Integer> visited = new HashSet<>();
        dfs(root, graph, visited);

        return visited.size() == n && !hasCycles(graph, n);
    }

    private static void dfs(int node, Map<Integer, List<Integer>> graph, Set<Integer> visited) {
        visited.add(node);
        for (int neighbor : graph.get(node)) {
            if (!visited.contains(neighbor)) {
                dfs(neighbor, graph, visited);
            }
        }
    }

    private static boolean hasCycles(Map<Integer, List<Integer>> graph, int n) {
        boolean[] visited = new boolean[n + 1];
        boolean[] recStack = new boolean[n + 1];

        for (int i = 1; i <= n; i++) {
            if (!visited[i]) {
                if (detectCycle(i, graph, visited, recStack)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean detectCycle(int node, Map<Integer, List<Integer>> graph, boolean[] visited, boolean[] recStack) {
        visited[node] = true;
        recStack[node] = true;

        for (int neighbor : graph.get(node)) {
            if (!visited[neighbor] && detectCycle(neighbor, graph, visited, recStack)) {
                return true;
            } else if (recStack[neighbor]) {
                return true;
            }
        }

        recStack[node] = false;
        return false;
    }

    private static Map<Integer, List<Integer>> buildDirectedTree(List<Edge> edges) {
        Map<Integer, List<Integer>> tree = new HashMap<>();
        for (Edge edge : edges) {
            tree.putIfAbsent(edge.u, new ArrayList<>());
            tree.get(edge.u).add(edge.v);
        }
        return tree;
    }

    private static String treeToString(Map<Integer, List<Integer>> tree, int root) {
        StringBuilder sb = new StringBuilder();
        Set<Integer> visited = new HashSet<>();
        dfsToString(root, tree, visited, sb);
        return sb.toString();
    }

    private static void dfsToString(int node, Map<Integer, List<Integer>> tree, Set<Integer> visited, StringBuilder sb) {
        visited.add(node);
        sb.append(node);

        List<Integer> children = tree.getOrDefault(node, new ArrayList<>());
        if (!children.isEmpty()) {
            sb.append("(");
            for (int i = 0; i < children.size(); i++) {
                if (i > 0) sb.append(","); // Sibling separator
                dfsToString(children.get(i), tree, visited, sb);
            }
            sb.append(")");
        }
    }

    private static <T> List<List<T>> getCombinations(List<T> list, int k) {
        List<List<T>> combinations = new ArrayList<>();
        getCombinationsHelper(list, k, 0, new ArrayList<>(), combinations);
        return combinations;
    }

    private static <T> void getCombinationsHelper(List<T> list, int k, int start, List<T> current, List<List<T>> combinations) {
        if (current.size() == k) {
            combinations.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i < list.size(); i++) {
            current.add(list.get(i));
            getCombinationsHelper(list, k, i + 1, current, combinations);
            current.remove(current.size() - 1);
        }
    }

}