package filesystem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatisticsVisitorTest {
    @Test
    void statisticsCountAllNodesAndMeasureDepthFromZero() {
        DirectoryNode root = new DirectoryNode("root");
        root.addChild(new FileNode("top.txt", 10));
        DirectoryNode nested = new DirectoryNode("nested");
        DirectoryNode deeper = new DirectoryNode("deeper");
        deeper.addChild(new FileNode("bottom.txt", 20));
        nested.addChild(deeper);
        root.addChild(nested);
        root.addChild(new DirectoryNode("empty"));

        StatisticsVisitor visitor = new StatisticsVisitor();
        visitor.analyze(root);
        assertStatistics(visitor, 2, 4, 3);
    }

    @Test
    void anEmptyDirectoryRootCountsAsOneDirectoryAtDepthZero() {
        StatisticsVisitor visitor = new StatisticsVisitor();
        visitor.analyze(new DirectoryNode("empty"));
        assertStatistics(visitor, 0, 1, 0);
    }

    @Test
    void depthIncludesEmptyDirectoriesAndRestartsAtTheChosenRoot() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode nested = new DirectoryNode("nested");
        DirectoryNode empty = new DirectoryNode("empty");
        root.addChild(nested);
        nested.addChild(empty);
        StatisticsVisitor visitor = new StatisticsVisitor();
        visitor.analyze(root);
        assertStatistics(visitor, 0, 3, 2);
        visitor.analyze(nested);
        assertStatistics(visitor, 0, 2, 1);
        visitor.analyze(new FileNode("single.txt", 0));
        assertStatistics(visitor, 1, 0, 0);
    }

    @Test
    void statisticsResetAfterRepeatedRunsAndReflectLaterValidChanges() {
        DirectoryNode root = new DirectoryNode("root");
        root.addChild(new FileNode("a.txt", 4));
        StatisticsVisitor visitor = new StatisticsVisitor();
        visitor.analyze(root);
        assertStatistics(visitor, 1, 1, 1);
        new SearchVisitor(name -> true, 0).search(root);
        visitor.analyze(root);
        assertStatistics(visitor, 1, 1, 1);
        root.addChild(new DirectoryNode("empty"));
        root.addChild(new FileNode("b.txt", 7));
        assertEquals(11, new TotalSizeVisitor().calculate(root));
        visitor.analyze(root);
        assertStatistics(visitor, 2, 2, 1);
        visitor.analyze(new DirectoryNode("empty"));
        assertStatistics(visitor, 0, 1, 0);
        assertThrows(NullPointerException.class, () -> visitor.analyze(null));
    }

    private static void assertStatistics(StatisticsVisitor visitor, int files, int directories, int depth) {
        assertAll(
                () -> assertEquals(files, visitor.getFileCount()),
                () -> assertEquals(directories, visitor.getDirectoryCount()),
                () -> assertEquals(depth, visitor.getMaximumDepth())
        );
    }
}
