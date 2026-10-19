package org.example.khkim;

import java.util.*;

public class playYut2 {

    private static int playerPosition = 0;
    private static int backupPosition = 0;

    private static String route = "main";
    private static String backupRoute = "main";

    private static Boolean end = false;

    private static final Scanner sc = new Scanner(System.in); // Scanner 하나만 공유

    // 기존 7x7 맵에서 실제 윷놀이 진행 순서
    private static List<Integer> mainRoute = Arrays.asList(
            48, 41, 34, 20, 13,
            6, 5, 4, 2, 1,
            0, 7, 14, 28, 35,
            42, 43, 44, 46, 47,48
    ); // 0~19

    //    ↙
    //  ㅁ
    // ↙
    private static List<Integer> shotRoute = Arrays.asList(
            6,12, 18, 24, 30, 36,42,43,44,46,47,48
    );

    // ↘
    //  ㅁ
    //   ↘
    private static List<Integer> shotRoute2 = Arrays.asList(
            0,8, 16, 24, 32, 40,48
    );

    // ㅁ
    //   ↘
    private static List<Integer> shotRoute3 = Arrays.asList(
            24, 32, 40, 48
    );

    private static List<Integer> excition = Arrays.asList(
            48
    );
    private static Map<Integer, Integer> map = new HashMap<Integer, Integer>();

    public static void main(String[] args) {
        settingMapNumber();
        while (!end) {
            playerTurn();
        }
    }

    static void settingMap () {
        int cnt = 0;
        List<Integer> removelist = Arrays.asList(
                3, 9, 10, 11, 15, 17, 19, 21, 22, 23,
                25, 26, 27, 29, 31, 33, 37, 38, 39, 45
        );
        int playerCell = getRoute().get(playerPosition); // 말이 있는 칸
        for (var i = 0; i < 7; i++) {
            for (var j = 0; j < 7; j++) {
                if (removelist.contains(cnt)) {
                    System.out.print("      ");
                } else if (map.containsKey(cnt)) {
                    if (cnt == playerCell) {
                        System.out.printf("[*%2d*]", map.get(cnt));
                    } else {
                        System.out.printf("[ %2d ]", map.get(cnt));
                    }
                } else{
                    System.out.print("[    ]");
                }
                cnt++;
            }
            System.out.println();
        }
    }

    static Map<Integer, Integer> settingMapNumber() {
        for (int i = 0; i < mainRoute.size(); i++) {
            map.put(mainRoute.get(i), i);
        }

        // 지름길 칸에 표시할 번호 (앞 칸부터 순서대로)
        putNumbers(shotRoute, 5, 22, 24, 25, 26, 28, 15);
        putNumbers(shotRoute2, 10, 21, 23, 25, 27, 29, 0);

        return map;
    }

    static void putNumbers(List<Integer> routeList, int... numbers) {
        for (int i = 0; i < numbers.length; i++) {
            map.put(routeList.get(i), numbers[i]);
        }
    }

    static void playerTurn() {
        System.out.println();
        settingMap();
        System.out.println("\n플레이어 차례입니다. 엔터를 입력해주세요.");
        String input = sc.nextLine();
        Map<Integer, String> resultMap = new HashMap<>();
        resultMap.putAll(settingYut());

        for (Map.Entry<Integer, String> entry : resultMap.entrySet()) {
            if (entry.getKey() > 3) { // 윷 또는 모
                System.out.println(entry.getValue()+"이 나왔기에 한번 더 윷을 던집니다.");
                resultMap.putAll(settingYut());
            }else{
                playerMove(playerPosition , entry.getKey());
            }
        }

        if(resultMap.size() > 1){ // 윷 또는 모가 나와서 선택지가 여러개일 때
            while(true){
                System.out.println("먼저 사용할 윷 결과를 선택하세요. 남은 윷은 자동으로 사용됩니다. \n현재 저장된 값 : " + resultMap);
                String str = sc.nextLine();

                if (!resultMap.containsValue(str)) {
                    System.out.println("입력하신 결과와 일치하는 윷이 없습니다. 다시 입력해주세요.");
                    continue;
                }

                for (Map.Entry<Integer, String> entry : resultMap.entrySet()) {
                    if(entry.getValue().equals(str)){
                        System.out.println("결과 : " + str + "\n이동한 수 : " + entry.getKey());
                        playerMove(playerPosition, entry.getKey());
                        break;
                    }
                }

                for (Map.Entry<Integer, String> entry : resultMap.entrySet()) {
                    if(!entry.getValue().equals(str)){
                        System.out.println("결과 : " + entry.getValue() + "\n이동한 수 : " +entry.getKey());
                        playerMove(playerPosition, entry.getKey());
                    }
                }
                break;
            }
        }
    }

    static void playerMove(int position, int result){
        playerPosition = position + result;
        if(playerPosition < 0){
            route = backupRoute;
            playerPosition = backupPosition;
        }else if(position == 0){
            System.out.println("현재위치 시작점");
        }

        List<Integer> nowRoute = getRoute();
        if(playerPosition >= nowRoute.size()){
            System.out.println("한바퀴 완료");
            end = true;
            return;
        }
        int here = nowRoute.get(playerPosition);

        changeRoute(here, position, result);

        System.out.println("현재 포지션 : " + map.get(here));
    }

    // 현재 경로 이름(route)에 해당하는 칸 목록
    static List<Integer> getRoute(){
        switch (route) {
            case "sub":
                return shotRoute;
            case "sub2":
                return shotRoute2;
            case "sub3":
                return shotRoute3;
            case "excition":
                return excition;
            default:
                return mainRoute;
        }
    }

    static void npcTurn() {
        System.out.println("상대차례입니다.");
    }

    static void changeRoute(int here, int position, int result) {
        if(here == 6){ //우상단
            if(askShortcut()){
                moveRoute("sub");
            }
        }else if(here == 0){ //좌상단
            if(askShortcut()){
                moveRoute("sub2");
            }
        }else if(here == 24){ //중앙
            if(route.equals("sub") && askShortcut()){
                backupRoute = route;
                moveRoute("sub3");
            }
        }else if(here == 48){ //시작점
            if((position + result) == 0){ // +1 -1 = 0
                route = "excition";
            }
        }
    }

    static boolean askShortcut() {
        System.out.println("지름길을 이용하시겠습니까?   Y/N");
        return sc.next().equals("Y");
    }

    // 지름길로 갈아타기: 지금 위치는 백업해두고 새 경로의 처음부터 시작
    static void moveRoute(String newRoute) {
        route = newRoute;
        backupPosition = playerPosition;
        playerPosition = 0;
    }

    static Map<Integer, String> settingYut () {
        ArrayList<Integer> yutList = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            int num = (int) (Math.random() * 2);
            yutList.add(num);
        }

        int first = yutList.get(0);
        int sum = first + yutList.get(1) + yutList.get(2) + yutList.get(3);

        //결과
        String text;
        int result;
        if (sum == 0) {
            text = "윷";
            result = 4;
        } else if (sum == 1) {
            if (first == 1) {
                text = "빽도";
                result = -1;
            } else {
                text = "도";
                result = 1;
            }
        } else if (sum == 2) {
            text = "개";
            result = 2;
        } else if (sum == 3) {
            text = "걸";
            result = 3;
        } else {
            text = "모";
            result = 5;
        }
        System.out.println("결과 : " + text + "\n이동한 수 : " +result);
        Map<Integer, String>resultMap  = new HashMap<>();
        resultMap.put(result,text);
        return  resultMap;
    }
}
