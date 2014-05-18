package org.davidkbainbridge.words;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The result of the search for the largest compound word in a word list.
 */
public class Result {

	/*
	 * This is a list, because it is possible to have compound words of the same
	 * length that are actually the longest.
	 */
	private int wordLength = 0;
	/*
	 * Keeps track of the number of compound words found. 
	 */
	private int compoundWordCount = 0;
	
	/*
	 * The set of the longest compound words found
	 */
	private Set<String> longestCompoundWords = new HashSet<String>();

	Result(List<String> longestCompoundWords, int compoundWordCount) {
		this.longestCompoundWords = new HashSet<String>(longestCompoundWords);
		this.compoundWordCount = compoundWordCount; 
	}
	
	Result() {
		// Basic Constructor
	}

	/**
	 * Checks the given words against the existing results to verify if it
	 * qualifies as a longest word in this result set. The qualifications are
	 * that either the set is currently empty or the given word is the same
	 * length as the words in the set.
	 * 
	 * @param word
	 *            the word to verify and add if it qualifies as a longest word
	 * @return true if the word qualifies as a longest word, else false
	 */
	public boolean checkUpdateLongestCompoundWord(String word) {

		if (longestCompoundWords.size() == 0 || word.length() == wordLength) {
			longestCompoundWords.add(word);
			wordLength = word.length();
			return true;
		}
		return false;
	}

	public Set<String> getLongestCompoundWords() {
		return longestCompoundWords;
	}

	public void incrementCompoundWordCount() {
		compoundWordCount++;
	}

	public int getCompoundWordCount() {
		return compoundWordCount;
	}
}
