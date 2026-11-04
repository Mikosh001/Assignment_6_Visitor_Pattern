package filesystem;

import java.util.Objects;

public abstract class Node implements Element {
    private final String name;
    private DirectoryNode parent;

    protected Node(String name) {
        Objects.requireNonNull(name, "name");
        if (name.isBlank() || name.equals(".") || name.equals("..")
                || name.contains("/") || name.contains("\\")) {
            throw new IllegalArgumentException("Name must be a single non-blank path segment.");
        }
        this.name = name;
    }

    @Override
    public final String getName() {
        return name;
    }

    final DirectoryNode getParent() {
        return parent;
    }

    final void attachTo(DirectoryNode parent) {
        this.parent = parent;
    }
}
