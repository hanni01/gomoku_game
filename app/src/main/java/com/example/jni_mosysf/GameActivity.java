package com.example.jni_mosysf;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    public class CellPos {
        public float x, y;

        public CellPos(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    static {
        System.loadLibrary("jni_mosysf"); // JNI 연결
    }

    private ImageView w_stoneCursor;
    private ImageView b_stoneCursor;
    private ImageView boardImage;

    // View에 그려진 오목판 커스텀 설정 (절대 수정 및 삭제 금지)
    private final int BOARD_WIDTH = 15;
    private final int BOARD_HEIGHT = 16;
    private final int MARGIN_TOP = 115;
    private final int MARGIN_LEFT = 7;

    // 초기 돌의 위치
    private final int INIT_X = 6;
    private final int INIT_Y = 7;
    private int cursorX = INIT_X;
    private int cursorY = INIT_Y;

    // 돌의 픽셀 위치를 처리할 배열
    private CellPos[][] cellPositions = new CellPos[BOARD_HEIGHT][BOARD_WIDTH];

    // 게임 상태 변수
    // 돌의 로직상의 위치를 처리할 배열
    private int[][] board = new int[BOARD_HEIGHT][BOARD_WIDTH]; // 보드 상태
    private int currentTurn = 1; // 1: 흑, 2: 백
    private boolean isGameRunning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        boardImage = findViewById(R.id.imageView3);
        w_stoneCursor = findViewById(R.id.imageView1);
        b_stoneCursor = findViewById(R.id.imageView2);

        boardImage.post(() -> {
            int boardWidth = boardImage.getWidth() + 1;
            int boardHeight = boardImage.getHeight() + 1;

            int cellWidth = boardWidth / BOARD_WIDTH;
            int cellHeight = boardHeight / BOARD_HEIGHT;

            for (int y = 1; y < BOARD_HEIGHT; y++) {
                for (int x = 1; x < BOARD_WIDTH; x++) {
                    cellPositions[y][x] = new CellPos(
                            MARGIN_LEFT + x * cellWidth,
                            MARGIN_TOP + y * cellHeight
                    );
                }
            }

            updateCursorPosition();
        });

        sendTurnToBoard("BLACK"); // 초기 턴 표시

        new Thread(() -> {
            while (true) {
                String key = onKeyInput();
                Log.d("KEY_INPUT", "Received from JNI: " + key);
                onKeyInputFromJNI(key);
            }
        }).start();
    }

    public void onKeyInputFromJNI(String keyCode) {
        runOnUiThread(() -> {
            if (!isGameRunning) return;

            switch (keyCode) {
                case "KEY_2":
                    if (cursorY > 1) cursorY--;
                    break;
                case "KEY_8":
                    if (cursorY < BOARD_HEIGHT - 1) cursorY++;
                    break;
                case "KEY_4":
                    if (cursorX > 1) cursorX--;
                    break;
                case "KEY_6":
                    if (cursorX < BOARD_WIDTH - 1) cursorX++;
                    break;
                case "KEY_5":
                    placeStone(cursorX, cursorY);
                    return;
            }

            updateCursorPosition();
        });
    }

    private void updateCursorPosition() {
        CellPos pos = cellPositions[cursorY][cursorX];
        ImageView targetCursor = (currentTurn == 1) ? b_stoneCursor : w_stoneCursor;
        targetCursor.setTranslationX(pos.x);
        targetCursor.setTranslationY(pos.y);
    }

    private void placeCursorPosition(ImageView targetStone, int x, int y){
        CellPos pos = cellPositions[x][y];
        targetStone.setTranslationX(pos.x);
        targetStone.setTranslationY(pos.y);
    }

    // 착수 처리
    private void placeStone(int x, int y) {
        if (board[y][x] != 0) {
            Toast.makeText(this, "이미 놓인 자리입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 현재 턴의 플레이어의 숫자를 넣어줌 흑: 1, 백: 2
        board[y][x] = currentTurn;

        // View에 돌 이미지 착수
        ImageView targetCursor = (currentTurn == 1) ? b_stoneCursor : w_stoneCursor;
        ImageView stone = new ImageView(targetCursor.getContext());
        stone.setImageResource(currentTurn == 1 ? R.drawable.black_stone : R.drawable.white_stone);

        // 크기 설정 (커서 이미지와 동일하게)
        stone.setLayoutParams(new FrameLayout.LayoutParams(
                targetCursor.getWidth(),
                targetCursor.getHeight()
        ));

        placeCursorPosition(stone, cursorY, cursorX); // 또는 placeCursorPosition(stone, x, y);

        FrameLayout layout = findViewById(R.id.main);
        layout.addView(stone, layout.getChildCount() - 2); // imageView1, imageView2 위에는 올리지 않도록!

        // 좌표 출력
        Toast.makeText(this, (currentTurn == 1 ? "흑" : "백") + " 착수: (" + x + ", " + y + ")", Toast.LENGTH_SHORT).show();

        if (checkWin(x, y, currentTurn)) {
            Toast.makeText(this, (currentTurn == 1 ? "흑" : "백") + " 승리!", Toast.LENGTH_LONG).show();
            sendTurnToBoard("");
            isGameRunning = false;

            Intent intent = new Intent(GameActivity.this, MainActivity.class);
            startActivity(intent);
            return;
        }

        if (isBoardFull()) {
            Toast.makeText(this, "무승부! 판을 초기화합니다.", Toast.LENGTH_SHORT).show();
            sendTurnToBoard("<-- Draw! Reset the Board! -- >");
            resetBoard();
            return;
        }

        // 턴 전환
        ChageTurn();
    }

    // 승리 조건 체크
    private boolean checkWin(int x, int y, int turn) {
        return checkDir(x, y, 1, 0, turn) || // 가로
                checkDir(x, y, 0, 1, turn) || // 세로
                checkDir(x, y, 1, 1, turn) || // ↘
                checkDir(x, y, 1, -1, turn);  // ↗
    }

    private boolean checkDir(int x, int y, int dx, int dy, int turn) {
        int count = 1;

        for (int i = 1; i < 5; i++) {
            int nx = x - dx * i;
            int ny = y - dy * i;
            if (nx < 0 || ny < 0 || nx >= BOARD_WIDTH || ny >= BOARD_HEIGHT) break;
            if (board[ny][nx] != turn) break;
            count++;
        }

        for (int i = 1; i < 5; i++) {
            int nx = x + dx * i;
            int ny = y + dy * i;
            if (nx < 0 || ny < 0 || nx >= BOARD_WIDTH || ny >= BOARD_HEIGHT) break;
            if (board[ny][nx] != turn) break;
            count++;
        }

        return count >= 5;
    }

    // 보드 꽉 찼는지 체크
    private boolean isBoardFull() {
        for (int y = 0; y < BOARD_HEIGHT; y++)
            for (int x = 0; x < BOARD_WIDTH; x++)
                if (board[y][x] == 0) return false;
        return true;
    }

    // 보드 초기화
    private void resetBoard() {
        for (int y = 0; y < BOARD_HEIGHT; y++)
            for (int x = 0; x < BOARD_WIDTH; x++)
                board[y][x] = 0;

        cursorX = 6;
        cursorY = 7;
        currentTurn = 1;
        isGameRunning = true;
        sendTurnToBoard("BLACK TURN!!");
        updateCursorPosition();
    }

    // 턴 체인지
    private void ChageTurn(){
        cursorX = INIT_X;
        cursorY = INIT_Y;

        currentTurn = (currentTurn == 1) ? 2 : 1;
        sendTurnToBoard(currentTurn == 1 ? "BLACK TURN!!" : "WHITE TURN!!");
        updateCursorPosition();
    }

    // JNI 함수들
    public native String onKeyInput();
    public native void sendTurnToBoard(String text); // 턴 출력
}
