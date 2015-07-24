package javax.arang.IO;

import java.text.MessageFormat;

import javax.arang.IO.basic.FileReader;

public abstract class R2wrapper {

	public void go(String inFile1, String inFile2) {
		long startTime = System.currentTimeMillis();
		
		FileReader fr1 = new FileReader(inFile1);
		FileReader fr2 = new FileReader(inFile2);
		
		hooker(fr1, fr2);
		
		fr1.closeReader();
		fr2.closeReader();
		
		long runningTime = (System.currentTimeMillis() - startTime) / 1000;
		System.out.println(MessageFormat.format("Running time : {0} h {1} m {2} sec", (runningTime/360), (runningTime/60),
				(runningTime%60)));
		System.out.println();
	}
	
	public abstract void hooker(FileReader fr1, FileReader fr2);
	
	public abstract void printHelp();
}
