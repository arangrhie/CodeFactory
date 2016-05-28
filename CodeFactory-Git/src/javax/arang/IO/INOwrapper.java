package javax.arang.IO;

import java.util.ArrayList;

import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.IOUtil;
import javax.arang.IO.basic.Wrapper;

public abstract class INOwrapper extends Wrapper {

	public abstract void printHelp();
	private String path = "";
	private int numFiles = 0;
	
	@Deprecated
	public void go(String[] files) {
		startTiming();
		
		String[] inFiles = new String[files.length - 1];
		numFiles = inFiles.length;
		for (int i = 0; i < inFiles.length; i++) {
			inFiles[i] = files[i];
		}
		
		String outFile = files[files.length - 1];
		System.err.println("Processing file");
		ArrayList<FileReader> fileReaders = new ArrayList<FileReader>();
		for (int i = 0; i < inFiles.length; i++) {
			FileReader fr = new FileReader(inFiles[i]);
			fileReaders.add(fr);
			System.err.println("\t" + fr.getFileName());
		}
		FileMaker fm = new FileMaker(outFile);
//		if (outFile.lastIndexOf("/") < 0) {
//			fm = new FileMaker(getPath(outFile), outFile);	
//		} else {
//			fm = new FileMaker(getPath(outFile), outFile.substring(outFile.lastIndexOf("/")));
//		}
		System.err.println("Into " + fm.getFileName());
		
		hooker(fileReaders, fm);
		
		for (FileReader fr : fileReaders) {
			fr.closeReader();
		}
		fm.closeMaker();
		
		printTiming();
	}
	
	public void go(String[] inFiles, String outFile) {
		startTiming();
		
		numFiles = inFiles.length;
		
		System.out.println("Processing file");
		ArrayList<FileReader> fileReaders = new ArrayList<FileReader>();
		for (int i = 0; i < inFiles.length; i++) {
			System.out.println("\t" + inFiles[i]);
			FileReader fr = new FileReader(inFiles[i]);
			fileReaders.add(fr);
		}
		FileMaker fm = null;
		if (IOUtil.retrieveDirectory(outFile).equals(".")) {
			fm = new FileMaker(outFile);
		} else {
			fm = new FileMaker(outFile);
		}
		System.out.println("Into " + fm.getDir() + "/" + fm.getFileName());
		
		hooker(fileReaders, fm);
		
		for (FileReader fr : fileReaders) {
			fr.closeReader();
		}
		fm.closeMaker();
		
		printTiming();
	}
	
	public int getNumFiles() {
		return numFiles;
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
	
	public abstract void hooker(ArrayList<FileReader> frs, FileMaker fm);
}
