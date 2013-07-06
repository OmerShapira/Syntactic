package syntaxLearner.corpus;
import syntaxLearner.corpus.source.*;
import syntaxLearner.UI.*;
import syntaxLearner.*;
import java.io.File;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * Main class for search. Typically, only one exists for any language.
 * @author Omer Shapira
 *
 */

public class Corpus implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 864168299858422048L;
	/* Properties */
	public String name;
	public long tokenCount=0;
	private Vocabulary vocab;
	private final Learner learner;
	
	/* Data Structures */
	private List<CorpusSource> sources;
	
	/* Parameters */
	private Pattern DELIMITER=Pattern.compile("[\\s]+?");
	
	/* Debug */
	//private boolean _DEBUG = false;

	
	/* Constructor */
	public Corpus(String name, Learner l){
		this.name = name;
		this.learner = l;
		sources = new LinkedList<CorpusSource>();
		vocab = new Vocabulary(this);
	}

	public void addPlainTextFile(String fileFullPath){
		PlainTextFile f = new PlainTextFile(fileFullPath);
		sources.add(f);
		Console.line(name+" : Included "+f.name);
	}
	
	public void addPlainTextFile(File file){
		PlainTextFile f = new PlainTextFile(file);
		sources.add(f);
		Console.line(name+" : Included "+f.name);
	}
	
	public void addWikiDump(File file){
		WikiDump f = new WikiDump(file);
		sources.add(f);
		Console.line(name+" : Included "+f.name);
	}
	

	/**
	 * To be called rarely. Builds the n-gram database and vocabulary
	 */
	public void buildDB(){
		Console.line("Building Database\n**********************\n");
		for (CorpusSource source : sources){
			if (!source.wasRead()){
				Console.text(".");
				if (source.open()){
					while (source.hasNext()){
						String sentence = source.readSentence();
						//sentence = sentence.replaceAll("[\\W\\d]+", " ").trim(); //^a-zA-Z
						sentence = sentence.replaceAll("\\d+?", " ").replaceAll("(\\s+?\\W+?)", " ")
						.replaceAll("(\\W+?\\s+?)"," ").replaceAll("[-/\\[\\]\\(\\)]", " " );
						if (sentence.length()!=0){
							String[] words = toWords(sentence);
							for (String word : words){
								vocab.add(word);
							}
							for (int i=0;i<words.length;i++) {
								//Make the right context for the word
								VocabularyContext cont = new VocabularyContext(
										vocab.getIndex(i==0 ? "$START" : words[i-1]),
										vocab.getIndex(i==(words.length-1) ? "$END" : words[i+1])
										);
								//add it to the word
								vocab.getWord(vocab.getIndex(words[i])).addContext(cont);
								//add 1 to the token count (absolute corpus size, with repititions)
								tokenCount++;
								
							}
						}
					}
				} else {
					Console.line("");
					Console.error("Can't open "+source.name, name);
				}
				source.close();
			}
			source.markAsRead();
		}
		Console.line("Done building database\n");
		sources.clear();
	}

	private String[] toWords(String sentence) {
		String[] words = DELIMITER.split(sentence);
		List<String> wordsBuffer = new LinkedList<String>();
		for (int i=0;i<words.length;i++){
			wordsBuffer.add(words[i]);
		}
		while (wordsBuffer.contains("")){
			wordsBuffer.remove("");
		}
		String[] newWords = new String[wordsBuffer.size()];
		int l = newWords.length;
		if (l!=0){
			for (int i=0; i<l;i++){
				String word=wordsBuffer.get(i).trim().toLowerCase();
				newWords[i]=word;
				//TODO Complete
			}
		}
		return newWords;
	}
	
	public Vocabulary getVocabulary(){
		return vocab;
	}
	
	public void nGramPrinter(String s){
		Word w = vocab.getWord(vocab.getIndex(s));
		for (Entry<VocabularyContext, Integer> e : w.vocabContexts.entrySet()){
			Context c = e.getKey();
			Console.line(vocab.getWord(c.type1).name+" "+
			s+" "+vocab.getWord(c.type2).name+" : "+e.getValue());
		}
	}
	
	public Learner getLearner(){
		return learner;
	}
}
