package org.davidkbainbridge.words;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.EventListenerList;

public class WordListTask implements Callable<Result> {
	private static final Logger log = Logger.getLogger(WordListTask.class
			.getName());

	private EventListenerList listeners = new EventListenerList();
	private final Words words;
	private final Integer startIdx;
	private final Integer endIdx;
	private final Integer stepSize;
	private boolean checkForLongest = true;
	private Result result = new Result();

	public WordListTask(Words words, Integer startIdx, Integer endIdx,
			Integer stepSize) {
		this.words = words;
		this.startIdx = startIdx;
		this.endIdx = endIdx;
		this.stepSize = stepSize;
	}

	public void addLongestCompoundWordResultListener(
			LongestCompoundWordResultListener listener) {
		listeners.add(LongestCompoundWordResultListener.class, listener);
	}

	public void removeLongestCompoundWordResultListener(
			LongestCompoundWordResultListener listener) {
		listeners.remove(LongestCompoundWordResultListener.class, listener);
	}

	private void fireLongestCompoundWordResultListners(Set<String> words) {
		for (LongestCompoundWordResultListener listener : listeners
				.getListeners(LongestCompoundWordResultListener.class)) {
			listener.longestWords(new LongestCompoundWordEvent(words));
		}
	}

	/**
	 * Users a recursive algorithm to determine if the given word can be
	 * constructed from words in the word list.
	 * 
	 * @param word
	 *            the word to to check to see if it is a compound word
	 * @param start
	 *            character index into the words from where to start searching
	 *            for other words
	 * @return true if the word is a compound word, else false
	 */
	private boolean checkWord(String word, int start) {

		/*
		 * If the start is at the end of the word that means that this word has
		 * been matched completely and is a compound word
		 */
		if (word.length() == start) {
			return true;
		}

		/*
		 * When searching for a compound word we iterate only over the words
		 * that will fit in the space left after the first match. There are two
		 * cases that are optimized:
		 * 
		 * 1. If the start index is 0 that means that we are matching against
		 * the whole word and thus we don't need to compare to all the words
		 * that are the same size as the word under test. Instead we just need
		 * to compare against words that are smaller.
		 * 
		 * 2. If the start index is > 0 then we need to compare against all
		 * words that fit in that space.
		 * 
		 * This difference is accounted for by an additional subtraction term at
		 * the end of the parameter to getWordIterator that subtracts 1 or a 0
		 * depending on the value of the start index.
		 */
		for (String match : words.getWordIterator(word.length() - start
				- (start == 0 ? 1 : 0))) {

			/*
			 * We don't have to check if we match ourselves because when the
			 * start index is 0 (matching whole word) we are only comparing to
			 * words shorter than it
			 */
			if (word.startsWith(match, start)) {
				if (checkWord(word, start + match.length())) {

					/*
					 * If checkword returned true then we know the word matched
					 * as a compound word and as we don't care if it matched in
					 * several different ways we can stop matching and return
					 * true.
					 */
					return true;
				}
			}
		}

		/*
		 * If we got here then the word is not a compound word
		 */
		return false;
	}

	@Override
	public Result call() throws Exception {

		log.log(Level.INFO, "TASK[started] working from index {0} to {1}",
				new Object[] { startIdx, endIdx });

		for (int idx = startIdx; idx < endIdx; idx += stepSize) {
			if (checkWord(words.get(idx), 0)) {
				/*
				 * We know this is a compound word, so we can now check if it is
				 * a longest compound word
				 */
				result.incrementCompoundWordCount();
				
				/*
				 * If we are still checking for the longest words than check to
				 * see if the matched word is one of the longest word
				 */
				if (checkForLongest
						&& !result.checkUpdateLongestCompoundWord(words.get(idx))) {

					/*
					 * The matched word is not a longest word (as returned by
					 * the checkUpdateLongest). Because we know the words are
					 * sorted by length we know that we know longer have to
					 * search for longest words.
					 */
					log.log(Level.INFO,
							"TASK[checkpoint] All the longest words have been found");
					checkForLongest = false;

					/*
					 * Start a background thread to notify the "controller" that
					 * this task has calculated all its longest words. Do this
					 * notification in a thread so that it doesn't stop the main
					 * processing. At this point this worker should no longer be
					 * writing to Result longest word list so its values are
					 * safe to share between threads (read only at this point).
					 */
					new Thread(new Runnable() {

						@Override
						public void run() {
							fireLongestCompoundWordResultListners(result
									.getLongestCompoundWords());
						}
					}).start();
				}
			}
		}
		/*
		 * If checkForLongest is still true it means that this task has not sent
		 * an event to notify our listeners of the longest found compound words.
		 */
		if (checkForLongest) {
			fireLongestCompoundWordResultListners(result
					.getLongestCompoundWords());
		}

		log.log(Level.INFO, "TASK[completed] worked from index {0} to {1}",
				new Object[] { startIdx, endIdx });

		return result;
	}
}
