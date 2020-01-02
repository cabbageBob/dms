package net.htwater.sesame.dms.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableScheduling
public class DmsApplication {

    public static void main(String[] args){
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        SpringApplication.run(DmsApplication.class, args);
    }
}
