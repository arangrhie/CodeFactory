package javax.arang.mapreduce.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.gmi.fx.util.Exome;
import org.gmi.fx.util.ExomeTable;


public class IndelCallMapper extends Map{
	
	private Vector<String> prevLine = new Vector<String>();
	private Vector<String> nextLine = new Vector<String>();

	private boolean isPair1 = false;
	private String readSequence;
	private String readID;
	private String read1Quality;
	private String read2Quality;
	private boolean isConcordant;
	private boolean hasMatches;
	private boolean isFirstLine = false;
	private boolean isSecondLine = false;
	private boolean isMultiple = false;
	private boolean isIndel = false;
	private boolean isFirstSeg = false;
	private int firstSegGenoStartPos;

	private int prevGenoStartPos;
	private int prevGenoEndPos;
	private int readLength = 0;

	
	protected IndelCallMapper(String path) {
		super(path);
	}

	@Override
	public void map(String line) {
		StringTokenizer st = new StringTokenizer(line.toString());
		if (st.hasMoreTokens()) {
			String read = st.nextToken();
			if( read.startsWith(">") || read.startsWith("<")){
				/***
				 *	read is a new starting match (aligned read) starting with > or <
				 *	>CCCACTTCTTCCTTCACCAACATGCAAGTTCTTTCCTTCCCTGCCAGCCAGATAGATAGACAGATGGGAAAGGCAGGC	7 concordant	GA04_0001:1:1:1049:17516
				 *		bbbbbbbbbbbbbcbbbbbbbbbbbbbbbbbbbb_bb_b_b^b`bbbbbbb__	bbbb\bbabbbb^ba]abb``bbbbcbab^b]bbab_b_aa\[bababc_bca
				 *	>[READ]	\t	[num_matches] [isConcordant]	\t	[read_anno_FC::::coordinates]	\t	[QUAL_1]	\t	[QUAL_2] 
				 */
				
				if (read.startsWith(">")) {
					sendIndelToCombiner();
					clearCandidates();
				}
				
				isIndel = false;
				isMultiple = false;
				isFirstSeg = true;
				hasMatches = Integer.parseInt(st.nextToken()) > 0;		// num_matches
				isConcordant = st.nextToken().equals("concordant");		// isConcordant	- uncomment for concordant read use
//				st.nextToken();											// isConcordant - uncomment for concordant read use
				//	if (isConcordant) {
				if (hasMatches) {
					// READ info parsing, see if it is concordant
					isPair1 = (read.startsWith(">")) ? true : false;
					readSequence = (String) read.subSequence(1, read.length());
					readLength = readSequence.length();
					readID = st.nextToken(); // read_id
					read1Quality = st.nextToken();
					read2Quality = st.nextToken();
					isFirstLine = true;
				}
			} else if (hasMatches) {
				/***
				 * read is an aligned info
				 * AAGCAGTGCCGTGAACGCTGGCACAACCACCTCAACCCTGAGGTGAAGAAGTCTTGCTGGACCGAGGAGGAGGACCGC	1..78	+ENST00000217026:514..591	start:0..end:0,sub:0	segs:1,align_score:0,mapq:1	pair_score:3,insert_length:190
				 * [referenceSequence]	[alignRange]	[genomicSeg]	[subSeg]	[alignSeg]	|	if (concordant) [pairSeg]
				 * BUT IN INDEL MAPPER, DISMISS THESE READS!!
				 * 
				 * So we take only
				 *  (isFirstSeg = true)
				 *  TGGGACTACAGTGCGTGCCACCATGCCTTTTTTTTTTTTTT-------------------------------------	1..41	+uc001cci.1:507..547	start:0..ins:3,sub:0	segs:2,align_score:2,mapq:2	pair_score:2,insert_length:287
				 *  [referenceSequence]	[alignRange]	[genomicSeg]	[subSeg]	[alignSeg]	|	if (concordant) [pairSeg]
				 *  
				 *  (isFirstSeg = false)
				 *  ,--------------------------------------------AATGAGAcAGGGTCTCACTATGTTACCTAGGCTA	45..78	+uc001cci.1:548..581	ins:3..end:0,sub:1
				 *  [referenceSequence]	[alignRange]	[genomicSeg]	[subSeg]
				 *  
				 *  and extract referenceRead, alignRange, geneAlignPos, ins/del and seqRead, qual from the >, < lines.
				 *   
				 */
				
				if (isMultiple) {
//					System.out.println("DEBUG :: is multiple");
					return;
				}
				
				// parse.. if refRead starts with ",", remove it.
				String referenceSequence = "";
				if (read.startsWith(",")) {
					referenceSequence = read.substring(1);
					isFirstSeg = false;
				} else {
					referenceSequence = read;
				}

				//Range of query sequence aligned in the segment.
				//	st.nextToken();	// alignRange
				String alignRange = st.nextToken();
				String alignRangeStart = alignRange.substring(0, alignRange.indexOf(".."));
				String alignRangeEnd = alignRange.substring(alignRange.indexOf("..") + 2);

				try {
					
				// Range of genomic segment aligned, again with inclusive coordinates.
				String genomicSeg = st.nextToken();
				String geneID = genomicSeg.substring(1, genomicSeg.indexOf(":"));
				Exome exome = ExomeTable.getInstance().get(geneID);
				alignRange = genomicSeg.substring(genomicSeg.indexOf(":") + 1);
				int geneStartPos = Integer.parseInt(alignRange.substring(0, alignRange.indexOf("..")));
				int geneEndPos = Integer.parseInt(alignRange.substring(alignRange.indexOf("..") + 2));

				//	String subSeg = st.nextToken();
				String subSeg = st.nextToken();
				
				String type = "";
				
				String alignSeg = "";
				String[] alignSegs = null;
				int length = 0;
				if (isFirstSeg) {
					firstSegGenoStartPos = geneStartPos;
					alignSeg = st.nextToken();
					alignSegs = alignSeg.split(",");
					String segs = alignSegs[0];
					int seg = Integer.parseInt(segs.substring(segs.indexOf(":")+1));
					if (seg > 1) {
						// if indel variants are ovserved
						isIndel = true;
						isFirstLine = true;
						type = subSeg.substring(subSeg.indexOf("..") + 2, subSeg.indexOf("..") + 5);
						length = Integer.parseInt(subSeg.substring(subSeg.indexOf(type)+4, subSeg.indexOf(",")));
					}
					//	String mapq = alignSegs[2];
				} else {
					isFirstSeg = true;
				}
				
				if (!isIndel) {
					// Dismiss if read is not an indel
//					System.out.println("Not an indel");
					return;
				}
				
				if (isConcordant && isFirstLine) {
					String pairSeg = st.nextToken();
					String[] pairSegs = pairSeg.split(",");
					int pairScore = Integer.parseInt(pairSegs[0].substring(pairSegs[0].indexOf(":") + 1));
					if (pairScore > Math.round(readLength*2*0.05)) {
						// skip if mismatch score is higher than (read length x 2pairs x 5%)
						return;
					}
				} else if (isFirstLine){	// is unpaired, aligned line
					int align_score = Integer.parseInt(alignSegs[1].substring(alignSegs[1].indexOf(":") + 1));
					if (align_score > Math.round(readLength*0.05)) {
						return;
					}
				}
				
				/***
				 *  TGGGACTACAGTGCGTGCCACCATGCCTTTTTTTTTTTTTT-------------------------------------	1..41	+uc001cci.1:507..547	start:0..ins:3,sub:0	segs:2,align_score:2,mapq:2	pair_score:2,insert_length:287
				 *  [referenceSequence]	[alignRange]	[genomicSeg]	[subSeg]	[alignSeg]	|	if (concordant) [pairSeg]
				 *  
				 *  (isFirstSeg = false)
				 *  ,--------------------------------------------AATGAGAcAGGGTCTCACTATGTTACCTAGGCTA	45..78	+uc001cci.1:548..581	ins:3..end:0,sub:1
				 *  [referenceSequence]	[alignRange]	[genomicSeg]	[subSeg]-------------------------------------------AATGAGAcAGGGTCTCACTATGTTACCTAGGCTA	45..78	+ENST00000373019:546..579	ins:3..end:0,sub:1
				 */

				// match 찾음.
				// baseCntTable 에 추가
				if (isFirstLine) {
					// Plus and minus strands are marked with a "+" or "-" sign.
					String strand = String.valueOf(genomicSeg.charAt(0));
					prevGenoStartPos = findGenomePos(exome, geneStartPos);
					String bases = "";
					if (type.equals("del")) {
						bases = referenceSequence.substring(Integer.parseInt(alignRangeEnd), Integer.parseInt(alignRangeEnd)+length);
					} else {
						bases = readSequence.substring(Integer.parseInt(alignRangeEnd), Integer.parseInt(alignRangeEnd) + length);
					}
					addToPrevLine(alignRangeEnd, geneID, geneEndPos, strand, readSequence, isPair1 ? read1Quality : read2Quality, type, bases);
//					System.out.println("Add to prevLine: " + prevLine.get(INNER_SEG_POS) + " " + prevLine.get(GENEID) + " " + prevLine.get(GENE_ALIGN_POS) + " " + readSequence + " " +  read1Quality + " " + type);
					isFirstLine = false;
					isSecondLine = true;
					isMultiple = false;
				}
				
				// indel 일 경우, ,로 시작하는 줄에 대하여
				else if (isSecondLine) {
					// Plus and minus strands are marked with a "+" or "-" sign.
					String strand = String.valueOf(genomicSeg.charAt(0));
					prevGenoEndPos = findGenomePos(exome, geneEndPos);
					//	System.out.println("DEBUG :: prevGenoStart = " + prevGenoStartPos + ", prevGenoEnd = " + prevGenoEndPos);
					addToNextLine(alignRangeStart, geneID, geneStartPos, strand);
//					System.out.println("add to nextLine: " + nextLine.get(INNER_SEG_POS) + " " + nextLine.get(GENEID) + " " + geneStartPos + " " + strand);
					isSecondLine = false;
				}

				// ref 상의 genomePos start, stop 위치가 같지 않으면
				// Multiple Match: 버릴것!
				else if (!hasSameGenomePos(firstSegGenoStartPos, geneEndPos, exome)) {
					isMultiple = true;
					clearCandidates();
					return;
				}
				
				} catch (Exception e) {
					System.out.println(line.toString() + " in read " + readID);
					e.printStackTrace();
					clearCandidates();
					return;
				}
			}
		}
	}

