package classpath.scan;

import java.io.InputStream;

/**
 * Simple interface used to process found resources as <code>InputStream</code>s.
 */
public interface ProcessInputStream<T> {
    /**
     * 
     * @param istream The stream created for a resource. Stream will only remain open
     * for the duration of the call to <code>process</code> and is guaranteed to be closed after
     * <code>process</code> finishes.
     * @return Whatever makes sense for your process method
     */
    public T process(InputStream istream);
}
