package classpath.scan;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used internally by Classpath Scan to abstract different root types.
 * Classpath Scan has <code>Root</code> implementations for jars and directories.
 */
public abstract class Root {

    private final ClassLoader classLoader;
    private final SortedSet<String> resources;

    public Root(final ClassLoader classLoader, final SortedSet<String> resources) {
	this.classLoader = classLoader;
	this.resources = Collections.unmodifiableSortedSet(resources);
    }

    public SortedSet<String> getResources() {
	return resources;
    }

    public boolean has(String resource) {
	return resources.contains(resource);
    }

    public ClassLoader getClassLoader() {
	return classLoader;
    }

    @Override
    public boolean equals(Object obj) {
	if(getClass() != obj.getClass()) {
	    return false;
	}

	Root that = (Root) obj;
	return classLoader == that.classLoader && resources.equals(that.resources);
    }

    @Override
    public int hashCode() {
	return (7 * classLoader.hashCode()) + (resources.hashCode() * 31);
    }

    public static boolean matchesAll(String resourceName, List<Pattern> patterns) {
	for(Pattern pattern : patterns) {
	    Matcher matcher = pattern.matcher(resourceName);
	    if(!matcher.matches()) {
		return false;
	    }
	}

	return true;
    }

    public abstract <T> T withStream(String resource, ProcessInputStream<T> processor);
    
    public <T> Map<String,T> withStream(ProcessInputStream<T> processor) {
	Map<String,T> ret = new LinkedHashMap<>();
	for(String resource : getResources()) {
	    ret.put(resource, withStream(resource, processor));
	}

	return Collections.unmodifiableMap(ret);
    }
}
