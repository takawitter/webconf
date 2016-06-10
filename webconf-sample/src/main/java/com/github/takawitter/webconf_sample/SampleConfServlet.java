package com.github.takawitter.webconf_sample;

import javax.servlet.annotation.WebServlet;

import com.github.takawitter.webconf.WebConfServlet;

@WebServlet("/config")
public class SampleConfServlet extends WebConfServlet{
	public static class SomeConfig1{
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getValue() {
			return value;
		}
		public void setValue(int value) {
			this.value = value;
		}
		private int value;
		private String name;
	}
	public static class SomeConfig2{
		public int getAge() {
			return age;
		}
		public void setAge(int age) {
			this.age = age;
		}
		private int age;
	}
	private static SomeConfig1 conf1 = new SomeConfig1();
	private static SomeConfig2 conf2 = new SomeConfig2();

	@Override
	protected Object[] configs() {
		return new Object[]{conf1, conf2};
	}

	private static final long serialVersionUID = -9071672526197504760L;
}
