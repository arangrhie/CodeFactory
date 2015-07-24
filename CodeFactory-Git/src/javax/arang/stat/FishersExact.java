package javax.arang.stat;

import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.annovar.ANNOVAR;

public class FishersExact extends IOwrapper {

	HashMap<Integer, Double> factNums = new HashMap<Integer, Double>();
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {

		initFactNums(numSide*2);
		int totalNum = 0;
		String line;
		String[] tokens;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.equals(""))	continue;
			tokens = line.split("\t");
			int rec0Negative = 0;
			int rec0Positive = 0;
			int rec2Negative = 0;
			int rec2Positive = 0;
			int dom0Negative = 0;
			int dom0Positive = 0;
			int dom1Negative = 0;
			int dom1Positive = 0;
			int alleleNegative = 0;
			int notAlleleNegative = 0;
			int allelePositive = 0;
			int notAllelePositive = 0;
			
			for (int i = ANNOVAR.NOTE; i < ANNOVAR.NOTE + numSide; i++) {
				if (tokens[i].equals("NA"))	continue;
				
				// Recessive test
				if (tokens[i].equals("2"))	{
					rec2Negative++;
					alleleNegative += 2;
				} else {
					rec0Negative++;
				}
				
				// Dominant test
				if (tokens[i].equals("0")) {
					dom0Negative++;
					notAlleleNegative += 2;
				} else {
					dom1Negative++;
				}
				
				if (tokens[i].equals("1")) {
					alleleNegative++;
					notAlleleNegative++;
				}
			}
			
			for (int i = ANNOVAR.NOTE + numSamples - numSide; i < ANNOVAR.NOTE + numSamples; i++) {
				if (tokens[i].equals("NA"))	continue;
				
				// Recessive test
				if (tokens[i].equals("2"))	{
					rec2Positive++;
					allelePositive += 2;
				} else {
					rec0Positive++;
				}
				
				// Dominant test
				if (tokens[i].equals("0")) {
					dom0Positive++;
					notAllelePositive += 2;
				} else {
					dom1Positive++;
				}
				
				if (tokens[i].equals("1")) {
					allelePositive++;
					notAllelePositive++;
				}
			}
			
			// Additive
			String addP = null;
			if ((allelePositive == 0 && alleleNegative == 0)
					|| notAllelePositive == 0 && notAlleleNegative == 0) {
				addP = "#NA\t#NA";
			} else {
				double add = fishersPvalue(allelePositive, notAllelePositive, alleleNegative, notAlleleNegative);
				addP = add + "\t" + (add*2);
			}
			
			
			// Dominant
			String domP = null;
			if ((dom0Positive == 0 && dom0Negative == 0)
					|| dom1Positive == 0 && dom1Negative == 0) {
				domP = "#NA\t#NA";
			} else {
				double dom = fishersPvalue(dom0Positive, dom1Positive, dom0Negative, dom1Negative);
				//if (dom > 0.45)	continue;
				domP = dom + "\t" + (dom*2);
			}
			
