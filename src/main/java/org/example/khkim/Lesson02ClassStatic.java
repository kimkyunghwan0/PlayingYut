package org.example.khkim;

/**
 * [2강] class / 객체 / public / static
 *
 * 1. class 와 객체(인스턴스)
 *    - class : 설계도. "무엇을 가지고(필드) 무엇을 할 수 있는지(메서드)"를 적어둔 틀.
 *    - 객체  : 설계도로 실제로 만든 것. new 로 만든다.   Car a = new Car();
 *    - 같은 class 로 객체를 여러 개 만들 수 있고, 객체마다 필드 값은 따로 가진다.
 *    - 생성자 : new 할 때 처음 한 번 실행되는 특수 메서드. 이름이 class 와 같고 반환형이 없다.
 *    - this   : "지금 이 객체 자신". 필드와 매개변수 이름이 같을 때 구분용으로 쓴다.
 *
 * 2. 접근제어자 (누가 이 멤버를 볼 수 있나)
 *    public     어디서든
 *    protected  같은 패키지 + 자식 클래스
 *    (없음)     같은 패키지 (default)
 *    private    이 클래스 안에서만
 *    - 관례: 필드는 private 으로 숨기고, 필요한 만큼만 public 메서드(getter/setter)로 연다 -> 캡슐화.
 *    - public class 는 파일당 1개, 파일명과 클래스명이 같아야 한다.
 *      (그래서 이 파일의 Lesson02ClassStatic 만 public 이고 아래 Counter/Account 는 static nested)
 *
 * 3. static (객체가 아니라 "클래스에 속한다")
 *    - static 필드/메서드는 객체 없이  클래스이름.멤버  로 쓴다.  Math.max(1, 2)
 *    - 모든 객체가 하나를 공유한다. (객체마다 따로 갖는 일반 필드와 반대)
 *    - static 메서드 안에서는 this 나 일반 필드를 바로 못 쓴다. (객체가 없을 수 있으므로)
 *    - main 이 static 인 이유: JVM 이 객체를 만들기 전에 클래스이름.main() 으로 먼저 호출하기 때문.
 *    - static final = 상수.  First.java 의 male 이 그 예.
 *
 * 4. 정리표
 *                 일반(인스턴스) 멤버        static 멤버
 *    소속          객체                       클래스
 *    사용          객체변수.멤버              클래스이름.멤버
 *    값            객체마다 다름              전부 공유
 */
public class Lesson02ClassStatic {

    // 설계도(class): 은행 계좌
    static class Account {
        private final String owner;      // private: 밖에서 직접 못 건드림
        private long balance;            // 객체마다 따로
        static int totalAccounts = 0;    // static: 모든 계좌가 공유

        Account(String owner, long balance) {   // 생성자
            this.owner = owner;                 // this.owner = 필드, owner = 매개변수
            this.balance = balance;
            totalAccounts++;                    // 계좌가 만들어질 때마다 공유 카운터 증가
        }

        public void deposit(long amount) {      // public: 밖에서 호출 가능
            if (amount <= 0) {
                throw new IllegalArgumentException("금액은 0보다 커야 함");
            }
            balance += amount;                  // 검증을 거쳐야만 값이 바뀜 -> 캡슐화의 이점
        }

        public long getBalance() {              // getter
            return balance;
        }

        public String getOwner() {
            return owner;
        }

        static String bankName() {              // static 메서드: 객체 없이 호출
            return "KH은행";
            // return owner;                    // 컴파일 에러: static 에서는 일반 필드 접근 불가
        }
    }

    public static void main(String[] args) {
        // 객체 생성: 같은 설계도에서 서로 다른 객체 2개
        Account a = new Account("김철수", 1000);
        Account b = new Account("이영희", 5000);

        a.deposit(500);
        System.out.println(a.getOwner() + " 잔액 = " + a.getBalance());   // 1500
        System.out.println(b.getOwner() + " 잔액 = " + b.getBalance());   // 5000 (a 와 무관)

        // a.balance = 999999;      // 컴파일 에러: private 이라 밖에서 직접 접근 불가
        try {
            a.deposit(-1);          // 잘못된 값은 메서드가 막아준다
        } catch (IllegalArgumentException e) {
            System.out.println("막힘: " + e.getMessage());
        }

        // static 은 객체가 아니라 클래스로 접근
        System.out.println("전체 계좌 수 = " + Account.totalAccounts);     // 2
        System.out.println("은행 이름 = " + Account.bankName());

        // 참조: 변수는 객체 자체가 아니라 "객체의 주소"
        Account c = a;              // 복사가 아니라 같은 객체를 가리킴
        c.deposit(100);
        System.out.println("a 잔액 = " + a.getBalance());                 // 1600 (c 로 바꿨는데 a 도 변함)
        System.out.println("a == c : " + (a == c));                       // true (같은 객체)
    }
}
