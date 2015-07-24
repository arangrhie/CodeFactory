package javax.arang.pop;

import java.util.ArrayList;
import java.util.Vector;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.snp.SNP;

public class DstatThreadMain extends IOwrapper {

	public void printHelp() {
		System.out.println("Usage: java -jar popDstat.jar <AA_derived.snp> <present_derived.snp> <h1_start:h1_end> <h2_start:h2_end> <numThreads>");
		System.out.println("<_derived.snp>: files generated from snpToDerived.jar");
		System.out.println("\t<h1>: h1 in <present_derived.snp>. h1_start is the name of the population begin to compare, compares until h2_end");
		System.out.println("\t<h2>: h2 in <present_derived.snp>. Calculates d between h1 to h2, by replacing h2 from h2_start to h2_end.");
		System.out.println("\t<out>: <AA_present.d> with D = (nBABA - nABBA) / (nBABA + nABBA)");
		System.out.println("Arang Rhie, 2014-12-22. arrhie@gmail.com");
	}
	
	public static void main(String[] args) {
		if (args.length != 5) {
			new DstatThreadMain().printHelp();
		} else {
			DstatThreadMain dStatRunner = new DstatThreadMain();
			dStatRunner.aaDerivedFile = args[0];
			dStatRunner.h1Start = args[2].split(":")[0];
			dStatRunner.h1End = args[2].split(":")[1];
			dStatRunner.h2Start = args[3].split(":")[0];
			dStatRunner.h2End = args[3].split(":")[1];
			dStatRunner.numThreads = Integer.parseInt(args[4]);
			dStatRunner.go(args[1], args[0].substring(0, args[0].indexOf("_")) + "_" + args[1].substring(0, args[1].indexOf("_")) + ".d");
		}
	}
	
	String aaDerivedFile;
	String h1Start;
	String h1End;
	String h2Start;
	String h2End;
	int numThreads;
	
	int h1StartIdx;
	int h1EndIdx;
	int h2StartIdx;
	int h2EndIdx;
	boolean isH1 = false;
	boolean isH2 = false;

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line = fr.readLine();
		String[] tokens = line.split("\\s+");
		Vector<String> h1List = new Vector<String>();
		Vector<String> h2List = new Vector<String>();
		for (int i = SNP.SAMPLE_START; i < tokens.length; i++) {
			if (h1Start.equals(tokens[i])) {
				h1StartIdx = i;
				isH1 = true;
			}
			if (h1End.equals(tokens[i])) {
				h1EndIdx = i;
				h1List.add(tokens[i]);
				isH1 = false;
			}
			if (h2Start.equals(tokens[i])) {
				h2StartIdx = i;
				isH2 = true;
			}
			if (h2End.equals(tokens[i])) {
				h2EndIdx = i;
				h2List.add(tokens[i]);
				isH2 = false;
			}
			if (isH1) {
				h1List.add(tokens[i]);
			}
			if (isH2) {
				h2List.add(tokens[i]);
			}
			
		}
		
		int h1Idx = 0;
		int h2Idx = 0;
		boolean isLeft = false;
		ArrayList<DstatThread> threads = new ArrayList<DstatThread>();
		
		if (numThreads < h1List.size() * h2List.size())	isLeft = true;
		
		while (isLeft) {
			THREAD_LOOP : for (int i = 0; i < numThreads; i++) {
				DstatThread dStatThread = new DstatThread(i, aaDerivedFile, fr.getFullPath(), h1List.get(h1Idx), h2List.get(h2Idx));
				dStatThread.start();
				threads.add(dStatThread);
				h2Idx++;
				if (h2Idx==h2List.size()) {
					h2Idx = 0;
					h1Idx++;
					if (h1Idx == h1List.size()) {
						isLeft = false;
						break THREAD_LOOP;
					}
				}
			}
			
			for (int i = 0; i < threads.size(); i++) {
				DstatThread t = threads.get(i);
				try {
					t.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			for (int i = 0; i < threads.size(); i++) {
				DstatThread t = threads.get(i);
				fm.writeLine(t.getH1() + "\t" + t.getH2() + "\t" + t.getD());
			}
			threads.clear();
		}
		
		
	}

}
