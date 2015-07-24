package javax.arang.test;

public class CharacterToString {

	public static void main(String[] args) {
		String sequence = "abcd";
		int len = sequence.length();
		char[] reversedSequence = new char[len];
		for (int j = 0; j < sequence.length(); j++) {
			reversedSequence[j] = sequence.charAt(len - j - 1);
		}
		String reversed = String.copyValueOf(reversedSequence);
		System.out.println(reversed);
	}

}
