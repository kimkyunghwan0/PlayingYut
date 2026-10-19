package org.example.khkim;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static java.lang.Math.random;

// 판을 만들고
@SpringBootTest
class KhkimApplicationTests {

    @Test
    void contextLoads() {

        // 윷놀이판
        settingMap();
        settingYut();
        player();
    }
    void settingMap() {
        int cnt = 0;
        List<Integer> list = Arrays.asList(3,9,10,11,15,17,19,21,22,23,25,26,27,29,31,33,37,38,39,45);
        System.out.println(list);
        for(var i=0; i<7; i++){
            for(var j=0; j<7; j++){
                if(list.contains(cnt)){
                    System.out.print("      ");
                }else{
                    //System.out.printf("[ %2d ]", cnt);
                    System.out.print("[    ]");
                }
                cnt++;
            }
            System.out.println();
        }
    }
    // 윷을 만든다
    // 윷은 총4개. 0또는1 이 들어가있는 4개의 배열 or 리스트
    // [0, 0, 0, 0] 윷
    // [1, 1, 1, 1] 모
    // [1, 0, 0, 0] 백도
    // 나머진 총합 개수에 따라
    void settingYut() {
        ArrayList<Integer> yutList = new ArrayList<>();
        int num;
        for(int i=0; i<4; i++){
            num = (int)(Math.random() * 2);
            yutList.add(num);
        }
        System.out.println(yutList);

        int first = yutList.get(0);
        int second = yutList.get(1);
        int third = yutList.get(2);
        int fourth = yutList.get(3);

        //결과
        if(first + second + third + fourth == 0){
            System.out.println("윷");
        }else if(first + second + third + fourth == 1){
            if(first == 1){
                System.out.println("빽도");
            }else{
                System.out.println("도");
            }
        }else if(first + second + third + fourth == 2){
            System.out.println("개");
        }else if(first + second + third + fourth == 3){
            System.out.println("걸");
        }else if(first + second + third + fourth == 4){
            System.out.println("모");
        }

    }
    void player(){
        Scanner in = new Scanner(System.in);
        String str1 = in.nextLine();
        System.out.println(str1);
    }
}
