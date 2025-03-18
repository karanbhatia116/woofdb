package org.woofdb.core.models;

import java.util.ArrayList;
import java.util.List;

public class ASTNode {
    private String type;
    private String value;
    private List<ASTNode> children;

    public ASTNode(final String type, final String value) {
        this.type = type;
        this.value = value;
        this.children = new ArrayList<>();
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public List<ASTNode> getChildren() {
        return children;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public void setChildren(final List<ASTNode> children) {
        this.children = children;
    }

    public void addChild(final ASTNode child) {
        this.children.add(child);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        printNode(result, "", true);
        return result.toString();
    }

    private void printNode(StringBuilder sb, String prefix, boolean isTail) {
        sb.append(prefix).append(isTail ? "└── " : "├── ");
        sb.append(type);
        if (value != null && !value.isEmpty()) {
            sb.append(": ").append(value);
        }
        sb.append("\n");

        for (int i = 0; i < children.size() - 1; i++) {
            children.get(i).printNode(sb, prefix + (isTail ? "    " : "│   "), false);
        }

        if (children.size() > 0) {
            children.get(children.size() - 1).printNode(sb, prefix + (isTail ? "    " : "│   "), true);
        }
    }
}
