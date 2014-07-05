/**
 * Classpath Scan does what it says, it scans the classpath for resources.
 *
 * <p>Classpath Scan scans local classpath resources and provides a simple
 * interface to query and retrieve resources.  Classpath Scan does not scan
 * or load resources that are not on the local system (that is what is meant by "local").
 * There are two main entry points for scanning the classpath.
 *
 * <p>The first entry point is the {@link classpath.scan.ResourceScanner}.  Use the <code>ResourceScanner</code>
 * when you want to look for resources that are not Java class files.  The <code>ResourceScanner</code>
 * gives you the ability to locate resources using Java regexes and to
 * process these resources as <code>InputStream</code>s without having to know the
 * source (directory, jar, etc.) of the resource.
 *
 * <p>The second entry point is the {@link classpath.scan.ClassScanner}.  Use the <code>ClassScanner</code>
 * when you are dealing with locating Java types or information stored in Java types, such as
 * <code>Field</code>s, <code>Method</code>, or <code>Annotation</code>s.
 *
 * <p>Whichever scanner you use, you should always keep in mind that the time it takes
 * to scan resources will always be proportional to the number of resources scanned.
 * To keep scan times low, try and construct <code>ClassScanner</code>s and <code>ResourceScanner</code>s
 * as restrictively as possible.  For example if you know that you will only find classes
 * of interest in the com.foo.bar package, make sure to specify that package in the 
 * constructor to <code>ClassScanner</code>.  Classpath Scan will work fine with less restrictive
 * arguments, but it will take longer and use more memory while it is scanning.
 *
 * <p>Classpath Scan is thread safe by default and it achieves this through heavy use
 * of immutable data structures and final variables.  To enforce this consistency, all
 * data returned by Classpath Scan is also immutable.  To mutate the data returned by
 * Classpath Scan will always require you to copy the returned data into a mutable
 * data structure.
 */
package classpath.scan;
