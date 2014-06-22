package classpath.scan;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ElementScanner {
    public abstract List<String> getResources();
    public abstract ClassLoader getClassLoader();

    public static boolean matchesAll(String resourceName, List<Pattern> patterns) {
	for(Pattern pattern : patterns) {
	    Matcher matcher = pattern.matcher(resourceName);
	    if(!matcher.matches()) {
		return false;
	    }
	}

	return true;
    }

}
