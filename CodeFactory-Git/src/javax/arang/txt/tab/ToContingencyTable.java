package javax.arang.txt.tab;

import java.util.ArrayList;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToContingencyTable extends I2Owrapper {

	private static int sampleIdx1;
	private static int sampleIdx2;
	private static int filt1 = 10;
	private static int filt2 = 10;
	
	private int sampleIdxOffset;	// sampleIdx1 + sampleIdxOffset = sampleIdx2 for the same sample.
	
	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		String line;
		String[] tokens;
		
		String key1;	// key identifier for fr1 (ex. SV)
		String key2;	// key identifier for fr2 (ex. allelic expression site)
		
		sampleIdxOffset = sampleIdx2 - sampleIdx1;
		
		ArrayList<Integer> sampleIdxsHavingIn1 = new ArrayList<Integer>();
		int a;
		int b;
		int c;
		int d;
		
		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			tokens = line.split(RegExp.TAB);
			key1 = tokens[0];
			for (int i = 1; i < sampleIdx1; i++) {
				key1 += "\t" + tokens[i];
			}
			
			sampleIdxsHavingIn1.clear();// reset sampleIdxsHavingIn1
			for (int i = sampleIdx1; i < tokens.length; i++) {
				if (!tokens[i].equals("NA") && Integer.parseInt(tokens[i]) >= filt1) {
					sampleIdxsHavingIn1.add(i + sampleIdxOffset);
				}
			}
			System.out.println("[DEBUG] :: samples having SV: " + sampleIdxsHavingIn1.size());
			
			while (fr2.hasMoreLines()) {
				line = fr2.readLine();
				if (line.startsWith("#"))	continue;
				tokens = line.split(RegExp.TAB);
				key2 = tokens[0];
				for (int i = 1; i < sampleIdx2; i++) {
					key2 += "\t" + tokens[i];
				}
				a = 0;
				b = 0;
				c = 0;
				d = 0;
				for (int i = sampleIdx2; i < tokens.length; i++) {
					if (sampleIdxsHavingIn1.contains(i)) {
						if (!tokens[i].equals("NA") && Integer.parseInt(tokens[i]) >= filt2) {
							a++;
						} else {
							b++;
						}
					} else {
						if (!tokens[i].equals("NA") && Integer.parseInt(tokens[i]) >= filt2) {
							c++;
						} else {
							d++;
						}
					}
				}
				fm.writeLine(key1 + "\t" + key2 + "\t" + a + "\t" + b + "\t" + c + "\t" + d);
			}
			fr2.reset();
		}
	}

	
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fisherToContingencyTable.jar <in_1> <in_2> <sample_idx_1> <sample_idx_2> <out.table> [filt_1] [filt_2]");
		System.out.println("Merge 2 files into a contingency table.");
		System.out.println("\t<in_1>: Any tdf file with sample information starting from <sample_idx_1>."
				+ " Numeric values or \"NA\" allowed for missing values.");
		System.out.println("\t<in_2>: Same as <in_1>. Sample order MUST MATCH that of <in_1>.");
		System.out.println("\t<sample_idx_1>: 1-based. First column containing sample information.");
		System.out.println("\t<sample_idx_2>: 1-based. Same as <sample_idx_1>.");
		System.out.println("\t<out.table>: Columns of <in_1> 1 ~ <sample_idx_1> and <in_2> 1 ~ <sample_idx_2> will be added.");
		System.out.println("\t\t4 columns will be following: a b c d.");
		System.out.println("\t\t\ta: has <in_1>, has <in_2>");
		System.out.println("\t\t\tb: has <in_1>, has not <in_2>");
		System.out.println("\t\t\tc: has not <in_1>, has <in_2>");
		System.out.println("\t\t\td: has not <in_1>, has not <in_2>");
		System.out.println("\t\t[filt_1]: >= [filt_1] will be treated as 'has'. DEFAULT=10");
		System.out.println("\t\t[filt_2]: Same as [filt_1]. DEFAULT=10");
		System.out.println("Arang Rhie, 2017-01-25. arrhie@gmail.com");
		
	}

	public static void main(String[] args) {
		if (args.length >= 5) {
			sampleIdx1 = Integer.parseInt(args[2]) - 1;
			sampleIdx2 = Integer.parseInt(args[3]) - 1;
			if (args.length > 5) {
				filt1 = Integer.parseInt(args[5]);
				filt1 = Integer.parseInt(args[6]);
			}
			new ToContingencyTable().go(args[0], args[1], args[4]);
		} else {
			new ToContingencyTable().printHelp();
		}
	}

}
