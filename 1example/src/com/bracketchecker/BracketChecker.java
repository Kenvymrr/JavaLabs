package com.bracketchecker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class BracketChecker {
    private Map<Character, Character> openingToClosing; // Maps opening to closing bracket
    private Map<Character, Character> closingToOpening; // Maps closing to opening bracket
    private Set<Character> allBrackets; // All bracket characters
    private Set<Character> selfPaired; // Brackets that are both opening and closing

    public BracketChecker(String configFilePath) throws IOException {
        openingToClosing = new HashMap<>();
        closingToOpening = new HashMap<>();
        allBrackets = new HashSet<>();
        selfPaired = new HashSet<>();
        loadConfig(configFilePath);
    }

    // Load bracket pairs from JSON configuration file
    private void loadConfig(String configFilePath) throws IOException {
        String jsonContent = new String(Files.readAllBytes(Paths.get(configFilePath)));
        JSONObject jsonObject = new JSONObject(jsonContent);
        JSONArray brackets = jsonObject.getJSONArray("bracket");

        for (int i = 0; i < brackets.length(); i++) {
            JSONObject bracket = brackets.getJSONObject(i);
            char left = bracket.getString("left").charAt(0);
            char right = bracket.getString("right").charAt(0);
            openingToClosing.put(left, right);
            closingToOpening.put(right, left);
            allBrackets.add(left);
            allBrackets.add(right);
            if (left == right) {
                selfPaired.add(left);
            }
        }
    }

    // Check if brackets in the input file are correctly matched
    public String checkBrackets(String inputFilePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(inputFilePath)));
        Stack<Character> stack = new Stack<>(); // Tracks opening brackets
        Stack<Integer> positionStack = new Stack<>(); // Tracks positions for error reporting

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);

            // Skip non-bracket characters
            if (!allBrackets.contains(c)) {
                continue;
            }

            // Handle self-paired brackets (e.g., |)
            if (selfPaired.contains(c)) {
                if (!stack.isEmpty() && openingToClosing.get(stack.peek()) == c) {
                    // Treat as closing bracket
                    stack.pop();
                    positionStack.pop();
                } else {
                    // Treat as opening bracket
                    stack.push(c);
                    positionStack.push(i + 1);
                }
            }
            // Handle regular opening brackets
            else if (openingToClosing.containsKey(c)) {
                stack.push(c);
                positionStack.push(i + 1);
            }
            // Handle regular closing brackets
            else if (closingToOpening.containsKey(c)) {
                if (stack.isEmpty()) {
                    return "Error: Unmatched closing bracket '" + c + "' at position " + (i + 1);
                }
                char lastOpening = stack.peek();
                if (openingToClosing.get(lastOpening) != c) {
                    return "Error: Mismatched bracket '" + c + "' at position " + (i + 1) +
                            ", expected '" + openingToClosing.get(lastOpening) + "' for opening bracket at position " + positionStack.peek();
                }
                stack.pop();
                positionStack.pop();
            }
        }

        if (!stack.isEmpty()) {
            return "Error: Unclosed opening bracket '" + stack.peek() + "' at position " + positionStack.peek();
        }

        return "Success: All brackets are correctly matched.";
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java BracketChecker <config_file> <input_file>");
            return;
        }

        try {
            BracketChecker checker = new BracketChecker(args[0]);
            String result = checker.checkBrackets(args[1]);
            System.out.println(result);
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}