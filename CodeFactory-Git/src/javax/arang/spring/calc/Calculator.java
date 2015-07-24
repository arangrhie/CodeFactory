package javax.arang.spring.calc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class Calculator {
	
	public Integer fileReadTamplate(String filePath, BufferedReaderCallback callback, Integer initValue) 
		throws IOException {
		
		BufferedReader br = null;
		
		try {
			br = new BufferedReader(new FileReader(filePath));
			String line = null;
			Integer res = initValue;
			while((line = br.readLine()) != null) {
				res = callback.doSomethingWithReader(line, res);
			}
			return res;
		} catch (IOException e) {
			System.out.println(e.getMessage());
			throw e;
		} finally {
			if (br != null) {
				try {	br.close();	} catch (IOException e2) {
					System.out.println(e2.getMessage());
				}
			}
		}
	}
	
	public <T> T lineReadTemplate(String filePath, LineCallback<T> callback, T initValue) 
		throws IOException {
			
			BufferedReader br = null;
			
			try {
				br = new BufferedReader(new FileReader(filePath));
				String line = null;
				T res = initValue;
				while((line = br.readLine()) != null) {
					res = callback.doSomethingWithLine(line, res);
				}
				return res;
			} catch (IOException e) {
				System.out.println(e.getMessage());
				throw e;
			} finally {
				if (br != null) {
					try {	br.close();	} catch (IOException e2) {
						System.out.println(e2.getMessage());
					}
				}
			}
	}

	public Integer calcSum(String filePath) throws IOException {
		BufferedReaderCallback sumCallback = new BufferedReaderCallback() {

			@Override
			public Integer doSomethingWithReader(String line, Integer res)
					throws IOException {
				return res += Integer.valueOf(line);
			}
			
		};
		return fileReadTamplate(filePath, sumCallback, 0);
	}
	
	public Integer calcMultiply(String filePath) throws IOException {
		BufferedReaderCallback sumCallback = new BufferedReaderCallback() {

			@Override
			public Integer doSomethingWithReader(String line, Integer res)
					throws IOException {
				return res *= Integer.valueOf(line);
			}
			
		};
		return fileReadTamplate(filePath, sumCallback, 1);
	}
	
	public String concatenate(String filePath) throws IOException {
		LineCallback<String> concatenateCallback = new LineCallback<String>() {

			@Override
			public String doSomethingWithLine(String line, String value) {
				return value + line;
			}
		};
		return lineReadTemplate(filePath, concatenateCallback, "");
	}
	
	
}
