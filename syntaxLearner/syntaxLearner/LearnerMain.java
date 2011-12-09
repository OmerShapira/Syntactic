package syntaxLearner;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import syntaxLearner.UI.Console;
import syntaxLearner.corpus.Corpus;

public class LearnerMain {

	public static void main (String[] args){
		Console.text("Syntactic, Build 53\n****************");
		if (args.length>5){
			testCorpus(args[0],args[1], args[2], args[3],args[4],args[5]);
		} else {
			System.out.println("Command structure: [name], [input folder] [output folder] [clusters] [threshold] [epsilon]");
		}
		
	}
	
	private static void testCorpus(String name, String inFolder, String outFolder, String clusters, String threshold, String epsilon){
		Learner l = new Learner(Integer.parseInt(clusters),Integer.parseInt(threshold),Double.parseDouble(epsilon));
		Corpus c = new Corpus(name,l);
		
		Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH-mm-ss");
		String outName = String.format("%1$s %2$s", name, sdf.format(cal.getTime()));
		File f1 = new File(inFolder);
		File f2 = new File(outFolder, outName);
		f2.mkdir();
		System.out.println(f2.getAbsolutePath()+" created");
		Recorder r = new Recorder(l,f2,name,true);
		l.setRecorder(r);
		recursiveAdd(f1,c);
		
		l.setCorpus(c);
		l.learn();
		
	}

	private static void recursiveAdd(File f, Corpus c) {
		File[] files = f.listFiles();
		for (File file:files){
			if (file.isFile()){
				if (file.getName().endsWith(".txt")){
					c.addPlainTextFile(file);
				}
				} else if (file.isDirectory()){
					recursiveAdd(file, c);
				}
		}
		
	}
	
}
