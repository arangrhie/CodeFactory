package javax.arang.genome.gsnap;

import java.util.StringTokenizer;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/***
 * Input: gsnap aligned file
 * Output: gsnap aligned file containing alignment result against extracted chromosome
 * @author 아랑
 *
 */
public class ExtractChr extends IOwrapper {

	String ref;

	private String getRef() {
		return ref;
	}
	
	private void setRef(String ref) {
		this.ref = ref;
	}
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		String refChr =  getRef();
		String line;
		String lines = "";
		StringTokenizer st;
		boolean hasChr = false;
		while (fr.hasMoreLines()) {
			line = fr.readLine().toString();
			if (line.equals("")) continue;
			else if (line.startsWith(">")) {
				if (hasChr) {
					fm.writeLine(lines + "\n");
				}
				lines = line + "\n";
				hasChr = false;
			} else if (line.startsWith("<")) {
				lines = lines + "\n\n" + line + "\n";
			} else	{
				lines = lines + line + "\n";
				st = new StringTokenizer(line);
				st.nextToken();	// refSeq
				st.nextToken();	// alignRange
				String ref = st.nextToken();	// ref
				ref = (String) ref.subSequence(1, ref.indexOf("_:"));
				if (ref.equals(refChr)) {
					hasChr = true;
				}
			}
		}
		
		if (hasChr) {
			fm.writeLine(lines);
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ExtractChr extChr = new ExtractChr();
		if (args.length == 3) {
			extChr.setRef(args[2]);
			extChr.go(args[0], args[1]);
		} else {
//			extChr.setRef("chr5");
//			extChr.go(inFile, outFile);
			extChr.printHelp();
		}
	}
	
	public void printHelp() {
		System.out.println("Usage: java -jar extractChr.jar <inFile> <outFile> chrN");
		System.out.println("Extract gsnap alignment result aligned against specific chromosome.");
		System.out.println("GSNAP ver: 2011-10-01 or higher, aligned against WGS, with reference name as chrN");
		System.out.println("Example: java -jar extractChr.jar ak45_4.gsnap ak45_4.chrM.gsnap chrM");
		System.out.println("Arang Rhie, Oct. 13, 2011");
	}

}
