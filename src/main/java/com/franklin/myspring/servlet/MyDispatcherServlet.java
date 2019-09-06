package com.franklin.myspring.servlet;

import com.franklin.myspring.annotations.*;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @author 叶俊晖
 * @date 2019/9/5 0005 10:33
 */
public class MyDispatcherServlet extends HttpServlet{

    private List<String> classNames = new ArrayList<String>();
    private Properties properties = new Properties();
    //IOC容器
    private Map<String,Object> beans = new HashMap<String,Object>();
    private Map<String,Method> handleMap = new HashMap<String,Method>();

    /**
     * IOC容器初始化
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        //加载配置文件
        doLoadConfig(config.getInitParameter("myapplicationContext"));
        basePackageScan(properties.getProperty("componentScan"));
        //实例化bean
        doInstance();
        //自动注入
        doAutowired();
        //映射关系
        doUrlMapping();
    }

    /**
     * 加载myspring配置文件application.properties
     * @param myapplicationContext
     */
    private void doLoadConfig(String myapplicationContext) {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(myapplicationContext);
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 映射路径对应的接口方法
     */
    private void doUrlMapping() {
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();
            if (clazz.isAnnotationPresent(MyController.class)){
                String path = "";
                if (clazz.isAnnotationPresent(MyRequestMapping.class)){
                    MyRequestMapping classMapping = clazz.getAnnotation(MyRequestMapping.class);
                    String pathOne = classMapping.value();
                    if (StringUtils.isNotBlank(pathOne)){
                        path = pathOne;
                    }
                }
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(MyRequestMapping.class)){
                        MyRequestMapping methodMapping = method.getAnnotation(MyRequestMapping.class);
                        String pathTwo = methodMapping.value();
                        if (StringUtils.isNotBlank(pathTwo)){
                            path += pathTwo;
                        }
                        handleMap.put(path,method);
                    }
                }
            }
        }
    }

    /**
     * 自动注入@Autowired
     */
    private void doAutowired() {
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();
            if (clazz.isAnnotationPresent(MyController.class)
                  ||
                clazz.isAnnotationPresent(MyService.class)
               ){
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    setBean(instance, field);
                }
            }
        }
    }

    /**
     * 注入bean
     * @param instance
     * @param field
     */
    private void setBean(Object instance, Field field) {
        if (field.isAnnotationPresent(MyAutowired.class)){
            MyAutowired autowired = field.getAnnotation(MyAutowired.class);
            String key = autowired.value();
            if (StringUtils.isBlank(key)){
                //根据类型注入
                Class<?> type = field.getType();
                for (Map.Entry<String, Object> entry : beans.entrySet()) {
                    Class<?>[] interfaces = entry.getValue().getClass().getInterfaces();
                    for (Class<?> inf : interfaces) {
                        if (type.isAssignableFrom(inf)){
                            key = entry.getKey();
                            break;
                        }
                    }
                    if (StringUtils.isNotBlank(key)){
                        break;
                    }
                }
            }
            Object bean = beans.get(key);
            field.setAccessible(true);
            try {
                field.set(instance,bean);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化bean对象到IOC容器中
     */
    private void doInstance() {
        for (String className : classNames) {
            String clazzName = className.replace(".class", "");
            try {
                Class<?> clazz = Class.forName(clazzName);
                if (clazz.isAnnotationPresent(MyController.class)){
                    //控制类
                    Object instance = clazz.newInstance();
                    String key = null;
                    if(clazz.isAnnotationPresent(MyRequestMapping.class)){
                        //有requestMapping的Controller
                        MyRequestMapping mapping = clazz.getAnnotation(MyRequestMapping.class);
                        key = mapping.value();
                    }
                    key = getBeanName(clazz, key);
                    beans.put(key,instance);
                }else if (clazz.isAnnotationPresent(MyService.class)){
                    //业务层
                    Object instance = clazz.newInstance();
                    MyService myService = clazz.getAnnotation(MyService.class);
                    String key = myService.value();
                    key = getBeanName(clazz,key);
                    beans.put(key,instance);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取bean的id
     * @param clazz
     * @param key
     * @return
     */
    private String getBeanName(Class<?> clazz, String key) {
        if (StringUtils.isBlank(key)){
            String simpleName = clazz.getSimpleName();
            String start = (simpleName.charAt(0) + "").toLowerCase();
            key = start + simpleName.substring(1);
        }
        return key;
    }

    /**
     * 模拟包扫描@ComponentScan("com.franklin.myspring")
     * componentScan
     * @param basePackage
     */
    private void basePackageScan(String basePackage) {
        URL url = this.getClass()
                .getClassLoader()
                .getResource("/" + basePackage.replaceAll("\\.", "/"));
        String urlFile = url.getFile();
        File file = new File(urlFile);
        String[] fileList = file.list();
        for (String path : fileList) {
            File filePath = new File(urlFile + path);
            if (filePath.isDirectory()){
                this.basePackageScan(basePackage + "." + path);
            }else{
                classNames.add(basePackage + "." + filePath.getName());
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath(); //工程名
        String path = uri.replace(contextPath, "");
        Method method = handleMap.get(path);
        if (method == null){
            //先找方法，没找到直接返回
            resp.getWriter().write("My Spring MVC : \n 404 NOT FOUND!");
            return;
        }

        Object bean = handlerMapping(path);
        if (bean == null){
            resp.getWriter().write("My Spring MVC : \n 404 NOT FOUND!");
            return;
        }
        Object[] args = getParameters(method, req, resp);
        handlerAdapter(method, bean, args);
    }

    /**
     * 模拟处理器适配器HandlerAdapter
     * @param method 请求路径匹配的方法
     * @param bean  请求路径匹配的bean实例
     * @param args  请求参数
     */
    private void handlerAdapter(Method method, Object bean, Object[] args) {
        try {
            method.invoke(bean,args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取请求参数
     * @param method
     * @param request
     * @param response
     * @return
     */
    private static Object[] getParameters(Method method,HttpServletRequest request,HttpServletResponse response){
        Class<?>[] parameterTypes = method.getParameterTypes();

        Object[] args = new Object[parameterTypes.length];
        int argsIndex = 0;
        int index = 0;
        for (Class<?> parameterType : parameterTypes) {
            if (ServletRequest.class.isAssignableFrom(parameterType)){
                //判断参数中是否有HttpServletRequest
                args[argsIndex++] = request;
            }
            if (ServletResponse.class.isAssignableFrom(parameterType)){
                //判断参数中是否有HttpServletResponse
                args[argsIndex++] = response;
            }
            if (Map.class.isAssignableFrom(parameterType)){
                //判断参数中是否有Map,Model,ModelMap
                try {
                    args[argsIndex++] = parameterType.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //判断是否有MyRequestParam注解的变量
            Annotation[] paramAnns = method.getParameterAnnotations()[index];
            for (Annotation paramAnn : paramAnns) {
                if (MyRequestParam.class.isAssignableFrom(paramAnn.getClass())){
                    MyRequestParam myRequestParam = (MyRequestParam) paramAnn;
                    args[argsIndex++] = request.getParameter(myRequestParam.value());
                }
            }
            index++;
        }
        return args;
    }

    /**
     * 模拟处理器映射器HandlerMapping
     * 寻找路径对应的Handler（Controller）
     * @param path
     * @return
     */
    private Object handlerMapping(String path){
        String[] split = path.split("/");
        StringBuilder beanName = new StringBuilder();
        if (split != null && split.length>1){
            for (int i = 1; i < split.length; i++) {
               beanName.append("/").append(split[i]);
                Object bean = beans.get(beanName.toString());
                if (bean != null){
                    return bean;
                }
            }
        }
        //寻找一轮没找到，直接通过路径找方法再反射找bean
        Method method = handleMap.get(path);
        Class<?> methodClass = method.getDeclaringClass();
        String name = getBeanName(methodClass, null);
        Object bean = beans.get(name);
        if (bean != null){
            return bean;
        }
        return null;
    }
}
