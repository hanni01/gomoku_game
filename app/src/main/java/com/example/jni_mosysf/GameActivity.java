package com.example.jni_mosysf;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
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

    private ImageView stoneCursor;
    private ImageView boardImage;

    // 오목판 구조에 맞게 조정
    private final int BOARD_WIDTH = 15;   // 가로 칸 수 (→ 방향)
    private final int BOARD_HEIGHT = 16;  // 세로 칸 수 (↓ 방향)
    private final int MARGIN_TOP = 115;
    private final int MARGIN_LEFT = 7;
    private int cursorX = 6;  // 시작 x 좌표 (0~14)
    private int cursorY = 7;  // 시작 y 좌표 (0~15)

    private CellPos[][] cellPositions = new CellPos[BOARD_HEIGHT][BOARD_WIDTH];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        boardImage = findViewById(R.id.imageView3);   // 오목판 이미지
        stoneCursor = findViewById(R.id.imageView2);  // 커서(검은돌)

        // 이미지가 화면에 완전히 그려진 후 위치 계산
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

        // JNI에서 키패드 입력 받아서 커서 이동
        new Thread(() -> {
            while (true) {
                String key = onKeyInput();  // JNI 함수 호출
                Log.d("KEY_INPUT", "Received from JNI: " + key);
                onKeyInputFromJNI(key);    // 실제 커서 처리
            }
        }).start();
    }

    // JNI에서 전달된 키 값 처리
    public void onKeyInputFromJNI(String keyCode) {
        runOnUiThread(() -> {
            switch (keyCode) {
                case "KEY_2": // ↑
                    if (cursorY > 1) cursorY--;
                    break;
                case "KEY_8": // ↓
                    if (cursorY < BOARD_HEIGHT - 1) cursorY++;
                    break;
                case "KEY_4": // ←
                    if (cursorX > 1) cursorX--;
                    break;
                case "KEY_6": // →
                    if (cursorX < BOARD_WIDTH - 1) cursorX++;
                    break;
                case "KEY_5": // 돌 놓기
                    placeStone(cursorX, cursorY);
                    return;
            }
            Log.d("CURSOR", "cursorX=" + cursorX + ", cursorY=" + cursorY);
            updateCursorPosition();
        });
    }

    private void updateCursorPosition() {
        CellPos pos = cellPositions[cursorY][cursorX];  // [y][x] 순서
        stoneCursor.setTranslationX(pos.x);
        stoneCursor.setTranslationY(pos.y);
    }

    private void placeStone(int x, int y) {
        // 여기에 착수 처리 추가 가능
        Toast.makeText(this, "착수: (" + x + ", " + y + ")", Toast.LENGTH_SHORT).show();
    }

    // JNI native 함수 선언
    public native String onKeyInput();



}
