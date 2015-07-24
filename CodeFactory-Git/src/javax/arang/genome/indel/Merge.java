package javax.arang.genome.indel;

import java.util.ArrayList;

import javax.arang.IO.INOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.genome.snp.Merge2SnpFiles;

public class Merge extends INOwrapper {

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar mergeIndels.jar <sample1.chr1.indel> <sample2.chr1.indel> ... <sampleN.chr1.indel> <chr1.indel>");
		System.out.println("\t<output>: chr1.indel - merged indel file");
	}

	@Override
	public void hooker(ArrayList<FileReader> frs, FileMaker fm) {
		if (frs.size() == 2) {
			new Merge2SnpFiles().hooker(frs.get(0), frs.get(1), fm);
		} else if (frs.size() > 2){
			FileMaker tmpFm = getNextTmpFileMaker(fm);
			new Merge2Indel().hooker(frs.get(0), frs.get(1), tmpFm);
			FileReader indelTableFile = null;
			for (int i = 2; i < frs.size(); i++) {
				indelTableFile = new FileReader(tmpFm.getDir() + "/" + tmpFm.getFileName());
				if ( i == frs.size() - 1) {
					tmpFm = fm;
				} else {
					tmpFm = getNextTmpFileMaker(fm);
				}
				new AddIndel().hooker(indelTableFile, frs.get(i), tmpFm);
			}
		}
		
	}
	
	private static int num = 0;
	public static FileMaker getNextTmpFileMaker(FileMaker fm) {
		num++;
		return new FileMaker(fm.getDir(), "tmp_indel_table_" + num);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 3) {
			new Merge().printHelp();
		} else {
			new Merge().go(args);
		}
		
	}

}
