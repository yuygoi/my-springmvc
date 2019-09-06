package com.franklin.myspring.controller;

import com.franklin.myspring.annotations.MyAutowired;
import com.franklin.myspring.annotations.MyController;
import com.franklin.myspring.annotations.MyRequestMapping;
import com.franklin.myspring.annotations.MyRequestParam;
import com.franklin.myspring.service.TestService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author 叶俊晖
 * @date 2019/9/5 0005 10:26
 */
@MyController
@MyRequestMapping("/my-spring-mvc/test")
public class TestController {

    @MyAutowired
    private TestService testService;

    @MyRequestMapping("/test")
    public void query(
            HttpServletRequest request,
            HttpServletResponse response,
            @MyRequestParam("name")String name,
            @MyRequestParam("age")String age){
        String query = testService.query(name, age);
        try {
            PrintWriter writer = response.getWriter();
            writer.write(query);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
