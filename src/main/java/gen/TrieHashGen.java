package gen;
import java.util.*;

/*
MIT License
Copyright (c) 2020 Chris Uzdavinis

Permission is hereby granted, free of charge, to any person obtaining a copy
        of this software and associated documentation files (the "Software"), to deal
        in the Software without restriction, including without limitation the rights
        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
        copies of the Software, and to permit persons to whom the Software is
        furnished to do so, subject to the following conditions:

        The above copyright notice and this permission notice shall be included in all
        copies or substantial portions of the Software.

        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
        SOFTWARE.
*/
public class TrieHashGen {

    private static final int defaultIndentation = 4;

    private static class KeyData {
        public KeyData(String s, int idx) {
            this.value = s;
            this.index = idx;
        }
        final String value;
        final int index;
    }
    private class Node {
        Node(List<KeyData> keys, int depth) {
            this.depth = depth;
            this.children = keys;
            this.childNodes = new HashMap<>();
            validate();

            for (Map.Entry<Character, ArrayList<KeyData>> entry : partitionChildren(keys, depth).entrySet()) {
                childNodes.put(entry.getKey(), new Node(entry.getValue(), depth + 1));
            }
        }

        public void generate(StringBuilder sb) {
            println(sb, "switch (*str++) {");
            for (Map.Entry<Character, Node> entry : childNodes.entrySet()) {
                println(sb, "case '" + entry.getKey() + "': ");
                entry.getValue().generate(sb);
            }

            KeyData terminal = findTerminal();
            if (terminal != null) {
                println(sb, "case 0:");
                println(sb, depth + 1, "return " + terminal.index + ";");
            }
            println(sb, "}");
            if (depth > 0) {
                println(sb, "break;");
            }
        }

        KeyData findTerminal() {
            for (KeyData child : children) {
                if (child.value.length() == depth) {
                    return child;
                }
            }
            return null;
        }

        void println(StringBuilder sb, String str) {
            TrieHashGen.this.println(sb, depth + 1, str);
        }
        void println(StringBuilder sb, int depth, String str) {
            TrieHashGen.this.println(sb, depth + 1, str);
        }

        void indent(StringBuilder sb) {
            TrieHashGen.this.indent(sb, depth + 1);
        }

        private HashMap<Character, ArrayList<KeyData>> partitionChildren(List<KeyData> keys, int depth) {
            HashMap<Character, ArrayList<KeyData>> partitionedChildren = new HashMap<>();
            for (KeyData child : keys) {
                if (child.value.length() <= depth) {
                    continue;
                }
                ArrayList<KeyData> childrenWithChar = partitionedChildren.computeIfAbsent(
                        child.value.charAt(depth),
                        k -> new ArrayList<>());
                childrenWithChar.add(child);
            }
            return partitionedChildren;
        }

        private void validate() {
            ensure(children.size() > 0, "Empty set of children");
            char expectedChar = depth > 0 ? children.get(0).value.charAt(depth - 1) : '?';
            for (KeyData key : children) {
                ensure(key.value.length() >= depth, "key " + key +
                        " too short for depth of " + depth);

                if (depth > 0) {
                    ensure(key.value.charAt(depth - 1) == expectedChar,
                            "Invalid keys, mismatch at char: " + depth);
                }
            }
        }

        private void ensure(boolean condition, String message) {
            if (!condition) {
                StringBuilder messageBuilder = new StringBuilder(message);
                messageBuilder.append("\n\tdepth=").append(depth);
                for (KeyData child : children) {
                    messageBuilder.append("\n\t").append(child.value);
                }
                message = messageBuilder.toString();
                throw new RuntimeException(message);
            }
        }

        private final int depth;
        private final List<KeyData> children;
        private final Map<Character, Node> childNodes;
    }

    private final Node root;
    private int indentation = 4;

    public TrieHashGen(List<String> keys) {
        this(keys, defaultIndentation);
    }

    public TrieHashGen(List<String> keys, int indentation)
    {
        this.indentation = indentation;
        int index = 0;
        ArrayList<KeyData> keydata = new ArrayList<>();
        for (String key : keys) {
            keydata.add(new KeyData(key, index++));
        }
        root = new Node(keydata, 0);
    }

    public String generate() {
        StringBuilder sb = new StringBuilder();
        println(sb, 0, "int setContains(char const * str) {");
        root.generate(sb);
        println(sb, 0, "}");
        return sb.toString();
    }

    void println(StringBuilder sb, int depth, String... values) {
        indent(sb, depth);
        for (String str : values) {
            sb.append(str);
        }
        sb.append("\n");
    }

    void indent(StringBuilder sb, int depth) {
        for (int i = 0; i < indentation * depth; ++i)
            sb.append(' ');
    }
}
