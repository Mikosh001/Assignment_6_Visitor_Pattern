package app;

import filesystem.DirectoryNode;
import filesystem.FileNode;
import filesystem.SearchVisitor;
import filesystem.StatisticsVisitor;
import filesystem.TotalSizeVisitor;

public class Main {
    public static void main(String[] args) {
        DirectoryNode root = createTree();
        TotalSizeVisitor sizeVisitor = new TotalSizeVisitor();
        SearchVisitor searchVisitor = new SearchVisitor(name -> name.endsWith(".txt"), 100);
        StatisticsVisitor statisticsVisitor = new StatisticsVisitor();

        System.out.println("Total size: " + sizeVisitor.calculate(root) + " bytes");
        System.out.println("Matches (.txt, >= 100 bytes):");
        for (String path : searchVisitor.search(root)) {
            System.out.println("  " + path);
        }
        statisticsVisitor.analyze(root);
        System.out.println("Statistics: files=" + statisticsVisitor.getFileCount()
                + ", directories=" + statisticsVisitor.getDirectoryCount()
                + ", maximum depth=" + statisticsVisitor.getMaximumDepth());
    }

    private static DirectoryNode createTree() {
        DirectoryNode root = new DirectoryNode("workspace");
        root.addChild(new FileNode("readme.txt", 100));

        DirectoryNode docs = new DirectoryNode("docs");
        docs.addChild(new FileNode("report.txt", 240));
        docs.addChild(new FileNode("notes.txt", 80));
        DirectoryNode drafts = new DirectoryNode("drafts");
        drafts.addChild(new FileNode("plan.txt", 120));
        docs.addChild(drafts);
        root.addChild(docs);

        DirectoryNode images = new DirectoryNode("images");
        images.addChild(new FileNode("logo.png", 500));
        root.addChild(images);
        root.addChild(new DirectoryNode("empty"));
        return root;
    }
}
