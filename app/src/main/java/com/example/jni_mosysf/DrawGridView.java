package com.example.jni_mosysf;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class DrawGridView extends View {

    private final Paint paint = new Paint();
    private final int BOARD_WIDTH = 15;   // 칸 수
    private final int BOARD_HEIGHT = 16;  // 칸 수

    private int cellWidth;
    private int cellHeight;
    private int marginTop;
    private int marginSide;

    public DrawGridView(Context context) {
        super(context);
        init();
    }

    public DrawGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(3f);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int boardWidth = getWidth() + 1;
        int boardHeight = getHeight() + 1;

        cellWidth = boardWidth / BOARD_WIDTH;
        cellHeight = boardHeight / BOARD_HEIGHT;

        // 세로줄
        for (int x = 0; x <= BOARD_WIDTH; x++) {
            float xPos = marginTop + x * cellWidth;
            canvas.drawLine(xPos, marginSide, xPos, marginSide + BOARD_HEIGHT * cellHeight, paint);
        }

        // 가로줄
        for (int y = 0; y <= BOARD_HEIGHT; y++) {
            float yPos = marginSide + y * cellHeight;
            canvas.drawLine(marginTop, yPos, marginTop + BOARD_WIDTH * cellWidth, yPos, paint);
        }
    }
}