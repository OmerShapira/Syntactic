package syntaxLearner.corpus;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import syntaxLearner.UI.Console;

/**
 * 
 * @author Omer Shapira
 * This class is meant to keep track of vocabulary operations,
 * including a data structure for words, hash functions, word
 * affinity in a language, and other helper functions.
 */
public class Vocabulary {

	/* Debug*/
	private boolean _DEBUG=false;

	/* Class Variables */
	private final Corpus corpus;
	private boolean indexedOnce = false;
	private boolean updated = false;
	private int numOfWords = 0; //number of individual words
	private int wordIDCounter = 0;

	/* Data Structures */
	private Map<String,Integer> 	wordIndices;
	private Map<Integer,Word> 		words;
	private SortedSet<Integer> 	wordHierarchy;
	private Map<Integer, Integer> ranks;
	private Set<Integer>			wordsUpdated;

	/* Class Constants */
	public final Word START_SYMBOL, END_SYMBOL;


	public Vocabulary(Corpus c){
		this.corpus = c;
		wordIndices = new TreeMap<String,Integer>();
		words = new HashMap<Integer, Word>();
		wordHierarchy = new TreeSet<Integer>( 
				new Comparator<Integer>() {
					@Override
					public int compare(Integer o1, Integer o2) {
						return -words.get(o1).compareTo(words.get(o2));
					}
				}
		);
		wordsUpdated = new TreeSet<Integer>();
		this.add("$START");
		this.add("$END");
		START_SYMBOL = words.get(wordIndices.get("$START"));
		END_SYMBOL = words.get(wordIndices.get("$END"));
	}




	public void add(String s){
		if (wordIndices.containsKey(s)){
			words.get(wordIndices.get(s)).increase(1);
			if (_DEBUG) Console.line("Vocab #"+wordIndices.get(s)+" : "+
					s+" = "+words.get(wordIndices.get(s)).frequency);
		} else {
			Word w = new Word(s, this);
			wordIndices.put(s, w.ID);
			words.put(w.ID, w);
			if (_DEBUG) Console.line("Vocab : NEW "+s);
			numOfWords++;
		}
		updated = false;
	}

	/**
	 * Updates the word 
	 */
	private void update(){
		wordHierarchy.addAll(words.keySet());
		indexedOnce = true;
		updated  = true;
		generateRanks();
	}

	public SortedSet<Integer> getWordHierarchy(){
		if (!updated) {
			update();
			return wordHierarchy;
		}else {
			return wordHierarchy;
		}
	}

	/**
	 * INNEFICIENT - Generates each time
	 * @return
	 */
	public void generateRanks(){
		if (!updated){
			update();
		}
		ranks = new HashMap<Integer,Integer>();
		Iterator<Integer> iter = wordHierarchy.iterator();
		int i = 1;
		while (iter.hasNext()){
			ranks.put(iter.next(), i++);	
		}
	}

	public int getRank(int index){
		if (!updated){
			generateRanks();
		}
		return ranks.get(index);
	}

	public int newID(){
		return wordIDCounter++;
	}

	public int getIndex(String s){
		return wordIndices.get(s);
	}

	public Word getWord(int i){
		return words.get(i);
	}

	/**
	 * 
	 * @return The number of individual words in the vocabulary
	 */
	public int getNumOfWords(){
		return numOfWords;
	}

	/**
	 * 
	 * @param threshold - Typically the "Rare word threshold"
	 * @return The number of words below a certain threshold. 
	 */
	public int countWordsBelowThreshold(int threshold){
		int i=0;
		for (Word w: words.values()){
			if (w.frequency < threshold){
				i++;
			}
		}
		return i;
	}

	/**
	 * 
	 * @return
	 */
	public Corpus getCorpus(){
		return corpus;
	}

	/**
	 * 
	 * @param i
	 */
	protected void registerWordUpdate(int i){
		wordsUpdated.add(i);
	}
	/**
	 * @param i The word index
	 * @return True if the word is registered as updated
	 */
	protected boolean isWordUpdated(int i ){
		return wordsUpdated.contains(i);
	}
	/**
	 * Resets the state of updated words
	 */
	public void purgeUpdatedWords(){
		wordsUpdated.clear();
	}
	public Set<Entry<String,Integer>> getWordIndicesEntrySet(){
		return wordIndices.entrySet();
	}

	public Set<Entry<Integer,Word>> getWordEntrySet(){
		SortedMap<Integer,Word> m = new TreeMap<Integer,Word>(words);
		return m.entrySet();
	}
}
