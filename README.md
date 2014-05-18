Programming Problem - Find Longest Word Made of Other Words
===========================================================

Write a program that reads a file containing a sorted list of words (one word
per line, no spaces, all lower case), then identifies the longest word in
the file that can be constructed by concatenating copies of shorter words
also found in the file.

For example, if the file contained:

       cat
       cats
       catsdogcats
       catxdogcatsrat
       dog
       dogcatsdog
       hippopotamuses
       rat
       ratcatdogcat

The answer would be 'ratcatdogcat' - at 12 letters, it is the longest
word made up of other words in the list.  The program should then go on to
report how many of the words in the list can be constructed of other words
in the list.

Approach
========
There are two aspects to the approach in this solution, one is splitting the problem
so that it can be solved using concurrency and the other is optimizing the the data
so that it can be efficiently processed.

Concurrency
-----------
The words list can be potentially large, as such concurrency and parallelism are used
to efficiently processes the data. To this end the solution is a bit like a map / reduce
algorithm where the problem space (list of words) is split to be processed among 1 or more
identical tasks. The solution includes the ability to dynamically switch how the word is
divided among tasks using the system property `work.mapper`. This system property is used
to set the implementation class to used to map words to tasks. The two available choice are:

- __`org.davidkbainbridge.words.WordListRangeWorkMapper`__ - This implementation does a simply 
division of the words among the tasks assigning continuous blocks of the words to each task.
This implementation has the effect that the distribution of words to tasks is not uniform in
terms of the length of words being processed by each task.
- __`org.davidkbainbridge.words.WordListStepWorkMapper`__ - This implementation maps words to tasks
based on an initial index and a step size. This implementation has the effect of providing a
uniform distribution of words to tasks based on word length.

The default mapper used in the application is `org.davidkbainbridge.words.WordListStepWorkMapper`
which tends to find the longest compound word faster.

Data Optimization
-----------------
In order to improve the performance to the algorithm the data is pre-processed and 
indexes are created to improve the efficiency of the search algorithm. These optimizations
include:

- __Descending sort by word length__ - After the words list is read into a `List<String>` the words
are sorted from longest to shortest. This has the effect that the algorithm processes the longer words
first in terms of looking for the longest compound word. This improve performance on most tests 
sets as the longest compound word tends to be one of the longer words in the test set.

- __Word Length Based Indexes__ - As the words are being read into memory, the words are mapped into
lists based on their length. After all the words are read index are created based on all the words
that are shorter than length X. These indexes are used while processing the list so that the 
algorithm can select which words need to be evaluated when finding "sub-words" based on the available
length. This allows the algorithm to only iterate over words that may actually fit in the available space,
this reducing iterations over not possible solutions.

Algorithm
---------
The basic algorithm for searching for compound words is to iterate over the target words from longest to
shortest and for each target word iterate over word list (again from longest to shortest) attempting to
match if the target word "starts with" the match word. If the target word starts with the match word then
recursively attempt to search the "tail" of the target word for matches. If any combination of words 
make up the target word it is considered a compound words and added to the result set and no longer processed
(i.e. some of the words can be formed using different combinations of sub words, but for this exercise once
a word is considered a compound word it doesn't matter or count if it can be formed multiple ways)

The algorithm presented here is recursive, but it would be interesting to investigate if the same algorithm
could be achieved in a non-recursive implementation and how its performance would compare.

The key to the performance of the algorithm, beyond concurrency is to minimize the list of match words evaluated
at step. The algorithm was optimized to find the longest compound word first it was still required to find all 
compound words and thus an exhaustive search was still required.

Alternative Algorithm Tried
----------------------------
In addition to the algorithm actually presented a "binary search" type algorithm was also investigated. In this 
algorithm instead of only attempting to match at the start of the target word a match anywhere was accepted and
then this was considered a pivot point and the "ends" were further processed. This approach was tried as a
key to performance was to minimize the words that were iterated for matches and thus it was though that this
approach would mean (on average) that the recursive checks would be sorter if a match word was in the middle, i.e.
given catdograt as the target word, if you matched cat then the rest of the target would be matched against all
words of length 6 or less (in the presented algorithm), where as given if you matched dog then two matches of length
3 or less would be performed.

In the end this approach was abandoned as it actually proved to be less efficient.

Trial Runs
----------

The following are the timings and results of some trial runs on a MBP with the follow specification:

	Hardware Overview:
	Model Name:	MacBook Pro
	Model Identifier:	MacBookPro11,3
	Processor Name:	Intel Core i7
	Processor Speed:	2.6 GHz
	Number of Processors:	1
	Total Number of Cores:	4
	L2 Cache (per Core):	256 KB
	L3 Cache:	6 MB
	Memory:	16 GB
	Boot ROM Version:	MBP112.0138.B02
	SMC Version (system):	2.19f3

__Test 1 using `org.davidkbainbridge.words.WordListRangeWorkMapper`__
	
	$ time java -Dwork.mapper=org.davidkbainbridge.words.WordListRangeWorkMapper -jar target/words-0.0.1-jar-with-dependencies.jar -threads=4 words.txt 
	Found longest compound word, '[ethylenediaminetetraacetates]', in 3078ms
	Found a total of 97107 compound words, not counting words that could be formed in multiple ways

	real	0m54.662s
	user	1m28.486s
	sys		0m0.145s

__Test 2 using `org.davidkbainbridge.words.WordListStepWorkMapper`__

	$ time java -Dwork.mapper=org.davidkbainbridge.words.WordListStepWorkMapper -jar target/words-0.0.1-jar-with-dependencies.jar -threads=4 words.txt 
	Found longest compound word, '[ethylenediaminetetraacetates]', in 38ms
	Found a total of 97107 compound words, not counting words that could be formed in multiple ways
	
	real	0m25.577s
	user	1m39.998s
	sys		0m0.164s
