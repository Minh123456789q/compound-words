package org.davidkbainbridge.words;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class FindCompoundWordApp implements LongestCompoundWordResultListener {
	private static final Logger log = Logger
			.getLogger(FindCompoundWordApp.class.getName());

	public static final int DEFAULT_NUMBNER_OF_THREADS = 1;
	private final int numberOfThreads;
	private final Words words;
	private volatile List<String> longestCompoundWords = new ArrayList<String>();
	private volatile long startTime = 0;
	private volatile int countDown = 0;
	private Result result = null;

	private static void usage() {
		System.err.println("Usage: FindCompoundWordApp -threads=# <word-list>");
		System.exit(1);
	}

	public FindCompoundWordApp(Words words, int numberOfThreads) {
		this.words = words;
		this.numberOfThreads = numberOfThreads;
	}

	public void run() {
		/*
		 * Used to track the worker tasks that have reported their results.
		 */
		countDown = numberOfThreads;

		ExecutorService executor = Executors
				.newFixedThreadPool(numberOfThreads);

		WorkMapper mapper;
		try {
			mapper = new WorkMapperFactory().<Result> newWorkMapper();
		} catch (ClassNotFoundException | ClassCastException
				| InstantiationException | IllegalAccessException badMapper) {
			System.err.format(
					"Unable to instantiate a valid work mapper [%s : %s]\n",
					badMapper.getClass().getName(), badMapper.getMessage());
			System.exit(1);
			return; // Shouldn't need this but eclipse can't tell that we are
					// not coming back from exit
		}
		List<Callable<Result>> tasks = mapper.mapTasks(words, numberOfThreads);

		/*
		 * We add this class as a listener for the longest compound word. If we
		 * also passed on this notification to each of the task classes then we
		 * could potentially shortcut the longest words search in some
		 * instances, i.e. if task A found the longest word at length 10 and let
		 * the other tasks know about this, then if they longest words they
		 * found is of length 8 or they are working on words shorter than length
		 * 10 they could simply stop searching for the shortest.
		 * 
		 * Will leave this to another time.
		 */
		for (Callable<Result> c : tasks) {
			((WordListTask) c).addLongestCompoundWordResultListener(this);
		}

		// Start the timer
		startTime = new Date().getTime();

		// Start the workers
		List<Future<Result>> results = null;
		try {
			results = executor.invokeAll(tasks, 1, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			System.err
					.format("Unable to invoke worker processes, terminating application [%s : %s]\n",
							e.getClass().getName(), e.getMessage());
			System.exit(1);
		} finally {
			executor.shutdown();
		}

		// Collate the responses
		int totalCount = 0;
		for (Future<Result> result : results) {
			if (result.isDone()) {
				try {
					totalCount += result.get().getCompoundWordCount();
				} catch (InterruptedException | ExecutionException e) {
					System.err
							.format("Unable to read result of a worker task, ignoring this result and continuing, but overall result may be inaccurate [%s : %s]\n",
									e.getClass().getName(), e.getMessage());
				}
			}
		}

		/*
		 * The longest compound word was already reported, so just report the
		 * total number of compound words found.
		 */
		System.err
				.format("Found a total of %d compound words, not counting words that could be formed in multiple ways\n",
						totalCount);

		result = new Result(longestCompoundWords, totalCount);
	}
	
	public Result getResult() {
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.davidkbainbridge.words.LongestCompoundWordResultListener#longestWords
	 * (org.davidkbainbridge.words.LongestCompoundWordEvent)
	 */
	@Override
	public void longestWords(LongestCompoundWordEvent event) {
		/*
		 * This listener method is called by every task worker when it has found
		 * the longest compound word in its area of responsibility. This simply
		 * keeps track of the longest compound word found to date, with the
		 * understanding that there could be multiple of the same length.
		 * 
		 * When all tasks have reported their longest compound words the longest
		 * compound word or words are output along with the time it took to find
		 * them.
		 */
		synchronized (longestCompoundWords) {
			Set<String> newWords = event.getLongestCompoundWords();
			int compare = 0;

			if (newWords != null && newWords.size() > 0) {
				if (longestCompoundWords.size() == 0) {
					longestCompoundWords.addAll(newWords);
				} else {

					compare = newWords.iterator().next().length()
							- longestCompoundWords.iterator().next().length();
					if (compare == 0) {
						// Same Length
						longestCompoundWords.addAll(newWords);
					} else if (compare > 0) {
						// New words longer
						longestCompoundWords.clear();
						longestCompoundWords.addAll(newWords);
					}
					// if compare < 0 then newWords are shorter
				}
			}
			if (--countDown == 0) {
				// longest word found as every task has reported in
				long duration = new Date().getTime() - startTime;
				if (longestCompoundWords.size() > 0) {
					System.err.format(
							"Found longest compound word, '%s', in %dms\n",
							longestCompoundWords, duration);
				} else {
					System.err
							.println("No compound words were found, therefore there is no longest compound word");
				}
			}
		}
	}

	public static void main(String[] args) {

		Words words = null;
		Reader source = null;
		int numberOfThreads = DEFAULT_NUMBNER_OF_THREADS;
		String wordList = null;

		if (System.getProperty("java.util.logging.config.file", null) == null) {
			/*
			 * The logging configuration was not set from the command line, so
			 * we will use our default as opposed to the system default
			 */

			try (InputStream logConfiguration = ClassLoader
					.getSystemResourceAsStream("app.properties");) {
				if (logConfiguration != null) {
					LogManager.getLogManager().readConfiguration(
							logConfiguration);
				} else {
					log.log(Level.SEVERE,
							"Unable to load custom logging settings, using system settings as 'app.properties' was not found");
				}
			} catch (SecurityException | IOException e) {
				log.log(Level.SEVERE,
						"Unable to load custom logging settings, using system settings, {0} : {1}",
						new Object[] { e.getClass().getName(), e.getMessage() });
			}

		}

		/*
		 * Simply parsing of the command line arguments. In a real application
		 * it would be far better to use a OTS command line parser, but as I
		 * wanted to stick to "raw" java I just hacked it.
		 */
		for (String arg : args) {
			if (arg.startsWith("-threads")) {
				try {
					numberOfThreads = Integer.parseInt(arg.split("=")[1]);
				} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
					usage();
				}
			} else if (arg.startsWith("-")) {
				usage();
			} else {
				wordList = arg;
			}
		}

		// no word list, no go
		if (wordList == null) {
			usage();
		}

		/*
		 * If the word "file" specification does not contain a ':' then assume
		 * it is a local file, else attempt to parse it as a URL
		 */
		if (wordList.indexOf(':') < 0) {
			try {
				source = new FileReader(wordList);
			} catch (FileNotFoundException fnfe) {
				System.err.format(
						"ERROR: location of word file,  '%s', not found\n",
						wordList);
				System.exit(1);
			}
		} else {
			try {
				source = new InputStreamReader(new URL(wordList).openStream());
			} catch (MalformedURLException e) {
				System.err
						.format("ERROR: location of word file,  '%s', cannot be processed as a URL\n",
								wordList);
				System.exit(1);
			} catch (IOException ioe) {
				System.err
						.format("ERROR: unknown exception occurred,  while processing word file from location '%s': %s [%s]\n",
								wordList, ioe.getClass().getSimpleName(),
								ioe.getMessage());
				System.exit(1);
			}
		}

		/*
		 * Load the words and fire away
		 */
		words = new Words();
		try {
			words.load(source);
		} catch (IOException ioe) {
			System.err
					.format("ERROR: unable to process word file from source '%s' : %s [%s]\n",
							ioe.getClass().getSimpleName(), ioe.getMessage());
			System.exit(1);
		}

		FindCompoundWordApp app = new FindCompoundWordApp(words,
				numberOfThreads);
		app.run();

	}
}