	/***
	 * addToPrevLine(readSequence, read1Quality, alignRangeEnd, geneID, geneEndPos, type, strand);
	 * @param seqRead
	 * @param qual
	 * @param from
	 * @param gene
	 * @param geneAlignEnd
	 * @param type
	 * @param strand
	 */
	private void addToPrevLine(String from, String gene, int geneAlignEnd, String strand, String seqRead, String qual, String type, String bases) {
		prevLine.add(from);
		prevLine.add(gene);
		prevLine.add(String.valueOf(geneAlignEnd));
		prevLine.add(strand);
		prevLine.add(seqRead);
		prevLine.add(qual);
		prevLine.add(type);
		prevLine.add(bases);
	}

	/***
	 * 
	 * @param seqRead	empty string. 자리맞춰주기용.
	 * @param qual
	 * @param to
	 * @param gene
	 * @param geneAlignStart
	 * @param type
	 * @param strand
	 */
	private void addToNextLine(String to, String gene, int geneAlignStart, String strand) {
		nextLine.add(to);
		nextLine.add(gene);
		nextLine.add(String.valueOf(geneAlignStart));
		nextLine.add(strand);
	}
	
	private void clearCandidates() {
		prevLine.clear();
		nextLine.clear();
	}
	
	private static final short INNER_SEG_POS = 0;
	private static final short GENEID = 1;
	private static final short GENE_ALIGN_POS = 2;
	private static final short STRAND = 3;
	private static final short SEQ_READ = 4;
	private static final short QUAL = 5;
	private static final short TYPE = 6;
	private static final short BASES = 7;
	
