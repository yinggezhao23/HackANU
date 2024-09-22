package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SleepDataView extends View {

    private float[][] sleepData;
    private Paint paint;
    private int[] colors = {Color.rgb(135, 206, 235), Color.rgb(144, 238, 144), Color.rgb(255, 223, 186), Color.rgb(250, 128, 114), Color.rgb(255, 182, 193)}; // 柔和的颜色
    private int selectedBarIndex = 0;  // 当前选中的柱子索引
    private String[] stateDescriptions = {"Awake", "Light Sleep", "Deep Sleep S1", "Deep Sleep S2", "REM Sleep"};

    public SleepDataView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
    }

    // 提供数据接口
    public void setData(float[][] data) {
        this.sleepData = data;
        invalidate(); // 刷新视图
    }

    public int getColor(int index) {
        if (index < 0) index = 0;
        if (index >= colors.length) index = colors.length - 1;
        return colors[index];
    }

    public String getStateDescriptions(int index) {
        if (index < 0) index = 0;
        if (index >= stateDescriptions.length) index = stateDescriptions.length - 1;
        return stateDescriptions[index];
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (sleepData == null) return;

        int width = getWidth();
        int chartHeight = 400;  // 为底部标签留出更多空间

        // 绘制柱状图
        float maxTime = sleepData[sleepData.length - 1][0]; // 假设时间是递增的
        float minTime = sleepData[0][0];
        float timeRange = maxTime - minTime;
        float barWidth = (float) width / sleepData.length; // 每个柱子的宽度
        float fixedBarHeight = chartHeight / 2; // 所有柱子的高度保持一致

        for (int i = 0; i < sleepData.length; i++) {
            float time = sleepData[i][0];
            float sleepState = sleepData[i][2];

            // 确保 sleepState 在有效范围内
            if (sleepState < 0) sleepState = 0;
            if (sleepState > 4) sleepState = 4;

            float x = i * barWidth;  // 根据索引计算每个柱子的X坐标
            float y = chartHeight - fixedBarHeight; // 使用固定的柱子高度

            // 如果当前柱子被选中，高亮显示
            if (i == selectedBarIndex) {
                paint.setColor(Color.rgb(0, 0, 0));  // 高亮颜色
            } else {
                paint.setColor(getColor((int) sleepState));  // 正常颜色
            }

            // 绘制柱状图
            canvas.drawRect(x, y, x + barWidth, chartHeight, paint);
        }

        // 绘制下方的状态标签
        paint.setTextSize(30);
        int labelStartY = chartHeight + 50; // 标签的初始Y轴位置
        int labelPadding = 40; // 每个标签的垂直间距

        for (int i = 0; i < colors.length; i++) {
            paint.setColor(getColor(i));
            float labelX = 50;  // 标签的X轴位置
            float labelY = labelStartY + i * labelPadding;  // 计算每个标签的Y轴位置
            canvas.drawRect(labelX, labelY - 30, labelX + 50, labelY, paint); // 绘制标签颜色块
            paint.setColor(Color.BLACK);
            canvas.drawText(getStateDescriptions(i), labelX + 60, labelY - 5, paint); // 绘制标签文字
        }

        // 如果有选中的柱子，展示对应的信息
        if (selectedBarIndex != -1) {
            float[] selectedData = sleepData[selectedBarIndex];
            float selectedTime = selectedData[0];
            float selectedState = selectedData[2];
            String selectedLabel = getStateDescriptions((int) selectedState);

            // 展示信息
            paint.setTextSize(40);
            paint.setColor(Color.BLACK);
            canvas.drawText("Time: " + selectedTime, 50, 100, paint);
            canvas.drawText("State: " + selectedLabel, 50, 150, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        int width = getWidth();
        float barWidth = (float) width / sleepData.length; // 计算每个柱子的宽度

        // 根据点击的X坐标确定点击了哪个柱子
        int clickedIndex = (int) (x / barWidth);

        if (clickedIndex >= 0 && clickedIndex < sleepData.length) {
            selectedBarIndex = clickedIndex; // 更新选中的柱子索引
            invalidate(); // 重新绘制视图
        }

        return true;
    }
}
