package syntaxLearner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import syntaxLearner.UI.Console;
import syntaxLearner.corpus.*;

public class Learner {

	/* Parameters */
	//If a distance between two clusters is below this, use their union
	final double 	IDENTITY_EPSILON; 
	//The algorithm halts when this portion has been clustered
	final float 	HALTING_RATIO=0.9f;	
	//a word is "rare" if it appears less than this many times in the corpus
	final int 		RARE_WORD_THRESHOLD; 
	final int		NUMBER_OF_CLUSTERS;		

	/* Class Variables */
	private short learnerID = 0;
	private Corpus corpus;
	private Map<Short, Cluster> clusters;
	private Cluster groundCluster;
	private Set<Short> updatedClusters;
	private int numOfRareWords;
	protected short[] parents;    //is very large
	private int iterationCounter = 0;
	private Recorder rec;

	//helper iterable set that saves time when calculating distances
	private Set<ClusterContext> clusterContexts;
	//Iterator that keeps track of word heirarchy
	Iterator<Integer> backupIterator;

	public Learner(int clusters, int threshold, double epsilon){
		IDENTITY_EPSILON = (epsilon<=0 ? 0.001 : epsilon);
		NUMBER_OF_CLUSTERS = (clusters<=1 ? 50 : clusters);
		RARE_WORD_THRESHOLD = (threshold<=1? 50: threshold);
	}
	
	/* Initializer */
	public void setCorpus(Corpus c){
		this.corpus = c;
		clusters = new HashMap<Short,Cluster>();
		groundCluster = new Cluster(corpus.getVocabulary(), this, true);
		updatedClusters = new HashSet<Short>();
		clusters.put((short)(-1), groundCluster);
		clusterContexts = new TreeSet<ClusterContext>();
	}

	/**
	 * Step 1 of the Algorithm
	 */
	private void prepareClusters(){
		Vocabulary vocab = corpus.getVocabulary();
		numOfRareWords = vocab.countWordsBelowThreshold(RARE_WORD_THRESHOLD);


		Console.line("Words: "+vocab.getNumOfWords());
		Console.line("Rare words: "+numOfRareWords);
		Console.line("Words to scan: "+(vocab.getNumOfWords()-numOfRareWords));


		SortedSet<Integer> wordHeirarchy = vocab.getWordHierarchy();
		

		//These two work in unison:
		Iterator<Integer> iter = wordHeirarchy.iterator();
		backupIterator = wordHeirarchy.iterator();

		for (int i=0;i<NUMBER_OF_CLUSTERS;i++){
			//advance in unison, so backupIterator will remember the next word in the hierarchy
			int l = 	iter.next();
			backupIterator.next();
			//make sure you're not clustering a start / end symbol
			Word w = vocab.getWord(l);
			if ((!(w.equals(vocab.START_SYMBOL)))
					&& (!(w.equals(vocab.END_SYMBOL)))) {
				Cluster c = new Cluster(vocab, this);
				c.add(l);
				Console.line(String.format("Created cluster #%1$2s with stem: \"%2$-1s\"",
						c.ID,w.name));
				clusters.put(c.getID(), c);	
			} else {
				//otherwise, skip a step without changing the placemarker
				i--;
				continue;
			}
		}
		while (iter.hasNext()){
			int l = iter.next();
			groundCluster.add(l);
		}

		//Associate START_SYMBOL and END_SYMBOL
		groundCluster.add(vocab.START_SYMBOL.ID);
		groundCluster.add(vocab.END_SYMBOL.ID);



		// Now build a helper set of all possible coordinates
		getClusterContexts();
	}

	/**
	 * Check to see if any distance between two clusters falls below epsilon
	 * (defined above), then update parenthood, add everything to c1,
	 * and reset c2.
	 */
	private boolean unifyCloseClusters() {		
		for (Cluster c1 : clusters.values()){
			for (Cluster c2 : clusters.values()){
				double dist;
				if ((c1.getID() != c2.getID())&&
						(c1.getID()!=-1) &&
						(c2.getID()!=-1) &&
						((dist = distance(c1,c2))<IDENTITY_EPSILON)
						&& (!c2.isNew())){

					//NOTE: 'add(cluster)' also transfers parenthood
					//Check if there is another word to add and it hasn't been added, else return false to halt
					boolean isNextClustered = true;
					int next=-1; //fail-fast
					while (backupIterator.hasNext() && (isNextClustered)){
						next = backupIterator.next();
						isNextClustered = (parents[next] != -1);
					}
					Word w = corpus.getVocabulary().getWord(next);
					if (w.frequency < RARE_WORD_THRESHOLD)
					{
						//the word is rare, halt
						return false;

					} else {
						c1.add(c2);
						c2.reset();
						c2.add(next);
						c2.setNew(true);
						groundCluster.remove(next);
						String message = String.format("Cluster #%1$2s merged into #%2$2s at distance [%3$-8g]. " +
								"Cluster #%1$2s recreated with stem: \"%4$1s\"", c2.ID, c1.ID, dist, 
								w.name);
						Console.line(message);
						updatedClusters.clear();
						//corpus.getVocabulary().purgeUpdatedWords();

					}
				}
			}
		}
		for (Cluster c : clusters.values()){
			c.setNew(false);
		}
		return true;
	}

