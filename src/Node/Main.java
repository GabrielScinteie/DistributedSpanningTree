package Node;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Main {
    private static HashMap<Integer, List<Integer>> neighborsMap = new HashMap<>(); // lista de adiacenta
    private static HashMap<Integer, Integer> portsMap = new HashMap<>();

    public static void readProperties(String fileName) {
        try {
            File file = new File(fileName);
            FileInputStream fileInput = new FileInputStream(file);
            Properties properties = new Properties();
            properties.load(fileInput);
            fileInput.close();

            Enumeration enuKeys = properties.keys();
            while (enuKeys.hasMoreElements()) {
                String key = (String) enuKeys.nextElement();
                String value = properties.getProperty(key);

                String[] parts = key.split("_");
                int node = Integer.parseInt(parts[1]);
                String operation = parts[0];

                if(operation.equals("port")){
                    Integer port = Integer.parseInt(value);
                    portsMap.put(node, port);
                } else if(operation.equals("neighbours")) {
                    List<String> portsString = Arrays.asList(value.split(","));
                    portsString.forEach(neighbour -> {
                        List<Integer> list = neighborsMap.computeIfAbsent(node, k -> new ArrayList<>());
                        list.add(Integer.parseInt(neighbour));
                    });
                } else{
                    throw new Exception("ERROR: Config file format: " + " + key: " + key + " value: "+ value);
                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println(args.length);
            System.out.println("Wrong CLI arguments.");
            return;
        }
        int nodeId = -1;
        try {
            nodeId = Integer.parseInt(args[0]);
            System.out.println("Node.Node number is: " + nodeId);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number provided. Please pass a valid integer.");
            return;
        }

        readProperties(".config");

        int port = portsMap.get(nodeId);
        List<Integer> neighbors = neighborsMap.get(nodeId);
        Node node = new Node(nodeId, port, neighbors, portsMap);
        node.start();
    }
}

// Compilare package noduri
// javac -d out src\Node\*.java

// Compilare package client
// javac -d out src\Client\*.java

// Compilare tot
// javac -d out src\Node\*.java src\Client\*.java

// Rulare noduri din root proiect
// java -cp out Node.Main 1
// java -cp out Node.Main 2

// Rulare client din root proiect
// java -cp out Client.Client 1503 "0:START"
