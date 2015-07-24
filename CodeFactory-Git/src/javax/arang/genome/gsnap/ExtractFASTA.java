package javax.arang.genome.gsnap;

import java.util.StringTokenizer;

import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ExtractFASTA {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			new ExtractFASTA().go(args[0], args[1]);
		} else {
			new ExtractFASTA().go("C://Documents and Settings/아랑/바탕 화면/gsnap_test.gsnap", "gsnap_test");
		}
		
	}
	
	public void go(String inFile, String outFile) {
		FileReader fr = new FileReader(inFile);
		String path = inFile.substring(0, inFile.lastIndexOf("/"));
		FileMaker fm = new FileMaker(path, outFile + "_sequence.fasta");
//		FileMaker fm2 = new FileMaker(path, outFile + "_2_sequence.fasta");
		
		String line;
		StringTokenizer st;
		String readId = "";
		String sequence1 = "";
		String sequence2 = "";
		String qual1 = "";
		String qual2 = "";
		while(fr.hasMoreLines()){
			line = fr.readLine().toString();
			if (line.startsWith(">")) {
				st = new StringTokenizer(line);
				sequence1 = st.nextToken();
				sequence1 = sequence1.substring(1);
				st.nextToken();	// numMatches
				st.nextToken();	// concordant
				readId = st.nextToken();	// readId
				qual1 = st.nextToken();
			} else if (line.startsWith("<")) {
				st = new StringTokenizer(line);
				sequence2 = st.nextToken();
				sequence2 = sequence2.substring(1);
				st.nextToken();	// numMatches
				st.nextToken();	// concordant
				st.nextToken();	// readId
				st.nextToken();	// qual1
				qual2 = st.nextToken();
				writeToFastq(readId, sequence1, qual1, sequence2, qual2, fm);
			}
		}
		fr.closeReader();
		fm.closeMaker();
//		fm2.closeMaker();
	}
	
	public void writeToFastq(String readId,
			String sequence1, String qual1,
			String sequence2, String qual2, FileMaker fm1) {
		fm1.writeLine(">" + readId);
		fm1.writeLine(sequence1);
		fm1.writeLine(sequence2);
		fm1.writeLine("+" + readId);
		fm1.writeLine(qual1);
		fm1.writeLine(qual2);
		
	}
}
