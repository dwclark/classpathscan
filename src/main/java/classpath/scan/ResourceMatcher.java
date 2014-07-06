package classpath.scan;

/**
 * A simple interface for testing if a resource matches an arbitrary condition.
 * Users of Groovy, Scala, Clojure, or Java 8 will typically implement this
 * using a closure/lambda expression/anonymous function.
 *
 * <p>Resource names will never have a leading '/' and will separate path
 * elements with a '/' character.  Some simple examples:
 * <ul>
 * <li>A <code>config.properties</code> file on the root of the classpath will have
 * a resource name of <code>config.properties</code></li>
 * <li>A class named <code>Foo</code> in the <code>com.acme</code> package
 * will have a resource name of <code>com/acme/Foo.class</code>
 * <li>An imaged named <code>simple.png</code> in the <code>images</code> folder
 * on the root of the classpath will have a resource name of <code>images/simple.png</code>
 */
public interface ResourceMatcher {
    
    /**
     *
     * @param resourceName The name of the resource.
     * @return Whether or not the resourceName matched the condition.
     */
    public boolean matches(String resourceName);
}
