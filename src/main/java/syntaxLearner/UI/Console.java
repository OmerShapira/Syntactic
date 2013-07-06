package syntaxLearner.UI;

public class Console {

	public static void message(String s, String identifier){
		System.out.println("["+identifier+"] : "+s);
	}
	
	public static void line (String s){
		System.out.println(s);
	}
	
	public static void text (String s){
		System.out.print(s);
	}
	
	public static void error (String s){
		System.out.println("ERROR: "+s);
	}
	
	public static void error (String s, String identifier){
		System.out.println("["+identifier+"] ERROR: "+s);
	}
}
