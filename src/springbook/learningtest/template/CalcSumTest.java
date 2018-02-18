package springbook.learningtest.template;

import java.io.IOException;

import org.junit.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CalcSumTest {
	@Test
	public void sumOfNumbers() throws IOException{
		Calculator calculator = new Calculator();
		int sum = calculator.calcSum("./src/springbook/learningtest/template/numbers.txt");
		assertThat(sum, is(10));
	}
}
