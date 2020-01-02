package net.htwater.sesame.dms.standalone.bootstrap;

import io.netty.util.internal.ThreadLocalRandom;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * @author Jokki
 * @date Created in 下午6:46 19-1-10
 * @modified By
 */
@SpringBootApplication
@RestController
public class Bootstrap {

    public static void main(String[] args) {
        SpringApplication.run(Bootstrap.class, args);
    }

    @GetMapping("/random")
    public Flux<ServerSentEvent<Integer>> random() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(seq -> ServerSentEvent.<Integer>builder()
                        .event("random")
                        .id(seq.toString())
                        .data(ThreadLocalRandom.current().nextInt())
                        .build()
                );
    }
}
