package javax.arang.txt;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.INwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class RunLengthMatrixToCounts extends INwrapper {
	
	
	/***
	 * Input runLengthMatrix looks like this: 
#ref_rl	read_1	read_2	read_3	read_4	read_5	read_6	read_7	read_8	read_9	read_10	read_11	read_12	read_13	read_14	read_15	read_16	read_17	read_18	read_19	read_20	read_21	read_22	read_23	read_24	read_25	read_26	read_27	read_28	read_29	read_30	read_31	read_32	read_33	read_34	read_35	read_36	read_37	read_38	read_39	read_40	read_41	read_42	read_43	read_44	read_45	read_46	read_47	read_48	read_49	read_50+
1	1218533571	662344	16503	4096	1793	878	393	181	62	40	17	15	15	13	10	18	17	4	6	13	12	4	6	2	4	8	2
2	671677	362377085	532197	5896	1070	471	230	106	33	18	18	16	7	2	3	2	2	3	5	1	1	2	1	0	0	0	0
3	47483	617511	128625417	495795	3393	538	205	118	44	29	8	13	7	3	7	2	1	1	1	1	0	0	1	0	0	0	0
4	17066	16451	447837	45633073	388039	2961	351	154	71	38	15	13	5	4	5	4	0	2	0	0	0	0	0	2	0	5	1
5	6127	5652	6332	283844	16429172	285548	2749	256	94	45	35	28	14	7	6	3	5	1	1	1	0	0	1	2	0	0	0
6	2093	1793	1700	2090	135296	5041943	151455	1901	160	29	17	19	3	2	1	1	0	0	2	1	0	0	0	0	0	1	0	0
7	882	860	763	746	965	76391	2081563	108148	2079	174	37	18	8	4	4	0	2	1	0	0	2	0	0	0	0	0	0	0
8	340	329	280	281	248	416	33950	700768	51749	1421	96	13	8	7	1	0	2	0	0	0	1	1	1	0	0	0	0	0
9	229	207	147	144	151	164	320	24880	401135	38139	1234	102	21	7	3	2	0	1	0	1	1	0	1	0	2	0	0	0
	 */

	@Override
	public void hooker(ArrayList<FileReader> frs) {
		String line;
		String[] tokens;
		
		int lRef = -1, lRead = 0; // lRef = length in the reference, lRead = length in the read
		int lenMax = 0;
		int iF = 0, iT = 0; 	// from ~ to col index to look for
		int diff;
		double count;
		
		HashMap<Integer, Double> obsDiff = new HashMap<Integer, Double>();	// -5 ~ 5
		
		for (FileReader fr : frs) {
			lRef = -1;
			lRead = 0;
			lenMax = 0;
			
			while (fr.hasMoreLines()) {
				line = fr.readLine();
				tokens=line.split(RegExp.TAB);
				lRef++; // ref len

				if (lRef < 1)	{
					lenMax = tokens.length - 1;	// -1 to exclude the ref_rl column
					continue;
				}
				
				// Skip ref len < Len_Min
				if (lRef < LEN_MIN) {
					continue;
				}

				//System.err.print("ref_rl " + lRef + " : ");
				iF = Math.max(lRef - DIFF_MAX, LEN_MIN);
				iT = Math.min(lRef + DIFF_MAX, lenMax);
				for (lRead = iF; lRead <= iT; lRead++) {
					// Get diff and count to save
					diff = lRead - lRef;
					//System.err.print("\t" + lRead);

					count = Double.parseDouble(tokens[lRead]);
					if (obsDiff.containsKey(diff))	count += obsDiff.get(diff);

					obsDiff.put(diff, count);
				}
				//System.err.println();
			}

		}
		
		System.err.println("Writing output ...");
		// Print output
		for (int i = DIFF_MAX * -1; i <= DIFF_MAX; i++) {
			if (obsDiff.containsKey(i)) {
				System.out.println(i + "\t" + String.format("%.0f", obsDiff.get(i)));
			} else {
				System.out.println(i + "\t0");
			}
		}
		
		System.err.println("Done!");
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar txtRunLengthMatrixToDiffCounts.jar [min=5] [diff=5] run_length*.tsv");
		System.err.println("  run_length.tsv : Output from runLengthMatrix. Could be multiple files.");
		System.err.println("  [min]          : Minimum run-length to start consider collecting. OPTIONAL. DEFAULT=5");
		System.err.println("  [diff]         : Maximum length difference to count. e.g. 5 will count from -5 to +5. DEFAULT=5");
	}
	
	private static int DIFF_MAX = 5;
	private static int LEN_MIN = 5;

	public static void main(String[] args) {
		if (args.length != 0) {
			ArrayList<String> newArgs = new ArrayList<String>();
			for (String arg : args) {
				if (arg.startsWith("min")) {
					LEN_MIN = Integer.parseInt(arg.split("=")[1]);
				} else if (arg.startsWith("diff")) {
					DIFF_MAX = Integer.parseInt(arg.split("=")[1]);
				} else {
					newArgs.add(arg);
				}
			}
			args = new String[newArgs.size()];
			for (int i = 0; i < newArgs.size(); i++) {
				args[i] = newArgs.get(i);
			}
			new RunLengthMatrixToCounts().go(args);
		} else {
			new RunLengthMatrixToCounts().printHelp();
		}
	}

}
