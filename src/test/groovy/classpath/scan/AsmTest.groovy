package classpath.scan;

import org.objectweb.asm.*;
import org.junit.*;
import org.objectweb.asm.tree.ClassNode;

public class AsmTest {
  
  private ClassLoader loader;
  
  @Before
  public void init() {
    loader = AsmTest.classLoader;
  }

  @Test
  public void testSimple() {
    ClassScanner cscanner = new ClassScanner(loader, [ 'classpath.scan' ] as String[], ~/.*ClassScannerTest\.class$/);
    ElementScanner scanner = cscanner.scanners.iterator().next();
    String resource = scanner.resources.iterator().next();
    ClassNode cnode = scanner.withStream(resource) { InputStream istream ->
      ClassReader creader = new ClassReader(istream)
      ClassNode cnode = new ClassNode();
      creader.accept(cnode, 0);
      return cnode; };

    println(cnode.methods.collect { it.name; })
  }

}