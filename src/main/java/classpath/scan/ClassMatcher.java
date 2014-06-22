package classpath.scan;

import java.util.Set;

public interface ClassMatcher<T> {
    public Set<T> matches(Class<?> type);
}
