package javax.arang.falcon;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.IOUtil;
import javax.arang.genome.fasta.ExtractScaffoldsFromList;

public class ExtractSubreadFaFromList extends R2wrapper {

	private static String outDir;
	private static final short PREFIX = 0;
	
	@Override
	public void hooker(FileReader frFoFn, FileReader frSubreadList) {
		HashMap<String, ArrayList<String>> subreadDirToReadsMap = new HashMap<String, ArrayList<String>>();
		ArrayList<String> subreadList;
		
		String line;
		String[] tokens;
		int numSubreads = 0;
		while (frSubreadList.hasMoreLines()) {
			line = frSubreadList.readLine();
			tokens = line.split("/");
			if (!subreadDirToReadsMap.containsKey(tokens[PREFIX])) {
				subreadList = new ArrayList<String>();
				subreadDirToReadsMap.put(tokens[PREFIX], subreadList);
			}
			subreadList = subreadDirToReadsMap.get(tokens[PREFIX]);
			subreadList.add(line);
			numSubreads++;
		}
		
		System.out.println("[DEBUG] :: " + subreadDirToReadsMap.size() + " prefixes will be touched");
		System.out.println("[DEBUG] :: " + numSubreads + " subreads to find");
		
		String prefix;
		String faFileName;
		ArrayList<String> faWritten;
		numSubreads = 0;
		while (frFoFn.hasMoreLines()) {
			line = frFoFn.readLine().trim();
			faFileName = IOUtil.retrieveFileName(line);
			prefix = faFileName.substring(0, faFileName.indexOf("."));
			//System.out.println("[DEBUG] :: Reading prefix " + prefix + " of " + line);
			subreadList = subreadDirToReadsMap.get(prefix);
			if (subreadList == null || subreadList.size() == 0)	continue;
			FileMaker fm = new FileMaker(outDir, faFileName);
			FileReader frFa = new FileReader(line);
			faWritten = ExtractScaffoldsFromList.readFaExtractSeq(frFa, subreadList, fm);
			fm.closeMaker();
			frFa.closeReader();
			System.out.println("[DEBUG] :: " + faWritten.size() + " / " + subreadList.size() + " (" + numSubreads + ") written to " + faFileName);
			if (faWritten.size() == 0)	continue;
			numSubreads += faWritten.size();
			for (int i = 0; i < faWritten.size(); i++) {
				subreadList.remove(subreadList.indexOf(faWritten.get(i)));
			}
			if (subreadList.size() == 0) {
				subreadDirToReadsMap.remove(prefix);
			}
		}
		System.out.println("[DEBUG] :: " + numSubreads + " subreads are written");
		
		FileMaker fmUnfoundReads = new FileMaker(outDir + ".unfound.list");
		int numUnfound = 0;
		for (String key : subreadDirToReadsMap.keySet()) {
			subreadList = subreadDirToReadsMap.get(key);
			for (String subread : subreadList) {
				fmUnfoundReads.writeLine(subread);
				numUnfound++;
			}
		}
		fmUnfoundReads.closeMaker();
		
		System.out.println("[DEBUG] :: " + numUnfound + " subreads are unfound, and written to " + fmUnfoundReads.getFileName());
		
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar falconExtractSubreadFaFromList.jar <in.fofn> <in.subreads.list> <out_dir>");
		System.out.println("\tSelect fa sequences out of in.fofn.");
		System.out.println("\t<out_dir>: drectory for output fa files. Small files containing subreads in list will be made from original <in.fofn>.");
		System.out.println("\t\tSo, do cat *.fa > <desired_file.fa> to aggregate.");
		System.out.println("Arang Rhie, 2015-08-18. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			outDir = args[2];
			new ExtractSubreadFaFromList().go(args[0], args[1]);
		} else {
			new ExtractSubreadFaFromList().printHelp();
		}
	}

}
