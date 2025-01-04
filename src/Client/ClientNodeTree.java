package Client;

import java.util.ArrayList;
import java.util.List;

public class ClientNodeTree {
    int nodeId;
    List<ClientNodeTree> children;

    public ClientNodeTree(int value) {
        this.nodeId = value;
        this.children = new ArrayList<>();
    }

    public void addChild(ClientNodeTree child) {
        children.add(child);
    }
}
