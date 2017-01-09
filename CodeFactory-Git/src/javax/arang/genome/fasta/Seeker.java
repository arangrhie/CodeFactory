package javax.arang.genome.fasta;

import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class Seeker {

	private FileReader fr;
	private int posIdx = 0;	// currently buffered posIdx
	private int seeker = 0;
	private String basesBuffer = ""; 
	private String faName = "";
	
	
	/***
	 * This class can open only 1 fasta file containing 1 contig (or chromosome)
	 * @param frFa
	 */
	public Seeker(FileReader frFa) {
		this.fr = frFa;
		String line = fr.readLine();
		if (line.startsWith(">")) {
			this.faName = line.replace(">", "").split(RegExp.WHITESPACE)[0];
		}
		basesBuffer = fr.readLine();
		posIdx = basesBuffer.trim().length();
		seeker = 0;
	}
	
	public boolean goToContig(String contig) {
		if (this.faName.equals(contig)) {
			return true;
		}
		while (!this.faName.equals(contig)) {
			String line = fr.readLine();
			if (line.startsWith(">")) {
				this.faName = line.replace(">", "").split(RegExp.WHITESPACE)[0];
				if (this.faName.equals(contig)) {
					basesBuffer = fr.readLine();
					posIdx = basesBuffer.trim().length();
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
		if (seeker < pos && pos <= posIdx) {
			// already buffered
			base = basesBuffer.charAt(basesBuffer.length() - (posIdx - pos) - 1);
		} else {
			READ_LOOP : while (fr.hasMoreLines()) {
				readNextLine();
				if (posIdx >= pos) {
					base = basesBuffer.charAt(basesBuffer.length() - (posIdx - pos) - 1);
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
	 * @param from
	 * @param len
	 * @return sequence[from,from+len-1]
	 */
	public String getBases(int from, int len) {
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
}
