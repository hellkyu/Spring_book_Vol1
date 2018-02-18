package springbook.learningtest.template;

import java.io.IOException;

import org.junit.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CalcSumTest {
	Calculator calculator;
	String numFilepath;
	
	@Before public void setUp() {
		this.calculator = new Calculator();
		this.numFilepath = "./src/springbook/learningtest/template/numbers.txt";
	}
	
	@Test public void sumOfNumbers() throws IOException{
		assertThat(calculator.calcSum(numFilepath), is(10));
	}
	
	@Test public void multiplyOfNumbers() throws IOException{
		assertThat(calculator.calcMultiply(this.numFilepath), is(24));
	}
}
