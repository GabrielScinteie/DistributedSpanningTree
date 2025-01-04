package Client;

// Client.Edge class to represent an edge between two nodes
public class Edge {
    int u, v;

    public Edge(int u, int v) {
        this.u = u;
        this.v = v;
    }

    @Override
    public String toString() {
        return "(" + u + ", " + v + ")";
    }
}
