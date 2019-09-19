package com.changgou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @Author：Mr.ran &Date：2019/8/25 15:05
 * <p>
 * @Description：
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(EurekaApplication.class, args);
        for (String beanDefinitionName : run.getBeanDefinitionNames()) {

            System.out.println(beanDefinitionName);

        }
    }

}
