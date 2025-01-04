package Client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientTreeParser {
    public static ClientNodeTree  parseTree(String s){
        return parse(s, 0).node;
    }

    static class ParseResult {
        ClientNodeTree node;
        int position;

        public ParseResult(ClientNodeTree node, int position) {
            this.node = node;
            this.position = position;
        }
    }

    public static ParseResult parse(String s, int position) {
        // Convert string with possibly multiple digits into number
        int value = 0;
        while(position < s.length() && Character.isDigit(s.charAt(position)))
        {
            value = value * 10 + (s.charAt(position) - '0');
            position += 1;
        }

        ClientNodeTree node = new ClientNodeTree(value);

        // If there are children
        if(position < s.length() && s.charAt(position) == '(')
        {
            // Skip (
            position += 1;
            while(position < s.length() && s.charAt(position) != ')'){
                // Parse the child together with all its subtree
                ParseResult childResult = parse(s, position);
                node.addChild(childResult.node);
                position = childResult.position;

                // Skip ,
                if(position < s.length() && s.charAt(position) == ','){
                    position += 1;
                }
            }

            // Skip )
            position += 1;
        }

        // 2nd param doesn't matter
        return new ParseResult(node, position);
    }

    public static boolean compareTrees(String tree1, String tree2) {
        String normalizedTree1 = normalizeTree(tree1);
        String normalizedTree2 = normalizeTree(tree2);
        return normalizedTree1.equals(normalizedTree2);
    }

    private static String normalizeTree(String tree) {
        ClientNodeTree root = ClientTreeParser.parseTree(tree);
        return buildNormalizedString(root);
    }

    // Build a normalized string from the tree structure
    private static String buildNormalizedString(ClientNodeTree node) {
        StringBuilder sb = new StringBuilder();
        sb.append(node.nodeId);
        if (!node.children.isEmpty()) {
            sb.append("(");
            List<String> childStrings = new ArrayList<>();
            for (ClientNodeTree child : node.children) {
                childStrings.add(buildNormalizedString(child));
            }
            // Sort children lexicographically
            Collections.sort(childStrings);
            sb.append(String.join(",", childStrings));
            sb.append(")");
        }
        return sb.toString();
    }
}
