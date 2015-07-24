package javax.arang.spring.calc;

import java.io.IOException;

public interface BufferedReaderCallback {
	Integer doSomethingWithReader(String line, Integer res) throws IOException;
}
