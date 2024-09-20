package javax.arang.gff;

import java.util.HashMap;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class CompareRefseqLiftoff extends R2wrapper {
	
	// HashMap< id , found >. found: 1 = in fr1, 2 = in fr2, 3 = in both fr1 and fr2
	private HashMap<String, Short> geneIDs = null;
	private HashMap<String, Short> genePrtnIDs = null;
	private HashMap<String, Short> transIDs = null;
	private HashMap<String, Short> transPrtnIDs = null;
	private HashMap<String, Integer> geneIdCounts1 = null;
	private HashMap<String, Integer> genePrtnIdCounts1 = null;
	private HashMap<String, Integer> transIdCounts1 = null;
	private HashMap<String, Integer> transPrtnIdCounts1 = null;
	
	// Additional counts in Liftoff (fr2)
	private HashMap<String, Integer> geneIdCounts2 = null;
	private HashMap<String, Integer> genePrtnIdCounts2 = null;
	private HashMap<String, Integer> transIdCounts2 = null;
	private HashMap<String, Integer> transPrtnIdCounts2 = null;
	
	private static short FOUND_IN_FR1 = 1;
	private static short FOUND_IN_FR2 = 2;
	private static short FOUND_IN_BOTH = 3;
	
	// unique gene counts: distinct copies found only in f1
	// distinct + additional = total
	private int gene1 = 0;
	private int gene2 = 0;
	private int gene3 = 0;	// found in both f1 and f2
	
	private int trans1 = 0;
	private int trans2 = 0;
	private int trans3 = 0;
	
	private int genePrtn1 = 0;
	private int genePrtn2 = 0;
	private int genePrtn3 = 0;
	
	private int transPrtn1 = 0;
	private int transPrtn2 = 0;
	private int transPrtn3 = 0;
	
	// additional copies found only in f1
	private int geneMulti1 = 0;
	private int genePrtnMulti1 = 0;
	private int transMulti1 = 0;
	private int transPrtnMulti1 = 0;
	
	// additional copies found only in f2
	private int geneMulti2 = 0;
	private int genePrtnMulti2 = 0;
	private int transMulti2 = 0;
	private int transPrtnMulti2 = 0;
	
	// found in both, additional copies in f1
	private int geneMultiBoth1 = 0;
	private int genePrtnMultiBoth1 = 0;
	private int transMultiBoth1 = 0;
	private int transPrtnMultiBoth1 = 0;
	
	// found in both, additional copies in f2
	private int geneMultiBoth2 = 0;
	private int genePrtnMultiBoth2 = 0;
	private int transMultiBoth2 = 0;
	private int transPrtnMultiBoth2 = 0;

	// found in both, shared in multi copies
	private int geneMultiBoth3 = 0;
	private int genePrtnMultiBoth3 = 0;
	private int transMultiBoth3 = 0;
	private int transPrtnMultiBoth3 = 0;

	@Override
	public void hooker(FileReader fr1, FileReader fr2) {
		
		// Variables
		geneIDs = new HashMap<String, Short>();
		genePrtnIDs = new HashMap<String, Short>();
		transIDs = new HashMap<String, Short>();
		transPrtnIDs = new HashMap<String, Short>();
		
		geneIdCounts1 = new HashMap<String, Integer>();
		genePrtnIdCounts1 = new HashMap<String, Integer>();
		transIdCounts1 = new HashMap<String, Integer>();
		transPrtnIdCounts1 = new HashMap<String, Integer>();
		
		geneIdCounts2 = new HashMap<String, Integer>();
		genePrtnIdCounts2 = new HashMap<String, Integer>();
		transIdCounts2 = new HashMap<String, Integer>();
		transPrtnIdCounts2 = new HashMap<String, Integer>();
		
		// Output file for unique genes and transcripts
		FileMaker fr1Only = new FileMaker(fr1.getFileName() + "_only.txt");
		FileMaker fr2Only = new FileMaker(fr2.getFileName() + "_only.txt");
		
		fr1Only.writeLine("#TYPE\tID\tCOUNT");
		fr2Only.writeLine("#TYPE\tID\tCOUNT");
		
		// Collect information from RefSeq
		collectIDsRefSeq(fr1);
		
		// Print files
		System.out.println("RefSeq GFF : " + fr1.getFileName());
		System.out.println("Loftoff GFF: " + fr2.getFileName());
		System.out.println();

		
		// Collect annotation from Liftoff
		collectIDsLiftoff(fr2);
		
		collectAdditionalCounts(fr1Only, fr2Only);
		
		// Print distinct num. features
		System.out.println("Distinct\tRefSeq_Only\tLiftoff_Only\tBoth\tRefSeq_Total\tLiftoff_Total");
		System.out.println("all");
		System.out.println("  genes:\t"       + gene1  + "\t" + gene2  + "\t" + gene3 + "\t"  + (gene1  + gene3)  + "\t" + (gene2 + gene3));
		System.out.println("  transcripts:\t" + trans1 + "\t" + trans2 + "\t" + trans3 + "\t" + (trans1 + trans3) + "\t" + (trans2 + trans3));
		System.out.println("protein-coding");
		System.out.println("  genes:\t"       + genePrtn1  + "\t" + genePrtn2  + "\t" + genePrtn3  + "\t"  + (genePrtn1  + genePrtn3)  + "\t" + (genePrtn2 + genePrtn3));
		System.out.println("  transcripts:\t" + transPrtn1 + "\t" + transPrtn2 + "\t" + transPrtn3 + "\t"  + (transPrtn1 + transPrtn3) + "\t" + (transPrtn2 + transPrtn3));
		System.out.println();
		
		// Print total, including multi-copies
		int g1 = gene1 + geneMulti1 + geneMultiBoth1;
		int g2 = gene2 + geneMulti2 + geneMultiBoth2;
		int g3 = gene3 + geneMultiBoth3;
		
		int t1 = trans1 + transMulti1 + transMultiBoth1;
		int t2 = trans2 + transMulti2 + transMultiBoth2;
		int t3 = trans3 + transMultiBoth3;
		
		int gp1 = genePrtn1 + genePrtnMulti1 + genePrtnMultiBoth1;
		int gp2 = genePrtn2 + genePrtnMulti2 + genePrtnMultiBoth2;
		int gp3 = genePrtn3 + genePrtnMultiBoth3;
		
		int tp1 = transPrtn1 + transPrtnMulti1 + transPrtnMultiBoth1;
		int tp2 = transPrtn2 + transPrtnMulti2 + transPrtnMultiBoth2;
		int tp3 = transPrtn3 + transPrtnMultiBoth3;
		
		System.out.println("Total\tRefSeq_Only\tLiftoff_Only\tBoth\tRefSeq_Total\tLiftoff_Total");
		System.out.println("all");
		System.out.println("  genes:\t"       + g1 + "\t" + g2 + "\t" + g3 + "\t" + (g1 + g3) + "\t" + (g2 + g3));
		System.out.println("  transcripts:\t" + t1 + "\t" + t2 + "\t" + t3 + "\t" + (t1 + t3) + "\t" + (t2 + t3));
		System.out.println("protein-coding");
		System.out.println("  genes:\t"       + gp1 + "\t" + gp2 + "\t" + gp3 + "\t" + (gp1 + gp3) + "\t" + (gp2 + gp3));
		System.out.println("  transcripts:\t" + tp1 + "\t" + tp2 + "\t" + tp3 + "\t" + (tp1 + tp3) + "\t" + (tp2 + tp3));
		System.out.println();
		
	}
	
	private void collectIDsLiftoff(FileReader fr) {
		
		String   line;
		String[] tokens;
		
		String   attributes;
		String[] tags;
		
		String id = "";
		short cnt;
		int   cntMulti;
		boolean prtn = false;
		
		while (fr.hasMoreLines()) {
			
			line = fr.readLine();
			
			// Ignore comments
			if (line.startsWith("#")) continue;
			
			tokens = line.split(RegExp.TAB);
			prtn = false;
		
			if (tokens[GFF.TYPE].equalsIgnoreCase("gene")) {
				
				attributes = tokens[GFF.NOTE];
				tags = attributes.split(RegExp.SEMICOLON);
				
				// ID contains _1 ... , can't use
				// id = tags[0].replace("ID=", "");
				
				for (int i = 0; i < tags.length; i++) {
					if (tags[i].startsWith("gene=")) {
						id = tags[i].split("=")[1];
					}
					if (tags[i].startsWith("gene_biotype") && tags[i].split("=")[1].equalsIgnoreCase("protein_coding")) {
						prtn = true;
					}
				}
				
				// System.err.println("[[ DEBUG ]] :: gene " + id + " protein_coding " + prtn);
				
				// found in fr2
				cnt = FOUND_IN_FR2;

				// add the counts
				cntMulti = 1;
				
				if (geneIDs.containsKey(id)) {
					if (geneIDs.get(id) == FOUND_IN_FR1) {
						cnt = FOUND_IN_BOTH;
					} else {
						// keep FOUND_IN_FR2 or FOUND_IN_BOTH
						cnt = geneIDs.get(id);
					}
				}
				geneIDs.put(id, cnt);
				
				if (geneIdCounts2.containsKey(id)) {
					cntMulti += geneIdCounts2.get(id); // increase by 1
				}
				geneIdCounts2.put(id, cntMulti);
				
				// new gene is protein coding
				if (prtn) {
					cnt = FOUND_IN_FR2;
					
					// add the counts
					cntMulti = 1;
					
					if (genePrtnIDs.containsKey(id)) {
						if (genePrtnIDs.get(id) == FOUND_IN_FR1) {
							cnt = FOUND_IN_BOTH;
						} else {
							// keep FOUND_IN_FR2 or FOUND_IN_BOTH
							cnt = genePrtnIDs.get(id);
						}
					}
					genePrtnIDs.put(id, cnt);
					
					if (genePrtnIdCounts2.containsKey(id)) {
						cntMulti += genePrtnIdCounts2.get(id); // increase by 1
					}
					genePrtnIdCounts2.put(id, cntMulti);
				}
			}

			if (tokens[GFF.TYPE].equalsIgnoreCase("transcript")) {

				attributes = tokens[GFF.NOTE];
				tags = attributes.split(RegExp.SEMICOLON);
				String[] idFields = tags[0].split("=")[1].split("_");
				id = idFields[0];
				// append idField[1] if
				// there are more than 1 idFields
				// and idField[0] is 2 letters in length (for NM, XM, XR, ...)
				if (idFields.length > 1 && idFields[0].length() == 2) {
					id += "_" + idFields[1];
				}
				// Use "Parent=" for "ID=unassigned"
				if (id.startsWith("unassigned")) {
					for (int i = 1; i < tags.length; i++) {
						if (tags[i].startsWith("Parent=")) {
							idFields = tags[i].split("=")[1].split("_");
							id = idFields[0];
							if (idFields.length > 1 && idFields[0].length() == 2) {
								id += "_" + idFields[1];
							}
							// System.err.println("[[ DEBUG ]] :: Use id " + id + " for " + tags[0] + "\t" + line);
							break;
						}
					}
				}
				

				cnt = FOUND_IN_FR2;
				cntMulti = 1;

				if (transIDs.containsKey(id)) {
					if (transIDs.get(id) == FOUND_IN_FR1) {
						cnt += transIDs.get(id);
					} else {
						// keep FOUND_IN_FR2 or FOUND_IN_BOTH
						cnt  = transIDs.get(id);
					}
				}
				transIDs.put(id, cnt);
				
				if (transIdCounts2.containsKey(id)) {
					cntMulti += transIdCounts2.get(id); // increase by 1
				}
				transIdCounts2.put(id, cntMulti);
				
				// new transcript is protein coding
				for (int i = 0; i < tags.length; i++) {
					if (tags[i].startsWith("gbkey")) {
						if (tags[i].split("=")[1].equalsIgnoreCase("mRNA")) {
							cnt = FOUND_IN_FR2;
							cntMulti = 1;

							if (transPrtnIDs.containsKey(id)) {
								if (transPrtnIDs.get(id) == FOUND_IN_FR1) {
									cnt = FOUND_IN_BOTH;
								} else {
									// keep FOUND_IN_FR2 or FOUND_IN_BOTH
									cnt = transPrtnIDs.get(id);
								}
							}
							transPrtnIDs.put(id, cnt);
							
							if (transPrtnIdCounts2.containsKey(id)) {
								cntMulti += transPrtnIdCounts2.get(id); // increase by 1
							}
							transPrtnIdCounts2.put(id, cntMulti);
						}
						// Found gbkey - stop search
						break;
					}
				}
			}
		}
	}

	private void collectIDsRefSeq(FileReader fr) {
		
		String   line;
		String[] tokens;
		
		String   attributes;
		String[] tags;
		
		String   id = null;
		int      cnt = 0;
		boolean  prtn = false;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			
			// Ignore comments
			if (line.startsWith("#")) continue;
			if (!line.startsWith("chr") && !line.startsWith("NC"))	continue;  // Ignore non-primary chromosomes
			if (line.startsWith("NC_012920.1")) continue;	// Ignore MT
			
			tokens = line.split(RegExp.TAB);
			attributes = tokens[GFF.NOTE];
			id = "";
			prtn = false;
			
			if (attributes.startsWith("ID=gene-")) {
				tags = attributes.split(RegExp.SEMICOLON);
				
				// String id = tags[0].replace("ID=gene-", "");
				// Use "gene=" field
				for (int i = 0; i < tags.length; i++) {
					if (tags[i].startsWith("gene=")) {
						id = tags[i].split("=")[1];
					}
					if (tags[i].startsWith("gene_biotype") && tags[i].split("=")[1].equalsIgnoreCase("protein_coding")) {
						prtn = true;
					}
				}
				
				// assert id
				if (id.equals("")) {
					System.err.println("[[ WARNING ]] :: No gene field fount at " + line);
				}
				

				cnt = 1;
				if (geneIDs.containsKey(id)) {
					cnt += geneIdCounts1.get(id);
				}
				geneIDs.put(id, FOUND_IN_FR1);
				geneIdCounts1.put(id, cnt);
				
				// System.err.println("gene " + id);
				
				if (prtn) {
					cnt = 1;
					if (genePrtnIDs.containsKey(id)) {
						cnt += genePrtnIdCounts1.get(id);
					}
					genePrtnIDs.put(id, FOUND_IN_FR1);
					genePrtnIdCounts1.put(id, cnt);
				}
			}
			else if (attributes.startsWith("ID=rna-")) {
				tags = attributes.split(RegExp.SEMICOLON);
				
				/** 
				 * Annotations with no "Name" field, nor "transcript_id" field
				 [[ ERROR ]] :: No Name field fount at chrY	BestRefSeq	miRNA	1293927	1293949	.	+	.	ID=rna-MIR3690-2;Parent=rna-NR_037461.1-2;Dbxref=GeneID:100500894,miRBase:MIMAT0018119,HGNC:HGNC:38967,miRBase:MI0016091;gbkey=ncRNA;gene=MIR3690;product=hsa-miR-3690
				 [[ ERROR ]] :: No Name field fount at chrY	BestRefSeq	miRNA	2609229	2609252	.	+	.	ID=rna-MIR6089-2;Parent=rna-NR_106737.1-2;Dbxref=GeneID:102464837,miRBase:MIMAT0023714,HGNC:HGNC:50179,miRBase:MI0020366;gbkey=ncRNA;gene=MIR6089;product=hsa-miR-6089
				 [[ ERROR ]] :: No Name field fount at chrY	BestRefSeq	miRNA	4606140	4606158	.	+	.	ID=rna-MIR9985;Parent=rna-NR_162096.1;Dbxref=GeneID:113218498,miRBase:MIMAT0039763,HGNC:HGNC:53998,miRBase:MI0032313;gbkey=ncRNA;gene=MIR9985;product=hsa-miR-9985
				 [[ ERROR ]] :: No Name field fount at chrY	BestRefSeq	miRNA	13479189	13479214	.	+	.	ID=rna-MIR12120;Parent=rna-NR_162134.1;Dbxref=GeneID:113219468,miRBase:MIMAT0049014,HGNC:HGNC:54028,miRBase:MI0039722;gbkey=ncRNA;gene=MIR12120;product=hsa-miR-12120
				 -> These are all found as "unassigned_transcripts" on the Y
				 On the whole genome, miRNA, MT mRNA, rRNA, and tRNA don't have a tarnsript_id
					mRNA	13 (MT)
					miRNA	3214
					rRNA	2
					tRNA	691
					
				 Try assigning id by transcript_id first, if that fails, use Parent=rna- without the additional copy - part
				*/
				
				boolean hasTranscriptId = false;
				
				for (int i = 0; i < tags.length; i++) {
					// use transcript_id if exists (for most cases)
					if (tags[i].startsWith("transcript_id")) {
						hasTranscriptId = true;
						id = tags[i].split("=")[1];
					}
				}
				
				if (! hasTranscriptId) {
					id = tags[0].replace("ID=rna-", "");
					String[] idFields = id.split("-");
					id = idFields[0];
					if (idFields.length > 1 && idFields[0].length() == 2) {
						id += "_" + idFields[1];
					}
					// System.err.println("[[ WARNING ]] :: No transcript_id found. Using ID : " + id + "\t" + line);
				}
				
				cnt = 1;
				if (transIDs.containsKey(id)) {
					cnt += transIdCounts1.get(id);
				}
				transIDs.put(id, FOUND_IN_FR1);
				transIdCounts1.put(id, cnt);
				// System.err.println("transcript " + id);
				
				if (tokens[GFF.TYPE].equalsIgnoreCase("mRNA")) {
					cnt = 1;
					if (transPrtnIDs.containsKey(id)) {
						cnt += transPrtnIdCounts1.get(id);
					}
					transPrtnIDs.put(id, FOUND_IN_FR1);
					transPrtnIdCounts1.put(id, cnt);
				}
			}
		}
	}

	private void collectAdditionalCounts(FileMaker fr1Only, FileMaker fr2Only) {
		for (String id : geneIDs.keySet()) {
			if (geneIDs.get(id) == FOUND_IN_FR1) {
				gene1++;
				if (geneIdCounts1.get(id) > 1) {
					geneMulti1 += geneIdCounts1.get(id) - 1;
				}
				fr1Only.writeLine("gene\t" + id + "\t" + geneIdCounts1.get(id));
			}
			else if (geneIDs.get(id) == FOUND_IN_FR2) {
				gene2++;
				if (geneIdCounts2.get(id) > 1) {
					geneMulti2 += geneIdCounts2.get(id) - 1;
				}
				fr2Only.writeLine("gene\t" + id + "\t" + geneIdCounts2.get(id));
			}
			else if (geneIDs.get(id) == FOUND_IN_BOTH) {
				gene3++;
				if (geneIdCounts1.get(id) >= geneIdCounts2.get(id)) {
					geneMultiBoth3 += geneIdCounts2.get(id) - 1;
					if (geneIdCounts1.get(id) > geneIdCounts2.get(id)) {
						geneMultiBoth1 += geneIdCounts1.get(id) - geneIdCounts2.get(id);
						fr1Only.writeLine("gene\t" + id + "\t" + (geneIdCounts1.get(id) - geneIdCounts2.get(id)));
					}
				}
				else { // if (geneIdCounts2.get(id) > geneIdCounts1.get(id)) {
					geneMultiBoth3 += geneIdCounts1.get(id) - 1;
					geneMultiBoth2 += geneIdCounts2.get(id) - geneIdCounts1.get(id);
					fr2Only.writeLine("gene\t" + id + "\t" + (geneIdCounts2.get(id) - geneIdCounts1.get(id)));
				}
			}
			else {
				System.err.println("[[ WARNING ]] :: Why is geneID " + id + " " + geneIDs.get(id));
			}
		}
		
		for (String id : genePrtnIDs.keySet()) {
			// System.err.println("[[ DEBUG ]] :: " + id + " is " + genePrtnIDs.get(id));
			if (genePrtnIDs.get(id) == FOUND_IN_FR1) {
				genePrtn1++;
				if (genePrtnIdCounts1.get(id) > 1) {
					genePrtnMulti1 += genePrtnIdCounts1.get(id) - 1;
				}
				fr1Only.writeLine("gene_prtn\t" + id + "\t" + genePrtnIdCounts1.get(id));
			}
			else if (genePrtnIDs.get(id) == FOUND_IN_FR2) {
				genePrtn2++;
				if (genePrtnIdCounts2.get(id) > 1) {
					genePrtnMulti2 += genePrtnIdCounts2.get(id) - 1;
				}
				fr2Only.writeLine("gene_prtn\t" + id + "\t" + genePrtnIdCounts2.get(id));
			}
			else if (genePrtnIDs.get(id) == FOUND_IN_BOTH) {
				genePrtn3++;
				if (genePrtnIdCounts1.get(id) >= genePrtnIdCounts2.get(id)) {
					genePrtnMultiBoth3 += genePrtnIdCounts2.get(id) - 1;
					if (genePrtnIdCounts1.get(id) > genePrtnIdCounts2.get(id)) {
						genePrtnMultiBoth1 += genePrtnIdCounts1.get(id) - genePrtnIdCounts2.get(id);
						fr1Only.writeLine("gene_prtn\t" + id + "\t" + (genePrtnIdCounts1.get(id) - genePrtnIdCounts2.get(id)));
					}
				}
				else { // if (genePrtnIdCounts2.get(id) >  genePrtnIdCounts1.get(id)) {
					genePrtnMultiBoth3 += genePrtnIdCounts1.get(id) - 1; // shared, multicopy
					genePrtnMultiBoth2 += genePrtnIdCounts2.get(id) - genePrtnIdCounts1.get(id);
					fr2Only.writeLine("gene_prtn\t" + id + "\t" + (genePrtnIdCounts2.get(id) - genePrtnIdCounts1.get(id)));
				}
			}
			else {
				System.err.println("[[ WARNING ]] :: Why is genePrtnIDs " + id + " " + genePrtnIDs.get(id));
			}
		}
		
		for (String id : transIDs.keySet()) {
			if (transIDs.get(id) == FOUND_IN_FR1) {
				trans1++;
				if (transIdCounts1.get(id) > 1) {
					transMulti1 += transIdCounts1.get(id) - 1;
				}
				fr1Only.writeLine("transcript\t" + id + "\t" + transIdCounts1.get(id));
				// System.err.println("[[ DEBUG ]] :: Transcript only found in RefSeq: " + id);
			}
			else if (transIDs.get(id) == FOUND_IN_FR2) {
				trans2++;
				if (transIdCounts2.get(id) > 1) {
					transMulti2 += transIdCounts2.get(id) - 1;
				}
				fr2Only.writeLine("transcript\t" + id + "\t" + transIdCounts2.get(id));
				// System.err.println("[[ DEBUG ]] :: Transcripts only found in Liftoff: " + id + " (" + transIdCounts2.get(id) + ")");
			}
			else if (transIDs.get(id) == FOUND_IN_BOTH) {
				trans3++;
				if (transIdCounts1.get(id) >= transIdCounts2.get(id)) {
					transMultiBoth3 += transIdCounts2.get(id) - 1;
					if (transIdCounts1.get(id) > transIdCounts2.get(id)) {
						transMultiBoth1 += transIdCounts1.get(id) - transIdCounts2.get(id);
						fr1Only.writeLine("transcript\t" + id + "\t" + (transIdCounts1.get(id) - transIdCounts2.get(id)));
					}
				}
				else { // if (geneIdCounts2.get(id) > geneIdCounts1.get(id)) {
					transMultiBoth3 += transIdCounts1.get(id) - 1;
					transMultiBoth2 += transIdCounts2.get(id) - transIdCounts1.get(id);
					fr2Only.writeLine("transcript\t" + id + "\t" + (transIdCounts2.get(id) - transIdCounts1.get(id)));
				}
			}
			else {
				System.err.println("[[ WARNING ]] :: Why is transIDs " + id + " " + transIDs.get(id));
			}
		}
		
		for (String id : transPrtnIDs.keySet()) {
			if (transPrtnIDs.get(id) == FOUND_IN_FR1) {
				transPrtn1++;
				if (transPrtnIdCounts1.get(id) > 1) {
					transPrtnMulti1 += transPrtnIdCounts1.get(id) - 1;
				}
				fr1Only.writeLine("transcript_prtn\t" + id + "\t" + transPrtnIdCounts1.get(id));
			}
			else if (transPrtnIDs.get(id) == FOUND_IN_FR2) {
				transPrtn2++;
				if (transPrtnIdCounts2.get(id) > 1) {
					transPrtnMulti2 += transPrtnIdCounts2.get(id) - 1;
				}
				fr2Only.writeLine("transcript_prtn\t" + id + "\t" + transPrtnIdCounts2.get(id));
			}
			else if (transPrtnIDs.get(id) == FOUND_IN_BOTH) {
				transPrtn3++;
				if (transPrtnIdCounts1.get(id) >= transPrtnIdCounts2.get(id)) {
					transPrtnMultiBoth3 += transPrtnIdCounts2.get(id) - 1;
					if (transPrtnIdCounts1.get(id) >= transPrtnIdCounts2.get(id)) {
						transPrtnMultiBoth1 += transPrtnIdCounts1.get(id) - transIdCounts2.get(id);
						fr1Only.writeLine("transcript_prtn\t" + id + "\t" + (transPrtnIdCounts1.get(id) - transIdCounts2.get(id)));
					}
				}
				else { // if (geneIdCounts2.get(id) > geneIdCounts1.get(id)) {
					transPrtnMultiBoth3 += transPrtnIdCounts1.get(id) - 1;
					transPrtnMultiBoth2 += transPrtnIdCounts2.get(id) - transPrtnIdCounts1.get(id);
					fr2Only.writeLine("transcript_prtn\t" + id + "\t" + (transPrtnIdCounts2.get(id) - transIdCounts1.get(id)));
				}
			}
			else {
				System.err.println("[[ WARNING ]] :: Why is transPrtnIDs " + id + " " + transPrtnIDs.get(id));
			}
		}
	}
	
	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar gff3CompareRefseqLiftoff.jar RefSeq.gff Liftoff.gff");
		System.err.println("Count unique and common counts of gene / transcript, by all and protein-coding");
		System.err.println("Output: RefSeq_only.txt and Liftoff_only.txt");
		System.err.println("2023-07-06");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new CompareRefseqLiftoff().go(args[0], args[1]);
		} else {
			new CompareRefseqLiftoff().printHelp();
		}
	}

}
