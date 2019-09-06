package com.franklin.myspring.controller;

import com.franklin.myspring.annotations.MyController;
import com.franklin.myspring.annotations.MyRequestMapping;
import com.franklin.myspring.annotations.MyRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author 叶俊晖
 * @date 2019/9/5 0005 17:50
 */
@MyController
public class NoMappingController {

    @MyRequestMapping("/abc")
    public void query(
            HttpServletRequest request,
            HttpServletResponse response,
            @MyRequestParam("name")String name,
            @MyRequestParam("age")String age){
        try {
            PrintWriter writer = response.getWriter();
            writer.write("passed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
