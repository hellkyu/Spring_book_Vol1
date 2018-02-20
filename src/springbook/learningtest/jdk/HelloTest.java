package springbook.learningtest.jdk;

import org.junit.Test;

import java.lang.reflect.Proxy;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HelloTest {

    @Test
    public void simpleProxy(){
        Hello hello = new HelloTarget();
        assertThat(hello.sayHello("Toby"), is("Hello Toby"));
        assertThat(hello.sayHi("Toby"), is("Hi Toby"));
        assertThat(hello.sayThankYou("Toby"), is("Thank You Toby"));

        Hello proxiedHello = (Hello) Proxy.newProxyInstance(
                getClass().getClassLoader(),    //다이내믹 프록시가 정의되는 클래스로더 지정
                new Class[] {Hello.class},      //다이내믹 프록시가 구현할 인터페이스 배열
                new UppercaseHandler(new HelloTarget()));   // 부가기능과 위임 관련 코드를 담는 InvocationHandler 구현 오브젝트



        assertThat(proxiedHello.sayHello("Toby"), is("HELLO TOBY"));
        assertThat(proxiedHello.sayHi("Toby"), is("HI TOBY"));
        assertThat(proxiedHello.sayThankYou("Toby"), is("THANK YOU TOBY"));
    }
}
