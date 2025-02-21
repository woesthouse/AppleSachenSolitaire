package applesachensolitaire;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;

public class Displayoutput extends JPanel {
    private static final int BOARD_ROWS = Mainapplecode.BOARD_ROWS;
    private static final int BOARD_COLS = Mainapplecode.BOARD_COLS;
    private static final int APPLE_ROWS = Mainapplecode.APPLE_ROWS;
    private static final int APPLE_COLS = Mainapplecode.APPLE_COLS;
    private static final int CELL_SIZE = Mainapplecode.CELL_SIZE;
    private static final int MARGIN = Mainapplecode.MARGIN;
    private static final int APPLE_START_ROW = Mainapplecode.APPLE_START_ROW;
    private static final int APPLE_START_COL = Mainapplecode.APPLE_START_COL;

    // MainAppleCode의 상수들 사용
    private final int[][] board;
    private final int score;
    private final int timeLeft;
    private final boolean isDragging;
    private final ArrayList<Point> selectedPoints;
    private final Point currentPoint;
    private final Point startPoint;

    public Displayoutput(int[][] board, int score, int timeLeft, boolean isDragging, 
                        ArrayList<Point> selectedPoints, Point currentPoint, Point startPoint) {
        this.board = board;
        this.score = score;
        this.timeLeft = timeLeft;
        this.isDragging = isDragging;
        this.selectedPoints = selectedPoints;
        this.currentPoint = currentPoint;
        this.startPoint = startPoint;
        setLayout(new BorderLayout());
        // 기본 배경색 설정
        setBackground(Color.WHITE);
        
        // 게임 타이틀 레이블 추가
        JLabel titleLabel = new JLabel("사과 사천성", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);
        
        // 게임 메인 패널
        JPanel gamePanel = new JPanel();
        gamePanel.setBackground(new Color(240, 240, 240));
        add(gamePanel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        
        drawBoard(g2d);
        drawUI(g2d);
        if(isDragging) {
            drawSelection(g2d);
            drawDragPath(g2d);
        }
    }

    private void drawBoard(Graphics2D g) {
        // 사과 그리기
        for (int i = 0; i < APPLE_ROWS; i++) {
            for (int j = 0; j < APPLE_COLS; j++) {
                if (board[i][j] > 0) {
                    int x = MARGIN + j * CELL_SIZE;  // APPLE_START_COL 제거
                    int y = MARGIN + i * CELL_SIZE;  // APPLE_START_ROW 제거
                    
                    // 사과 몸체 그리기
                    g.setColor(new Color(255, 59, 48));  // 빨간 사과
                    g.fillOval(x + 5, y + 5, CELL_SIZE - 10, CELL_SIZE - 10);
                    
                    // 사과 꼭지 그리기
                    g.setColor(new Color(76, 217, 100));  // 초록색 꼭지
                    g.fillRect(x + CELL_SIZE/2 - 3, y + 2, 6, 8);
                    
                    // 숫자 그리기
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("맑은 고딕", Font.BOLD, 20));
                    String number = String.valueOf(board[i][j]);
                    FontMetrics fm = g.getFontMetrics();
                    int textX = x + (CELL_SIZE - fm.stringWidth(number)) / 2;
                    int textY = y + (CELL_SIZE + fm.getAscent() - fm.getDescent()) / 2;
                    g.drawString(number, textX, textY);
                }
            }
        }
    }
    
    private void drawSelection(Graphics2D g) {
        if (!selectedPoints.isEmpty()) {
            g.setColor(new Color(255, 255, 0, 100));
            for (Point p : selectedPoints) {
                int x = MARGIN + (p.y + APPLE_START_COL) * CELL_SIZE;
                int y = MARGIN + (p.x + APPLE_START_ROW) * CELL_SIZE;
                g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
            }
        }
    }
    
    private void drawDragPath(Graphics2D g) {
        if (selectedPoints.size() > 1) {
            g.setColor(Color.YELLOW);
            g.setStroke(new BasicStroke(3));
            for (int i = 0; i < selectedPoints.size() - 1; i++) {
                Point p1 = selectedPoints.get(i);
                Point p2 = selectedPoints.get(i + 1);
                int x1 = MARGIN + (p1.y + APPLE_START_COL) * CELL_SIZE + CELL_SIZE/2;
                int y1 = MARGIN + (p1.x + APPLE_START_ROW) * CELL_SIZE + CELL_SIZE/2;
                int x2 = MARGIN + (p2.y + APPLE_START_COL) * CELL_SIZE + CELL_SIZE/2;
                int y2 = MARGIN + (p2.x + APPLE_START_ROW) * CELL_SIZE + CELL_SIZE/2;
                g.drawLine(x1, y1, x2, y2);
            }
        }
    }
    
    private void drawUI(Graphics2D g) {
        int rightMargin = 20;
        int topMargin = 20;
        int gaugeWidth = 200;
        int gaugeHeight = 20;
        
        // UI 요소들을 오른쪽에 배치
        int x = getWidth() - gaugeWidth - rightMargin;
        
        // 점수 표시
        g.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        g.setColor(Color.BLACK);
        g.drawString("점수: " + score, x, topMargin);
        
        // 시간 게이지 (점수 아래에 배치)
        int y = topMargin + 25;  // 점수 텍스트 아래로
        
        // 게이지 배경
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(x, y, gaugeWidth, gaugeHeight);
        
        // 남은 시간 게이지
        float timeRatio = (float) timeLeft / 120.0f;
        int remainingWidth = (int)(gaugeWidth * timeRatio);
        g.setColor(new Color(50, 205, 50));
        g.fillRect(x, y, remainingWidth, gaugeHeight);
        
        // 게이지 테두리
        g.setColor(Color.BLACK);
        g.drawRect(x, y, gaugeWidth, gaugeHeight);
        
        // 시간 텍스트
        g.drawString(timeLeft + "초", x + gaugeWidth + 10, y + 15);
    }
}
