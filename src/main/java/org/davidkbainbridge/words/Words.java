package org.davidkbainbridge.words;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class used to store the word list. This class is used to load the raw word
 * list which is then sorted by word length as well as several index are created
 * to support more efficient processing of the data.
 */
public class Words {
	private static Logger log = Logger.getLogger(Words.class.getName());

	/*
	 * Complete word list, eventually sorted by word length from longest to
	 * shortest.
	 */
	private List<String> words = new ArrayList<String>();

	/*
	 * Used to keep track of the lengths of words in the list
	 */
	private List<Integer> wordLengths = new ArrayList<Integer>();

	/*
	 * Index that maps length of word to an array of words of that length.
	 */
	private Map<Integer, List<String>> byLength = new HashMap<Integer, List<String>>();

	/*
	 * Maintains a list of words shorter than a given size. This is utilized to
	 * help the algorithms on ly work with words of a given size or less.
	 */
	private volatile Map<Integer, List<String>> listByLength = new HashMap<Integer, List<String>>();

	public int numberOfWords() {
		return words.size();
	}

	/**
	 * Loads a word list from a given source
	 * 
	 * @param source
	 *            the source from which to read the words
	 * @throws IOException
	 *             thrown where an exception occurs while trying to read the
	 *             word list
	 */
	public void load(Reader source) throws IOException {
		/*
		 * While reading the word list other information is gathered that is
		 * used later to help build data indexes to help make the processing of
		 * the data more efficient.
		 * 
		 * Most importantly the lengths of the words are captured and each word
		 * is put in a separate list based on its length.
		 * 
		 * After the entire word list is loaded into memory it is sorted by word
		 * length from longest to shorted. This is done also to help make the
		 * processing of the word list more efficient as the longest words, is
		 * well, likely to be the longest. So if the word list is prcessed from
		 * longest to shortest the longest word is more likely to be found
		 * quickly.
		 */
		String word = null;
		BufferedReader reader = new BufferedReader(source);
		List<String> list = null;

		words.clear();
		while ((word = reader.readLine()) != null) {
			if ((word = word.trim()).length() > 0) {
				words.add(word);
				if ((list = byLength.get(word.length())) == null) {
					list = new ArrayList<String>();
					byLength.put(word.length(), list);
				}
				list.add(word);
				if (!wordLengths.contains(word.length())) {
					wordLengths.add(word.length());
				}
			}
		}
		Collections.sort(wordLengths);

		/*
		 * Sort the word list from longest word to shortest. This is done
		 * because the algorithms checking for compound words can leverage this
		 * information so that longer words are checked first. This means that
		 * the first "found" compound word is actually the longest and it is not
		 * required to do an exhaustive search to find the longest compound
		 * word. It is still required to an exhaustive search to find all the
		 * compound words.
		 */
		Collections.sort(words, new Comparator<String>() {

			@Override
			public int compare(String s1, String s2) {
				return s2.length() - s1.length();
			}
		});

		/*
		 * Build of lists of words that are shorted then length X for each known
		 * length. This allows the processing algorithms to search only those
		 * words that will fit in the available space, thus making processing
		 * more efficient.
		 */
		int maxWordLength = wordLengths.get(wordLengths.size() - 1);
		for (int len = 0; len < maxWordLength; len++) {
			list = new ArrayList<String>();
			for (int lenIdx : byLength.keySet()) {
				if (lenIdx <= len) {
					list.addAll(byLength.get(lenIdx));
				}
			}
			listByLength.put(len, list);
		}

		log.log(Level.INFO,
				"Loaded {0} words, shortest is {1}, longest is {2}",
				new Object[] { words.size(), wordLengths.get(0),
						wordLengths.get(wordLengths.size() - 1) });
	}

	public final String get(final int idx) {
		return words.get(idx);
	}

	public Iterable<String> getWordIterator(final int smallerThan) {
		return listByLength.get(smallerThan);
	}
}
