package javax.arang.genome.snp;

import java.util.StringTokenizer;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/***
 * <snpTable>
 * chr     pos     pos     ref     snp     ss1.snp ss2.snp ss3.snp ss4.snp ss5.snp ss6.snp ss7.snp ss8.snp ss9.snp ak45.snp
 * M       73      73      A       G       2       2       2       2       2       2       2       2       2       2
 * M       146     146     T       C       0       0       0       0       0       0       0       2       2       0
 * 
 * <somaticMutations.mitomap>
 * Locus	Nucleotide Position	Nucleotide Change	Amino Acid Change	Homo-plasmy	Hetero-Plasmy	Cell or Tissue type	Note	References
 * MT-DLOOP	1	G-C	noncod	+	-	prostate tumor	.	references
 * MT-DLOOP	32	A-G	noncod	-	+	POLG/PEO patient	.	references
 * 
 * <out>
 * chr     pos     pos     ref     snp     ss1.snp ss2.snp ss3.snp ss4.snp ss5.snp ss6.snp ss7.snp ss8.snp ss9.snp ak45.snp	Locus aaChange Hom Het Cell/TissueType Note
 * M       73      73      A       G       2       2       2       2       2       2       2       2       2       2	MT-..	noncod	+	+	...
 * 
 * @author 아랑
 *
 */
public class MtDnaAnno extends I2Owrapper {

	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		
		// skip header lines
		fm.writeLine(fr1.readLine().toString() + "\tLocus\taaChange\tHom\tHet\tCell/TissueType\tNote");
		fr2.readLine();
		
		String line1;
		String line2;
		StringTokenizer st1;
		StringTokenizer st2;
		boolean hasWritten = false;
		
		LOOP1: while (fr1.hasMoreLines()) {
			line1 = fr1.readLine().toString();
			st1 = new StringTokenizer(line1);
			st1.nextToken();	// chr
			int pos1 = Integer.parseInt(st1.nextToken());	// posStart
			st1.nextToken();
			st1.nextToken();	// ref
			String snp = st1.nextToken();
			while (fr2.hasMoreLines()) {
				line2 = fr2.readLine().toString();
				line2 = line2.replace("references", "");
				st2 = new StringTokenizer(line2, "\t");
				String locus = st2.nextToken();
				String pos2Str = st2.nextToken();
				if (pos2Str.contains(":")) {
					continue;
				}
				int pos2 = Integer.parseInt(pos2Str);
//				System.out.println(pos1 + " : " + pos2);
				while (pos1 != pos2) {
//					System.out.println(pos1 + " : " + pos2);
					if (pos1 > pos2) {
						if (!fr2.hasMoreLines()) {
							break LOOP1;
						}
						line2 = fr2.readLine().toString();
						line2 = line2.replace("references", "");
						st2 = new StringTokenizer(line2, "\t");
						locus = st2.nextToken();
						pos2Str = st2.nextToken();
						if (pos2Str.contains(":")) {
							continue;
						}
						pos2 = Integer.parseInt(pos2Str);
					}
					if (pos1 < pos2) {
						if (!hasWritten)	{
							fm.writeLine(line1);
						}
						hasWritten = false;
						if (!fr1.hasMoreLines()) {
							break LOOP1;
						}
						line1 = fr1.readLine().toString();
						st1 = new StringTokenizer(line1);
						st1.nextToken();	// chr
						pos1 = Integer.parseInt(st1.nextToken());	// posStart
						st1.nextToken();
						st1.nextToken();	// ref
						snp = st1.nextToken();
					}
				}
				if (pos1 == pos2) {
					String ncChange = st2.nextToken();
//					System.out.println(ncChange + " : " + snp);
					if (ncChange.length() == 3 && snp.equals(Character.toString(ncChange.charAt(2)))) {
						fm.write(line1 + "\t" + locus);
						while (st2.hasMoreTokens()) {
							fm.write("\t" + st2.nextToken());
						}
						fm.writeLine("");
						hasWritten = true;
					}
				}
			}
		}
		
		while (fr1.hasMoreLines()) {
			fm.writeLine(fr1.readLine().toString());
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar mtDnaAnno.jar <snpTable> <somaticMutations.mtmap> <outFile>");
		System.out.println("\t<snpTable>: chr\tpos\trefAllele\tsnpAllele\t(0/1/2)");
		System.out.println("\t<somaticMutations.mtmap>: locus\tposition\tntChange\taaChange\t+/-\t... from MITOMAP");
		System.out.println("Add somatic mutation annotation at the end of the snp table column");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 3) {
			new MtDnaAnno().go(args[0], args[1], args[2]);
		} else {
			new MtDnaAnno().printHelp();
		}
			

	}

}
