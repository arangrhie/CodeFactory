package javax.arang.genome.fasta;

import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class Seeker {

	private FileReader fr;
	private int posIdx = 0;	// currently buffered posIdx, 1-based
	private int seeker = 0; // last time seeked position, 0-based
	private String basesBuffer = ""; 
	private String faName = "";
	
	
	/***
	 * This class can open only 1 fasta file containing 1 contig (or chromosome)
	 * @param frFa
	 */
	public Seeker(FileReader frFa) {
		this.fr = frFa;
		initSeeker();
	}
	
	public void initSeeker() {
		String line = fr.readLine();
		if (line.startsWith(">")) {
			this.faName = line.replace(">", "").split(RegExp.WHITESPACE)[0];
		}
		posIdx = 0;
		basesBuffer = "";
		readNextLine();
		seeker = 0;
	}
	
	public boolean goToContig(String contig) {
		if (this.faName.equals(contig)) {
			return true;
		}
		while (fr.hasMoreLines() && !this.faName.equals(contig)) {
			String line = fr.readLine();
			if (line.startsWith(">")) {
				this.faName = line.replace(">", "").split(RegExp.WHITESPACE)[0];
				if (this.faName.equals(contig)) {
					posIdx = 0;
					readNextLine();
					seeker = 0;
					return true;
				}
			}
		}
		if (!fr.hasMoreLines()) {
			System.err.println("[DEBUG] :: " + contig + " Not found. Searching again...");
			fr.reset();
			initSeeker();
		}
		while (fr.hasMoreLines() && !this.faName.equals(contig)) {
			String line = fr.readLine();
			if (line.startsWith(">")) {
				this.faName = line.replace(">", "").split(RegExp.WHITESPACE)[0];
				if (this.faName.equals(contig)) {
					posIdx = 0;
					readNextLine();
					seeker = 0;
					return true;
				}
			}
		}
		return false;
	}
	
	public String getFaName() {
		return this.faName;
	}
	
	/**
	 * Can only seek forward. Backward search is not guaranteed.
	 * pos is 1-based.
	 * @param pos
	 * @return
	 */
	public char baseAt(int pos) {
		char base = 'n';
		if (seeker > pos) {
			System.err.println("[DEBUG] :: Backward searching? " + pos);
			System.exit(-1);
		}
		if (seeker <= pos && pos <= posIdx - 1) {
			// already buffered
			base = basesBuffer.charAt(basesBuffer.length() - (posIdx - pos));
		} else {
			READ_LOOP : while (fr.hasMoreLines()) {
				readNextLine();
				if (posIdx > pos) {
					base = basesBuffer.charAt(basesBuffer.length() - (posIdx - pos));
					break READ_LOOP;
				}
			}
		}
		seeker = pos;
		return base;
	}
	
	private void readNextLine() {
		basesBuffer = fr.readLine().trim();
		posIdx += basesBuffer.length();
	}
	
	/***
	 * 
	 * @param from (0-based)
	 * @param len (1-based)
	 * @return sequence[from,from+len-1]
	 */
	public String getBases(int from, int len) {
		// TODO: Allow random access
		if (seeker > from) {
			this.fr.reset();
			this.goToContig(getFaName());
			initSeeker();
		}
		
		StringBuffer buffer = new StringBuffer();
		for (int pos = from; pos < (from + len); pos++) {
			buffer.append(baseAt(pos));
		}
		return buffer.toString();
	}
	
	/***
	 * Read to the end
	 * 1-based.
	 * @return ending index of the last base
	 */
	public int getEndPos() {
		while (fr.hasMoreLines()) {
			basesBuffer = fr.readLine().trim();
			posIdx += basesBuffer.length();
		}
		
		return posIdx;
	}

	public boolean isAt(String query) {
		if (faName.equals(query)) return true;
		return false;
	}
	
	public void close() {
		fr.closeReader();
	}
}
