# Assignment 6 | Visitor Pattern

Berdibek Meiirbek, SE-2528

An in-memory file tree with `TotalSizeVisitor`, `SearchVisitor` and `StatisticsVisitor`. Requires JDK 17 and Maven 3.6.3 or newer.

## Run

From the folder containing `pom.xml`:

```text
mvn test
mvn -q compile exec:java
```

In IntelliJ IDEA, open `pom.xml` as a project, select JDK 17, let Maven import dependencies, and run `app.Main`. No program arguments are needed.

```text
Total size: 1040 bytes
Matches (.txt, >= 100 bytes):
  docs/drafts/plan.txt
  docs/report.txt
  readme.txt
Statistics: files=5, directories=5, maximum depth=3
```

## Design

- `Element` defines `accept(Visitor)`. Both concrete nodes call `visitor.visit(this)`; `Visitor` declares both overloads.
- `Node` shares name validation and package-private parent bookkeeping. `DirectoryNode.addChild()` rejects duplicate names, existing ownership and cycles before mutation. Child snapshots are unmodifiable. Names are case-sensitive path segments.
- Visitors own recursive traversal. `accept()` does not recurse. Start each operation with `calculate(root)`, `search(root)` or `analyze(root)`; these reset all accumulated state, so instances are reusable. Direct `visit()`/`accept()` calls are traversal callbacks, not the reset entry points.
- Search takes a `Predicate<String>` and an inclusive minimum byte size. Paths omit the chosen directory root, use `/` and sort lexicographically. A file root is reported by its filename. Depth counts edges from the chosen root at zero, including empty directories.
- Size uses `long`; overflow throws `ArithmeticException`. Visitors are used sequentially; do not mutate the tree during traversal.

Change the predicate or threshold in `Main` to demonstrate different searches. With the same `.txt` predicate and threshold `240`, only `docs/report.txt` matches.

## Verification and extension

24 JUnit 5 tests: `VisitorTest` (12), `TreeIntegrityTest` (8), `StatisticsVisitorTest` (4). They cover dispatch, all operations, paths, boundaries, reuse, empty trees and invalid mutations.

The second commit adds statistics without changing `Element`, `Visitor`, `Node`, `FileNode` or `DirectoryNode`. A new operation needs another `Visitor` implementation; a new element type requires another overload and changes to all visitors.

See [report.pdf](report.pdf) and the PlantUML files in `uml/`.

Reference: [Refactoring.Guru - Visitor](https://refactoring.guru/design-patterns/visitor). OpenAI Codex assisted with implementation, documentation and verification.
