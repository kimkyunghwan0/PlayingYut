package org.example.khkim;

import java.util.*;

public class playYut {

    private static int first = 0;
    private static int second = 0;
    private static int third = 0;
    private static int fourth = 0;

    private static int playerPosition = 0;
    private static int backupPosition = 0;

    private static int npcPosition = 0;

    private static String route = "main";
    private static String backupRoute = "main";

    private  static Boolean test = true;
    private  static Boolean end = false;

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

    private static List<Integer> exception = Arrays.asList(
            48
    );
    private static Map<Integer, Integer> map = new HashMap<Integer, Integer>();

    private static List<String> mapList = new ArrayList<>();

    public static void main(String[] args) {
        settingMap();
        while (!end) {
            nowMap();
            playerTurn();
        }
//        while (true) {
//            playerTurn();
//            if(end){
//                break;
//            }
//        }
    }

    static void settingMap () {
        int cnt = 0;
        List<Integer> removelist = Arrays.asList(
                3, 9, 10, 11, 15, 17, 19, 21, 22, 23,
                25, 26, 27, 29, 31, 33, 37, 38, 39, 45
        );
        Map<Integer, Integer> map = settingMapNumber();
        //System.out.println(list);

        for (var i = 0; i < 7; i++) {
            for (var j = 0; j < 7; j++) {
                if (removelist.contains(cnt)) {
                    System.out.print("       ");
                    //System.out.print("     ");
                    mapList.add("       ");
                } else if (map.containsKey(cnt)) {
                    //System.out.printf("[ %2d ]", map.get(cnt));
                    System.out.print("[     ]");
                    mapList.add("[     ]");
                } else{
                    mapList.add("[     ]");
                    System.out.print("[     ]");
                }
                cnt++;
            }
            System.out.println();
        }

    }
    static void nowMap () {
        List<Integer> nowRoute = routeCheck();
        int playerHere;

        if (playerPosition >= nowRoute.size()) {
            playerHere = -1;
        } else {
            playerHere = nowRoute.get(playerPosition);
        }
        //System.out.println("playerHere: " + playerHere);
        for(var i =0; i<mapList.size(); i++){
            //if(playerHere != 48){
                if ((i + 1) % 7 == 0) {
                    if (i == playerHere) {
                        System.out.println("[  P  ]");
                    } else {
                        System.out.println(mapList.get(i));
                    }
                } else {
                    if (i == playerHere) {
                        System.out.print("[  P  ]");
                    } else {
                        System.out.print(mapList.get(i));
                    }
                }
            //}
        }
    }
    static Map<Integer, Integer> settingMapNumber() {
        for (int i = 0; i < mainRoute.size(); i++) {
            map.put(mainRoute.get(i), i);
        }

        // 지름길 칸에 표시할 번호 (앞 칸부터 순서대로)
        putNumbers(shotRoute, 5, 22, 24, 25, 26, 28, 15);
        putNumbers(shotRoute2, 10, 21, 23, 25, 27, 29, 0);

        System.out.println(map);
        return map;
    }

    static void putNumbers(List<Integer> routeList, int... numbers) {
        for (int i = 0; i < numbers.length; i++) {
            map.put(routeList.get(i), numbers[i]);
        }
    }

    static void playerTurn() {
        System.out.println("\n플레이어 차례입니다. 엔터를 입력해주세요.");
        String input = sc.nextLine();
        //Map<Integer, String> resultMap = new HashMap<>();
        List<Integer> resultList = new ArrayList<>();
        resultList.add(settingYut());

        if (resultList.get(0) > 3) { // 윷 또는 모
            System.out.println("윷 또는 모가 나왔기에 한번 더 윷을 던집니다.");
            resultList.add(settingYut());
            if (resultList.get(1) > 3) { // 두번째 윷 또는 모
                System.out.println("한턴에 최대 3번까지만 행동 할 수 있습니다.");
                resultList.add(settingYut()); // 세번째 윷 결과
            }
        }
        System.out.println(resultList);

        if(resultList.size() > 1){ // 윷 또는 모가 나와서 선택지가 여러개일 때
            while(true){
                System.out.println("사용할 윷 결과를 선택하세요. \n현재 저장된 값 : " + resultList);
                String str = sc.nextLine();
                int i;
                try {
                    i = Integer.parseInt(str);
                } catch (NumberFormatException e) {
                    System.out.println("[Error] 올바른 정수 형식이 아닙니다. 다시 입력해주세요.");
                    continue;
                }

                if (!resultList.contains(i)) {
                    System.out.println("입력하신 결과와 일치하는 윷이 없습니다. 다시 입력해주세요.");
                    continue;
                }

                System.out.println("결과 : " + i);

                playerMove(playerPosition, i);

                // 값 i를 하나만 삭제
                resultList.remove(Integer.valueOf(i));

                if (!resultList.isEmpty()) {
                    nowMap();
                    continue;
                }
                break;
            }
        }else{
            playerMove(playerPosition, resultList.get(0));
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

        List<Integer> nowRoute = routeCheck();
        if(playerPosition >= nowRoute.size()){
            System.out.println("한바퀴 완료");
            end = true;
            return;
        }
        int here =  nowRoute.get(playerPosition);

        routeChange(here, position, result);

        //System.out.println("현재 포지션 : " + map.get(here));


    }

    static List<Integer> routeCheck(){
        return switch (route) {
            case "sub" -> shotRoute;
            case "sub2" -> shotRoute2;
            //case "sub3" -> shotRoute3;
            case "exception" -> exception;
            default -> mainRoute;
        };
//        switch(route){
//            case "sub":
//                return shotRoute;
//            case "sub2":
//                return shotRoute2;
//            case "exception":
//                return exception;
//            default:
//                return mainRoute;
//        }
    }

    static void routeChange(int here ,int position ,int result){
        switch (here){
            case 6: //우상단
                if(answerPath()) {
                    route = "sub";
                    positionSet();
                }
                break;
            case 0: //좌상단
                if(answerPath()) {
                    route = "sub2";
                    positionSet();
                }
                break;
            case 24: //중앙
                if(answerPath() && (route.equals("sub"))){
                    backupRoute = route;
                    route = "sub3";
                    positionSet();
                }
                break;
            case 48: //시작점
                if((position + result) == 0) { // +1 -1 = 0
                    route = "exception";
                }
                break;
        }

    }
    static void positionSet(){
        backupPosition = playerPosition;
        playerPosition = 0;
    }

    static boolean answerPath(){
        System.out.println("지름길을 이용하시겠습니까?   Y/N");
        String input = sc.nextLine();

        return input.equalsIgnoreCase("Y");
//        if(n.equals("Y")){
//            return true;
//        }else{
//            return false;
//        }
    }

    static Integer settingYut () {
        ArrayList<Integer> yutList = new ArrayList<>();

//        if(test){
//            yutList.add(1);
//            yutList.add(0);
//            yutList.add(0);
//            yutList.add(0);
//            test = false;
//        }else{
//            yutList.add(1);
//            yutList.add(0);
//            yutList.add(0);
//            yutList.add(0);
//            test = true;
//        }

        for (int i = 0; i < 4; i++) {
            int num = (int) (Math.random() * 2);
            yutList.add(num);
        }
        //System.out.println(yutList);

        int first = yutList.get(0);
        int sum = first + yutList.get(1) + yutList.get(2) + yutList.get(3);

        //결과
        int result;
        String text;
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
        return  result;
    }
}
