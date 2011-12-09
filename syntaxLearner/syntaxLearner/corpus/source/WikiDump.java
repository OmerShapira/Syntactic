package syntaxLearner.corpus.source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

import syntaxLearner.UI.Console;
/**
 * Takes in any kind of file with <> brackets that need to be ignored.
 * @author Omer
 *
 */
public class WikiDump extends PlainTextFile {

	public WikiDump(String fullFilePath) {
		super(fullFilePath);
	}
	
	public WikiDump(File f){
		super(f);
	}
	
	@Override
	public boolean open() {
		if (!isOpen){
			try {
				FileReader fr = new FileReader(source);
				BufferedReader br = new BufferedReader(fr);
				sc = new Scanner(br).useDelimiter("[\\.\\;]+?");
				isOpen = true;
			} catch (FileNotFoundException e) {
				Console.line("ERROR: File not found at "+source.getAbsolutePath());
				//e.printStackTrace();
			}
		}
		return isOpen;
	}

	
	
	@Override
	public String readSentence() {
		return sc.next();
	}
}
