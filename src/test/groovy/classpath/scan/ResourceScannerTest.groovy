package classpath.scan;

import org.junit.*;

public class ResourceScannerTest {

  private ClassLoader loader;

  @Before
  public void init() {
    loader = ResourceScannerTest.classLoader;
  }

  @Test
  public void testFindOrgJunitClasses() {
    ResourceScanner scanner = new ResourceScanner(loader, 'org/junit');
    assert(scanner.all.find { it.endsWith('Test.class'); });
  }

  @Test
  public void testPatternJunitClasses() {
    ResourceScanner scanner = new ResourceScanner(loader, ['org/junit'] as String[], ~/org\/junit\/Test\.class/);
    assert(scanner.all.size() == 1);
  }

  @Test
  public void testGenericJunitClasses() {
    ResourceScanner scanner = new ResourceScanner();
    Set<String> found = scanner.findMatches(~/org\/junit\/Test\.class/);
    assert(found.size() == 1);
  }
}