package classpath.scan;

import java.util.Set;
import org.objectweb.asm.tree.ClassNode;

public interface ClassNodeMatcher<T> {
    public Set<T> matches(ClassLoader classLoader, ClassNode node);
}
