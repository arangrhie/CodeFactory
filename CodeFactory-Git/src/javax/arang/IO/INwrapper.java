package javax.arang.IO;

import java.util.ArrayList;

import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.Wrapper;

public abstract class INwrapper extends Wrapper{
	public abstract void printHelp();
	private String path = "";
	
	public void go(String[] files) {
		startTiming();
		
		String[] inFiles = new String[files.length];
		for (int i = 0; i < inFiles.length; i++) {
			inFiles[i] = files[i];
		}
		
		System.out.println("Processing file");
		ArrayList<FileReader> fileReaders = new ArrayList<FileReader>();
		for (int i = 0; i < inFiles.length; i++) {
			FileReader fr = new FileReader(inFiles[i]);
			fileReaders.add(fr);
			System.out.println("\t" + fr.getFileName());
		}
		
		hooker(fileReaders);
		
		for (FileReader fr : fileReaders) {
			fr.closeReader();
		}
		
		printTiming();
	}
	
	public String getPath() {
		return path;
	}
	
	public String getPath(String inPath) {
		if (inPath.contains("/")) {
			path = inPath.substring(0, inPath.lastIndexOf("/"));
		} else if (inPath.contains("\\")) {
			path = inPath.substring(0, inPath.lastIndexOf("\\"));
		} else {
			path = ".";
		}
		return path;
	}
	
	public abstract void hooker(ArrayList<FileReader> frs);

}
