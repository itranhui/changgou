package com.changgou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @Author：Mr.ran &Date：2019/8/27 16:04
 * <p>
 * @Description：
 */
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})//禁止加载DataSource的创建
@EnableEurekaClient
public class FileApplication {
    public static void main(String[] args) {
        SpringApplication.run(FileApplication.class);
    }
}