package io.dataease.plugins.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SpringContextUtil implements ApplicationContextAware {

    //获取bean工厂，用来实现动态注入bean
    //不能使用其他类加载器加载bean
    //否则会出现异常:类未找到，类未定义
    public static DefaultListableBeanFactory getBeanFactory(){
        return (DefaultListableBeanFactory) getApplicationContext().getAutowireCapableBeanFactory();
    }



    //获取所有的bean
    public static List<Map<String, Object>> getAllBean() {


        List<Map<String, Object>> list = new ArrayList<>();


        String[] beans = getApplicationContext()
                .getBeanDefinitionNames();

        for (String beanName : beans) {
            Class<?> beanType = getApplicationContext()
                    .getType(beanName);

            Map<String, Object> map = new HashMap<>();

            map.put("BeanName", beanName);
            map.put("beanType", beanType);
            map.put("package", beanType.getPackage());
            list.add(map);

        }

        return list;
    }




    /**
     * 上下文对象实例
     */
    private static ApplicationContext applicationContext;


    //实现ApplicationContextAware接口的回调方法，设置上下文环境
    //这个方法会在Spring启动的时候被自动调用，这是Spring的一个特性，叫做回调方法
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 获取applicationContext
     *  applicationContext表示Spring容器，它负责创建对象，装配对象，管理对象和销毁对象
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }


    //通过name获取 Bean.
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }
    //通过class获取Bean.
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }
    //通过name,和class返回指定的Bean
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }
}
