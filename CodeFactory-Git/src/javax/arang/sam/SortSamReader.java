package javax.arang.sam;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public abstract class SortSamReader extends Rwrapper {

	String line;
	static int QUAL_THRESHOLD = 33;
	static String outPath = ".";
	static String frName = "";
	
	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		int prevPos = -1;
		int endPos = -1;
		String prevRef = "";
		
		FileMaker fm = null;
		frName = fr.getFileName();
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("@"))	continue;
			if (line.equals(""))	continue;
			
			tokens = line.split("\t");
			if (!tokens[Sam.RNEXT].equals("="))	continue;
			if (tokens[Sam.CIGAR].equals("*"))	continue;
			int pos = Integer.parseInt(tokens[Sam.POS]);
			String ref = tokens[Sam.RNAME];
			if (pos == prevPos) {
				// Collect all reads w/ same starting position and add on Base obj
				putPosMap(tokens, pos);
			} else {
				// Write base coverage
				if (!ref.equals(prevRef)) {
					removePosQ(fm, prevPos, endPos);
					if (fm != null) {
						fm.closeMaker();
					}
					fm = new FileMaker(outPath, getFileName(ref));
				} else if (prevPos > 0) {
					removePosQ(fm, prevPos, pos);
				}
				
				// add to posQ
				putPosMap(tokens, pos);
				
				prevRef = ref;
				prevPos = pos;
				endPos = pos + tokens[Sam.SEQ].length();
			}
		}
		
		removePosQ(fm, prevPos, endPos);
		fm.closeMaker();
		
	}

	abstract protected void removePosQ(FileMaker fm, int prevPos, int endPos);

	abstract protected void putPosMap(String[] tokens, int pos);
	
	abstract protected String getFileName(String ref);
	
	abstract public void printHelp();

}
