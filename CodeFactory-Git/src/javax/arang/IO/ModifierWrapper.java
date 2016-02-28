package javax.arang.IO;

import javax.arang.IO.basic.FileModifier;
import javax.arang.IO.basic.Wrapper;

public abstract class ModifierWrapper extends Wrapper {

	public void go(String inFile) {
		startTiming();
		
		FileModifier fr = new FileModifier(inFile);
		System.out.println("Processing file " + fr.getFileName());
		
		hooker(fr);
		
		fr.closeModifier();

		printTiming();
	}

	public abstract void hooker(FileModifier fr);
	
	public abstract void printHelp();

}
