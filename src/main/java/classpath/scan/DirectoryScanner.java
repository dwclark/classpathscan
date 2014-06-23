package classpath.scan;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class DirectoryScanner extends ElementScanner {
    
    final private List<String> resources;
    
    public List<String> getResources() {
	return resources;
    }

    final private ClassLoader classLoader;

    public ClassLoader getClassLoader() {
	return classLoader;
    }
    
    public DirectoryScanner(final ClassLoader classLoader, final List<String> resources) {
	this.classLoader = classLoader;
	this.resources = Collections.unmodifiableList(resources);
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
    
    private static void process(final List<String> resources, final File baseDirectory, 
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
	List<String> resources = new ArrayList<>();
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
	    return new DirectoryScanner(classLoader, resources);
	}
	else {
	    return null;
	}
    }
}
