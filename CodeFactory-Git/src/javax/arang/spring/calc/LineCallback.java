package javax.arang.spring.calc;

public interface LineCallback<T>{
	T doSomethingWithLine(String line, T value);
}
