package com.example.mazegame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class GameView extends View {

    private enum Directions{
        UP,DOWN,LEFT,RIGHT
    }

    private Context ct;
    private Cell[][] cells;
    private Cell start,exit;
    private static final int colls = 15,rows = 20;
    private static final float wallThickness = 4;
    private float cellSize, vMargin, hMargin;
    private Paint wallPaint,startPaint,exitPaint;
    private Random random;

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        ct = context;
        wallPaint = new Paint();
        wallPaint.setColor(Color.BLACK);
        wallPaint.setStrokeWidth(wallThickness);

        startPaint = new Paint();
        startPaint.setColor(Color.RED);

        exitPaint = new Paint();
        exitPaint.setColor(Color.GREEN);
        random = new Random();
        createMaze();
    }

    private void createMaze(){
        cells = new Cell[colls][rows];
        Stack<Cell> stack = new Stack<>();
        Cell current,next;

        for(int i=0;i<colls;i++)
        {
            for(int j=0;j<rows;j++)
            {
                cells[i][j] = new Cell(i,j);
            }
        }

        start = cells[0][0];
        exit = cells[colls-1][rows-1];
        current = cells[0][0];
        current.visited = true;

        do{
            next = getNeighbour(current);
            if(next!=null)
            {
                removeWall(current,next);
                stack.push(current);
                current = next;
                current.visited = true;
            }else{
                current = stack.pop();
            }
        }while(!stack.empty());
    }

    private Cell getNeighbour(Cell cell){
        ArrayList<Cell> neighbours = new ArrayList<>();
        if(cell.x>0 && cells[cell.x-1][cell.y].visited==false){
            neighbours.add(cells[cell.x-1][cell.y]);
        }
        if(cell.x<colls-1 && cells[cell.x+1][cell.y].visited==false){
            neighbours.add(cells[cell.x+1][cell.y]);
        }
        if(cell.y>0 && cells[cell.x][cell.y-1].visited==false){
            neighbours.add(cells[cell.x][cell.y-1]);
        }
        if(cell.y<rows-1 && cells[cell.x][cell.y+1].visited==false){
            neighbours.add(cells[cell.x][cell.y+1]);
        }
        if(neighbours.size()>0) {
            int index = random.nextInt(neighbours.size());
            return neighbours.get(index);
        }
        return null;
    }

    private void removeWall(Cell current, Cell next) {
        if((current.x == next.x) && (current.y == next.y+1)){
            current.topWall = false;
            next.bottomWall = false;
        }

        if((current.x == next.x) && (current.y == next.y-1)){
            current.bottomWall = false;
            next.topWall = false;
        }

        if((current.y == next.y) && (current.x == next.x+1)){
            current.leftWall = false;
            next.rightWall = false;
        }

        if((current.y == next.y) && (current.x == next.x-1)){
            current.rightWall = false;
            next.leftWall = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
//        int width = metrics.widthPixels;
//        int height = metrics.heightPixels;
        int width = getWidth()-20;
        int height = getHeight()-rows*10;

//        Toast.makeText(ct, Integer.toString(width)+" "+Integer.toString(height),Toast.LENGTH_LONG).show();
        if(width/height < colls/rows){
            cellSize = width/(colls+1);
        }else{
            cellSize = height/(rows+1);
        }

        hMargin = (width - colls*cellSize)/2;
        vMargin = (height - rows*cellSize)/2;
        Log.i("test",Float.toString(cellSize));
        canvas.translate(hMargin,vMargin);

        for(int i=0;i<colls;i++)
        {
            for(int j=0;j<rows;j++)
            {
                if(cells[i][j].topWall){
                    canvas.drawLine(i*cellSize, j*cellSize, (i+1)*cellSize, j*cellSize, wallPaint);
                }

                if(cells[i][j].bottomWall){
                    canvas.drawLine(i*cellSize, (j+1)*cellSize, (i+1)*cellSize, (j+1)*cellSize, wallPaint);
                }

                if(cells[i][j].leftWall){
                    canvas.drawLine(i*cellSize, j*cellSize, i*cellSize, (j+1)*cellSize, wallPaint);
                }

                if(cells[i][j].rightWall){
                    canvas.drawLine((i+1)*cellSize, j*cellSize, (i+1)*cellSize, (j+1)*cellSize, wallPaint);
                }
            }
        }

        float margin = cellSize/10;

        canvas.drawRect((start.x*cellSize)+margin,(start.y*cellSize)+margin,(start.x+1)*cellSize-margin,(start.y+1)*cellSize-margin, startPaint);
        canvas.drawRect((exit.x*cellSize)+margin,(exit.y*cellSize)+margin,(exit.x+1)*cellSize-margin,(exit.y+1)*cellSize-margin, exitPaint);
    }

    private void movePlayer(Directions directions){
        switch (directions){
            case UP:
                if(!start.topWall)
                    start = cells[start.x][start.y-1];
                break;
            case DOWN:
                if(!start.bottomWall)
                    start = cells[start.x][start.y+1];
                break;
            case LEFT:
                if(!start.leftWall)
                    start = cells[start.x-1][start.y];
                break;
            case RIGHT:
                if(!start.rightWall)
                    start = cells[start.x+1][start.y];
                break;
        }
        if(start == exit)
            createMaze();
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(event.getAction() == MotionEvent.ACTION_DOWN){
            return true;
        }

        if(event.getAction() == MotionEvent.ACTION_MOVE){
            float x = event.getX();
            float y = event.getY();

            float centerX = hMargin + (start.x+0.5f)*cellSize;
            float centerY = vMargin + (start.y+0.5f)*cellSize;

            float dx = Math.abs(x - centerX);
            float dy = Math.abs(y - centerY);

            if(dx>dy) {
                if(x>centerX) {
                    movePlayer(Directions.RIGHT);
                }else{
                    movePlayer(Directions.LEFT);
                }
            }else{
                if(y>centerY){
                    movePlayer(Directions.DOWN);
                }else{
                    movePlayer(Directions.UP);
                }
            }
            return true;
        }

        return super.onTouchEvent(event);
    }

    private class Cell{
        boolean topWall = true;
        boolean bottomWall = true;
        boolean leftWall = true;
        boolean rightWall = true;
        boolean visited = false;

        int x,y;

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}