			// Recessive
			String recP = null;
			if ((rec0Positive == 0 && rec0Negative == 0)
					|| (rec2Positive == 0 && rec2Negative == 0)) {
				recP = "#NA\t#NA";
			} else {
				double rec = fishersPvalue(rec0Positive, rec2Positive, rec0Negative, rec2Negative);
				//if (rec > 0.45)	continue;
				recP = rec + "\t" + (rec*2);
			}
			fm.writeLine(line + "\t" + addP + "\t" + domP + "\t" + recP);
			totalNum++;
		}
		System.out.println("Total # of SNPs\t" + totalNum);
	}

	private void initFactNums(int numSampleSides) {
		double value = 1;
		factNums.put(0, value);
		for (int i = 1; i <= numSampleSides*2; i++) {
			value = value * i;
			factNums.put(i, new Double(value));
		}
	}
	
	private double getFactNum(int num) {
		return factNums.get(num);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar statFishersExact.jar <in.snp.table.annovar> <num_samples> [num_side]");
		System.out.println("\t<out> <in.snp.table.annovar.fisher>: ");
	}

	static int numSamples = 0;
	static int numSide = 10;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			numSamples = Integer.parseInt(args[1]);
			new FishersExact().go(args[0], args[0] + ".fisher." + numSide);
		} else if (args.length == 3) {
			numSamples = Integer.parseInt(args[1]);
			numSide = Integer.parseInt(args[2]);
			new FishersExact().go(args[0], args[0] + ".fisher." + numSide);
		} else {
			new FishersExact().printHelp();
		}
	}
	
	private double fisher(int a, int b, int c, int d) {
		// (a+b)!*(c+d)!*(a+c)!*(b+d)! / n!*a!*b!*c!*d!
//		System.out.println(a + " " + b + " " + c + " " + d);
		double numerator = getFactNum(a+b) * getFactNum(c+d) * getFactNum(a+c) * getFactNum(b+d);
		double denominator = getFactNum(a+b+c+d) * getFactNum(a) * getFactNum(b)* getFactNum(c) * getFactNum(d);
		double fisherP = numerator / denominator;
		return fisherP;
	}
	
	private double fishersPvalue(int a, int b, int c, int d) {
		// (a+b)!*(c+d)!*(a+c)!*(b+d)! / n!*a!*b!*c!*d!
		
		double oneTale = fisher(a, b, c, d);
		
		int[][] matrix = new int[2][2];
		matrix[0][0] = a;
		matrix[1][0] = b;
		matrix[0][1] = c;
		matrix[1][1] = d;
		
		int[][] minValIdx = getMinValIdx(matrix);
		
		int[][] newMat = null;
		for (int i = minValIdx[VAL][MIN1]-1; i >= 0; i--) {
			newMat = getDec(matrix,  minValIdx[IDX][MIN1], i);
			oneTale += fisher(newMat[0][0], newMat[1][0], newMat[0][1], newMat[1][1]);
		}
		return oneTale;
	}
	
	final static int VAL = 0;
	final static int IDX = 1;
	final static int MIN1 = 0;
	final static int MIN2 = 1;
	private int[][] getMinValIdx(int[][] matrix) {
		boolean isOnSameSide = false;
		
		if ((matrix[0][0] < matrix[1][0] && matrix[0][1] < matrix[1][1])
				|| (matrix[0][0] > matrix[1][0] && matrix[0][1] > matrix[1][1])) {
			isOnSameSide = true;
		}
		
		int[][] values = new int[2][2];
		if (!isOnSameSide) {
			if (Math.min(matrix[0][0], matrix[1][1]) < Math.min(matrix[1][0], matrix[0][1])) {
				if (matrix[0][0] < matrix[1][1]) {
					// 1 < 4
					values[VAL][MIN1] = matrix[0][0];
					values[VAL][MIN2] = matrix[1][1];
					values[IDX][MIN1] = 1;
					values[IDX][MIN2] = 4;
				} else {
					// 4 < 1
					values[VAL][MIN1] = matrix[1][1];
					values[VAL][MIN2] = matrix[0][0];
					values[IDX][MIN1] = 4;
					values[IDX][MIN2] = 1;
				}
			} else {
				if (matrix[1][0] < matrix[0][1]) {
					// 2 < 3
					values[VAL][MIN1] = matrix[1][0];
					values[VAL][MIN2] = matrix[0][1];
					values[IDX][MIN1] = 2;
					values[IDX][MIN2] = 3;
				} else {
					// 3 < 2
					values[VAL][MIN1] = matrix[0][1];
					values[VAL][MIN2] = matrix[1][0];
					values[IDX][MIN1] = 3;
					values[IDX][MIN2] = 2;
				}
			}
		} else {
			if (Math.min(matrix[0][0], matrix[0][1]) < Math.min(matrix[1][0], matrix[1][1])) {
				if (matrix[0][0] < matrix[0][1]) {
					// 1 < 3
					values[VAL][MIN1] = matrix[0][0];
					values[VAL][MIN2] = matrix[0][1];
					values[IDX][MIN1] = 1;
					values[IDX][MIN2] = 3;
				} else {
					// 3 < 1
					values[VAL][MIN1] = matrix[0][1];
					values[VAL][MIN2] = matrix[0][0];
					values[IDX][MIN1] = 3;
					values[IDX][MIN2] = 1;
				}
			} else {
				if (matrix[1][0] < matrix[1][1]) {
					// 2 < 4
					values[VAL][MIN1] = matrix[1][0];
					values[VAL][MIN2] = matrix[1][1];
					values[IDX][MIN1] = 2;
					values[IDX][MIN2] = 4;
				} else {
					// 4 < 2
					values[VAL][MIN1] = matrix[1][1];
					values[VAL][MIN2] = matrix[1][0];
					values[IDX][MIN1] = 4;
					values[IDX][MIN2] = 2;
				}
			}
		}
		
//		System.out.println("DEBUG :: ");
//		for (int i = 0; i < 2; i++) {
//			for (int j = 0; j < 2; j++) {
//				System.out.print(values[i][j] + " ");
//			}
//			System.out.println();
//		}
		return values;
	}

	private int[][] getDec(int[][] matrix, int index, int value) {
		int[][] newMatrix = new int[2][2];
		
		if (index == 1) {
			newMatrix[0][0] = value;
			newMatrix[1][0] = matrix[0][0] + matrix[1][0] - value;
			newMatrix[0][1] = matrix[0][0] + matrix[0][1] - value;
			newMatrix[1][1] = matrix[1][1] - matrix[0][0] + value;
		} else if (index == 2) {
			newMatrix[0][0] = matrix[0][0] + matrix[1][0] - value;
			newMatrix[1][0] = value;
			newMatrix[0][1] = matrix[0][1] - matrix[1][0] + value;
			newMatrix[1][1] = matrix[1][1] + matrix[1][0] - value;
		} else if (index == 3) {
			newMatrix[0][0] = matrix[0][0] + matrix[0][1] - value;
			newMatrix[1][0] = matrix[1][0] - matrix[0][1] + value;
			newMatrix[0][1] = value;
			newMatrix[1][1] = matrix[0][1] + matrix[1][1] - value;
		} else if (index == 4) {
			newMatrix[0][0] = matrix[0][0] - matrix[1][1] + value;
			newMatrix[1][0] = matrix[1][0] + matrix[1][1] - value;
			newMatrix[0][1] = matrix[0][1] + matrix[1][1] - value;
			newMatrix[1][1] = value;
		}
		return newMatrix;
	}
	
	public double lfactorial(int n) {
		double result = 0; // = log(1)
		for (int i =2; i < n; i++) {
			result += Math.log(i);
		}
		return result;
	}

}
