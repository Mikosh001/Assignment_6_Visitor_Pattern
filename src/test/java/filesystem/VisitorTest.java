package filesystem;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VisitorTest {
    @Test
    void concreteElementsSelectTheirMatchingOverloads() {
        List<String> calls = new ArrayList<>();
        Visitor visitor = new Visitor() {
            @Override
            public void visit(FileNode file) {
                calls.add("file:" + file.getName());
            }

            @Override
            public void visit(DirectoryNode directory) {
                calls.add("directory:" + directory.getName());
            }
        };
        Element file = new FileNode("data.txt", 7);
        Element directory = new DirectoryNode("folder");
        file.accept(visitor);
        directory.accept(visitor);
        assertEquals(List.of("file:data.txt", "directory:folder"), calls);
    }

    @Test
    void sizeIncludesEveryNestedFileOnce() {
        assertEquals(220, new TotalSizeVisitor().calculate(createTree()));
    }

    @Test
    void sizeSupportsAFileRootAndZeroByteFiles() {
        TotalSizeVisitor visitor = new TotalSizeVisitor();
        assertEquals(6_000_000_000L, visitor.calculate(new FileNode("large.bin", 6_000_000_000L)));
        assertEquals(0, visitor.calculate(new FileNode("zero.bin", 0)));
    }

    @Test
    void sizeRejectsLongOverflowInsteadOfReturningAWrongTotal() {
        DirectoryNode root = new DirectoryNode("root");
        root.addChild(new FileNode("large.bin", Long.MAX_VALUE));
        root.addChild(new FileNode("extra.bin", 1));
        assertThrows(ArithmeticException.class, () -> new TotalSizeVisitor().calculate(root));
    }

    @Test
    void searchCombinesNameAndInclusiveSizeConditions() {
        SearchVisitor visitor = new SearchVisitor(name -> name.endsWith(".txt"), 40);
        assertEquals(List.of("docs/note.txt", "z.txt"), visitor.search(createTree()));
    }

    @Test
    void searchCriterionCanSelectAnExactFilename() {
        SearchVisitor visitor = new SearchVisitor(name -> name.equals("ignore.bin"), 100);
        assertEquals(List.of("docs/inner/ignore.bin"), visitor.search(createTree()));
    }

    @Test
    void searchSortsPathsAndExcludesTheChosenDirectoryRootName() {
        SearchVisitor visitor = new SearchVisitor(name -> name.endsWith(".txt"), 0);
        DirectoryNode tree = createTree();
        assertEquals(List.of("docs/inner/report.txt", "docs/note.txt", "z.txt"), visitor.search(tree));

        DirectoryNode subtree = new DirectoryNode("chosen");
        subtree.addChild(new FileNode("z.txt", 3));
        DirectoryNode nested = new DirectoryNode("a");
        nested.addChild(new FileNode("b.txt", 4));
        subtree.addChild(nested);
        DirectoryNode parent = new DirectoryNode("outside");
        parent.addChild(subtree);
        assertEquals(List.of("a/b.txt", "z.txt"), visitor.search(subtree));
    }

    @Test
    void emptyDirectoryAndNoMatchesProduceEmptyResults() {
        DirectoryNode empty = new DirectoryNode("empty");
        assertEquals(0, new TotalSizeVisitor().calculate(empty));
        assertTrue(new SearchVisitor(name -> true, 0).search(empty).isEmpty());
        assertTrue(new SearchVisitor(name -> name.endsWith(".pdf"), 0).search(createTree()).isEmpty());
    }

    @Test
    void searchHandlesAFileRootWithoutAddingDirectoryPrefixes() {
        SearchVisitor visitor = new SearchVisitor(name -> true, 5);
        assertEquals(List.of("single.txt"), visitor.search(new FileNode("single.txt", 5)));
        assertTrue(visitor.search(new FileNode("small.txt", 4)).isEmpty());
    }

    @Test
    void visitorsResetBetweenRunsAndDoNotDependOnOperationOrder() {
        DirectoryNode root = createTree();
        TotalSizeVisitor size = new TotalSizeVisitor();
        SearchVisitor search = new SearchVisitor(name -> name.endsWith(".txt"), 40);
        List<String> expected = List.of("docs/note.txt", "z.txt");
        assertEquals(expected, search.search(root));
        assertEquals(220, size.calculate(root));
        assertEquals(220, size.calculate(root));
        assertEquals(expected, search.search(root));
        root.addChild(new FileNode("new.txt", 50));
        assertEquals(270, size.calculate(root));
        assertEquals(List.of("docs/note.txt", "new.txt", "z.txt"), search.search(root));
        assertEquals(0, size.calculate(new DirectoryNode("empty")));
    }

    @Test
    void searchReturnsAnImmutableSnapshot() {
        SearchVisitor visitor = new SearchVisitor(name -> true, 0);
        List<String> result = visitor.search(new FileNode("a.txt", 1));
        assertThrows(UnsupportedOperationException.class, () -> result.add("fake.txt"));
        visitor.search(new DirectoryNode("empty"));
        assertEquals(List.of("a.txt"), result);
    }

    @Test
    void invalidVisitorInputsAreRejectedAndTheNextRunStillWorks() {
        assertThrows(NullPointerException.class, () -> new SearchVisitor(null, 0));
        assertThrows(IllegalArgumentException.class, () -> new SearchVisitor(name -> true, -1));
        TotalSizeVisitor size = new TotalSizeVisitor();
        SearchVisitor search = new SearchVisitor(name -> true, 0);
        assertThrows(NullPointerException.class, () -> size.calculate(null));
        assertThrows(NullPointerException.class, () -> search.search(null));
        assertThrows(NullPointerException.class, () -> new FileNode("f", 0).accept(null));
        assertThrows(NullPointerException.class, () -> new DirectoryNode("d").accept(null));
        assertEquals(2, size.calculate(new FileNode("f", 2)));
        assertEquals(List.of("f"), search.search(new FileNode("f", 2)));
    }

    private static DirectoryNode createTree() {
        DirectoryNode root = new DirectoryNode("root");
        root.addChild(new FileNode("z.txt", 60));
        DirectoryNode docs = new DirectoryNode("docs");
        docs.addChild(new FileNode("note.txt", 40));
        DirectoryNode inner = new DirectoryNode("inner");
        inner.addChild(new FileNode("report.txt", 20));
        inner.addChild(new FileNode("ignore.bin", 100));
        docs.addChild(inner);
        root.addChild(docs);
        root.addChild(new DirectoryNode("empty"));
        return root;
    }
}