	/***
	 * generates the key and values that are sent to combiner (or reducer)
	 * @param context
	 */
	private void sendIndelToCombiner() {
		if (hasNoIndelLines()) {
//			System.out.println("DEBUG :: NO INDEL LINES FOUND");
			return;
		}
		
		String geneID_1 = prevLine.get(GENEID);
		String geneID_2 = nextLine.get(GENEID);
		if (!geneID_1.equals(geneID_2)) return;

		String strand_1 = prevLine.get(STRAND);
		String strand_2 = nextLine.get(STRAND);
		if (!strand_1.equals(strand_2))	return;
		
		String read = prevLine.get(SEQ_READ);
		String qual = prevLine.get(QUAL);
		int from = Integer.parseInt(prevLine.get(INNER_SEG_POS));
		int to = Integer.parseInt(nextLine.get(INNER_SEG_POS));
		int geneAlignPosFrom = Integer.parseInt(prevLine.get(GENE_ALIGN_POS));
		int geneAlignPosTo = Integer.parseInt(nextLine.get(GENE_ALIGN_POS));
		String type = prevLine.get(TYPE);
		String bases = prevLine.get(BASES);
		
		/***
		 *  if (isPolySeq(bases)) such as  TTTT, AAA, etc.
		 *  search for the leftmost / rightmost bound
		 *  
		 *  else such as ATG, GGT, CAGGGGAG, etc.
		 *  search for exact dup leftmost / rightmost bound
		 */
		
		int leftMost = from;
		int rightMost = to;
		int leftMostGene = geneAlignPosFrom;
		int rightMostGene = geneAlignPosTo;
		bases = bases.toUpperCase();
		
		// frame shift & screen out for INSERTION
		if (type.equals("ins")) {
			int i = 0;
			while (i < from) {
				if (read.charAt(from-1 - i) != read.charAt(from-1 + bases.length() - i)) {
					leftMost = from - i + 1;
					leftMostGene = strand_1.equals("+") ? geneAlignPosFrom - i + 1: geneAlignPosFrom + i - 1;
					break;
				}
				i++;
			}

			i = 0;
			System.out.println("FrameRead: " + read);
			while (i < (read.length() - to)) {
				if (read.charAt(to-1 - bases.length() + i) != read.charAt(to-1 + i)) {
					rightMost = to + i - 1;
					rightMostGene = strand_1.equals("+") ? geneAlignPosTo + i - 1: geneAlignPosTo - i + 1;
					break;
				}
				i++;
			}
			
		}
		
		// frame shift & screen out for DELETION
		else {
			String frameRead = read.substring(0, from) + bases;
			int i = 0;
			while (i < from) {
				if (frameRead.charAt(from-1 - i) != frameRead.charAt(from-1 + bases.length() - i)) {
					leftMost = from - i + 1;
					leftMostGene = strand_1.equals("+") ? geneAlignPosFrom - i + 1: geneAlignPosFrom + i - 1;
					break;
				}
				i++;
			}
			
			i = 0;
			frameRead = read.substring(0, from) + bases + read.substring(to - 1, read.length());
			System.out.println("FrameRead: " + frameRead);
			while (i < (frameRead.length() - to)) {
				if (frameRead.charAt(to-1 + i) != frameRead.charAt(to-1 + bases.length() + i)) {
					rightMost = to + i - 1;
					rightMostGene = strand_1.equals("+") ? geneAlignPosTo + i - 1: geneAlignPosTo - i + 1;
					break;
				}
				i++;
			}
			
			frameRead = read.substring(0, from) + bases + read.substring(to - 1, read.length());
		}
		System.out.println("DEBUG :: " + type + " is : " + bases + " in " + leftMost + " to " + rightMost + ", "
				+ leftMostGene + " to " +rightMostGene + ", qual is " + (int)qual.charAt(from - 1));

//		Exome exome = ExomeTable.getInstance().get(geneID_1);
//		int leftMostGenoPos = findGenomePos(exome, leftMostGene);
//		int rightMostGenoPos = findGenomePos(exome, rightMostGene);
//		
//		System.out.print("KEY: " + type + "\t" + exome.getChrom() + "\t" + leftMostGenoPos + "\t" + rightMostGenoPos + "\t");
//		System.out.println("VALUE: " + 1 + "\t" + (int)qual.charAt(from - 1));
		
//		try {
//			context.write(new Text(type + "\t" + exome.getChrom() + "\t" + leftMostGenoPos + "\t" + rightMostGenoPos),
//					new Text(1 + "\t" + (int)qual.charAt(from - 1)));
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}
	
