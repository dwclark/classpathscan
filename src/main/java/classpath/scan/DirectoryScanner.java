package classpath.scan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class DirectoryScanner extends ElementScanner {
    
    final private File directory;

    public <T> T withStream(String resource, ProcessInputStream<T> processor) {
	File file = new File(directory, resource);
	try(InputStream istream = new FileInputStream(file)) {
	    return processor.process(istream);
	}
	catch(IOException ex) {
	    throw new RuntimeException(ex);
	}
    }
    
    public DirectoryScanner(final ClassLoader classLoader, final File directory, final SortedSet<String> resources) {
	super(classLoader, resources);
	this.directory = directory;
    }
    
    private static String cleanResource(final File baseDirectory, final File classFile) {
	try {
	    String ret = classFile.getCanonicalPath().replace(baseDirectory.getCanonicalPath(), "");
	    if(ret.startsWith("/") || ret.startsWith(File.separator)) {
		ret = ret.substring(1);
	    }
	    
	    if(!File.separator.equals("/")) {
		ret = ret.replace(File.separator, "/");
	    }
	    
	    return ret;
	}
	catch(java.io.IOException ioe) {
	    throw new RuntimeException(ioe);
	}
    }
    
    private static void process(final SortedSet<String> resources, final File baseDirectory, 
				final File directory, final List<Pattern> patterns) {
	for(File file : directory.listFiles()) {
	    if(file.isFile()) {
		String resourceName = cleanResource(baseDirectory, file);
		if(matchesAll(resourceName, patterns)) {
		    resources.add(resourceName);
		}
	    }
	    else {
		process(resources, baseDirectory, file, patterns);
	    }
	}
    }
    
    public static DirectoryScanner factory(final ClassLoader classLoader, final File directory, 
					   final List<String> prefixes, final List<Pattern> patterns) {
	SortedSet<String> resources = new TreeSet<>();
	if(prefixes != null && !prefixes.isEmpty()) {
	    for(String prefix : prefixes) {
		File subDir = new File(directory, prefix);
		if(subDir.exists() && subDir.isDirectory()) {
		    process(resources, directory, subDir, patterns);
		}
	    }
	}
	else {
	    process(resources, directory, directory, patterns);
	}
	
	if(!resources.isEmpty()) {
	    return new DirectoryScanner(classLoader, directory, resources);
	}
	else {
	    return null;
	}
    }
}
