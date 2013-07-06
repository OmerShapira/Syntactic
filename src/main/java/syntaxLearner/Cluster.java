package syntaxLearner;

import java.util.Set;
import java.util.TreeSet;

import syntaxLearner.corpus.Vocabulary;
import syntaxLearner.corpus.Word;

public class Cluster {

	public final short ID;
	public int totalSize;	//size of this portion of the corpus
	public int wordCount;	//individual word
	public Set<Integer> words;
	//private Map<Context,Double> distribution;
	private double[][] distribution;
	private final Vocabulary vocab;
	private final Learner learner;
	private boolean isNew = false;

	public Cluster(Vocabulary v, Learner l){
		this.vocab=v;
		this.learner=l;
		this.ID = learner.newClusterID();
		words = new TreeSet<Integer>();
		reset();
	}

	public Cluster (Vocabulary v, Learner l, boolean isGround){
		this.vocab=v;
		this.learner=l;
		this.ID = isGround? -1 : learner.newClusterID();
		words = new TreeSet<Integer>();
		reset();
	}



	/**
	 * Calculates the distribution of an entire Cluster.
	 * Similar to calculating a centroid with k-means.
	 * @return a "Map" vector 
	 */
	public double[][] clusterDistribution(){
		if (learner.isClusterUpdated(this)) {
			return distribution;
		} else {
			/* Initialize */
			distribution = new double[learner.NUMBER_OF_CLUSTERS+1][learner.NUMBER_OF_CLUSTERS+1];
			Set<ClusterContext> clusterContexts = learner.getClusterContexts();

			/* Sum every context in every word */

			for (int i : words){
				Word w = vocab.getWord(i);
				double weight =  (1.0*w.frequency)/(totalSize*wordCount);
				for (ClusterContext cc : clusterContexts){
					distribution[cc.type1 + 1][cc.type2 + 1]+= (w.clusterDistribution(cc)*weight);
				}
				learner.registerClusterUpdate(this);
			}
			return distribution;
		}
	}

	/* Sets parenthood */ 
	public void add(Cluster c){
		//Actually asserting that intersection (this, c) is empty.
		for (int i: c.words){
			vocab.getWord(i).setParent(this);
			learner.setParent(i,this.ID);
		}
		words.addAll(c.words);
		totalSize+=c.totalSize;
		wordCount+=c.wordCount;
	}

	/* Adds, counts the values and sets parenthood */
	public void add(int i){
		words.add(i);
		wordCount++;
		Word w = vocab.getWord(i);
		totalSize+=w.frequency;
		w.setParent(this);
	}

	public void remove(int i){
		if (words.contains(i)){
			words.remove(i);
			wordCount--;
			totalSize-=vocab.getWord(i).frequency;
			assert (totalSize>=0 && wordCount>=0);
			}
	}

	/**
	 * Wipes all data apart from the parent vocabulary.
	 * Done this way instead of destructing the object
	 * altogether in order to keep a constant number of 
	 * clusters in the count.
	 */
	public void reset(){
		words.clear();
		wordCount = 0;
		totalSize=0;	
	}

	public short getID(){
		return ID;
	}
	
	public boolean isNew(){
		return isNew;
	}
	
	public void setNew(boolean b){
		isNew=b;
	}
}
