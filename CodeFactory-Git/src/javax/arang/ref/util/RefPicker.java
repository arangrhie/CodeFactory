package javax.arang.ref.util;

import java.util.StringTokenizer;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;


/***
 * Pick specific region [from ~ to] from a fasta file 
 * Input
 * chr2	44113341	44114001
 * chr2	44114289	44115909
 * chr2	44116763	44117123
 * 
 * Output - contig file
 * >chr2:44113341..44114001
 * AGGAGCGT...
 * @author 아랑
 *
 */
public class RefPicker extends Rwrapper{
	
	StringBuffer line = new StringBuffer();
	long pointer = 0;
	String chr = "chr20";
	String refName = "";
	
	@Override
	public void hooker(FileReader fr) {
		
		String line;
		StringTokenizer st;
		String prevChr;
		chr = "";
		
		FileReader frRef = null;
		FileMaker fmOut = null;
		while(fr.hasMoreLines()) {
			line = fr.readLine().toString();
			if (line.startsWith("/") || line.startsWith("#") || line.length() < 2) {
				continue;
			}
			st = new StringTokenizer(line);
			String[] tokens = line.split("\t");
			if (tokens.length == 4) {
				refName = st.nextToken();
			} else {
				refName = tokens[0] + ":" + tokens[1] + ".." + tokens[2];
			}
//			prevChr = chr;
			chr = st.nextToken();
//			if (!prevChr.equals(chr)) {
//			fmOut = new FileMaker(".", "hg19_" + refName + "_contig.fa");
//			frRef = new FileReader("hs_ref_GRCh37_" + chr + ".fa");
			fmOut = new FileMaker(".", "hg18_" + chr + "_contig.fa");
			frRef = new FileReader("hg18_" + chr + ".fa");
//			fmOut = new FileMaker(".", "hg19_" + chr + "_contig.fa");
//			frRef = new FileReader("hg19_" + chr + ".fa");
			pointer = 0;
			line = "";
//			}
			pickWrite(frRef, fmOut, Long.parseLong(st.nextToken()), Long.parseLong(st.nextToken()));
		}
	}
	
	public void pickWrite(FileReader fr, FileMaker fm, long from, long to) {
		boolean skip = false;
		
		fm.writeLine(">" + refName + " " + chr + ":" + from + ".." + to);
		to++;
		if (pointer > from) {
			fm.writeLine(line.substring(line.length() - (int) (pointer - from)));
			skip = true;
		}
		
		while (!skip && fr.hasMoreLines()) {
			line = new StringBuffer(fr.readLine());
			if (line.indexOf(">") >= 0) {
				continue;
			}
			pointer += line.length();
			
			if (pointer > from) {
				long wpoint = line.length() - (pointer - from);
				fm.writeLine(line.substring((int) wpoint));
				break;
			}
		}
		
		while (fr.hasMoreLines()) {
			line = new StringBuffer(fr.readLine());
			pointer += line.length();
			if (pointer <= to) {
				if (!line.equals("")) {
					fm.writeLine(line.toString());
				}
			} else break;
		}
		
		if (pointer > to) {
			int wpoint = line.length() - (int) (pointer - to);
			if (!line.substring(0, wpoint).equals("")) {
				fm.writeLine(line.substring(0, wpoint));
			}
		}
		System.out.println(chr + ":" + from + ".." + to + ", pointer: " + pointer);
	}
	
	public static void main(String[] args) {
		if (args.length > 0) {
			new RefPicker().go(args[0]);
		} else {
			new RefPicker().printHelp();
		}
	}
	
	public void pickFromFile(String filePath, long from, long to){
		FileReader fr = new FileReader(filePath);
		String path = filePath.substring(0, filePath.lastIndexOf("/"));
		String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
		fileName = fileName.replaceAll(".fasta", "");
		FileMaker fm = new FileMaker(path, fileName + "_" + from + "-" + to + ".fasta");
		
		StringBuffer line;
		long pointer = 0;
		while (fr.hasMoreLines()) {
			line = new StringBuffer(fr.readLine());
			if (line.indexOf(">") >= 0) {
				fm.writeLine(line.toString() + " " + from + "~" + to);
				continue;
			}
			pointer += line.length();
			
			if (pointer > from) {
				long wpoint = pointer - from;
				fm.writeLine(line.substring((int) wpoint));
				break;
			}
		}
		
		while (fr.hasMoreLines() && (pointer < to)) {
			line = new StringBuffer(fr.readLine());
			fm.writeLine(line.toString());
			pointer += line.length();
		}
		
		line = new StringBuffer(fr.readLine());
		int wpoint = (int) (pointer - to);
		fm.writeLine(line.substring(0, wpoint));
		
		fr.closeReader();
		fm.closeMaker();
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar refPicker.jar <inFile>");
		System.out.println("Pick specific region [from ~ to] from a fasta file"); 
		System.out.println("\t<inFile> format:");
		System.out.println("\t\tchr2	44113341	44114001");
		System.out.println("\t\tchr2	44114289	44115909");
		System.out.println("\t\tchr2	44116763	44117123");
		System.out.println("\t\t  or");
		System.out.println("\t\tBCS1L.9	chr2	 219524272 	 219528275");
		System.out.println("\t\tSURF1.9	chr9	 136218561 	 136223200");
		System.out.println("\t\tCOX10.7	chr17	 13972601 	 14095661");
		System.out.println();
		System.out.println("\t<Output> format: contig files");
		System.out.println("\t\t>chr2:44113341...44114001 or");
		System.out.println("\t\t>BCS1L.9 chr2:219524272...219528275");
		System.out.println("\t\tAGGAGCGT...");
	}
}
