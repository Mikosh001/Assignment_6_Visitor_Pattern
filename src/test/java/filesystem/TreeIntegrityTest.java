package filesystem;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TreeIntegrityTest {
    @Test
    void negativeSizeIsRejectedAndZeroIsAllowed() {
        assertThrows(IllegalArgumentException.class, () -> new FileNode("bad.bin", -1));
        assertEquals(0, new FileNode("empty.bin", 0).getSize());
    }

    @Test
    void namesMustBeValidSinglePathSegments() {
        for (String name : List.of("", " ", ".", "..", "a/b", "a\\b")) {
            assertThrows(IllegalArgumentException.class, () -> new FileNode(name, 1));
            assertThrows(IllegalArgumentException.class, () -> new DirectoryNode(name));
        }
        assertThrows(NullPointerException.class, () -> new FileNode(null, 1));
        assertThrows(NullPointerException.class, () -> new DirectoryNode(null));
    }

    @Test
    void duplicateSiblingNamesAreRejectedAcrossElementTypes() {
        DirectoryNode root = new DirectoryNode("root");
        root.addChild(new FileNode("same", 1));
        DirectoryNode rejected = new DirectoryNode("same");
        assertThrows(IllegalArgumentException.class, () -> root.addChild(rejected));
        assertThrows(IllegalArgumentException.class, () -> root.addChild(new FileNode("same", 2)));
        assertEquals(1, root.getChildren().size());
        DirectoryNode other = new DirectoryNode("other");
        assertDoesNotThrow(() -> other.addChild(rejected));
    }

    @Test
    void filesAndDirectoriesCannotBelongToTwoParents() {
        DirectoryNode first = new DirectoryNode("first");
        DirectoryNode second = new DirectoryNode("second");
        FileNode file = new FileNode("f", 1);
        DirectoryNode directory = new DirectoryNode("d");
        first.addChild(file);
        first.addChild(directory);
        assertThrows(IllegalArgumentException.class, () -> second.addChild(file));
        assertThrows(IllegalArgumentException.class, () -> second.addChild(directory));
        assertTrue(second.getChildren().isEmpty());
        assertEquals(List.of(file, directory), first.getChildren());
    }

    @Test
    void sameObjectCannotBeInsertedTwice() {
        DirectoryNode root = new DirectoryNode("root");
        FileNode file = new FileNode("f", 1);
        root.addChild(file);
        assertThrows(IllegalArgumentException.class, () -> root.addChild(file));
        assertEquals(1, root.getChildren().size());
    }

    @Test
    void selfAndAncestorCyclesAreRejectedWithoutDamagingTheTree() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode middle = new DirectoryNode("middle");
        DirectoryNode leaf = new DirectoryNode("leaf");
        root.addChild(middle);
        middle.addChild(leaf);
        assertThrows(IllegalArgumentException.class, () -> root.addChild(root));
        assertThrows(IllegalArgumentException.class, () -> leaf.addChild(root));
        assertTrue(leaf.getChildren().isEmpty());
        leaf.addChild(new FileNode("valid.txt", 9));
        assertEquals(9, new TotalSizeVisitor().calculate(root));
    }

    @Test
    void childCollectionIsAnUnmodifiableSnapshot() {
        DirectoryNode root = new DirectoryNode("root");
        root.addChild(new FileNode("a", 1));
        List<Node> snapshot = root.getChildren();
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.add(new FileNode("b", 2)));
        root.addChild(new FileNode("c", 3));
        assertEquals(1, snapshot.size());
        assertEquals(2, root.getChildren().size());
    }

    @Test
    void nullChildIsRejectedAndValidNestingIsStillSupported() {
        DirectoryNode root = new DirectoryNode("root");
        assertThrows(NullPointerException.class, () -> root.addChild(null));
        DirectoryNode current = root;
        for (int i = 0; i < 40; i++) {
            DirectoryNode next = new DirectoryNode("level" + i);
            current.addChild(next);
            current = next;
        }
        current.addChild(new FileNode("deep.txt", 17));
        assertEquals(17, new TotalSizeVisitor().calculate(root));
        assertEquals(1, new SearchVisitor(name -> true, 0).search(root).size());
    }
}
