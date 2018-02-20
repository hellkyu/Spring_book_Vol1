package springbook.learningtest.jdk;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ReflectionTest {
    @Test
    public void invokeMethod() throws Exception{
        String name = "Spring";

        // length()
        assertThat(name.length(), is(6));

        Method lengthMethod = String.class.getMethod("length");
        assertThat(lengthMethod.invoke(name), is(6));

        //charAt()
        assertThat(name.charAt(0), is('S'));

        Method chatAtMethod = String.class.getMethod("charAt", int.class);
        assertThat(chatAtMethod.invoke(name, 0), is('S'));
    }
}
