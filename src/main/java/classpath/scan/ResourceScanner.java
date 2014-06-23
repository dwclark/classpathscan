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

    public List<String> getPrefixes() {
	return prefixes;
    }

    final private List<ElementScanner> scanners;

    public List<ElementScanner> getScanners() {
	return scanners;
    }

    final private List<Pattern> patterns;

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

    public ResourceScanner() {
	this(fixClassLoader(null), fixPrefixes(null), fixPatterns(null, null));
    }

    public ResourceScanner(final ClassLoader classLoader) {
	this(fixClassLoader(classLoader), fixPrefixes(null), fixPatterns(null, null));
    }

    public ResourceScanner(final ClassLoader classLoader, final String[] prefixes) {
	this(fixClassLoader(classLoader), fixPrefixes(prefixes), fixPatterns(null, null));
    }

    public ResourceScanner(final ClassLoader classLoader, final Pattern[] patterns) {
	this(fixClassLoader(classLoader), fixPrefixes(null), fixPatterns(patterns, null));
    }
    
    public ResourceScanner(final ClassLoader classLoader, final String[] prefixes, final Pattern[] patterns) {
	this(fixClassLoader(classLoader), fixPrefixes(prefixes), fixPatterns(patterns, null));
    }

    protected ResourceScanner(final ClassLoader classLoader, final List<String> prefixes, final List<Pattern> patterns) {
	this.prefixes = Collections.unmodifiableList(prefixes);
	this.patterns = Collections.unmodifiableList(patterns);
	this.scanners = Collections.unmodifiableList(findScanners(classLoader, new ArrayList<ElementScanner>()));
    }
	
    private List<ElementScanner> findScanners(final ClassLoader classLoader, final List<ElementScanner> result) {
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
			    DirectoryScanner scanner = DirectoryScanner.factory(classLoader, asFile, prefixes, patterns);
			    if(scanner != null) {
				result.add(scanner);
			    }
			}
			else if(asFile.getName().endsWith("jar")) {
			    JarScanner scanner = JarScanner.factory(classLoader, asFile, prefixes, patterns);
			    if(scanner != null) {
				result.add(scanner);
			    }
			}
		    }
		}
	    }

	    return findScanners(classLoader.getParent(), result);
	}
	catch(URISyntaxException e) {
	    throw new RuntimeException(e);
	}
    }

    public Set<String> findMatches(final ResourceMatcher matcher) {
	Set<String> ret = new HashSet<>();
	for(ElementScanner scanner : scanners) {
	    for(String resource : scanner.getResources()) {
		if(matcher.matches(resource)) {
		    ret.add(resource);
		}
	    }
	}

	return ret;
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
