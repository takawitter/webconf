# webconf
Easy and lightweight configuration for web application.

1. Write config classes

```java
	public static class SomeConfig1{
		private int value = 1;
		private String name = "hello";
		// accessors
	}
	public static class SomeConfig2{
		private int age = 26;
		// accessors
	}
```

2. Write config servlet

```java
@WebServlet("/config")
public class SampleConfServlet extends com.github.takawitter.webconf.WebConfServlet{
	private static SomeConfig1 conf1 = new SomeConfig1();
	private static SomeConfig2 conf2 = new SomeConfig2();

	@Override
	protected Object[] configs() {
		return new Object[]{conf1, conf2};
	}
}
```

3. Now you can see and modify values of configs through your browser

![](https://github.com/takawitter/webconf/raw/screenshots/screenshot.png)
