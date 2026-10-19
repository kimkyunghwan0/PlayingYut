package org.example.khkim;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * [6강] 스프링 DI (Dependency Injection, 의존성 주입)
 *
 * 1. 문제: 객체가 다른 객체를 직접 new 하면
 *      class OrderService { private PaymentMethod pay = new CardPayment(); }
 *    - OrderService 가 CardPayment 에 "단단히 묶인다"(강결합). 계좌이체로 바꾸려면 OrderService 를 고쳐야 한다.
 *    - 테스트할 때 가짜 결제를 끼워 넣을 수 없다.
 *
 * 2. 해결: 필요한 객체를 "밖에서 넣어준다"(주입)
 *      class OrderService {
 *          private final PaymentMethod pay;
 *          OrderService(PaymentMethod pay) { this.pay = pay; }   // 생성자로 받기만 한다
 *      }
 *    - OrderService 는 어떤 구현체인지 모른다. 인터페이스(약속)만 안다. (3강의 interface)
 *    - 누가 넣어주나? 스프링 컨테이너가.
 *
 * 3. 용어
 *    IoC 컨테이너 : 객체(빈)를 만들고 연결해 주는 스프링의 핵심. ApplicationContext.
 *    Bean         : 컨테이너가 관리하는 객체.
 *    IoC          : 제어의 역전. 객체 생성/연결을 내 코드가 아니라 컨테이너가 쥔다.
 *
 * 4. 빈 등록
 *    @Component                 일반 빈
 *    @Service / @Repository     역할을 표시하는 @Component (Service=비즈니스 로직, Repository=DB 접근)
 *    @SpringBootApplication 이 붙은 클래스의 패키지 이하를 스캔해서 자동 등록한다.
 *    (그래서 KhkimApplication 과 같은 org.example.khkim 아래에 클래스를 둬야 한다)
 *
 * 5. 주입 방법 3가지
 *    생성자 주입 (권장) : final 필드 + 생성자. 생성자가 하나면 @Autowired 생략 가능.
 *                         빠뜨리면 컴파일/기동 시점에 바로 드러나고, 테스트에서 직접 new 하기 쉽다.
 *    필드 주입          : 필드에 @Autowired. 짧지만 테스트/불변성에 불리해 실무에서 지양.
 *    세터 주입          : 선택적 의존성일 때.
 *
 * 6. 같은 타입의 구현체가 여러 개일 때
 *    - 어떤 걸 넣을지 몰라 기동 실패(NoUniqueBeanDefinitionException).
 *    - @Primary : 기본값으로 지정.   @Qualifier("이름") : 이름으로 명시.
 *
 * 7. 싱글톤
 *    - 빈은 기본적으로 컨테이너에 하나만 만들어 모두가 공유한다.
 *    - 그래서 빈 안에 요청별 상태를 필드로 두면 안 된다. (여러 요청이 같은 객체를 동시에 쓴다)
 *
 * 8. 흐름 (실제 스프링 서버의 구조)
 *      Controller  ->  Service  ->  Repository  ->  DB
 *      각 계층이 다음 계층을 생성자로 주입받는다. (아래 예제가 그 축소판)
 *
 * 이 파일은 스프링 부트 전체를 띄우지 않고, 컨테이너(AnnotationConfigApplicationContext)에
 * 필요한 클래스만 직접 등록해서 DI 원리만 보여준다.
 */
public class Lesson06SpringDi {

    // ---- 약속(interface) ----
    interface PaymentMethod {
        String pay(int amount);
    }

    // ---- 구현체 2개: 같은 타입이 둘 -> 선택 필요 ----
    @Component
    @Primary                                        // 특별한 지정이 없으면 이게 주입됨
    static class CardPayment implements PaymentMethod {
        public String pay(int amount) { return "카드 결제 " + amount; }
    }

    @Component("bank")                              // 빈 이름 지정
    static class BankPayment implements PaymentMethod {
        public String pay(int amount) { return "계좌이체 " + amount; }
    }

    // ---- Repository ----
    @Repository
    static class OrderRepository {
        private final List<String> saved = new ArrayList<>();
        void save(String order) { saved.add(order); }
        List<String> findAll() { return saved; }
    }

    // ---- Service: 의존성 3개를 생성자로 주입받는다 ----
    @Service
    static class OrderService {
        private final OrderRepository repository;
        private final PaymentMethod defaultPay;     // @Primary -> CardPayment
        private final PaymentMethod bankPay;        // @Qualifier -> BankPayment

        OrderService(OrderRepository repository,
                     PaymentMethod defaultPay,
                     @Qualifier("bank") PaymentMethod bankPay) {   // 생성자가 하나라 @Autowired 생략
            this.repository = repository;
            this.defaultPay = defaultPay;
            this.bankPay = bankPay;
        }

        void order(String item, int amount, boolean byBank) {
            String result = (byBank ? bankPay : defaultPay).pay(amount);
            repository.save(item + " <- " + result);
        }

        List<String> list() { return repository.findAll(); }
    }

    // ---- DI 없이 직접 new 한 경우 (비교용) ----
    static class TightOrderService {
        private final PaymentMethod pay = new CardPayment();   // 구현체에 고정. 바꾸려면 이 코드를 수정해야 함
        String order(int amount) { return pay.pay(amount); }
    }

    public static void main(String[] args) {
        // 컨테이너에 클래스 등록 -> 컨테이너가 의존 관계를 분석해 알아서 만들고 연결
        try (var ctx = new AnnotationConfigApplicationContext(
                CardPayment.class, BankPayment.class, OrderRepository.class, OrderService.class)) {

            OrderService service = ctx.getBean(OrderService.class);   // 완성된 객체를 꺼내 쓴다
            service.order("책", 1000, false);
            service.order("펜", 200, true);
            service.list().forEach(System.out::println);

            // 싱글톤: 몇 번을 꺼내도 같은 객체
            System.out.println("같은 객체? " + (service == ctx.getBean(OrderService.class)));

            // 등록된 빈 이름 확인
            System.out.println("빈 이름들: " + String.join(", ", ctx.getBeanDefinitionNames()).replaceAll("org\\.springframework\\.[^,]*,? ?", ""));
        }

        // ---- 테스트 관점: DI 덕분에 가짜를 끼울 수 있다 ----
        PaymentMethod fake = amount -> "가짜 결제 " + amount;
        OrderService testTarget = new OrderService(new OrderRepository(), fake, fake);   // 스프링 없이 직접 new
        testTarget.order("테스트", 1, false);
        System.out.println(testTarget.list());

        System.out.println(new TightOrderService().order(500));   // 카드로 고정, 가짜로 못 바꿈
    }
}
