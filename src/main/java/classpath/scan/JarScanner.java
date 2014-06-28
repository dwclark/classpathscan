package classpath.scan;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.io.InputStream;

public class JarScanner extends ElementScanner {

    private final JarFile jar;

    public <T> T withStream(String resource, ProcessInputStream<T> processor) {
	JarEntry entry = jar.getJarEntry(resource);
	try(InputStream istream = jar.getInputStream(entry)) {
	    return processor.process(istream);
	}
	catch(IOException ex) {
	    throw new RuntimeException(ex);
	}
    }

    public JarScanner(final ClassLoader classLoader, final JarFile jar, final SortedSet<String> resources) {
	super(classLoader, resources);
	this.jar = jar;
    }
    
    private static boolean shouldProcess(final JarFile jar, final List<String> prefixes) {
	if(prefixes == null || prefixes.isEmpty()) {
	    return true;
	}

	for(String prefix : prefixes) {
	    JarEntry entry = jar.getJarEntry(prefix);
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
	    SortedSet<String> resources = new TreeSet<>();
	    
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
		return new JarScanner(classLoader, jar, resources);
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
