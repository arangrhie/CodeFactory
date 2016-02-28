package javax.arang.bed;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class RemoveBACsMappedToOneContig extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String contig;
		String bacid;
		
		HashMap<String, String> bacToContig = new HashMap<String, String>();
		ArrayList<String> bacListToRemain = new ArrayList<String>();
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			contig = tokens[Bed.CHROM];
			bacid = tokens[Bed.NOTE];
			if (!bacToContig.containsKey(bacid)) {
				bacToContig.put(bacid, contig);
			} else {
				if (!bacToContig.get(bacid).equals(contig) && !bacListToRemain.contains(bacid)) {
					bacListToRemain.add(bacid);
				}
			}
		}
		
		System.out.println(bacToContig.size() + "\tunique markers");
		System.out.println(bacListToRemain.size() + "\tare remained");
		System.out.println((bacToContig.size() - bacListToRemain.size()) + "\tare removed");
		
		fr.reset();
		
		fm.writeLine("#CONTIG\tSTART\tEND\tMARKER\tLEN\tNUM_READS\tAVG_DEPTH");
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			bacid = tokens[Bed.NOTE];
			if (bacListToRemain.contains(bacid)) {
				fm.writeLine(line);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedRemoveBACsMappedToOneContig.jar <in.bed> <out.bed>");
		System.out.println("\t<in.bed>: after applying filter; such as \"awk \'$5>500 && $6>3\' BAC_HiSeq_Tiling_to_AK1.all.bed > BAC_HiSeq_Tiling_to_AK1.all.depth3.len500.bed\"");
		System.out.println("\t<out.bed>: Only BACs mapped to >=2 contigs will be left.");
		System.out.println("\t\t*This code may used for 10X supports");
		System.out.println("Arang Rhie, 2015-10-01. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new RemoveBACsMappedToOneContig().go(args[0], args[1]);
		} else {
			new RemoveBACsMappedToOneContig().printHelp();
		}
	}

}
