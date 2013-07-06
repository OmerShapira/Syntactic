package syntaxLearner.corpus;
/**
 * 3-gram identifier instance. Uses the two exterior words.
 * @author Omer
 *
 */
public abstract class Context implements Comparable<Context> {
	public final int type1, type2;

	public Context(int t1, int t2){
		this.type1=t1;
		this.type2=t2;
	}
	
	@Override
	public boolean equals(Object o){
		if (o instanceof Context){
			Context c = (Context) o;
		return (c.type1==type1 && c.type2==type2);
		}
		else return false;
	}

	@Override
	public int compareTo(Context o) {
		if (type1>o.type1) {
			return 1;
		} else if (type1<o.type1){
			return -1;
		} else if (type2>o.type2){
			return 1;
		} else if (type2<o.type2){
			return -1;
		} else return 0;
	}
	
	
}
