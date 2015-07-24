package javax.arang.ref.util;

import java.util.StringTokenizer;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class IGVtoGSNAP extends IOwrapper{

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String inFile = "C://Documents and Settings/아랑/바탕 화면/mito/ref/mitocon_hg19_bait_igv.fasta";
		String outFile = "mitocon_hg19_bait_gsnap.fasta";
		if (args.length > 0) {
			inFile = args[0];
			outFile = args[1];
		}
		new IGVtoGSNAP().go(inFile, outFile);
	}

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		StringTokenizer st;
		while (fr.hasMoreLines()) {
			line = fr.readLine().toString();
			if (line.equals(""))	continue;
			st = new StringTokenizer(line, ">_-");
			String firstLine = ">" + st.nextToken() + ":" + st.nextToken() + ".." + st.nextToken();
			String secondLine = fr.readLine().toString();
			fm.writeLine(firstLine);
			fm.writeLine(secondLine);
		}
	}

	@Override
	public void printHelp() {
		// TODO Auto-generated method stub
		
	}


}
