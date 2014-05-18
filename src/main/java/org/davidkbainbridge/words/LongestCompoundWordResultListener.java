package org.davidkbainbridge.words;

import java.util.EventListener;

/**
 * Interface through which listeners are notified when a WordListTask has found
 * the longest compound word in its part of the domain
 */
public interface LongestCompoundWordResultListener extends EventListener {
	public void longestWords(LongestCompoundWordEvent event);
}
