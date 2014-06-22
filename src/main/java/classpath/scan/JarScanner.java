package classpath.scan;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

public class JarScanner extends ElementScanner {
    
    private final List<String> resources;

    public List<String> getResources() {
	return resources;
    }

    private final ClassLoader classLoader;

    public ClassLoader getClassLoader() {
	return classLoader;
    }

    public JarScanner(final ClassLoader classLoader, final List<String> resources) {
	this.classLoader = classLoader;
	this.resources = Collections.unmodifiableList(resources);
    }
    
    private static boolean shouldProcess(final JarFile jar, final List<String> prefixes) {
	if(prefixes == null || prefixes.isEmpty()) {
	    return true;
	}

	for(String prefix : prefixes) {
	    JarEntry entry = (JarEntry) jar.getEntry(prefix);
	    if(entry != null) {
		return true;
	    }
	}

	return false;
    }

    private static boolean matchesPrefixes(final String resourceName, final List<String> prefixes) {
	if(prefixes == null || prefixes.isEmpty()) {
	    return true;
	}

	for(String prefix : prefixes) {
	    if(resourceName.startsWith(prefix)) {
		return true;
	    }
	}
	
	return false;
    }

    public static JarScanner factory(final ClassLoader classLoader, final File jarFile, 
				     final List<String> prefixes, final List<Pattern> patterns) {
	try {
	    JarFile jar = new JarFile(jarFile);
	    List<String> resources = new ArrayList<>();
	    
	    if(shouldProcess(jar, prefixes)) {
		for(Enumeration<JarEntry> iter = jar.entries(); iter.hasMoreElements(); ) {
		    JarEntry entry = iter.nextElement();
		    String resourceName = entry.getName();
		    if(matchesPrefixes(resourceName, prefixes) && matchesAll(resourceName, patterns)) {
			resources.add(resourceName);
		    }
		}
	    }
	    
	    if(!resources.isEmpty()) {
		return new JarScanner(classLoader, resources);
	    }
	    else {
		return null;
	    }
	}
	catch(IOException ioe) {
	    throw new RuntimeException(ioe);
	}
    }	
}
