package com.changgou;


import com.changgou.entity.IdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @Author：Mr.ran &Date：2019/8/25 15:18
 * @MapperScan 是tk.mybatis.spring.annotation包下的，用于扫描Mapper接口 ,使用通用mapper
 * @Description：
 */
@SpringBootApplication
@EnableEurekaClient//表是 eureka的客户端(服务提供者，和服务消费者都是写这个注解)
@MapperScan(value = "com.changgou.goods.dao")
public class GoodsApplication {
    //启动类 19081
    public static void main(String[] args) {
        SpringApplication.run(GoodsApplication.class,args);
    }

    @Bean
    public IdWorker idWorker(){
        return new IdWorker();
    }
}
