package classpath.scan;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import classpath.scan.jarjar.ClassReader;
import classpath.scan.jarjar.tree.AnnotationNode;
import classpath.scan.jarjar.tree.ClassNode;
import classpath.scan.jarjar.tree.FieldNode;
import classpath.scan.jarjar.tree.MethodNode;
import classpath.scan.jarjar.tree.MethodNode;

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

    public <T> Set<T> findNodeMatches(final ClassNodeMatcher<T> matcher) {
	Set<T> set = new HashSet<>();
	for(ElementScanner scanner : getScanners()) {
	    for(String resource : scanner.getResources()) {
		set.addAll(matcher.matches(scanner.getClassLoader(), nodeCache.get(resource)));
	    }
	}

	return Collections.unmodifiableSet(set);
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

    public static <T> Collection<T> safe(Collection<T> col) {
	if(col == null) {
	    return Collections.<T>emptyList();
	}
	else {
	    return col;
	}
    }

    public Set<Method> findMethodsAnnotatedWith(final Class<? extends Annotation> annotation) {
	return findNodeMatches(new ClassNodeMatcher<Method>() {
		@SuppressWarnings("unchecked")
		public Set<Method> matches(ClassLoader classLoader, ClassNode node) {
		    Set<Method> set = Collections.<Method>emptySet();

		    methodNodeSearch:
		    for(MethodNode methodNode : safe((List<MethodNode>) node.methods)) {
			for(AnnotationNode annotationNode : safe((List<AnnotationNode>) methodNode.visibleAnnotations)) {
			    String cleaned = cleanDescription(annotationNode.desc);
			    if(cleaned.equals(annotation.getName())) {
				set = new HashSet<>();
				break methodNodeSearch;
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

    public Set<Method> findMethodsWithParameterAnnotation(final Class<? extends Annotation> annotation) {
	return findNodeMatches(new ClassNodeMatcher<Method>() {
		@SuppressWarnings("unchecked")
		public Set<Method> matches(ClassLoader classLoader, ClassNode node) {
		    Set<Method> set = Collections.<Method>emptySet();

		    methodNodeSearch:
		    for(MethodNode methodNode : safe((List<MethodNode>) node.methods)) {
			if(methodNode.visibleParameterAnnotations != null) {
			    for(List<AnnotationNode> annotationNodes : methodNode.visibleParameterAnnotations)  {
				for(AnnotationNode annotationNode : safe(annotationNodes)) {
				    String cleaned = cleanDescription(annotationNode.desc);
				    if(cleaned.equals(annotation.getName())) {
					set = new HashSet<>();
					break methodNodeSearch;
				    }
				}
			    }
			}
		    }

		    if(set != Collections.<Method>emptySet()) {
			try {
			    Class theType = Class.forName(cleanClass(node.name), false, classLoader);
			    for(Method method : theType.getMethods()) {
				outerAnnotations:
				for(Annotation[] outerAnnotations : method.getParameterAnnotations()) {
				    for(Annotation innerAnnotation : outerAnnotations) {
					if(innerAnnotation.annotationType() == annotation) {
					    set.add(method);
					    break outerAnnotations;
					}
				    }
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
	return findNodeMatches(new ClassNodeMatcher<Field>() {
		@SuppressWarnings("unchecked")
		public Set<Field> matches(ClassLoader classLoader, ClassNode node) {
		    Set<Field> set = Collections.emptySet();
		
		    fieldNodeSearch:
		    for(FieldNode fieldNode : safe((List<FieldNode>) node.fields)) {
			for(AnnotationNode annotationNode : safe((List<AnnotationNode>) fieldNode.visibleAnnotations)) {
			    String cleaned = cleanDescription(annotationNode.desc);
			    if(cleaned.equals(annotation.getName())) {
				set = new HashSet<>();
				break fieldNodeSearch;
			    }
			}
		    }
		    
		    if(set != Collections.<Field>emptySet()) {
			try {
			    Class type = Class.forName(cleanClass(node.name), false, classLoader);
			    for(Field field : type.getFields()) {
				if(field.isAnnotationPresent(annotation)) {
				    set.add(field);
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

    public Set<Class> findTypesAnnotatedWith(final Class<? extends Annotation> annotation) {
	return findNodeMatches(new ClassNodeMatcher<Class>() {
		@SuppressWarnings("unchecked")
		public Set<Class> matches(ClassLoader classLoader, ClassNode node) {
		    for(AnnotationNode annotationNode : safe((List<AnnotationNode>) node.visibleAnnotations)) {
			String cleaned = cleanDescription(annotationNode.desc);
			if(cleaned.equals(annotation.getName())) {
			    try {
				return Collections.<Class>singleton(Class.forName(cleanClass(node.name), false, classLoader));
			    }
			    catch(ClassNotFoundException ex) {
				throw new RuntimeException(ex);
			    }
			}
		    }

		    return Collections.<Class>emptySet();
		} });
    }

    public Set<Class> findDirectSubTypesOf(final Class<?> type) {
	return findNodeMatches(new ClassNodeMatcher<Class>() {
		@SuppressWarnings("unchecked")
		public Set<Class> matches(ClassLoader classLoader, ClassNode node) {
		    if(type.getName().equals(cleanClass(node.superName))) {
			try {
			    return Collections.<Class>singleton(Class.forName(cleanClass(node.name), false, classLoader));
			}
			catch(ClassNotFoundException ex) {
			    throw new RuntimeException(ex);
			}
		    }
		    else {
			return Collections.<Class>emptySet();
		    }
		} });
    }

    public Set<Class> findSubTypesOf(final Class<?> type) {
	Set<Class> initial = findDirectSubTypesOf(type);
	if(initial.isEmpty()) {
	    return initial;
	}

	Set<Class> accum = new HashSet<>();
	Set<Class> testFor = new HashSet<>(initial);
	Set<Class> nextTestFor = new HashSet<>();
	while(!testFor.isEmpty()) {
	    for(Class toTest : testFor) {
		nextTestFor.addAll(findDirectSubTypesOf(toTest));
	    }
	    
	    accum.addAll(testFor);
	    testFor.clear();
	    testFor.addAll(nextTestFor);
	    nextTestFor.clear();
	}
	
	return Collections.unmodifiableSet(accum);
    }

    public Set<Class> findDirectlyImplements(final Class<?> type) {
	return findNodeMatches(new ClassNodeMatcher<Class>() {
		@SuppressWarnings("unchecked")
		public Set<Class> matches(ClassLoader classLoader, ClassNode node) {
		    for(String intf : safe((List<String>) node.interfaces)) {
			if(type.getName().equals(cleanClass(intf))) {
			    try {
				return Collections.<Class>singleton(Class.forName(cleanClass(node.name), false, classLoader));
			    }
			    catch(ClassNotFoundException ex) {
				throw new RuntimeException(ex);
			    }
			}
		    }
		    return Collections.<Class>emptySet();
		} });
    }

    public Set<Class> findImplements(final Class<?> type) {
	Set<Class> initial = findDirectlyImplements(type);
	if(initial.isEmpty()) {
	    return initial;
	}

	Set<Class> accum = new HashSet<>(initial);
	for(Class findFor : initial) {
	    accum.addAll(findSubTypesOf(findFor));
	}

	return Collections.unmodifiableSet(accum);
    }
}
