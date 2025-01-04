package Client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

public class Client {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Client.Client <port> <message>");
            return;
        }

        int n = 7;

        List<Edge> edges = Arrays.asList(
                new Edge(1, 2), new Edge(1, 4),
                new Edge(2, 3), new Edge(2, 5),
                new Edge(3, 1), new Edge(3, 2),new Edge(3, 4), new Edge(3, 5),
                new Edge(4, 5), new Edge(4, 6),new Edge(4, 7),
                new Edge(5, 1), new Edge(5, 6),
                new Edge(6, 1), new Edge(6, 3), new Edge(6, 7),
                new Edge(7, 1), new Edge(7, 6)
        );
        List<String> spanningTrees = SpanningTreeGenerator.generateSpanningTrees(n, edges);


//        System.out.println("Generated Spanning Trees:");
//        for (int i = 0; i < spanningTrees.size(); i++) {
//            System.out.println("Tree " + (i + 1) + ": " + spanningTrees.get(i));
//        }

        try {
            int port = Integer.parseInt(args[0]);
            String message = args[1];

            DatagramSocket socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName("localhost");
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            socket.send(packet);
            System.out.println("Message sent: " + message);

            byte[] responseBuffer = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
            System.out.println("Waiting for response...");

            socket.receive(responsePacket);
            String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
            System.out.println("Response received: " + response);

            ClientTreeParser parser = new ClientTreeParser();
            String treeString = response.split(":")[2];
            ClientNodeTree root = parser.parseTree(treeString);
            printTree(root, "", true);

            boolean isCorrect = false;
            for (String tree : spanningTrees) {
                if (ClientTreeParser.compareTrees(tree, treeString)) {
                    isCorrect = true;
                    break;
                }
            }
            System.out.println("Correct: " + isCorrect);
            socket.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void printTree(ClientNodeTree node, String indent, boolean isLast) {
        String marker = isLast ? "└── " : "├── ";
        System.out.println(indent + marker + node.nodeId);

        for (int i = 0; i < node.children.size(); i++) {
            printTree(node.children.get(i), indent + (isLast ? "    " : "│   "), i == node.children.size() - 1);
        }
    }
}
