package classpath.scan;

import java.util.Set;
import classpath.scan.jarjar.tree.ClassNode;

public interface ClassNodeMatcher<T> {
    public Set<T> matches(ClassLoader classLoader, ClassNode node);
}
