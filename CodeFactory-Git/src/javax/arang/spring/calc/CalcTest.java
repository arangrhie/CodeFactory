package javax.arang.spring.calc;

import java.io.IOException;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class CalcTest {
	
	Calculator calculator;
	String numFilePath;
	
	@Before
	public void setUp() {
		this.calculator = new Calculator();
		numFilePath = "resources/numbers.txt";
	}
	
	@Test
	public void sumOfNumbers() throws IOException {
		Calculator calc = new Calculator();
		int sum = calc.calcSum(numFilePath);
		Assert.assertThat(sum, CoreMatchers.is(10));
		int mul = calc.calcMultiply(numFilePath);
		Assert.assertThat(mul, CoreMatchers.is(24));
	}

	@Test
	public void concatenateStrings() throws IOException {
		Assert.assertThat(calculator.concatenate(this.numFilePath), CoreMatchers.is("1234"));
	}

}
