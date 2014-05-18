package org.davidkbainbridge.words;

import java.util.HashSet;
import java.util.Set;

/**
 * Event thrown when a WorkListTask has found the longest word in its part of
 * the domain
 */
public class LongestCompoundWordEvent {
	private final Set<String> longestCompoundWords;

	LongestCompoundWordEvent(Set<String> longestCompoundWords) {
		this.longestCompoundWords = new HashSet<String>(longestCompoundWords);
	}

	public Set<String> getLongestCompoundWords() {
		return longestCompoundWords;
	}
}