	/**
	 * MAIN ALGORITHM METHOD
	 */
	private void clusterCommonWords(){
		/* Safety Assertions */
		assert (HALTING_RATIO > 0 && HALTING_RATIO < 1);
		assert (NUMBER_OF_CLUSTERS > 0);

		/* Step 1 */
		corpus.buildDB();
		parents = new short[corpus.getVocabulary().getNumOfWords()+1];
		prepareClusters();
		int size = corpus.getVocabulary().getNumOfWords();

		/* 
		 * Data structure to save candidate lists for adding to clusters.
		 * Data is organized: <Distance,wordIndex> inside a TreeMap for every Cluster:
		 * <ClusterIndex,TreeMap<K,V>>
		 */
		//TODO Change description
		TreeMap<Integer,TreeMap<Double,Integer>> candidateLists = new TreeMap<Integer,TreeMap<Double,Integer>>();
		for (int i : clusters.keySet()){
			TreeMap<Double,Integer> closestValues = new TreeMap<Double,Integer>();
			candidateLists.put(i, closestValues);
		}
		Console.line("Algorithm launched.");
		rec.recordCorpusData(corpus, this);
		double percentageTracker;
		/* Here be iterations */
		mainLoop: while ((percentageTracker= (1.0*size-groundCluster.wordCount)
				/(size-numOfRareWords)) < HALTING_RATIO){



			iterationCounter++;
			//unification done before iterations begin, not after they end. 
			if (iterationCounter>0) {
				Console.line("Unifying clusters.");
				if(!unifyCloseClusters()){
					//if there are no words left, we're done
					break mainLoop; 
				}
			}
			//Automatically deletes the update list if necessary
			updatedClusters.clear();
			corpus.getVocabulary().purgeUpdatedWords();
			//List sortable Words TODO use this list for the algorithm itself, instead of just for the recording
			StringBuilder sb = new StringBuilder();
			sb.append("[ ");
			for (int index: groundCluster.words){
				if (corpus.getVocabulary().getWord(index).frequency >= RARE_WORD_THRESHOLD){
					sb.append(Integer.toString(index)+", ");
				}
			}
			sb.deleteCharAt(sb.length()-2);
			sb.append("]");
			//update cluster records
			
			rec.recordNewIteration(sb.toString());
			for (Cluster c : clusters.values()){
				if (c.ID>=0){
					rec.recordClusterInfo(c);
				}
			}
			Console.line("Calculating distances");

			//int displayCounter = 0;
			for (int index: groundCluster.words){
				Word w = corpus.getVocabulary().getWord(index);
				//Data structure: <distance,cluster Index >
				TreeMap <Double, Short> distances = new TreeMap<Double, Short>();
				if (w.frequency >= RARE_WORD_THRESHOLD){
					for (Cluster c : clusters.values()){
						//if not ground cluster, calculate distance and store
						if (c.getID()!=-1) {
							distances.put(distance(w,c),c.getID());
						}
					}
				}
				
				if (!distances.isEmpty()){
					
					//Build Vector
					StringBuilder distributionVector = new StringBuilder();
					distributionVector.append("[");
					Iterator<ClusterContext> iter = clusterContexts.iterator();
					while (iter.hasNext()){
						distributionVector.append(String.format("%1$.5f", w.clusterDistribution(iter.next())));
						if (iter.hasNext()){
							distributionVector.append(", ");
						}	
					}
					distributionVector.append("]");
					rec.recordWordInfo(w, distances, distributionVector);
					
					
					int closestCluster = distances.firstEntry().getValue();
					double distanceToClosestCluster = distances.firstEntry().getKey();
					candidateLists.get(closestCluster).put(distanceToClosestCluster,w.ID);
					distances.remove(distances.firstEntry().getKey());
					double distanceGap = distances.firstEntry().getKey()- distanceToClosestCluster;
					
					String message = String.format("\"%1$-15s\"  -> %2$-2s [%3$-8g] Count: %7$-8s ; Next: %4$2s [%5$-8g] ; Gap: [%6$-8g]",
							w.name, 
							closestCluster, 
							distanceToClosestCluster,
							distances.firstEntry().getValue(), 
							distances.firstEntry().getKey(),
							distanceGap,
							w.frequency);

					Console.line(message);
					//Console.text((++displayCounter)+" Calculated\r");
				}
			}
			Console.line(" ");
			//Throttles the insertion
			//TODO find some insertion strategy better than this
			int insertionLimit = iterationCounter<10? iterationCounter : 10;
			boolean insertionOccured=false;
			addToClusterByDistance:
				for (Map.Entry<Integer, TreeMap<Double,Integer>> candidateList : candidateLists.entrySet()){
					Iterator<Integer> iter = candidateList.getValue().values().iterator();

					for (int i=0;i<(5+iterationCounter*2);i++){
						if (!iter.hasNext()) {
							continue addToClusterByDistance;
						}
						else {
							int next = iter.next();
							Word w = corpus.getVocabulary().getWord(next);
							Cluster parent = w.getParent();
							parent.remove((int)w.ID);
							int clusterKey = candidateList.getKey();
							clusters.get((short)clusterKey).add(w.ID);
							insertionOccured=true;
						}
					}
				}

			Console.line(" ");

			//TODO Complete Algorithm

			//Report
			Vocabulary vocab = corpus.getVocabulary();
			for (Cluster c : clusters.values()){
				if (c.ID!=-1){
					Console.line("Cluster "+c.ID+" :\n********************");
					for(int l : c.words){
						Console.text(vocab.getWord(l).name+", ");
					}
					Console.line("");
				}
			}

			double newPercentage = 1.0*(size-groundCluster.wordCount)/(size-numOfRareWords);
			Console.text("Iteration "+iterationCounter+" complete. ");
			Console.text(Double.toString(100.0*newPercentage));
			Console.text("% clustered.");
			//if no change, halt
			if (!insertionOccured){ break mainLoop;}
		}

	}

