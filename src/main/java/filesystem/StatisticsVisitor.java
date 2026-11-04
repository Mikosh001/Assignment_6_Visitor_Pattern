package filesystem;

import java.util.Objects;

public final class StatisticsVisitor implements Visitor {
    private int fileCount;
    private int directoryCount;
    private int maximumDepth;
    private int depth;

    public void analyze(Element root) {
        Objects.requireNonNull(root, "root");
        fileCount = 0;
        directoryCount = 0;
        maximumDepth = 0;
        depth = 0;
        root.accept(this);
    }

    @Override
    public void visit(FileNode file) {
        fileCount++;
        maximumDepth = Math.max(maximumDepth, depth);
    }

    @Override
    public void visit(DirectoryNode directory) {
        directoryCount++;
        maximumDepth = Math.max(maximumDepth, depth);
        depth++;
        for (Element child : directory.getChildren()) {
            child.accept(this);
        }
        depth--;
    }

    public int getFileCount() {
        return fileCount;
    }

    public int getDirectoryCount() {
        return directoryCount;
    }

    public int getMaximumDepth() {
        return maximumDepth;
    }
}
