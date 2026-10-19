package org.example.khkim;

import java.util.List;

/**
 * [3강] 상속 / 객체지향
 *
 * 1. 객체지향(OOP) 4가지 핵심
 *    캡슐화  데이터(private 필드)를 숨기고 메서드로만 다루게 한다.          (2강)
 *    상속    부모 class 의 필드/메서드를 자식이 물려받는다.  extends
 *    다형성  부모 타입 변수로 여러 자식 객체를 다룬다. 같은 호출, 다른 동작.
 *    추상화  공통된 "무엇을 하는가"만 정의하고 "어떻게"는 자식에게 맡긴다. abstract / interface
 *
 * 2. 상속 (extends)
 *    - "자식 is a 부모" 관계일 때만 쓴다. (Dog is an Animal O / Car is an Engine X -> 이건 포함관계)
 *    - 자바는 클래스 단일 상속: 부모는 하나만.
 *    - super(...)  부모 생성자 호출 (자식 생성자 첫 줄).  super.메서드()  부모 메서드 호출.
 *    - 오버라이딩(@Override): 부모 메서드를 자식이 다시 정의. 시그니처가 같아야 한다.
 *      (오버로딩 = 같은 이름, 매개변수 다르게. 완전히 다른 개념이므로 혼동 주의)
 *    - 모든 클래스의 최상위 부모는 Object (toString, equals, hashCode 가 여기서 온다).
 *
 * 3. 다형성
 *    - 부모타입 변수 = 자식객체 가능.   Animal a = new Dog();
 *    - 실행되는 메서드는 "변수 타입"이 아니라 "실제 객체"의 것 (동적 바인딩).
 *    - 그래서 List<Animal> 하나에 Dog, Cat 을 섞어 담고 같은 방식으로 호출할 수 있다.
 *
 * 4. abstract class 와 interface
 *    abstract class : 미완성 클래스. new 불가. 공통 필드/구현 + 추상 메서드(몸통 없음)를 함께 가짐.
 *    interface      : "이런 기능을 제공한다"는 약속. 여러 개 implements 가능.
 *                     관계가 is-a 가 아니라 can-do 일 때 (Swimmer 처럼 능력).
 *    - 스프링에서 Service 인터페이스 + ServiceImpl 구현체 패턴이 이 구조다.
 *
 * 5. final
 *    final class 는 상속 불가(String 이 그 예), final 메서드는 오버라이딩 불가.
 */
public interface Lesson03Inheritance {

    // 추상 부모: 공통 필드 + 반드시 구현해야 할 추상 메서드
    static abstract class Animal {
        protected String name;                 // protected: 자식이 접근 가능

        Animal(String name) {
            this.name = name;
        }

        String sound(){
            return sound();
        };               // 몸통 없음: 자식이 반드시 구현

        void introduce() {                     // 공통 동작은 부모에 한 번만 작성
            System.out.println(name + " : " + sound());
        }
    }

    // 능력을 나타내는 interface
    interface Swimmer {
        void swim();
    }

    static class Dog extends Animal implements Swimmer {
        Dog(String name) {
            super(name);                       // 부모 생성자 호출
        }

        @Override
        String sound() {
            return "멍멍";
        }

        @Override
        public void swim() {                   // interface 메서드는 public 이어야 함
            System.out.println(name + " 수영중");
        }
    }

    static class Cat extends Animal {
        Cat(String name) {
            super(name);
        }

        @Override
        String sound() {
            return "야옹";
        }

        @Override
        void introduce() {                     // 오버라이딩 + 부모 기능 재사용
            super.introduce();
            System.out.println("  (고양이는 도도함)");
        }
    }

    public static void main(String[] args) {
        // Animal x = new Animal("x");         // 컴파일 에러: abstract 는 new 불가

        // 다형성: 부모 타입 리스트에 서로 다른 자식을 담는다
        List<Animal> animals = List.of(new Dog("바둑이"), new Cat("나비"));
        for (Animal a : animals) {
            a.introduce();                     // 변수 타입은 Animal 이지만 실제 객체의 sound() 가 실행됨
        }

        // 자식만의 기능을 쓰려면 실제 타입 확인 후 형변환
        for (Animal a : animals) {
            if (a instanceof Swimmer s) {      // Java 16+ 패턴 매칭: 검사 + 형변환 한 번에
                s.swim();
            }
        }

        // interface 타입으로도 다룰 수 있다
        Swimmer swimmer = new Dog("초코");
        swimmer.swim();
        // swimmer.sound();                    // 컴파일 에러: Swimmer 타입에는 sound() 가 없음

        // 모든 클래스는 Object 를 상속
        Animal d = new Dog("루피");
        System.out.println(d instanceof Object);                // true
        System.out.println(d.toString().startsWith("org.example.khkim.Lesson03Inheritance$Dog"));
    }
}
