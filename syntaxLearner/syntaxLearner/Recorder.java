package syntaxLearner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import syntaxLearner.corpus.Corpus;
import syntaxLearner.corpus.Vocabulary;
import syntaxLearner.corpus.Word;
/**
 * A library that records the algorithm in JSON for a standard interpreter
 * @author User 1
 *
 */
public class Recorder {
	private File mainPath;
	private File currentPath;
	private File wordPath;
	private String name;
	private boolean isRecording;
	private int iterationCounter = 0;
	private File corpusFile;
	private Learner l;
	private File iterationFile;
	private Corpus c; 

	public Recorder(Learner l, File mainPath,  String name, boolean isRecording){
		this.mainPath = mainPath;
		this.name = name;
		this.isRecording = isRecording;
		this.l=l;
	}
	/*
	 * 
	 * corpus_%corpusName% = {
		name: "...",
		tokenCount: 500,
		typeCount: 1000, 
		commonTypes: 900,
		clusterCount: 100,
		identityEps: 0.00004 
		iterationCount: 7,
		typeToId: 
		{
			typeName:0,
			typeName2: 1, 
			...
		},
		idToType: {
		1: "word",
		4: "bleh",
		}
	};

	 */
	/**
	 * 
	 */
	public void recordCorpusData (Corpus c, Learner l){
		if (!isRecording) return;
		this.c=c;
		Vocabulary v = c.getVocabulary();
		Set<Map.Entry<String, Integer>> entrySet = v.getWordIndicesEntrySet();
		Set<Map.Entry<Integer, Word>> wordSet = v.getWordEntrySet();
		StringBuilder s = new StringBuilder(v.getNumOfWords()*30);
		corpusFile = new File(mainPath, "corpus_"+name+".js");

		s.append(String.format("corpus_%1$s = \n{\n\tname:\"%1$s\",\n", name));
		s.append(String.format("\ttokenCount: %1$s,\n", c.tokenCount));
		s.append(String.format("\ttypeCount: %1$s,\n", v.getNumOfWords()));
		s.append(String.format("\tcommonTypes: %1$s,\n", (v.getNumOfWords()-v.countWordsBelowThreshold(l.RARE_WORD_THRESHOLD))));
		s.append(String.format("\tclusterCount: %1$s,\n", l.NUMBER_OF_CLUSTERS));
		//TODO add proper number formatters
		s.append(String.format("\tidentityEps: %1$s,\n", l.IDENTITY_EPSILON));
		s.append(String.format("\titerationCount: %1$s,\n", iterationCounter));
		s.append("\ttypeToId: \n\t{\n\t");
		for (Map.Entry<String, Integer> e: entrySet){
			if (v.getWord(e.getValue()).frequency >= l.RARE_WORD_THRESHOLD){ //TODO see if necessary
				s.append(String.format("\t\t%1$s: %2$s,\n", e.getKey(), e.getValue()));
			}
		}
		s.append("},\n");
		s.append("\tidToType: {");
		for (Map.Entry<Integer, Word> e: wordSet){ //No rare word check here, to keep vector status
			if (e.getValue().frequency >= l.RARE_WORD_THRESHOLD){
				s.append(String.format("\t\t%1$s: \"%2$s\",\n",e.getKey(), e.getValue().name));	
			}
		}
		s.append("}");
		s.append("\n};");

		try {
			corpusFile.createNewFile();
			BufferedWriter out = new BufferedWriter(new FileWriter(corpusFile));
			out.append(s);
			out.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private void updateIterationData(){
		if (iterationCounter<l.getIterationCount()){
			iterationCounter = l.getIterationCount();
			//TODO Write iteration number to file		
		}
	}
	/*
	 * iteration_%corpusName%_%iterationNumber% = {
	iteration_number : 2,
	unsorted_words: ["a", "b", ...]
};

	 */
	/**
	 * 
	 */
	public void recordNewIteration(String words){
		if (!isRecording) return;
		updateIterationData();
		currentPath = new File(mainPath, Integer.toString(iterationCounter));
		currentPath.mkdir();
		wordPath = new File(currentPath, "words");
		wordPath.mkdir();
		iterationFile = new File(currentPath, 
				String.format("iteration_%1$s_%2$s.js",name,iterationCounter));
		StringBuilder s = new StringBuilder();
		s.append(String.format("iteration_%1$s_%2$s = {\n",name,iterationCounter));
		s.append(String.format("\titeration_number : %1$s,\n",iterationCounter));
		s.append("\tunsorted_words: ");
		s.append(words);
		s.append("\n}");
		try {
			iterationFile.createNewFile();
			BufferedWriter out = new BufferedWriter(new FileWriter(iterationFile));
			out.append(s);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/*
	 * cluster_%corpusName%_%iterationNumber%_%clusterIndex% =
{
	index: 30,
	words: [8, 9, ...],
	distribution: [[0.1, 0.1,...],
		       [0.1, 0.1,...],
		       [0.1, 0.1,....], 
		       ..]
}

	 */
	/**
	 * 
	 */
	public void recordClusterInfo(Cluster c){
		if (!isRecording) return;
		StringBuilder s = new StringBuilder();
		String filename = String.format("cluster_%1$s_%2$s_%3$s", name, iterationCounter,c.ID);
		s.append(filename+" =\n{\n");
		s.append(String.format("\tindex: %1$s,\n", c.ID));
		s.append("\twords: [");
		for (int i: c.words){
			s.append(" "+i+",");
		}
		//remove last comma
		s.deleteCharAt(s.length()-1);
		s.append(" ],\n");
		s.append("\tdistribution: [");
		for (double d1[] : c.clusterDistribution()){
			for (double d2: d1){
				s.append(String.format(" %1$-4f,", d2));
			}
		}
		s.deleteCharAt(s.length()-1);
		s.append(" ]\n}");
		File f = new File (currentPath, filename+".js");
		try {
			f.createNewFile();
			BufferedWriter out = new BufferedWriter(new FileWriter(f));
			out.append(s);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * word_%corpusName%_%iterationNumber%_%wordIndex% = 
{
	word: "..",
	wordIndex: 5,
	freq: 80,
	rank: 150,
	distribution: [[0.1, 0.1,...],
		       [0.1, 0.1,...],
		       [0.1, 0.1,....], 
		       ..],
	dist_to_cluster: []
}

	 */
	public void recordWordInfo(Word w, TreeMap<Double, Short> distances, StringBuilder distribution){
		if (!isRecording) return;
		double[] distanceArray = new double[l.NUMBER_OF_CLUSTERS];
		for (Map.Entry<Double, Short> e : distances.entrySet()){
			assert (e.getValue()<distanceArray.length);
			distanceArray[e.getValue()]=e.getKey();
		}
		String filename = String.format("word_%1$s_%2$s_%3$s", name, iterationCounter, w.ID);
		StringBuilder s = new StringBuilder();
		s.append(filename+" =\n{\n");
		s.append(String.format("\tword: \"%1$s\",\n\twordIndex: %2$s,\n", w.name, w.ID));
		s.append(String.format("\tfreq: %1$s,\n", w.frequency));
		s.append(String.format("\trank: %1$s,\n", c.getVocabulary().getRank(w.ID)));
		s.append("\tdistribution: ");
		s.append(distribution);
		s.append(",\n\tdist_to_cluster : [");
		for (double d: distanceArray){
			s.append(String.format(" %1$-4f,", d));
		}
		s.deleteCharAt(s.length()-1);
		s.append(" ]\n}");
		File f = new File(wordPath, filename+".js");
		try {
			f.createNewFile();
			BufferedWriter out = new BufferedWriter(new FileWriter(f));
			out.append(s);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}	
