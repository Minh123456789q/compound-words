package org.davidkbainbridge.words;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Segments up the word list to be processed by multiple threads. The
 * segmentation is done by splitting the word list into equal parts (accounting
 * for the fact that the word list may not be able to be divided equally).
 * 
 * Thus if there are 30 words in the list and we have ten threads
 * 
 * Thread 1 gets words 0 to 9
 * 
 * Thread 2 gets words 10 to 19
 * 
 * Thread 3 gets words 20 to 29
 * 
 * This is a pretty straight forward segmentation and doesn't lead to the most
 * performant algorithm, but it works.
 * 
 */
public class WordListRangeWorkMapper implements WorkMapper {
	private static final Logger log = Logger
			.getLogger(WordListRangeWorkMapper.class.getName());

	@Override
	public List<Callable<Result>> mapTasks(Words words, int numberOfTasks) {
		int wordsPerTask = 0;
		int remainder = 0;
		int startIdx = 0;
		int endIdx = 0;

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
		wordsPerTask = (int) (words.numberOfWords() / numberOfTasks);
		remainder = words.numberOfWords() % numberOfTasks;
		log.log(Level.INFO,
				"Each task will process at least {0} words, there is a remainder of {1}",
				new Object[] { wordsPerTask, remainder });

		List<Callable<Result>> list = new ArrayList<Callable<Result>>();
		for (int i = 0; i < numberOfTasks; i++) {
			endIdx = startIdx + wordsPerTask + (remainder-- > 0 ? 1 : 0);
			list.add(new WordListTask(words, startIdx, endIdx, 1));

			log.log(Level.INFO,
					"TASK[created] with start index of {0} (inclusive) and end index of {1} (exclusive) ({2} words)",
					new Object[] { startIdx, endIdx, endIdx - startIdx });
			startIdx = endIdx;
		}

		return list;
	}
}
