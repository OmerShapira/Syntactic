package syntaxLearner.corpus.source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

import syntaxLearner.UI.Console;

public class PlainTextFile extends CorpusSource {

	Scanner sc;

	public PlainTextFile(String fileFullPath) {
		super(fileFullPath);
		source = new File(fileFullPath);
		isOpen = false;
	}
	
	public PlainTextFile(File f){
		super(f);
		isOpen = false;
	}

	@Override
	public String readSentence() {
		return sc.next();
	}

	@Override
	public void seekToStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean open() {
		if (!isOpen){
			try {
				FileReader fr = new FileReader(source);
				BufferedReader br = new BufferedReader(fr);
				sc = new Scanner(br).useDelimiter("[\\.\\,\\;\\!\\?\\\"\\“\\”]+?"); // removed \\\'
				isOpen = true;
			} catch (FileNotFoundException e) {
				Console.line("ERROR: File not found at "+source.getAbsolutePath());
				//e.printStackTrace();
			}
		}
		return isOpen;
	}

	@Override
	public boolean close() {
		sc.close();
		isOpen=false;
		return !isOpen;
	}

	@Override
	public boolean hasNext() {
		if (isOpen && !wasRead){
			return sc.hasNext();
		} else {
			return false;
		}
	}

}
