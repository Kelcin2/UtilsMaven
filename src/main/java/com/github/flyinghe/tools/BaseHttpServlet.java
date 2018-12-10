package com.github.flyinghe.tools;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 该类用于其他Servlet继承，这里只重写了HttpServlet的service方法，
 * 用于决定调用request里method参数指定的方法，调用的方法必须写在了其子类中。
 * 
 * @author Flying
 * 
 */
public abstract class BaseHttpServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		/*
		 * 1. 获取参数，用来识别用户想请求的方法 2. 然后判断是否哪一个方法，是哪一个我们就调用哪一个
		 */
		String methodName = req.getParameter("method");

		if (methodName == null || methodName.trim().isEmpty()) {
			throw new RuntimeException(
					"There is no parameter of \"method\" in request so CAN NOT CALL the method!");
		}

		/*
		 * 得到方法名称，是否可通过反射来调用方法？ 1. 得到方法名，通过方法名再得到Method类的对象！ *
		 * 需要得到Class，然后调用它的方法进行查询！得到Method * 我们要查询的是当前类的方法，所以我们需要得到当前类的Class
		 */
		Class<? extends BaseHttpServlet> c = this.getClass();// 得到当前类的class对象
		Method method = null;
		try {
			method = c.getMethod(methodName, HttpServletRequest.class,
					HttpServletResponse.class);
			// 调用指定方法
			method.invoke(this, req, resp);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(
					"The method you called："
							+ methodName
							+ "(HttpServletRequest,HttpServletResponse)，NOT EXIST or SOMETHING WRONG HAPPENED INSIDE OF THE METHOD！");
		}
	}

}
