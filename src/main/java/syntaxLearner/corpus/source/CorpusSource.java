package syntaxLearner.corpus.source;

import java.io.File;

public abstract class CorpusSource {
	public final String name;
	public File source;
	
	
	public CorpusSource(String name){
		this.name = name;
		this.wasRead = false;
	}
	
	public CorpusSource(File f){
		source = f;
		this.name = f.getAbsolutePath();
		this.wasRead = false;
	}
	
	protected boolean wasRead;
	protected boolean isOpen;
	public abstract String readSentence();
	public abstract void seekToStart();
	public abstract boolean open();
	public abstract boolean close();
	
	public boolean wasRead(){
		return this.wasRead;
	}
	
	public abstract boolean hasNext();
	
	public void markAsRead(){
		this.wasRead=true;
	}
	
}
