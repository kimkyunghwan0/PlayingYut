package org.example.khkim;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * [5강] 메모리 (JVM, Stack / Heap, 참조, GC)
 *
 * 1. JVM 메모리 구조 (실무에서 알아야 할 3영역)
 *    Stack   : 메서드를 호출할 때마다 쌓이는 "작업 공간". 지역변수, 매개변수가 여기.
 *              메서드가 끝나면 자동으로 사라진다. 스레드마다 따로 가진다.
 *              너무 깊게 호출되면(무한 재귀) StackOverflowError.
 *    Heap    : new 로 만든 "객체"가 저장되는 큰 공간. 모든 스레드가 공유. GC 가 정리한다.
 *              가득 차면 OutOfMemoryError.
 *    Metaspace(Method Area) : 클래스 정보, static 변수. 프로그램이 끝날 때까지 남는다.
 *
 * 2. 변수와 객체는 다른 것
 *    Account a = new Account();
 *      - a       : Stack 에 있는 "주소값(참조)"
 *      - new ... : Heap 에 만들어진 실제 객체
 *    - int 같은 기본형은 값 자체가 Stack 에, 참조형은 주소가 Stack 에 / 객체가 Heap 에.
 *    - b = a 는 객체를 복사하는 게 아니라 "주소만 복사". 둘이 같은 객체를 본다. (2강에서 확인)
 *
 * 3. 자바는 항상 값 전달(pass by value)
 *    - 메서드에 넘길 때 "변수의 값"이 복사된다. 참조형이면 "주소값"이 복사된다.
 *    - 그래서 메서드 안에서 객체의 내용을 바꾸면 밖에도 반영되지만,
 *      매개변수에 새 객체를 대입해도 밖의 변수는 안 바뀐다.
 *
 * 4. GC (Garbage Collection)
 *    - 어디서도 참조하지 않는 객체를 JVM 이 알아서 지운다. 개발자가 free 하지 않는다.
 *    - 그렇다면 메모리 누수는? "참조가 남아 있으면" 못 지운다.
 *      대표 원인: static 컬렉션(List, Map)에 계속 add 만 하고 remove 안 함, 캐시, 닫지 않은 리소스.
 *
 * 5. String 은 불변(immutable)
 *    - 한 번 만든 문자열은 안 바뀐다. s += "a" 는 "새 객체를 만들고 참조만 교체".
 *    - 반복문 안에서 문자열을 계속 이어 붙이면 객체가 쌓여 느려진다 -> StringBuilder.
 *
 * 6. equals / hashCode
 *    - == 는 "같은 객체(주소)인가", equals 는 "내용이 같은가".
 *    - equals 를 재정의하면 hashCode 도 반드시 같이. 안 하면 HashSet/HashMap 에서 중복이 안 걸러진다.
 *
 * 7. null 과 NullPointerException
 *    - 참조가 아무 객체도 가리키지 않는 상태. 그 참조로 메서드를 호출하면 NPE.
 *    - 실무에서 가장 흔한 예외. 방어: null 체크, Objects.requireNonNull, Optional.
 */
public class Lesson05Memory {

    static class Box {
        int value;
        Box(int value) { this.value = value; }
    }

    // 값 전달 확인용
    static void change(Box box, int number) {
        box.value = 99;         // 주소가 가리키는 객체 내용 변경 -> 밖에도 반영
        number = 99;            // 복사본만 바뀜 -> 밖에 영향 없음
        box = new Box(-1);      // 복사된 주소를 교체 -> 밖의 변수는 그대로
    }

    // equals / hashCode 를 재정의한 클래스와 안 한 클래스
    static class GoodKey {
        final String id;
        GoodKey(String id) { this.id = id; }
        @Override public boolean equals(Object o) {
            return o instanceof GoodKey g && Objects.equals(id, g.id);
        }
        @Override public int hashCode() { return Objects.hash(id); }
    }

    static class BadKey {
        final String id;
        BadKey(String id) { this.id = id; }
    }

    // 메모리 누수 예: static 컬렉션은 프로그램이 끝날 때까지 참조를 잡고 있다
    static final List<byte[]> LEAK = new ArrayList<>();

    static int depth = 0;
    static void recurse() {     // 종료 조건 없는 재귀 -> Stack 이 가득 참
        depth++;
        recurse();
    }

    public static void main(String[] args) {
        // ---- 값 전달 ----
        Box box = new Box(1);
        int number = 1;
        change(box, number);
        System.out.println("box.value = " + box.value + " (객체 내용은 바뀜)");   // 99
        System.out.println("number    = " + number + " (기본형은 그대로)");         // 1

        // ---- 참조 복사 ----
        Box x = new Box(10);
        Box y = x;              // 주소 복사
        y.value = 20;
        System.out.println("x.value = " + x.value);                              // 20

        // ---- String 불변 ----
        String s1 = "abc";
        String s2 = s1;
        s1 += "d";              // s1 이 새 객체를 가리키게 됨. 기존 "abc" 는 그대로
        System.out.println("s1 = " + s1 + ", s2 = " + s2);                        // abcd, abc

        StringBuilder sb = new StringBuilder();   // 이어붙이기는 이걸로
        for (int i = 0; i < 5; i++) sb.append(i);
        System.out.println("sb = " + sb);

        // ---- equals / hashCode ----
        Set<GoodKey> good = new HashSet<>();
        good.add(new GoodKey("A"));
        good.add(new GoodKey("A"));
        Set<BadKey> bad = new HashSet<>();
        bad.add(new BadKey("A"));
        bad.add(new BadKey("A"));
        System.out.println("재정의 O : " + good.size() + "개");      // 1
        System.out.println("재정의 X : " + bad.size() + "개");       // 2 (같은 A 인데 중복)

        // ---- null / NPE ----
        String name = null;
        try {
            System.out.println(name.length());
        } catch (NullPointerException e) {
            System.out.println("NPE 발생: null 참조로 메서드 호출");
        }

        // ---- StackOverflowError ----
        try {
            recurse();
        } catch (StackOverflowError e) {
            System.out.println("StackOverflowError, 재귀 깊이 약 " + (depth > 1000 ? "1000+" : depth));
        }

        // ---- 메모리 확인 / 누수 ----
        Runtime rt = Runtime.getRuntime();
        System.out.println("최대 Heap = " + rt.maxMemory() / 1024 / 1024 + "MB");
        long before = rt.totalMemory() - rt.freeMemory();
        for (int i = 0; i < 20; i++) LEAK.add(new byte[1024 * 1024]);   // 1MB x 20, static 이 잡고 있음
        long after = rt.totalMemory() - rt.freeMemory();
        System.out.println("누수 시뮬레이션 후 사용량 증가 >= 20MB : " + ((after - before) >= 20L * 1024 * 1024));
        LEAK.clear();           // 참조를 끊어야 GC 대상이 된다
        // 힙 크기 지정: java -Xms256m -Xmx512m  (스프링 부트 서버 실행 시 자주 조정)
    }
}
