package syntaxLearner.UI;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Report {
	
	public String reportPath;
	String filename;
	File file;
	Date date;
	
	public Report(String reportPath, String name){
		this.reportPath = reportPath;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		date = new Date();
		filename = "[Report]"+name+" at "+sdf.format(date)+".txt";
		file = new File (reportPath, filename);
	}
	
	public boolean open(){
		
		return false;
	}
	
	public boolean close(){
		
		return false;
	}

}
