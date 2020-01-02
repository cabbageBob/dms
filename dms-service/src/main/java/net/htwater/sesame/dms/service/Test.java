package net.htwater.sesame.dms.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * @author Jokki
 */
public class Test {
    public static void main(String[] args) {
        /*Flux.range(1, 100).buffer(20).subscribe(System.out::println);
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
        ).toStream().forEach(System.out::println);*/

        /*Flux.create(sink -> {
            sink.next(1 + Thread.currentThread().getName());
            sink.complete();
        })
                .publishOn(Schedulers.single())
                .map(x -> 2 + String.format("[%s] %s", Thread.currentThread().getName(), x))
                .publishOn(Schedulers.elastic())
                .map(x -> 3 + String.format("[%s] %s", Thread.currentThread().getName(), x))
                .subscribeOn(Schedulers.parallel())
                .toStream()
                .forEach(System.out::println);*/
        /*Flux.just("1")
                .publishOn(Schedulers.single())
                .doOnNext(s -> {
                    System.out.println("[a] Thread name: " + Thread.currentThread().getName());
                })
                .subscribeOn(Schedulers.elastic())
                .subscribe(s ->
                        System.out.println("[subscribe] Thread name: " + Thread.currentThread().getName()));*/
       /* Flux.just("tom")
                .map(s -> {
                    System.out.println("[map] Thread name: " + Thread.currentThread().getName());
                    return s.concat("@mail.com");
                })
                .publishOn(Schedulers.newElastic("thread-publishOn"))
                .filter(s -> {
                    System.out.println("[filter] Thread name: " + Thread.currentThread().getName());
                    return s.startsWith("t");
                })
                .subscribeOn(Schedulers.newElastic("thread-subscribeOn"))
                .subscribe(s -> {
                    System.out.println("[subscribe] Thread name: " + Thread.currentThread().getName());
                    System.out.println(s);
                });*/

        Flux.just("a")
                .subscribeOn(Schedulers.elastic())
                .subscribe(s -> System.out.println(s));

        Mono.just("a")
                .zipWith(Mono.just("c"), (s1, s2) -> String.format("%s-%s", s1, s2))
                .subscribe(System.out::println);

    }
}
