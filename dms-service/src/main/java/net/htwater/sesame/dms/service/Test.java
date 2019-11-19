package net.htwater.sesame.dms.service;

import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;

/**
 * @author Jokki
 */
public class Test {
    public static void main(String[] args) {
        Flux.range(1, 100).buffer(20).subscribe(System.out::println);
        //Flux.interval(Duration.ofMillis(1000)).buffer(Duration.ofMillis(2001)).take(5).toStream().forEach(System.out::println);
        Flux.range(1, 10).filter(i -> i % 2 == 0).subscribe(System.out::println);
        Flux.range(1, 100).window(20).subscribe(System.out::println);
        //Flux.interval(Duration.ofMillis(1000)).window(Duration.ofMillis(2001)).take(2).toStream().forEach(System.out::println);
        Flux.just("a", "b")
                .zipWith(Flux.just("c", "d"))
                .subscribe(System.out::println);
        Flux.just("a", "b")
                .zipWith(Flux.just("c", "d"), (s1, s2) -> String.format("%s-%s", s1, s2))
                .subscribe(System.out::println);
        Flux.range(1, 1000).take(10).subscribe(System.out::println);
        Flux.range(1, 1000).takeLast(10).subscribe(System.out::println);
        Flux.range(1, 1000).takeWhile(i -> i < 10).subscribe(System.out::println);
        Flux.range(1, 100).reduce((x, y) -> x + y).subscribe(System.out::println);
        Flux.range(1, 100).reduceWith(() -> 100, (x, y) -> x + y).subscribe(System.out::println);
        Flux.merge(Flux.interval(Duration.ofMillis(0), Duration.ofMillis(100)).take(5), Flux.interval(Duration.ofMillis(50), Duration.ofMillis(100)).take(5))
                .toStream()
                .forEach(System.out::println);
        Flux.mergeSequential(Flux.interval(Duration.ofMillis(0), Duration.ofMillis(100)).take(5), Flux.interval(Duration.ofMillis(50), Duration.ofMillis(100)).take(5))
                .toStream()
                .forEach(System.out::println);
        System.out.println("---------------");
        Flux.just(5, 10)
                .flatMap(x -> Flux.interval(Duration.ofMillis(x * 10), Duration.ofMillis(100)).take(x))
                .toStream()
                .forEach(System.out::println);
        System.out.println("---------------");
        Flux.just(5, 10)
                .flatMapSequential(x -> Flux.interval(Duration.ofMillis(x * 10), Duration.ofMillis(100)).take(x))
                .toStream()
                .forEach(System.out::println);
        System.out.println("---------------");
        Flux.just(5, 10)
                .concatMap(x -> Flux.interval(Duration.ofMillis(x * 10), Duration.ofMillis(100)).take(x))
                .toStream()
                .forEach(System.out::println);
        System.out.println("---------------");
        Flux.combineLatest(
                Arrays::toString,
                Flux.interval(Duration.ofMillis(100)).take(5),
                Flux.interval(Duration.ofMillis(100)).take(5)
                ).toStream().forEach(System.out::println);
        System.out.println("---------------");
        Flux.combineLatest(
                Arrays::toString,
                Flux.interval(Duration.ofMillis(100)).take(5),
                Flux.interval(Duration.ofMillis(50), Duration.ofMillis(100)).take(5)
        ).toStream().forEach(System.out::println);
    }
}
