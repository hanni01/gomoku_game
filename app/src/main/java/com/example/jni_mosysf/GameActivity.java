package com.example.jni_mosysf;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    private static final int BOARD_WIDTH = 15;
    private static final int BOARD_HEIGHT = 16;
    private static final int MARGIN_TOP = 115;
    private static final int MARGIN_LEFT = 7;
    private static final int INIT_X = 6;
    private static final int INIT_Y = 7;

    static {
        System.loadLibrary("jni_mosysf");
    }

    private ImageView w_stoneCursor;
    private ImageView b_stoneCursor;
    private ImageView boardImage;

    private int cursorX = INIT_X;
    private int cursorY = INIT_Y;

    private final CellPos[][] cellPositions = new CellPos[BOARD_HEIGHT][BOARD_WIDTH];
    private final int[][] board = new int[BOARD_HEIGHT][BOARD_WIDTH];
    private int currentTurn = 1;
    private boolean isGameRunning = true;

    private static class CellPos {
        public final float x, y;

        public CellPos(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initViews();
        setupBoardGrid();
        sendTurnToBoard("BLACK");
        startKeypadThread();
    }

    private void initViews() {
        boardImage = findViewById(R.id.imageView3);
        w_stoneCursor = findViewById(R.id.imageView1);
        b_stoneCursor = findViewById(R.id.imageView2);
    }

    private void setupBoardGrid() {
        boardImage.post(() -> {
            int cellWidth = (boardImage.getWidth() + 1) / BOARD_WIDTH;
            int cellHeight = (boardImage.getHeight() + 1) / BOARD_HEIGHT;

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
    }

    private void startKeypadThread() {
        new Thread(() -> {
            while (true) {
                String key = onKeyInput();
                Log.d("KEY_INPUT", "Received from JNI: " + key);
                runOnUiThread(() -> handleKeyInput(key));
            }
        }).start();
    }

    private void handleKeyInput(String keyCode) {
        if (!isGameRunning) return;

        switch (keyCode) {
            case "KEY_2": if (cursorY > 1) cursorY--; break;
            case "KEY_8": if (cursorY < BOARD_HEIGHT - 1) cursorY++; break;
            case "KEY_4": if (cursorX > 1) cursorX--; break;
            case "KEY_6": if (cursorX < BOARD_WIDTH - 1) cursorX++; break;
            case "KEY_5": placeStone(cursorX, cursorY); return;
        }
        updateCursorPosition();
    }

    private void updateCursorPosition() {
        CellPos pos = cellPositions[cursorY][cursorX];
        ImageView targetCursor = (currentTurn == 1) ? b_stoneCursor : w_stoneCursor;
        targetCursor.setTranslationX(pos.x);
        targetCursor.setTranslationY(pos.y);
    }

    private void placeCursorPosition(ImageView targetStone, int x, int y) {
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
        ImageView stone = new ImageView(this);
        stone.setImageResource(currentTurn == 1 ? R.drawable.black_stone : R.drawable.white_stone);
        stone.setLayoutParams(new FrameLayout.LayoutParams(
                targetCursor.getWidth(), targetCursor.getHeight()));

        placeCursorPosition(stone, cursorY, cursorX);
        FrameLayout layout = findViewById(R.id.main);
        layout.addView(stone, layout.getChildCount() - 2);

        Toast.makeText(this, (currentTurn == 1 ? "흑" : "백") + " 착수: (" + x + ", " + y + ")", Toast.LENGTH_SHORT).show();

        if (checkWin(x, y, currentTurn)) {
            Toast.makeText(this, (currentTurn == 1 ? "흑" : "백") + " 승리!", Toast.LENGTH_LONG).show();
            sendTurnToBoard("");
            isGameRunning = false;
            startActivity(new Intent(this, MainActivity.class));
            return;
        }

        if (isBoardFull()) {
            Toast.makeText(this, "무승부! 판을 초기화합니다.", Toast.LENGTH_SHORT).show();
            sendTurnToBoard("<-- Draw! Reset the Board! -- >");
            resetBoard();
            return;
        }

        // 턴 전환
        switchTurn();
    }

    // 승리 조건 체크
    private boolean checkWin(int x, int y, int turn) {
        return checkDir(x, y, 1, 0, turn) ||
                checkDir(x, y, 0, 1, turn) ||
                checkDir(x, y, 1, 1, turn) ||
                checkDir(x, y, 1, -1, turn);
    }

    private boolean checkDir(int x, int y, int dx, int dy, int turn) {
        int count = 1;
        count += countStones(x, y, -dx, -dy, turn);
        count += countStones(x, y, dx, dy, turn);
        return count >= 5;
    }

    private int countStones(int x, int y, int dx, int dy, int turn) {
        int count = 0;
        for (int i = 1; i < 5; i++) {
            int nx = x + dx * i;
            int ny = y + dy * i;
            if (nx < 0 || ny < 0 || nx >= BOARD_WIDTH || ny >= BOARD_HEIGHT || board[ny][nx] != turn) break;
            count++;
        }
        return count;
    }

    // 보드 꽉 찼는지 체크
    private boolean isBoardFull() {
        for (int[] row : board)
            for (int cell : row)
                if (cell == 0) return false;
        return true;
    }

    // 보드 초기화
    private void resetBoard() {
        for (int y = 0; y < BOARD_HEIGHT; y++)
            for (int x = 0; x < BOARD_WIDTH; x++)
                board[y][x] = 0;

        cursorX = INIT_X;
        cursorY = INIT_Y;
        currentTurn = 1;
        isGameRunning = true;
        sendTurnToBoard("BLACK TURN!!");
        updateCursorPosition();
    }

    private void switchTurn() {
        cursorX = INIT_X;
        cursorY = INIT_Y;
        currentTurn = (currentTurn == 1) ? 2 : 1;
        sendTurnToBoard(currentTurn == 1 ? "BLACK TURN!!" : "WHITE TURN!!");
        updateCursorPosition();
    }

    public native String onKeyInput();
    public native void sendTurnToBoard(String text);
}
