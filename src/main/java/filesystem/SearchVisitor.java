package filesystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class SearchVisitor implements Visitor {
    private final Predicate<String> filenameCriterion;
    private final long minimumSize;
    private final List<String> matches = new ArrayList<>();
    private String directoryPath;
    private int depth;

    public SearchVisitor(Predicate<String> filenameCriterion, long minimumSize) {
        this.filenameCriterion = Objects.requireNonNull(filenameCriterion, "filenameCriterion");
        if (minimumSize < 0) {
            throw new IllegalArgumentException("Minimum size cannot be negative.");
        }
        this.minimumSize = minimumSize;
    }

    public List<String> search(Element root) {
        Objects.requireNonNull(root, "root");
        matches.clear();
        directoryPath = "";
        depth = 0;
        root.accept(this);
        Collections.sort(matches);
        return List.copyOf(matches);
    }

    @Override
    public void visit(FileNode file) {
        if (filenameCriterion.test(file.getName()) && file.getSize() >= minimumSize) {
            matches.add(directoryPath + file.getName());
        }
    }

    @Override
    public void visit(DirectoryNode directory) {
        String previousPath = directoryPath;
        if (depth > 0) {
            directoryPath += directory.getName() + "/";
        }
        depth++;
        for (Element child : directory.getChildren()) {
            child.accept(this);
        }
        depth--;
        directoryPath = previousPath;
    }
}
