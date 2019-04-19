package org.smart.framework.util;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaScriptUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(JavaScriptUtil.class);
	private static ScriptEngineManager mgr = new ScriptEngineManager(); 
	private static ScriptEngine engine = mgr.getEngineByExtension("js"); 
	
	public static Object execute(String expression, Number... args) {
		for (int i = 0; i < args.length; i++) {
			String argTmp = "x" + (i+1);
			expression = expression.replace(argTmp, args[i].toString());
		}
		try {
			return engine.eval(expression);
		} catch (ScriptException e) {
			LOGGER.error("{}",e);
			return null;
		}
	}
	
	public static void main(String[] args) {
		System.out.println(execute("x1+2", 1));
	}
	
}
