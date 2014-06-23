package classpath.scan;

import org.junit.*;
import java.lang.reflect.Method;

@Deprecated
public class ClassScannerTest {
  
  @Deprecated
  public String foo;

  private ClassLoader classLoader;

  @Before
  public void init() {
    classLoader = ClassScannerTest.getClassLoader();
  }

  @Test
  public void testElementScannerSubtypes() {
    ClassScanner scanner = new ClassScanner(classLoader, [ 'classpath.scan' ] as String[]);
    Set<Class> subs = scanner.findSubTypesOf(ElementScanner);
    assert(subs.containsAll([ ElementScanner, DirectoryScanner, JarScanner ]));
  }

  @Test
  public void testMethodAnnotations() {
    ClassScanner scanner = new ClassScanner(classLoader, [ 'classpath.scan' ] as String[]);
    Set<Method> methods = scanner.findMethodsAnnotatedWith(Before);
    assert(methods.size() >= 1);
  }

  @Test
  public void testFieldAnnotations() {
    ClassScanner scanner = new ClassScanner(classLoader, [ 'classpath.scan' ] as String[], ~/.*ClassScannerTest\.class$/)
    assert(scanner.scanners.size() == 1);
    ElementScanner elementScanner = scanner.scanners.iterator().next();
    assert(elementScanner.resources.size() == 1);
    Set set = scanner.findFieldsAnnotatedWith(Deprecated);
    assert(set.size() == 1);
  }
  
  @Test
  public void testClassAnnotations() {
    ClassScanner scanner = new ClassScanner(classLoader, 'classpath.scan')
    Set<Class> set = scanner.findClassesAnnotatedWith(Deprecated);
    assert(set.size() == 1);
    assert(this.getClass() == set.iterator().next());
  }
}