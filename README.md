********************************************
					 
 Syntactic, Build 54 
					  
	by Omer Shapira
	http://omershapira.com		
			  
********************************************

VISUALIZATION: http://syntactic.omershapira.com

ACCEPTING CONTRIBUTORS! Read the 'tasks' list and join in. If there are any questions, feel free to contact me at info|at|omershapira|dot|com.

Description
**********************
Syntactic is a program that reads huge texts and divides common words in the text to categories. Here are some of its automatic categorizations in the Simple English Wikipedia:

	Cluster 17
	----------
	with, including, without, involving, containing, featuring, requiring, reaching, covering

	Cluster 52
	----------
	city, town, district, province, university, river, county, site, community, village, moon, field, state, series

It does this by examining contexts in 3-grams. For example, if the sentences

"the cat sat on the mat"
and
"the dog sat on the porch"

appear in the text, then the words "cat" and "dog" are likely to appear in the same category.

Usage:
**********************
When jar'd to Syntactic.jar, then:
java -jar Syntactic.jar [name] [input folder] [output folder] [clusters] [threshold] [epsilon]

[name] 		is the corpus name. Only alphanumeric characters and underscores(_).
[input folder]	is the folder where the corpus is. By default, only .txt files are read.
[output folder] is a folder in which Syntactic will create the output root folder, with a timestamp. If it is set to "Output/", then Syntactic will place everything in "Output/CorpusName dd.MM.yy HH.mm.ss /"
[clusters] 	the amount of resulting groups. Good results appear above 75. Speed decreases polynomially with the number of clusters. Default is 50.
[threshold]	the minimum frequency a word has to have in order to be clustered. Default is 50.
[epsilon]	clusters who are not mutually separated by this distance are merged. Values vary significantly. Typical values are between 0.5 and 0.05.


Versatility
**********************
* The program has a replacable class for parsing texts, so it can be modified to remove XML tags (or just read them), or any other modification in regular expressions.

* The program outputs JSON in a very chatty form (lots of info), which can be reduced quickly.

Credit
**********************
Syntactic was written by Omer Shapira, based on an algorithm described by Alexander Clark.


Structure
**********************
Syntactic
\_syntaxLearner.java
  |
  |__LearnerMain.java
  |__Learner.java
  |__Recorder.java
  |__Cluster.java
  |__ClusterContext.java
  |
  \_Corpus
  . |
  . |__Context.java
  . |__VocabularyContext.java
  . |__Word.java
  . |__Corpus.java
  . |__Vocabulary.java
  . \_source
  . .|
  . .|__CorpusSource.java
  . .|__PlainTextFile.java
  . .|__WikiDump.java
  \_UI
  .|
  .|__Console.java
  .|__Report.java
  ..

Tasks
**********************

Features and Performance:
-------------
* Add multithreading to the main clustering procedure. Relevant scope: Learner.java
* Improve the parser and source ingest. Relevant scope: the Corpus and Corpus.source packages.
* Serialize the data structures so that the program will be interruptible. NOTE: Some of the code needs to be made more generic beforehand, please let me know before beginning.
* Add a parser for Google's 3-gram database. Because of size constraints, this requires serializing the data structure first.
* Add rulesets for parsers. Rulesets should be written in regexes, with a rule for SPLIT, and a 2-place rule for REPLACE.
* Add output processors. Currently the output is JSON and save for its copious space requirements, it works well. That's why I suggest processing the output with scripts instead of with the main code.

Algorithms:
-------------
I'm currently relying on Clark's description of the learner. Here are his notes:
	http://www.cs.rhul.ac.uk/home/alexc/papers/09194cla.pdf
	http://www.cs.rhul.ac.uk/home/alexc/papers/thesis.pdf

* Implement the EM algorithm in order to deal with ambiguity (Chapter 5.5 in the second paper). This requires intrusive surgery, so I suggest talking to me before the cutting begins.
* Implement a method to deal with rare words (Chapter 5.6 in the second paper).

