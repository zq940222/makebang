package com.makebang;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 码客邦启动类
 *
 * @author MakeBang
 */
@SpringBootApplication
@MapperScan("com.makebang.repository")
@EnableTransactionManagement
@EnableAsync
public class MakebangApplication {

    public static void main(String[] args) {
        SpringApplication.run(MakebangApplication.class, args);
        System.out.println("""

            ███╗   ███╗ █████╗ ██╗  ██╗███████╗██████╗  █████╗ ███╗   ██╗ ██████╗
            ████╗ ████║██╔══██╗██║ ██╔╝██╔════╝██╔══██╗██╔══██╗████╗  ██║██╔════╝
            ██╔████╔██║███████║█████╔╝ █████╗  ██████╔╝███████║██╔██╗ ██║██║  ███╗
            ██║╚██╔╝██║██╔══██║██╔═██╗ ██╔══╝  ██╔══██╗██╔══██║██║╚██╗██║██║   ██║
            ██║ ╚═╝ ██║██║  ██║██║  ██╗███████╗██████╔╝██║  ██║██║ ╚████║╚██████╔╝
            ╚═╝     ╚═╝╚═╝  ╚═╝╚═╝  ╚═╝╚══════╝╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═══╝ ╚═════╝

            码客邦服务启动成功!
            """);
    }
}
