package classpath.scan;

import java.util.Set;
import classpath.scan.jarjar.tree.ClassNode;

/**
 * Simple interface used to test if a class resource matches a condition
 */
public interface ClassNodeMatcher<T> {
    
    /**
     * @param classLoader The classLoader that was used to load the resource.
     * @param node The class node that was constructed from the scanned resource.
     * The <code>ClassNode</code> type comes from the excellent <a href="http://asm.ow2.org/">ASM Library</a>.
     * The package of <code>ClassNode</code> has been changed to <code>classpath.scan.jarjar.tree.ClassNode</code>
     * in case your code uses a different version of asm.  The documentation for the <code>ClassNode</code>
     * type <a href="http://asm.ow2.org/asm50/javadoc/user/org/objectweb/asm/tree/ClassNode.html">can be found at the ASM site</a>
     * @return The entities that matched. Can be anything, but will usually be something inside the
     * <code>java.lang.reflect</code> package, such as <code>Field</code> or <code>Method</code>.
     */
    public Set<T> matches(ClassLoader classLoader, ClassNode node);
}
