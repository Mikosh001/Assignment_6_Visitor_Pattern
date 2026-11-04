package filesystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DirectoryNode extends Node {
    private final List<Node> children = new ArrayList<>();

    public DirectoryNode(String name) {
        super(name);
    }

    public void addChild(Node child) {
        Objects.requireNonNull(child, "child");
        if (child.getParent() != null) {
            throw new IllegalArgumentException("Child already belongs to a directory.");
        }
        for (Node ancestor = this; ancestor != null; ancestor = ancestor.getParent()) {
            if (ancestor == child) {
                throw new IllegalArgumentException("Adding this child would create a cycle.");
            }
        }
        for (Node existing : children) {
            if (existing.getName().equals(child.getName())) {
                throw new IllegalArgumentException("Duplicate sibling name: " + child.getName());
            }
        }
        children.add(child);
        child.attachTo(this);
    }

    public List<Node> getChildren() {
        return List.copyOf(children);
    }

    @Override
    public void accept(Visitor visitor) {
        Objects.requireNonNull(visitor, "visitor").visit(this);
    }
}
