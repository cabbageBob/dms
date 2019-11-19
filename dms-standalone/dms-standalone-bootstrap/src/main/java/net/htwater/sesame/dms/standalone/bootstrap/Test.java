package net.htwater.sesame.dms.standalone.bootstrap;

import java.io.File;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Comparator.comparingInt;

/**
 * @author Jokki
 * @date Created in 上午8:28 19-1-14
 * @modified By
 */
public class Test {
    private static final String VALUE = "0";

    private int a ;

    public static void main(String[] args) throws Exception {
        long t1 = System.currentTimeMillis();
        long sum = 0L;
        for (long i = 0; i <= Integer.MAX_VALUE; i++)
            sum += i;
        System.out.println(sum);
        System.out.println(System.currentTimeMillis() - t1);
       /* Map a = new WeakHashMap(1);
        Long b = 1L;
        Object o = new Object();
        try {
            a = null;
            a.toString();
        } catch (Exception e) {
            System.out.println("error");
            throw new Exception(e.getMessage());
        } finally {
            System.out.println(1);
        }*/
        Long a = 1L;

        List<Integer> in = new ArrayList<Integer>(3);
        in.add(3);
        in.add(1);
        in.add(2);
        Collections.sort(in);
        System.out.println(in);
        Comparator<Integer> comparator = comparingInt(value -> value);
        double b = 6.022_140_875e23;
        System.out.println(b);
        List<String> strings = new ArrayList<>();
        strings.sort(comparingInt(String::length));
        String[][] data = new String[][]{{"a", "b"}, {"c", "d"}, {"e", "f"}};
        //Stream<String[]>
        Stream<String[]> temp = Arrays.stream(data);
        //Stream<String>, GOOD!
        Stream<String> stringStream = temp.flatMap(Arrays::stream);
        //Stream<String> stream = stringStream.filter("a"::equals);
        File file = new File("/home/jokki/桌面/compute language/java");
        Arrays.asList(file.list()).forEach(System.out::println);
        final Instant now = Instant.now();
        Map<String, Object> map = new HashMap<>(2);
        map.put("1", "1");
        map.put("2", "2");
        System.out.println(map.keySet());
    }

    class MyException extends RuntimeException {
        MyException(String error) {
            super(error);
        }

    }
}
