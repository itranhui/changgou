package com.changgou.domain;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @Author：Mr.ran &Date：2019/9/18 10:21
 * <p>
 * @Description：SpringBoot启动的时候执行，可以做一些初始化操作
 */
@Component
public class Init implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("springBoot启动的时候执行了的方法");
    }
}
