package org.example.khkim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.IntStream;

/**
 * playYut 리팩터링 버전 (원본 playYut 은 수정하지 않음).
 *
 * 구조
 *  - Yut   : 윷 결과(이름, 이동 칸 수, 한 번 더 던지는지)
 *  - Route : 경로 5개(기존 mainRoute/shotRoute/shotRoute2/shotRoute3/excition). 칸 번호 + 화면 표시 번호
 *  - Board : 7x7 판 출력과 칸 번호 -> 표시 번호 변환
 *  - Piece : 말의 상태(현재 경로, 경로 안 위치, 되돌아갈 백업)
 *  - 게임 진행 : 던지기 -> 순서 선택 -> 이동 -> 갈림길 처리
 */
public class PlayYutRefactored {

    // ---------------- 윷 결과 ----------------
    private enum Yut {
        BACKDO("빽도", -1), DO("도", 1), GAE("개", 2), GEOL("걸", 3), YUT("윷", 4), MO("모", 5);

        final String label;
        final int steps;

        Yut(String label, int steps) {
            this.label = label;
            this.steps = steps;
        }

        boolean throwAgain() {            // 윷, 모는 한 번 더
            return steps > 3;
        }

        /** 막대 4개(0 또는 1)로 결과 결정. 합 0=윷, 1=도(첫 막대만 1이면 빽도), 2=개, 3=걸, 4=모 */
        static Yut of(int[] sticks) {
            int sum = 0;
            for (int s : sticks) sum += s;
            return switch (sum) {
                case 0 -> YUT;
                case 1 -> sticks[0] == 1 ? BACKDO : DO;
                case 2 -> GAE;
                case 3 -> GEOL;
                default -> MO;
            };
        }
    }

    // ---------------- 경로 ----------------
    // cells : 7x7 판(0~48)의 칸 번호, 말이 지나가는 순서
    // labels: 판에 찍어 보여주는 번호 (null 이면 경로 안 위치와 동일)
    private enum Route {
        MAIN(new int[]{48, 41, 34, 20, 13, 6, 5, 4, 2, 1, 0, 7, 14, 28, 35, 42, 43, 44, 46, 47, 48}, null),
        DIAG_RIGHT(new int[]{6, 12, 18, 24, 30, 36, 42, 43, 44, 46, 47, 48},      // 우상단(6) -> 중앙 -> 시작점
                new int[]{5, 22, 24, 25, 26, 28, 15}),                             // 표시 번호는 앞 7칸만 사용
        DIAG_LEFT(new int[]{0, 8, 16, 24, 32, 40, 48},                             // 좌상단(0) -> 중앙 -> 시작점
                new int[]{10, 21, 23, 25, 27, 29, 0}),
        CENTER_TO_END(new int[]{24, 32, 40, 48}, null),                            // 중앙 -> 시작점
        EXIT(new int[]{48}, null);                                                 // 시작점에서 빽도로 들어오는 출구

        final int[] cells;
        final int[] labels;

        Route(int[] cells, int[] labels) {
            this.cells = cells;
            this.labels = labels;
        }
    }

    // ---------------- 판 ----------------
    private static class Board {
        private static final int SIZE = 7;
        private static final List<Integer> HIDDEN = List.of(
                3, 9, 10, 11, 15, 17, 19, 21, 22, 23,
                25, 26, 27, 29, 31, 33, 37, 38, 39, 45);

        private final Map<Integer, Integer> labelOf = new HashMap<>();

        Board() {
            // 나중에 등록한 경로가 같은 칸의 번호를 덮어쓴다 (원본과 같은 규칙)
            for (Route route : new Route[]{Route.MAIN, Route.DIAG_RIGHT, Route.DIAG_LEFT}) {
                for (int i = 0; i < route.cells.length; i++) {
                    if (route.labels == null) {
                        labelOf.put(route.cells[i], i);
                    } else if (i < route.labels.length) {
                        labelOf.put(route.cells[i], route.labels[i]);
                    }
                }
            }
        }

        int label(int cell) {
            return labelOf.get(cell);
        }

        void print() {
            for (int row = 0; row < SIZE; row++) {
                StringBuilder line = new StringBuilder();
                for (int col = 0; col < SIZE; col++) {
                    int cell = row * SIZE + col;
                    if (HIDDEN.contains(cell)) {
                        line.append("      ");
                    } else if (labelOf.containsKey(cell)) {
                        line.append(String.format("[ %2d ]", labelOf.get(cell)));
                    } else {
                        line.append("[    ]");
                    }
                }
                System.out.println(line);
            }
        }
    }

