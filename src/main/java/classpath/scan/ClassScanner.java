package classpath.scan;

import java.io.InputStream;
import java.io.IOException;
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
import java.util.Map;
import java.util.LinkedHashMap;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

public class ClassScanner extends ResourceScanner {

    private final Map<String,ClassNode> nodeCache;

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
	this.nodeCache = Collections.unmodifiableMap(populateNodeCache());
    }
    
    private static String toClassName(final String resource) {
	return resource.replace("/", ".").replace(File.separator, ".").replace(".class", "");
    }

    private Map<String,ClassNode> populateNodeCache() {
	Map<String,ClassNode> tmp = new LinkedHashMap<>();
	ProcessInputStream<ClassNode> processor = new ProcessInputStream<ClassNode>() {
	    public ClassNode process(InputStream istream) {
		try {
		    ClassReader creader = new ClassReader(istream);
		    ClassNode cnode = new ClassNode();
		    creader.accept(cnode, 0);
		    return cnode;
		}
		catch(IOException ioe) {
		    throw new RuntimeException(ioe);
		}
	    } };

	for(ElementScanner scanner : getScanners()) {
	    tmp.putAll(scanner.withStream(processor));
	}
	
	return tmp;
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

    public <T> Set<T> findNodeMatches(final ClassNodeMatcher<T> matcher) {
	Set<T> set = new HashSet<>();
	for(ElementScanner scanner : getScanners()) {
	    for(String resource : scanner.getResources()) {
		set.addAll(matcher.matches(scanner.getClassLoader(), nodeCache.get(resource)));
	    }
	}

	return set;
    }

    private static <T> Set<T> addToSet(final Set<T> set, final T element) {
	Set<T> ret = set;
	if(ret == Collections.<T>emptySet()) {
	    ret = new HashSet<T>();
	}

	ret.add(element);
	return ret;
    }

    public static String cleanDescription(String str) {
	return str.substring(1, str.length() - 1).replace('/', '.');
    }

    public static String cleanClass(String str) {
	return str.replace('/', '.');
    }

    public Set<Method> findMethodsAnnotatedWith(final Class<? extends Annotation> annotation) {
	return findNodeMatches(new ClassNodeMatcher<Method>() {
		@SuppressWarnings("unchecked")
		public Set<Method> matches(ClassLoader classLoader, ClassNode node) {
		    Set<Method> set = Collections.<Method>emptySet();
		    if(node.methods != null) {
			for(MethodNode methodNode : (List<MethodNode>) node.methods) {
			    if(methodNode.visibleAnnotations != null) {
				for(AnnotationNode annotationNode : (List<AnnotationNode>) methodNode.visibleAnnotations) {
				    String cleaned = cleanDescription(annotationNode.desc);
				    if(cleaned.equals(annotation.getName())) {
					set = new HashSet<>();
					break;
				    }
				    
				    if(set != Collections.<Method>emptySet()) {
					break;
				    }
				}
			    }
			}
		    }
		    
		    if(set != Collections.<Method>emptySet()) {
			try {
			    Class type = Class.forName(cleanClass(node.name), false, classLoader);
			    for(Method method : type.getMethods()) {
				if(method.isAnnotationPresent(annotation)) {
				    set.add(method);
				}
			    }
			}
			catch(ClassNotFoundException ex) {
			    throw new RuntimeException(ex);
			}
		    }

		    return set;
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

    public Set<Class> findSubTypesOf(final Class<?> type) {
	return findMatches(new ClassMatcher<Class>() {
		public Set<Class> matches(Class<?> found) {
		    if(found != null && type.isAssignableFrom(found)) {
			return Collections.<Class>singleton(found);
		    }
		    else {
			return Collections.<Class>emptySet();
		    }
		} });
    }
}
