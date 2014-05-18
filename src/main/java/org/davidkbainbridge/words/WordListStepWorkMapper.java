package org.davidkbainbridge.words;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Segments the word list based on the number of threads. Each task is assigned
 * a starting index and a step size such that each task iterates over the word
 * list starting from the given index and incrementing the given step size.
 * 
 * As the word list is initially sorted by word length this has the effect of
 * giving each task an essentially equal distribution of words based on word
 * length.
 * 
 * This is important because this means each task will first work through the
 * longest words in the word list (where statistically the longest compound word
 * will be found) and thus the longest words will be found more quickly using 
 * this word distribution algorithm.
 */
public class WordListStepWorkMapper implements WorkMapper {
	private static final Logger log = Logger
			.getLogger(WordListStepWorkMapper.class.getName());

	@Override
	public List<Callable<Result>> mapTasks(Words words, int numberOfTasks) {

		/*
		 * Split the work load by assigning each task a starting index and a
		 * step index. The worker will then iterate over the list of words from
		 * the given starting point using the given step
		 */

		/*
		 * Number of tasks must be > 0
		 */
		if (numberOfTasks < 1) {
			throw new IllegalArgumentException(
					"Number of tasks must be greater than 0 (>0)");
		}

		/*
		 * If the data given us is null then throw an exception, we must have
		 * data to process
		 */
		if (words == null) {
			throw new IllegalArgumentException(String.format(
					"Expecting instance of type '%s', but received 'null'",
					Words.class.getName()));
		}

		/*
		 * Mapping the words to tasks will be pretty straight forward, divide
		 * the words as equally as possible among the tasks. If the number of
		 * words does not divide exactly spread the remainder among the tasks
		 */
		List<Callable<Result>> list = new ArrayList<Callable<Result>>();

		for (int i = 0; i < numberOfTasks; i++) {
			list.add(new WordListTask(words, i, words.numberOfWords(),
					numberOfTasks));

			log.log(Level.INFO,
					"TASK[created] with start index of {0} and step size {1}",
					new Object[] { i, numberOfTasks });
		}

		return list;
	}
}
