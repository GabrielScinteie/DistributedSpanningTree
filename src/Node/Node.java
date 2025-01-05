package Node;

import java.net.*;
import java.util.*;

public class Node {
    private int nodeId;
    private int port;
    private int parentId = -1; // If node has parent = 0, then he is root. -1 means not assigned yet
    private List<Integer> neighbors;
    private Map<Integer, Integer> portsMap;
    private DatagramSocket socket = null;
    private String subtree = "";
    private Set<Integer> setChildren = new HashSet<>();
    private Set<Integer> setParent = new HashSet<>();
    private Set<Integer> setOther = new HashSet<>();
    private Set<Integer> connections = new HashSet<>();
    private Set<Integer> union;
    private int number_children_done = 0;
    private boolean done = false;
    Random random = new Random();

    public Node(int nodeId, int port, List<Integer> neighbors, Map<Integer, Integer> portsMap) {
        this.nodeId = nodeId;
        this.port = port;
        this.neighbors = new ArrayList<>(neighbors);
        this.portsMap = new HashMap<>(portsMap);

        // Delete from portsMap all the nodes that aren't neighbors. A node must only know about its neighbors
        Iterator<Map.Entry<Integer, Integer>> iterator = this.portsMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            if (!neighbors.contains(entry.getKey())) {
                iterator.remove();
            }
        }

        System.out.println("Node: " + nodeId + " neighbors ports: " + this.portsMap);

        connections.addAll(neighbors);
    }

    // TODO: WHEN RECEIVING A MESSAGE FROM SOMEONE, IF THEY ARE NEIGHBORS, ADD THEM TO PORTS MAP

    public void start() {
        try {
            System.out.println("Node " + nodeId + " port " + port);
            socket = new DatagramSocket(port);
            byte[] buffer = new byte[256];
            String message;
            String senderIdString;
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String fullMessage = new String(packet.getData(), 0, packet.getLength());

                int senderPort = packet.getPort();
                InetAddress senderAddress = packet.getAddress();

                String[] parts = fullMessage.split(":", 2);
                if (parts.length != 2) {
                    System.out.println("Message " + fullMessage + " has wrong format");
                    continue;
                }

                senderIdString = parts[0];
                message = parts[1];

                int senderId = Integer.parseInt(senderIdString);
                // If the sender is not in the map of known ports, add him
                if(!portsMap.containsKey(senderId))
                {
                    portsMap.put(senderId, senderPort);
                }

                System.out.println("Node " + nodeId + " received from " + senderId + " message: " + message);
                handleMessage(senderId, message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleMessage(int senderId, String message) {
        // Initial start message, it means I'm root
        if(senderId == 0) {
            parentId = 0;
            sendNeighbors("START", parentId);
        } else if(Objects.equals(message, "START")){
            connections.add(senderId); // Add to connections set also the incoming connections
            // If no parent, parent found
            if(!hasParent()) {
                parentId = senderId;
                setParent.add(parentId);
                sendNeighbors("START", parentId);
                sendMessage(parentId, "PARENT");
            } else {
                setOther.add(senderId);
                sendMessage(senderId, "ALREADY");
            }
        } else if(Objects.equals(message, "PARENT")) {
            setChildren.add(senderId);

        } else if(Objects.equals(message, "ALREADY")) {
            setOther.add(senderId);

            // If every connection responded and I haven't already sent DONE, then I can send done to the parent
            if(allConnectionsResponded() && !done) {
                // If I'm leaf, then I'll send a tree building message to my parent containing only my ID
                if(setChildren.isEmpty()){
                    done = true;
                    sendMessage(parentId, "DONE:" + nodeId);
                } else if(number_children_done == setChildren.size()) {
                    // If I have children, and all connections responded and I haven't sent DONE to parent, then I can close the children list and send my subtree to parent
                    subtree += ")";

                    done = true;
                    sendMessage(parentId, "DONE:" + subtree);
                }
            }
        } else if(message.contains("DONE")) {
            String children_subtree = message.split(":")[1];

            number_children_done += 1;
            if(number_children_done == 1) {
                subtree = nodeId + "(" + children_subtree ;
            } else {
                subtree += "," + children_subtree;
            }

            // If all connections responded and the number of children who are done is equal to the total number of children
            // We need to check that all conections responded because if not, we might send a DONE message before all the possible children responded to us
            if(number_children_done == setChildren.size() && allConnectionsResponded() && !done) {
                subtree += ")";

                done = true;
                sendMessage(parentId, "DONE:" + subtree);
            }
        }
    }

    // Function that clears the node so that another call from the client can be made
    private void clearNode() {
        subtree = "";
        setChildren = new HashSet<>();
        setParent = new HashSet<>();
        setOther = new HashSet<>();
        connections = new HashSet<>(neighbors);
        number_children_done = 0;
        // If node is root, remove the client port
        if (parentId == 0)
            portsMap.remove(0);

        parentId = -1;
    }

    private void simulateDelay() {
        int randomDelay = random.nextInt(2000); // Random number between 0 and 2000 ms

        // System.out.println("Sleeping for " + randomDelay + " milliseconds...");

        try {
            Thread.sleep(randomDelay);
        } catch (InterruptedException e) {
            System.err.println("Thread was interrupted");
        }
    }

    private boolean allConnectionsResponded(){
        union = new HashSet<>(setChildren);
        union.addAll(setOther);
        if(!isRoot()) {
            union.addAll(setParent);
        }

        return connections.equals(union);
    }

    private void printChildren(){
        System.out.print("Node " + nodeId + " has children: [");
        Iterator<Integer> iterator = setChildren.iterator();
        while (iterator.hasNext()) {
            System.out.print(iterator.next());
            if (iterator.hasNext()) {
                System.out.print(" ");
            }
        }
        System.out.println("]");
    }

    private boolean isRoot(){
        return parentId == 0;
    }

    private boolean hasParent(){
        return parentId != -1;
    }

    private void sendNeighbors(String message, int parentId) {
        List<Thread> threads = new ArrayList<>(); // Store all threads

        for (Integer neighbor : neighbors) {
            if (neighbor != parentId) {
                Thread thread = new Thread(() -> sendMessage(neighbor, message)); // Create a thread for sendMessage
                thread.start(); // Start the thread
                threads.add(thread); // Keep track of the thread
            }
        }

        // Wait for all threads to finish
        for (Thread thread : threads) {
            try {
                thread.join(); // Wait for the thread to complete
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private void sendMessage(int neighbor, String message) {
        try {
            InetAddress address = InetAddress.getByName("localhost");
            String fullMessage = nodeId + ":" + message; // Include sender ID in the message
            byte[] buffer = fullMessage.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, portsMap.get(neighbor));

            // Simulate delay of message sending
            simulateDelay();

            socket.send(packet);

            System.out.println("Node " + nodeId + " sent: " + fullMessage + " to neighbor " + neighbor);
            System.out.println(portsMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
