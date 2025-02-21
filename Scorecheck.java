package applesachensolitaire;

import java.awt.Point;
import java.util.ArrayList;

public class Scorecheck {
    public Scorecheck() {
        // 빈 생성자
    }

    public boolean isValidMove(Point last, Point current) {
        if (last == null || current == null) return false;
        return (last.x == current.x && Math.abs(last.y - current.y) == 1) ||
               (last.y == current.y && Math.abs(last.x - current.x) == 1);
    }

    public boolean isSumTen(ArrayList<Point> points, int[][] board) {
        if (points == null || points.isEmpty()) return false;
        int sum = 0;
        for (Point p : points) {
            if (p != null && isValidPosition(p, board)) {
                sum += board[p.x][p.y];
            }
        }
        return sum == 10;
    }

    public boolean isValidPath(ArrayList<Point> points) {
        if (points == null || points.size() < 2) return true;
        for (int i = 0; i < points.size() - 1; i++) {
            if (!isValidMove(points.get(i), points.get(i + 1))) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidPosition(Point p, int[][] board) {
        return p.x >= 0 && p.x < board.length && 
               p.y >= 0 && p.y < board[0].length;
    }
}