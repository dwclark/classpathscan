package classpath.scan;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class ResourceScanner {
	
    final private List<String> prefixes;

    /**
     * Returns a list of prefixes that are checked before making a particular resource
     * available for scanning.  As long as one prefix in this list is matched, the
     * resource will be available for scanning.
     *
     * @return The list of checked prefixes
     */
    public List<String> getPrefixes() {
	return prefixes;
    }

    final private List<Root> roots;

    /**
     * Returns a list of roots (directory or jar) that contain at least one of the
     * matching resources specified at construction time.  Any root in this list
     * will have at least one resource that matches at least one prefix and all
     * of the patterns.
     *
     * @return The list of matching roots
     */
    public List<Root> getRoots() {
	return roots;
    }

    final private List<Pattern> patterns;

    /**
     * Returns a list of patterss that are checked before making a particular resource
     * available for scanning.  Every pattern must match in a particular resource for
     * a <code>ResourceScanner</code> instance to include that resource.
     *
     * @return The list of patterns 
     */
    public List<Pattern> getPatterns() {
	return patterns;
    }

    protected static List<Pattern> fixPatterns(final Pattern[] patterns, final Pattern mustHave) {
	if(patterns == null && mustHave == null) {
	    return Collections.<Pattern>emptyList();
	}

	List<Pattern> ret = new ArrayList<>();
	if(patterns != null) {
	    for(Pattern pattern : patterns) {
		ret.add(pattern);
	    }
	}
	
	if(mustHave != null) {
	    for(Pattern pattern : ret) {
		if(pattern.equals(mustHave)) {
		    return ret;
		}
	    }

	    ret.add(mustHave);
	}

	return ret;
    }

    protected static List<String> fixPrefixes(final String[] prefixes) {
	return (prefixes == null) ? Collections.<String>emptyList() : Arrays.asList(prefixes);
    }

    protected static ClassLoader fixClassLoader(final ClassLoader classLoader) {
	return (classLoader == null) ? ResourceScanner.class.getClassLoader() : classLoader; 
    }

    /**
     * Creates a <code>ResourceScanner</code> that will scan the classloader returned
     * by calling <code>ResourceScanner.class.getClassLoader()</code>.  Every resource
     * in that classloader will be made available for scanning.  Equivalent to calling
     * <code>ResourceScanner(null, null, null)</code>
     */
    public ResourceScanner() {
	this(fixClassLoader(null), fixPrefixes(null), fixPatterns(null, null));
    }

    /**
     * Creates a <code>ResourceScanner</code> that will scan the passed classloader.
     * Every resource in that classlaoer will be make available for scanning. Passing
     * <code>null</code> for the classloader argument is equivalent to calling
     * <code>ResourceScanner(classLoader, null, null)</code>
     *
     * @param classLoader The classloader to scan
     */
    public ResourceScanner(final ClassLoader classLoader) {
	this(fixClassLoader(classLoader), fixPrefixes(null), fixPatterns(null, null));
    }

    /**
     * Creates a <code>ResourceScanner</code> that will scan the passed classloader, making
     * only the resources whose prefixes match at least one of the prefixes available for scanning.
     * Equivalent to calling <code>ResourceScanner(classLoader, prefixes, null)</code>.
     *
     * @param classLoader The classloader to scan
     * @param prefixes The prefixes to check
     */
    public ResourceScanner(final ClassLoader classLoader, final String[] prefixes) {
	this(fixClassLoader(classLoader), fixPrefixes(prefixes), fixPatterns(null, null));
    }

    /**
     * Creates a <code>ResourceScanner</code> that will scan the passed classloader, making
     * only the resources who match all of the patterns available for scanning.
     * Equivalent to calling <code>ResourceScanner(classLoader, null, patterns)</code>.
     *
     * @param classLoader The classloader to scan
     * @param patterns The patterns to check
     */
    public ResourceScanner(final ClassLoader classLoader, final Pattern[] patterns) {
	this(fixClassLoader(classLoader), fixPrefixes(null), fixPatterns(patterns, null));
    }
    
    /**
     * The main constructor for <code>ResourceScanner</code>.  All other constructors
     * are just shortcuts for calling this constructor.
     *
     * <p>Prefixes should be specified without a leading '/' character, and each path
     * element should be separated by a '/' character.  As an example, suppose you want
     * to match the following resources: "com/foo/bar/config.properties", "com/foo/Foo.class",
     * and "org/other/stuff/important.txt", but not "org/other/config.properties".  You
     * could achieve this by constructing the prefixes array like this:
     * 
     * <p><code>String[] prefixes = new String[] { "com/foo", "org/other/stuff" };</code>
     *
     * <p>Patterns must all match for a particular resource to be included for scanning.
     * Ideally, you could always include a single complex regex that did the exact
     * matching you need.  However, sometimes it is simpler to include several simpler
     * regexes, so <code>ResourceScanner</code> gives you that ability.  For example, suppose
     * you want to only match properties files.  You could achieve this by constructing
     * the patterns array like this:
     *
     * <p><code>Pattern[] patterns = new Pattern[] { Pattern.compile(".*&#092;&#092;.properties$") };</code>
     *
     * @param classLoader The classloader to scan.  The resource scanner will scan this
     * classloader and also any ancestor classloaders of the passed classloader.
     * @param prefixes The list of prefixes checked for matching before future scanning
     * takes place.
     * @param patterns The list of patterns checked for matching before future scanning
     * takes place.
     */
    public ResourceScanner(final ClassLoader classLoader, final String[] prefixes, final Pattern[] patterns) {
	this(fixClassLoader(classLoader), fixPrefixes(prefixes), fixPatterns(patterns, null));
    }

    /**
     * Internal constructor used by <code>ResourceScanner</code>.
     */
    protected ResourceScanner(final ClassLoader classLoader, final List<String> prefixes, final List<Pattern> patterns) {
	this.prefixes = Collections.unmodifiableList(prefixes);
	this.patterns = Collections.unmodifiableList(patterns);
	this.roots = Collections.unmodifiableList(findRoots(classLoader, new ArrayList<Root>()));
    }
	
    private List<Root> findRoots(final ClassLoader classLoader, final List<Root> result) {
	try {
	    if(classLoader == null) {
		return result;
	    }
	    
	    if(classLoader instanceof URLClassLoader) {
		URL[] urls = ((URLClassLoader) classLoader).getURLs();
		for(URL url : urls) {
		    if(url.getProtocol().equals("file")) {
			File asFile = new File(url.toURI());
			if(asFile.isDirectory()) {
			    DirectoryRoot root = DirectoryRoot.factory(classLoader, asFile, prefixes, patterns);
			    if(root != null) {
				result.add(root);
			    }
			}
			else if(asFile.getName().endsWith("jar")) {
			    JarRoot root = JarRoot.factory(classLoader, asFile, prefixes, patterns);
			    if(root != null) {
				result.add(root);
			    }
			}
		    }
		}
	    }

	    return findRoots(classLoader.getParent(), result);
	}
	catch(URISyntaxException e) {
	    throw new RuntimeException(e);
	}
    }

    public Set<String> findMatches(final ResourceMatcher matcher) {
	Set<String> ret = new HashSet<>();
	for(Root root : roots) {
	    for(String resource : root.getResources()) {
		if(matcher.matches(resource)) {
		    ret.add(resource);
		}
	    }
	}

	return Collections.unmodifiableSet(ret);
    }
    
    public Set<String> getAll() {
	return findMatches(new ResourceMatcher() {
		public boolean matches(String resource) {
		    return true;
		} });
    }

    public Set<String> findMatches(final Pattern pattern) {
	return findMatches(new ResourceMatcher() {
		public boolean matches(String resource) {
		    return pattern.matcher(resource).matches();
		} });
    }
}
