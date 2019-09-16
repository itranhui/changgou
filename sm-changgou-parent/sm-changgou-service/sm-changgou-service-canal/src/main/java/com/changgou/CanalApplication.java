package com.changgou;
import com.xpand.starter.canal.annotation.EnableCanalClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Author：Mr.ran &Date：2019/8/30 17:18
 * <p>
 * @Description：
 */
@EnableEurekaClient//注册中心Eureka客户端
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableCanalClient//Canal客户端
@EnableFeignClients//开启feign注解
public class CanalApplication {

    public static void main(String[] args) {
        SpringApplication.run(CanalApplication.class,args);
    }
}