	public short newClusterID() {
		return learnerID++;
	}

	protected boolean isClusterUpdated(Cluster c){
		return updatedClusters.contains(c.getID());
	}

	/**
	 * Updates are wiped every iteration, so updating these is important.
	 * @param c
	 */
	protected void registerClusterUpdate(Cluster c){
		updatedClusters.add(c.getID());
	}

	/**
	 * 
	 * @param a A cluster
	 * @param b Another cluster
	 * @return the KLD: D(a||b)
	 */
	private double distance (Cluster a, Cluster b){
		double sum = 0;
		for (ClusterContext cc : clusterContexts){
			double aDist = a.clusterDistribution()[(int) cc.type1+1][(int) cc.type2+1];
			double bDist = b.clusterDistribution()[(int) cc.type1+1][(int) cc.type2+1];

			sum += aDist * Math.log( aDist / bDist );

		}
		sum = Math.abs(sum);
		return sum;

	}

	/**
	 * @param w A word
	 * @param c A cluster
	 * @return The KLD: D(w||c)
	 */
	private double distance (Word w, Cluster c){
		double sum = 0;
		for (ClusterContext cc : clusterContexts){
			double wDist = w.clusterDistribution(cc);
			double cDist = c.clusterDistribution()[(int) cc.type1+1][(int) cc.type2+1];

			sum +=wDist * Math.log( wDist / cDist );

		}
		sum = Math.abs(sum);
		return sum;

	}

	public int getNumOfClusters() {
		return NUMBER_OF_CLUSTERS;
	}

	public Cluster getCluster(short index){
		if (index < 0){
			return groundCluster;
		} else {
			return clusters.get(index);
		}

	}

	public Set<ClusterContext> getClusterContexts() {
		if (clusterContexts.isEmpty()){
			for (short i : clusters.keySet()) {
				for (short j : clusters.keySet()){
					clusterContexts.add(new ClusterContext(i, j));
				}
			}	
		}
		return clusterContexts;
	}

	public void learn(){
		clusterCommonWords();
	}

	public void setParent(int i, short id) {
		this.parents[i]=id;
	}

	public short getParent(int i){
		return this.parents[i];
	}
	
	public int getIterationCount(){
		return iterationCounter;
	}

	public void setRecorder (Recorder rec){
		this.rec = rec;
	}
}
