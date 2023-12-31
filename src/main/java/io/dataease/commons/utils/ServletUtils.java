package io.dataease.commons.utils;

import io.dataease.commons.constants.AuthConstants;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletUtils {

    //这个方法是用来获取HttpServletRequest对象的,HttpServletRequest对象中包含了请求的所有信息
    public static HttpServletRequest request(){
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        return request;
    }


    //这个方法是用来获取HttpServletResponse对象的,HttpServletResponse对象中包含了响应的所有信息
    public static HttpServletResponse response(){
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = servletRequestAttributes.getResponse();
        return response;
    }


    public static void setToken(String token){
        //获取HttpServletResponse对象
        HttpServletResponse httpServletResponse = response();
        //添加响应头，表明Authorization可以作为请求头暴露给客户端
        httpServletResponse.addHeader("Access-Control-Expose-Headers", "Authorization");
        httpServletResponse.setHeader(AuthConstants.TOKEN_KEY, token);
    }

    public static String getToken(){
        //获取HttpServletRequest对象
        HttpServletRequest request = request();
        //从请求头中获取token
        String token = request.getHeader(AuthConstants.TOKEN_KEY);
        return token;
    }




}