	@Override
	public void cleanup() {
		sendIndelToCombiner();
	};

	private boolean hasNoIndelLines() {
		if (prevLine.isEmpty() || nextLine.isEmpty()) {
			clearCandidates();
			return true; 
		}
		return false;
	}

	/***
	 * Find the genome position of the given gene position
	 * align 된 gene position을  바탕으로 해당 gene의 exome들로부터 genome상의 pos을 추출
	 * notice that the starting boundary is 1 base less than the actual exon starting position!!
	 * exonStart[exonCnt] EXON{exonStart[exonCnt]+1 ... exonEnd[exonCnt]}EXON
	 * @param exome
	 * @param genePos
	 * @return
	 */
	private int findGenomePos(Exome exome, int genePos) {
		int exonCnt = 0;
		int exonLength = 0;
		int exonCount = exome.getExonCount();
		int[] exonStarts = exome.getExonStarts();
		int[] exonEnds = exome.getExonEnds();
		for( ; exonCnt < exonCount; exonCnt++ ){
			exonLength += exonEnds[exonCnt] - exonStarts[exonCnt];
			if(exonLength >= genePos) {
				return exonEnds[exonCnt] - (exonLength - genePos);
			}
		} return -1;
	}

	/***
	 * Start/Stop genome position of the reference are identical
	 * @param curGeneEndPos 
	 * @param curGeneStartPos 
	 * @param exome 
	 * @return
	 */
	private boolean hasSameGenomePos(int curGeneStartPos, int curGeneEndPos, Exome exome) {
		int genoStart = findGenomePos(exome, curGeneStartPos);
		int genoEnd = findGenomePos(exome, curGeneEndPos);
		//		System.out.println("DEBUG :: curGenoStart = " + genoStart + ", curGenoEnd = " + genoEnd);
		return (prevGenoStartPos == genoStart 
				&& prevGenoEndPos == genoEnd);
	}
	
	public static void main(String[] args) {
		new IndelCallMapper("C://Documents and Settings/아랑/바탕 화면/CloudSNP/sample/indel_sample.result").go();
	}

}
