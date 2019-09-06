package com.franklin.myspring.service.impl;

import com.franklin.myspring.annotations.MyService;
import com.franklin.myspring.service.TestService;

/**
 * @author 叶俊晖
 * @date 2019/9/5 0005 10:24
 */
@MyService
public class TestServiceImpl implements TestService {
    public String query(String name, String age) {
        return "name = " + name + " , age = " + age;
    }
}
