package javax.arang.genome.fasta;

public class FASTA {
	
	public static StringBuffer getReverseComplement(String inSeq, StringBuffer seq) {
		char base;
		for (int i = 0; i < inSeq.length(); i++) {
			base = inSeq.charAt(i);
			seq.insert(0, FASTA.toComplement(base));
		}
		return seq;
	}
	
	public static StringBuffer getReverseComplement(String inSeq) {
		return getReverseComplement(inSeq, new StringBuffer());
	}
	
	public static char toComplement(char base) {
		switch(base) {
		case 'a': return 't';
		case 'A': return 'T';
		case 'c': return 'g';
		case 'C': return 'G';
		case 'g': return 'c';
		case 'G': return 'C';
		case 't': return 'a';
		case 'T': return 'A';
		}
		return 'n';
	}
}