    // ---------------- 말 ----------------
    private static class Piece {
        Route route = Route.MAIN;
        int index = 0;                      // route 안에서의 위치
        Route backupRoute = Route.MAIN;     // 빽도로 시작점 밖으로 나갈 때 돌아갈 곳
        int backupIndex = 0;

        int cell() {
            return route.cells[index];
        }

        /** 갈림길로 들어감: 현재 위치를 백업하고 새 경로의 처음에서 시작 */
        void enter(Route next) {
            backupIndex = index;
            route = next;
            index = 0;
        }
    }

    // ---------------- 게임 ----------------
    private final Scanner in;
    private final Random random;
    private final Board board = new Board();
    private final Piece player = new Piece();
    private boolean finished = false;

    PlayYutRefactored(Scanner in, Random random) {
        this.in = in;
        this.random = random;
    }

    public static void main(String[] args) {
        new PlayYutRefactored(new Scanner(System.in), new Random()).play();
    }

    void play() {
        board.print();
        while (!finished) {
            playerTurn();
        }
    }

    private void playerTurn() {
        System.out.println("\n플레이어 차례입니다. 엔터를 입력해주세요.");
        in.nextLine();

        List<Yut> results = throwUntilNoBonus();
        if (results.size() > 1) {
            results = askOrder(results);
        }
        for (Yut yut : results) {
            if (results.size() > 1) {
                System.out.println("결과 : " + yut.label + "\n이동한 수 : " + yut.steps);
            }
            move(yut.steps);
            if (finished) {
                return;
            }
        }
    }

    /** 윷/모가 나오는 동안 계속 던진다. */
    private List<Yut> throwUntilNoBonus() {
        List<Yut> results = new ArrayList<>();
        Yut yut;
        do {
            yut = throwYut();
            System.out.println("결과 : " + yut.label + "\n이동한 수 : " + yut.steps);
            results.add(yut);
            if (yut.throwAgain()) {
                System.out.println(yut.label + "이 나왔기에 한번 더 윷을 던집니다.");
            }
        } while (yut.throwAgain());
        return results;
    }

    private Yut throwYut() {
        int[] sticks = IntStream.generate(() -> random.nextInt(2)).limit(4).toArray();
        return Yut.of(sticks);
    }

    /** 먼저 쓸 결과를 고르면 그것을 맨 앞으로, 나머지는 던진 순서대로 뒤에 둔다. */
    private List<Yut> askOrder(List<Yut> results) {
        while (true) {
            System.out.println("먼저 사용할 윷 결과를 선택하세요. 남은 윷은 자동으로 사용됩니다. \n현재 저장된 값 : " + labels(results));
            String input = in.nextLine().trim();
            for (Yut yut : results) {
                if (yut.label.equals(input)) {
                    List<Yut> ordered = new ArrayList<>(results);
                    ordered.remove(yut);
                    ordered.add(0, yut);
                    return ordered;
                }
            }
            System.out.println("입력하신 결과와 일치하는 윷이 없습니다. 다시 입력해주세요.");
        }
    }

    private static List<String> labels(List<Yut> results) {
        return results.stream().map(y -> y.label).toList();
    }

    private void move(int steps) {
        int before = player.index;
        player.index += steps;

        if (player.index < 0) {                       // 빽도로 시작점 앞으로 나감 -> 백업 위치로
            player.route = player.backupRoute;
            player.index = player.backupIndex;
        } else if (before == 0) {
            System.out.println("현재위치 시작점");
        }

        if (player.index >= player.route.cells.length) {
            System.out.println("한바퀴 완료");
            finished = true;
            return;
        }

        int cell = player.cell();
        handleCrossroad(cell, before + steps);
        System.out.println("현재 포지션 : " + board.label(cell));
    }

    /** 갈림길 칸에 멈췄을 때의 처리. 칸 번호 6=우상단, 0=좌상단, 24=중앙, 48=시작점 */
    private void handleCrossroad(int cell, int rawIndex) {
        switch (cell) {
            case 6 -> {
                if (askShortcut()) {
                    player.enter(Route.DIAG_RIGHT);
                }
            }
            case 0 -> {
                if (askShortcut()) {
                    player.enter(Route.DIAG_LEFT);
                }
            }
            case 24 -> {
                if (player.route == Route.DIAG_RIGHT && askShortcut()) {
                    player.backupRoute = player.route;
                    player.enter(Route.CENTER_TO_END);
                }
            }
            case 48 -> {
                if (rawIndex == 0) {                  // 예: 위치 1에서 빽도(-1)로 정확히 시작점
                    player.route = Route.EXIT;
                }
            }
            default -> { }
        }
    }

    private boolean askShortcut() {
        System.out.println("지름길을 이용하시겠습니까?   Y/N");
        return in.nextLine().trim().equalsIgnoreCase("Y");
    }
}
