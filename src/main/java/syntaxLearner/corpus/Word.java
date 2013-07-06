package syntaxLearner.corpus;

import java.util.Map;
import java.util.TreeMap;
import syntaxLearner.*;

public class Word implements Comparable<Word>{

	/* Identifiers */ 
	public final String name;
	public final Vocabulary vocab;
	public final int ID;

	/* Data Structures*/
	public Map<VocabularyContext, Integer> vocabContexts;		//# of contexts in V*xV* space, NEVER updates
	private int[][] clusterContexts;

	//public Cluster parent;			

	/* Empirical Tools */
	public int frequency;
	private double smoothingFactor=0;		//The number to be added to zero values
	private double smoothingCoefficent=1;	//The coefficient multiplying non-zero values


	public Word (String name, Vocabulary v){
		this.name=name;
		this.vocab=v;
		ID = vocab.newID();
		vocabContexts = new TreeMap<VocabularyContext,Integer>();
		frequency=1;
	}

	public void addContext(VocabularyContext c){
		if (vocabContexts.containsKey(c)){
			vocabContexts.put(c, vocabContexts.get(c)+1); 
		} else {
			vocabContexts.put(c, 1);
		}
	}

	/**
	 * @return The smoothed distribution of the word in (K+1)^2 space.
	 */
	public double clusterDistribution(ClusterContext c) {

		calculateSmoothingFactor();
		int value;
		if ((value = clusterContexts[c.type1+1][c.type2+1])!=0){
			return value*smoothingCoefficent*1.0/frequency;
		} else {
			return smoothingFactor;
		}
	}

	private void calculateSmoothingFactor(){
		if (!vocab.isWordUpdated(ID)) {
			clusterWords();
			//zeros = (K+1)^2 - occupied contexts
			//count nonZeros and find the minimum and its frequency
			int nonZeros = 0;
			int min = 0;
			int minValues = 0;
			for (int[] ai : clusterContexts){
				for (int j : ai){
					if (j!=0) {
						nonZeros++;
						//initialize min
						if ((min==0) || (min>j)) {
							min=j;
							minValues=1;
						} else if (j==min){
							minValues++;
						}

					}

				}
			}

			int zeros = (vocab.getCorpus().getLearner().getClusterContexts().size() - 
					nonZeros);
			if (zeros!=0) {
				smoothingCoefficent = 1 - (minValues * 1.0 / frequency);
				smoothingFactor = minValues * 1.0 / (frequency * zeros);  
			} else {
				//if there are no zeros, don't smoothe
				smoothingFactor=0;
				smoothingCoefficent= 1;

				//Register in the vocabulary as calculated
				vocab.registerWordUpdate(ID);
			}
		}
	}

	/**
	 * To be updated at every turn.
	 */
	private void clusterWords() {
		if (!vocab.isWordUpdated(ID)){

			Learner learner = vocab.getCorpus().getLearner();
			int numOfClusters = learner.getNumOfClusters();
			clusterContexts = new int[numOfClusters+1][numOfClusters+1];
			for (Map.Entry<VocabularyContext, Integer> e : vocabContexts.entrySet()){
				clusterContexts[learner.getParent(e.getKey().type1)+1]
						[learner.getParent(e.getKey().type2)+1]+=e.getValue();
			}
		}
	}


	public void setParent(Cluster c){
		//this.parent=c;
		vocab.getCorpus().getLearner().setParent(this.ID, c.getID());
	}

	public Cluster getParent(){
		short parentID = vocab.getCorpus().getLearner().getParent(this.ID);
		return vocab.getCorpus().getLearner().getCluster(parentID);
	}

	public int getParentID(){
		return vocab.getCorpus().getLearner().getParent(this.ID);
	}

	public void increase(int i){
		frequency+=i;
	}

	@Override
	public int compareTo(Word w) {
		if 		(this.frequency>w.frequency) 	return 2;
		else if (this.frequency<w.frequency) 	return -2;
		else 							return -Math.abs(this.name.compareTo(w.name));
	}

}
