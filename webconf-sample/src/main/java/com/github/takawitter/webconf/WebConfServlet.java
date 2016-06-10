package com.github.takawitter.webconf;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.go.nict.langrid.commons.beanutils.Converter;
import jp.go.nict.langrid.commons.io.StreamUtil;
import jp.go.nict.langrid.commons.util.Pair;
import jp.go.nict.langrid.repackaged.net.arnx.jsonic.JSON;

public abstract class WebConfServlet extends HttpServlet{

	protected abstract Object[] configs();

	@Override
	public void init(ServletConfig config) throws ServletException {
		for(Object c : configs()){
			try {
				Map<String, PropertyDescriptor> props = new LinkedHashMap<>();
				for(PropertyDescriptor pd : Introspector.getBeanInfo(c.getClass()).getPropertyDescriptors()){
					if(pd.getReadMethod() == null) continue;
					if(pd.getWriteMethod() == null) continue;
					props.put(pd.getName(), pd);
				}
				configs.put(c.getClass().getName(),
						Pair.create(c, props));
			} catch (IntrospectionException e) {
				throw new ServletException(e);
			}
		}
	}
	
	private Map<String, Pair<Object, Map<String, PropertyDescriptor>>> configs = new LinkedHashMap<>();

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String method = req.getParameter("method");
		if(method == null) method = "";
		if(method.equals("getConfigs")){
			List<Map<String, Object>> ret = new ArrayList<>();
			for(Map.Entry<String, Pair<Object, Map<String, PropertyDescriptor>>> c : configs.entrySet()){
				Map<String, Object> cm = new HashMap<>();
				cm.put("name", c.getKey());
				Object instance = c.getValue().getFirst();
				List<Map<String, Object>> props = new ArrayList<>();
				for(Map.Entry<String, PropertyDescriptor> p : c.getValue().getSecond().entrySet()){
					
					try{
						Map<String, Object> pm = new HashMap<>();
						pm.put("value", p.getValue().getReadMethod().invoke(instance));
						pm.put("name", p.getKey());
						pm.put("type", p.getValue().getPropertyType());
						props.add(pm);
					} catch(IllegalAccessException | InvocationTargetException e){
						e.printStackTrace();
					}
				}
				cm.put("properties", props);
				ret.add(cm);
			}
			JSON.encode(ret, resp.getOutputStream());
		} else if(method.equals("set")){
			String conf = req.getParameter("config");
			String name = req.getParameter("name");
			String value = req.getParameter("value");
			System.out.println(
					"conf: " + conf + ", name: " + name + ", value: " + value
					);
			Pair<Object, Map<String, PropertyDescriptor>> config = configs.get(conf);
			if(config == null){
				resp.sendError(HttpServletResponse.SC_NOT_FOUND, String.format(
						"config \"%s\" is not found.", conf));
				return;
			}
			PropertyDescriptor pd = config.getSecond().get(name);
			if(pd == null){
				resp.sendError(HttpServletResponse.SC_NOT_FOUND, String.format(
						"method \"%s\" of config \"%s\" is not found.",
						name, conf));
				return;
			}
			try {
				pd.getWriteMethod().invoke(config.getFirst(), new Converter().convert(value, pd.getPropertyType()));
			} catch (Exception e){
				throw new ServletException(e);
			}
			resp.setStatus(HttpServletResponse.SC_OK);
		} else{
			try(InputStream is = getClass().getResourceAsStream("/webconf.html")){
				StreamUtil.transfer(is, resp.getOutputStream());
			}
		}
	}

	private static final long serialVersionUID = 5876028337554013784L;
}
