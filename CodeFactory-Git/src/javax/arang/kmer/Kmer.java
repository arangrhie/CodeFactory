package javax.arang.kmer;

public class Kmer {

	private static StringBuffer bases = null;
	
	public static short toKmerBits(char base) {
		switch(base) {
			case 'A': return 0;
			case 'a': return 0;
			case 'T': return 1;
			case 't': return 1;
			case 'G': return 2;
			case 'g': return 2;
			case 'C': return 3;
			case 'c': return 3;
		}
		return -1;
	}
	
	public static char toBase(char base) {
		switch(base) {
			case 0: return 'A';
			case 1: return 'T';
			case 2: return 'G';
			case 3: return 'C';
		}
		return 'N';
	}
	
	public static char toBase(int base) {
		switch(base) {
			case 0: return 'A';
			case 1: return 'T';
			case 2: return 'G';
			case 3: return 'C';
		}
		return 'N';
	}
	
	/***
	 * 
	 * @param bases of size k
	 * @return null if bases contains n or N
	 */
	public static String toKmer(String bases) {
		if (bases.contains("n") || bases.contains("N")) {
			return null;
		}
		
		int kSize = bases.length();
		
		char[] kmer = new char[(int)((kSize + 1) / 2)];
		int j = 0;
		int i = 0;
		
		for (; i < bases.length() - 2; i += 2) {
			kmer[j] = (char) (toKmerBits(bases.charAt(i)) + (toKmerBits(bases.charAt(i + 1)) << 2));
			j++;
		}
		if (bases.length() % 2 == 0) {
			kmer[j] = (char) (toKmerBits(bases.charAt(i)) + (toKmerBits(bases.charAt(i + 1)) << 2));
		} else {
			kmer[j] = (char) toKmerBits(bases.charAt(i));
		}
		return String.valueOf(kmer);
	}
	
	/***
	 * Converts a kmer byte array back to the original sequence
	 * @param kmer char[]
	 * @return kmer String
	 */
	public static String toBases(String kmer, int kSize) {
		if (kmer == null) {
			return "";
		}
		int i = 0;
		bases = new StringBuffer();
		char base = kmer.charAt(kmer.length() - 1);
		for (i = 0; i < kmer.length() - 1; i++) {
			base = kmer.charAt(i);
			bases.append(toBase(base & 0x3));
			bases.append(toBase((base >> 2)));
		}
		base = kmer.charAt(i);
		bases.append(toBase(base & 0x3));
		if (kSize %2 == 0) {
			bases.append(toBase((base >> 2)));
		}
		return bases.toString();
	}
	
	public static void main(String[] args) {
		String word="ACGCTTGGTGCAAATATATTT";
		System.out.println("Original: " + word);
		String kmer = toKmer(word);
		System.out.println("Kmer: " + Kmer.toBases(kmer, word.length()));
		
		KmerCountQryTable kmerTable = new KmerCountQryTable(word.length());
		kmerTable.addTable(word);
		System.out.println("Count of " + word + " : " + kmerTable.queryTableCount(word));
		
		
	}
}
