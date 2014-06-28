package classpath.scan;

import java.io.InputStream;

public interface ProcessInputStream<T> {
    T process(InputStream istream);
}
