package classpath.scan;

import org.junit.*;

@Deprecated
public class ClassScannerTest implements Cloneable {
  
  @Deprecated
  public String foo;

  private ClassLoader classLoader;

  @Before
  public void init() {
    classLoader = ClassScannerTest.getClassLoader();
  }

  @Test
  public void testFindDirectSubTypesOf() {
    ClassScanner scanner = new ClassScanner(classLoader, [ 'classpath.scan' ] as String[]);
    Set<Class> subs = scanner.findDirectSubTypesOf(ElementScanner);
    assert(subs.containsAll([ DirectoryScanner, JarScanner ]));
  }

  @Test
  public void testFindMethodsAnnotatedWith() {
    ClassScanner scanner = new ClassScanner(classLoader, [ 'classpath.scan' ] as String[]);
    Set methods = scanner.findMethodsAnnotatedWith(Before);
    assert(methods.size() >= 1);
  }

  @Test
  public void testFindFieldsAnnotatedWith() {
    ClassScanner scanner = new ClassScanner(classLoader, [ 'classpath.scan' ] as String[], ~/.*ClassScannerTest\.class$/)
    assert(scanner.scanners.size() == 1);
    ElementScanner elementScanner = scanner.scanners.iterator().next();
    assert(elementScanner.resources.size() == 1);
    Set set = scanner.findFieldsAnnotatedWith(Deprecated);
    assert(set.size() == 1);
  }
  
  @Test
  public void testFindTypesAnnotatedWith() {
    ClassScanner scanner = new ClassScanner(classLoader, 'classpath.scan')
    Set<Class> set = scanner.findTypesAnnotatedWith(Deprecated);
    assert(set.size() == 1);
    assert(this.getClass() == set.iterator().next());
  }

  @Test
  public void testFindDirectlyImplements() {
    ClassScanner scanner = new ClassScanner(classLoader, 'classpath.scan')
    Set<Class> set = scanner.findDirectlyImplements(Cloneable);
    assert(set.size() == 1);
    assert(this.getClass() == set.iterator().next());
  }

  @Test
  public void testFindImplements() {
    ClassScanner scanner = new ClassScanner(classLoader, 'classpath.scan')
    Set<Class> set = scanner.findImplements(SimpleInterface);
    Set<Class> shouldBe = [ TestA, TestB, SubTestA, SubTestAA ] as Set;
    assert(set == shouldBe);
    println(set);
  }

  @Test
  public void testFindSubTypesOf() {
    ClassScanner scanner = new ClassScanner(classLoader, 'classpath.scan')
    Set<Class> set = scanner.findSubTypesOf(TestA);
    Set<Class> shouldBe = [ SubTestA, SubTestAA ] as Set;
    assert(set == shouldBe);
    println(set);
  }

  @Test
  public void testFindMethodsWithParameterAnnotation() {
    ClassScanner scanner = new ClassScanner(classLoader, 'classpath.scan');
    Set methods = scanner.findMethodsWithParameterAnnotation(Deprecated);
    assert(methods);
    assert(methods.find { def method -> method.name == 'imBad' });
    assert(!methods.find { def method -> method.name == 'imGood' });
  }
}

interface SimpleInterface { }

class TestA implements SimpleInterface { }

class TestB implements SimpleInterface { }

class SubTestA extends TestA { }

class SubTestAA extends SubTestA { }

class IHaveDeprecatedMethodParameters {

  public void imGood(String foo) { }

  public void imBad(@Deprecated String foo) { }
}