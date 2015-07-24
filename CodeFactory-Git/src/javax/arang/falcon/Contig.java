package javax.arang.falcon;

public class Contig {
	
	private String contigName;
	private StringBuffer sequence;
	
	public Contig(String name, StringBuffer seq) {
		this.contigName = name;
		this.sequence = seq;
	}
	
	public String getName() {
		return this.contigName;
	}
	
	public StringBuffer getSeq() {
		return this.sequence;
	}
	
	public int getSeqLen() {
		return this.sequence.length();
	}
	
	public String getSeq(int start, int end) {
		return this.sequence.substring(start, end);
	}
	
	public String getReversedSeq(int start, int end) {
		char[] seq = this.sequence.substring(start, end).toCharArray();
		StringBuffer reversed = new StringBuffer();
		for (int i = seq.length - 1; i >= 0; i--) {
			reversed.append(complement(seq[i]));
		}
		return reversed.toString();
	}

	private char complement(char c) {
		switch(c) {
		case 'A': return 'T';
		case 'T': return 'A';
		case 'G': return 'C';
		case 'C': return 'G';
		case 'a': return 't';
		case 't': return 'a';
		case 'g': return 'c';
		case 'c': return 'g';
		}
		return 'N';
	}
}
