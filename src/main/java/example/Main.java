package example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
})
@MapperScan("example.mapper")
public class Main {
    public static void main(String[] args) {
        // 解决 java.lang.IllegalArgumentException: Invalid value type for attribute 'factoryBeanObjectType': java.lang.String
        // 这是一个已知的 MyBatis-Plus 3.5.3.1 和 Spring Boot 3.2+ 的兼容性问题
        // 或者是 langchain4j 的 scanner 导致的冲突，尝试禁用 langchain4j 的自动扫描（如果不需要）或者升级版本
        // 但这里我们先尝试设置系统属性来规避一些潜在的 JNA 或其他库的问题（虽然可能不相关）
        
        SpringApplication.run(Main.class, args);
        System.out.println("Application started successfully!");
    }
}
