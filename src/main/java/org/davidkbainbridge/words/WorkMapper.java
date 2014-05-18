package org.davidkbainbridge.words;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Interface to support the mapping of words to multiple threads.
 */
public interface WorkMapper {
	public List<Callable<Result>> mapTasks(Words words, int numberOfTasks);
}
