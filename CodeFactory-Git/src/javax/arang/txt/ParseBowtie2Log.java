/**
 * 
 */
package javax.arang.txt;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class ParseBowtie2Log extends IOwrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#hooker(javax.arang.IO.FileReader, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		StringBuffer outline = new StringBuffer();
		String sampleName = null;
		fm.writeLine("SampleID\tReads\tPaired (%)\tAligned Concordantly 1 time\tConcordant (%)\tOverall alignment rate");
		
		while (fr.hasMoreLines()) {
			// skip 5 lines
			fr.readLine();	// Time loading reference: 00:00:31
			fr.readLine();	// Time loading forward index: 00:00:47
			fr.readLine();	// Time loading mirror index: 00:00:22
			fr.readLine();	// Multiseed full-index search: 04:42:02
			fr.readLine();	// 33344242 reads; of these:
			line = fr.readLine();	//   33344242 (100.00%) were paired; of these:
			if (line.startsWith("Warning") || line.startsWith("Error")) {
				for (int i = 0; i < 18; i++ ) {
					fr.readLine();	// skip Warnings
				}
				continue;
			}
			tokens = line.split(" ");
			outline.append(tokens[2] + "\t" + tokens[3].replace("(", "").replace(")","") + "\t");
			fr.readLine();	    // 1686201 (5.06%) aligned concordantly 0 times
			line = fr.readLine();	//     27603276 (82.78%) aligned concordantly exactly 1 time
			tokens = line.split(" ");
//			System.out.println(line);
//			System.out.println(tokens[4] + "\t" + tokens[5]);
			outline.append(tokens[4] + "\t" + tokens[5].replace("(", "").replace(")","") + "\t");
			fr.readLine();	//     4054765 (12.16%) aligned concordantly >1 times
			fr.readLine();	//     ----
			fr.readLine();	//    1686201 pairs aligned concordantly 0 times; of these:
			fr.readLine();	//      126593 (7.51%) aligned discordantly 1 time
			fr.readLine();	//    ----
			fr.readLine();	//    1559608 pairs aligned 0 times concordantly or discordantly; of these:
			fr.readLine();	//      3119216 mates make up the pairs; of these:
			fr.readLine();	//        2114101 (67.78%) aligned 0 times
			fr.readLine();	//        442400 (14.18%) aligned exactly 1 time
			fr.readLine();	//        562715 (18.04%) aligned >1 times
			line = fr.readLine();	//96.83% overall alignment rate
			tokens = line.split(" ");	
			outline.append(tokens[0] + "\t");
			fr.readLine();	//Time searching: 04:43:43
			fr.readLine();	//Overall time: 04:43:44
			line = fr.readLine();	//[Fri Jul 26 21:50:06 KST 2013] net.sf.picard.sam.AddOrReplaceReadGroups INPUT=AlignedReads/14-0274.sam OUTPUT=AlignedReads/14-0274.sorted.bam SORT_ORDER=coordinate RGID=14-0274 RGLB=14-0274 RGPL=illumina RGPU=SureSelectAllExonG3362 RGSM=14-0274    VERBOSITY=INFO QUIET=false VALIDATION_STRINGENCY=STRICT COMPRESSION_LEVEL=5 MAX_RECORDS_IN_RAM=500000 CREATE_INDEX=false CREATE_MD5_FILE=false
			tokens = line.split(" ");
			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i].startsWith("INPUT")) {
					String[] inputSam = tokens[i].split("/");
					sampleName = inputSam[1].replace(".sam", "");
					break;
				}
			}
			fr.readLine();	//[Fri Jul 26 21:50:06 KST 2013] Executing as serena@gmi-com04 on Linux 2.6.18-194.el5 amd64; Java HotSpot(TM) 64-Bit Server VM 1.6.0_43-b01; Picard version: 1.74(1243)
			fr.readLine();	//--
			fm.writeLine(sampleName + "\t" + outline.toString());
			//System.out.println(sampleName);
			outline = new StringBuffer();
		}
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtParseBowtie2Log.jar <bowtie2.log> <out.tsv>");
		System.out.println("\t<bowtie2.log> Looks as follows:");
		System.out.println("grep -A22 \"Time loading reference:\" 01_run_MGL_wgs_low_re.log > bowtie2_MGL_low_re.log\n" +
				"Time loading reference: 00:00:31\n" +
				"Time loading forward index: 00:00:47\n" +
				"Time loading mirror index: 00:00:22\n" +
				"Multiseed full-index search: 04:42:02\n" +
				"  33344242 reads; of these:\n" +
				"    33344242 (100.00%) were paired; of these:\n" +
				"    1686201 (5.06%) aligned concordantly 0 times\n" +
				"    27603276 (82.78%) aligned concordantly exactly 1 time\n" +
				"    4054765 (12.16%) aligned concordantly >1 times\n" +
				"    ----\n" +
				"    1686201 pairs aligned concordantly 0 times; of these:\n" +
				"      126593 (7.51%) aligned discordantly 1 time\n" +
				"    ----\n" +
				"    1559608 pairs aligned 0 times concordantly or discordantly; of these:\n" +
				"      3119216 mates make up the pairs; of these:\n" +
				"        2114101 (67.78%) aligned 0 times\n" +
				"        442400 (14.18%) aligned exactly 1 time\n" +
				"        562715 (18.04%) aligned >1 times\n" +
				"96.83% overall alignment rate\n" +
				"Time searching: 04:43:43\n" +
				"Overall time: 04:43:44\n" +
				"[Fri Jul 26 21:50:06 KST 2013] net.sf.picard.sam.AddOrReplaceReadGroups INPUT=AlignedReads/14-0274.sam OUTPUT=AlignedReads/14-0274.sorted.bam SORT_ORDER=coordinate RGID=14-0274 RGLB=14-0274 RGPL=illumina RGPU=SureSelectAllExonG3362 RGSM=14-0274    VERBOSITY=INFO QUIET=false VALIDATION_STRINGENCY=STRICT COMPRESSION_LEVEL=5 MAX_RECORDS_IN_RAM=500000 CREATE_INDEX=false CREATE_MD5_FILE=false\n" +
				"[Fri Jul 26 21:50:06 KST 2013] Executing as serena@gmi-com04 on Linux 2.6.18-194.el5 amd64; Java HotSpot(TM) 64-Bit Server VM 1.6.0_43-b01; Picard version: 1.74(1243)\n" +
				"--\n");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			new ParseBowtie2Log().go(args[0], args[1]);
		} else {
			new ParseBowtie2Log().printHelp();
		}
	}

}
