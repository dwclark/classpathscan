package classpath.scan;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class ClassScanner extends ResourceScanner {

    private static final Pattern PATTERN = Pattern.compile(".*\\.class$");

    private static String[] packagesToPrefixes(final String[] packages) {
	if(packages == null) {
	    return null;
	}
	else {
	    String[] ret = new String[packages.length];
	    for(int i = 0; i < ret.length; ++i) {
		ret[i] = packages[i].replace(".", "/");
	    }

	    return ret;
	}
    }

    public ClassScanner(final ClassLoader classLoader) {
	this(classLoader, null, null);
    }
    
    public ClassScanner(final ClassLoader classLoader, final String[] packages) {
	this(classLoader, packages, null);
    }
    
    public ClassScanner(final ClassLoader classLoader, final String[] packages, final Pattern[] patterns) {
	super(fixClassLoader(classLoader), fixPrefixes(packagesToPrefixes(packages)), fixPatterns(patterns, PATTERN));
    }
    
    private static String toClassName(final String resource) {
	return resource.replace("/", ".").replace(File.separator, ".").replace(".class", "");
    }

    public <T> Set<T> findMatches(final ClassMatcher<T> matcher) {
	try {
	    Set<T> ret = new HashSet<>();
	    for(ElementScanner scanner : getScanners()) {
		for(String resource : scanner.getResources()) {
		    String typeName = toClassName(resource);
		    Class type = Class.forName(typeName, false, scanner.getClassLoader());
		    Set<T> matched = matcher.matches(type);
		    ret.addAll(matched);
		}
	    }
	    
	    return ret;
	}
	catch(ClassNotFoundException cnfe) {
	    throw new RuntimeException(cnfe);
	}
    }

    private static <T> Set<T> addToSet(final Set<T> set, final T element) {
	Set<T> ret = set;
	if(ret == Collections.<T>emptySet()) {
	    ret = new HashSet<T>();
	}

	ret.add(element);
	return ret;
    }

    public Set<Method> findMethodsAnnotatedWith(final Class<? extends Annotation> annotation) {
	return findMatches(new ClassMatcher<Method>() {
		public Set<Method> matches(Class<?> type) {
		    Set<Method> ret = Collections.<Method>emptySet();
		    if(type == null) {
			return ret;
		    }

		    for(Method method : type.getMethods()) {
			if(method.isAnnotationPresent(annotation)) {
			    ret = addToSet(ret, method);
			}
		    }

		    return ret;
		} });
    }

    public Set<Field> findFieldsAnnotatedWith(final Class<? extends Annotation> annotation) {
	return findMatches(new ClassMatcher<Field>() {
		public Set<Field> matches(Class<?> type) {
		    Set<Field> ret = Collections.<Field>emptySet();
		    if(type == null) {
			return ret;
		    }

		    for(Field field : type.getFields()) {
			if(field.isAnnotationPresent(annotation)) {
			    ret = addToSet(ret, field);
			}
		    }

		    return ret;
		} });
    }

    public Set<Class> findClassesAnnotatedWith(final Class<? extends Annotation> annotation) {
	return findMatches(new ClassMatcher<Class>() {
		public Set<Class> matches(Class<?> type) {
		    if(type != null && type.isAnnotationPresent(annotation)) {
			return Collections.<Class>singleton(type);
		    }
		    else {
			return Collections.<Class>emptySet();
		    }
		} });
    }

    public Set<Class> findSubTypesOf(final Class type) {
	return findMatches(new ClassMatcher<Class>() {
		public Set<Class> matches(Class<?> found) {
		    if(found != null && found.isInstance(type)) {
			return Collections.<Class>singleton(found);
		    }
		    else {
			return Collections.<Class>emptySet();
		    }
		} });
    }
}
