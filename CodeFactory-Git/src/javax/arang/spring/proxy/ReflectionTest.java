package javax.arang.spring.proxy;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Proxy;

import org.junit.Test;


public class ReflectionTest {
	
	@Test
	public void invokeMethod() throws Exception {
		String name = "Spring";
		
		// length()
		assertThat(name.length(), is(6));
		
		Method lengthMethod = String.class.getMethod("length");
		assertThat((Integer)lengthMethod.invoke(name), is(6));
		
		// charAt()
		assertThat(name.charAt(0), is('S'));
		
		Method charAtMethod = String.class.getMethod("charAt", int.class);
		assertThat((Character)charAtMethod.invoke(name, 0), is('S'));
		
	}
	
	@Test
	public void simpleProxy() {
		Hello hello = (Hello)Proxy.newProxyInstance(
				getClass().getClassLoader(),
				new Class[] {Hello.class}, 
				new UppercaseHandler(new HelloTarget()));
		assertThat(hello.sayHello("Toby"), is("HELLO TOBY"));
		assertThat(hello.sayHi("Toby"), is("HI TOBY"));
		assertThat(hello.sayThankYou("Toby"), is("THANK YOU TOBY"));
	}

}
