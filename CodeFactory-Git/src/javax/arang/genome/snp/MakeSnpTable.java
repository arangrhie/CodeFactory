package javax.arang.genome.snp;

import java.util.ArrayList;

import javax.arang.IO.INOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/***
 * This is the entry class for merging snp tables made in ANNOVAR format.
 * @author �꾨옉
 *
 */
public class MakeSnpTable extends INOwrapper{
	
	@Override
	public void hooker(ArrayList<FileReader> frs, FileMaker fm) {
		if (frs.size() == 2) {
			new Merge2SnpFiles().hooker(frs.get(0), frs.get(1), fm);
		} else if (frs.size() > 2){
			FileMaker tmpFm = getNextTmpFileMaker(fm);
			new Merge2SnpFiles().hooker(frs.get(0), frs.get(1), tmpFm);
			FileReader snpTableFile = null;
			for (int i = 2; i < frs.size(); i++) {
				snpTableFile = new FileReader(tmpFm.getDir() + "/" + tmpFm.getFileName());
				if ( i == frs.size() - 1) {
					tmpFm = fm;
				} else {
					tmpFm = getNextTmpFileMaker(fm);
				}
				new AddSnpFile().hooker(snpTableFile, frs.get(i), tmpFm);
			}
		}
		
	}
	
	private static int num = 0;
	public static FileMaker getNextTmpFileMaker(FileMaker fm) {
		num++;
		return new FileMaker(fm.getDir() + "/tmp_snp_table_" + num);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar makeSnpTable.jar <sample1_file> <sample2_file> ... <sampleN_file> <outFile>");
		System.out.println("\tsample1..N_file: snp file to add");
		System.out.println("\t\t*sample names are considered from 0 to first _ substring.*");
		System.out.println("\toutFile: out file (possibly next snp table to merge)");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 3) {
			new MakeSnpTable().printHelp();
		} else {
			new MakeSnpTable().go(args);
		}
	}
}
