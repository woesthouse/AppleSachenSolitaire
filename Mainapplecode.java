package applesachensolitaire;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class Mainapplecode extends JFrame {
    public static final int BOARD_ROWS = 14;
    public static final int BOARD_COLS = 21;
    public static final int APPLE_ROWS = 10;
    public static final int APPLE_COLS = 17;
    public static final int CELL_SIZE = 50;
    public static final int MARGIN = 50;
    public static final int APPLE_START_ROW = (BOARD_ROWS - APPLE_ROWS) / 2;
    public static final int APPLE_START_COL = (BOARD_COLS - APPLE_COLS) / 2;
    
    private int[][] board;
    private int score;
    private int timeLeft;
    private boolean isDragging;
    private Point startPoint;
    private Point currentPoint;
    private ArrayList<Point> selectedPoints;
    private Timer gameTimer;
    private Displayoutput displayPanel;
    private final Scorecheck scoreChecker;

    public Mainapplecode() {
        scoreChecker = new Scorecheck();
        setTitle("사과 사천성");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        
        initializeGame();
        setupUI();
        setupTimer();
    }

    private void initializeGame() {
        board = new int[APPLE_ROWS][APPLE_COLS];
        score = 0;
        timeLeft = 120;
        isDragging = false;
        selectedPoints = new ArrayList<>();
        
        Random random = new Random();
        for(int i = 0; i < APPLE_ROWS; i++) {
            for(int j = 0; j < APPLE_COLS; j++) {
                board[i][j] = random.nextInt(9) + 1;
            }
        }
    }
    
    private void setupTimer() {
        gameTimer = new Timer(1000, e -> {
            timeLeft--;
            if(timeLeft <= 0) {
                endGame();
            }
            repaint();
        });
        gameTimer.start();
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        displayPanel = new Displayoutput(board, score, timeLeft, isDragging, selectedPoints, currentPoint, startPoint);
        add(displayPanel, BorderLayout.CENTER);
        
        // 마우스 이벤트 리스너 추가
        displayPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startDrag(e.getPoint());
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                endDrag();
            }
        });
        
        displayPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                updateDrag(e.getPoint());
            }
        });
    }
    
    private void startDrag(Point p) {
        int gridX = (p.y - MARGIN) / CELL_SIZE - APPLE_START_ROW;
        int gridY = (p.x - MARGIN) / CELL_SIZE - APPLE_START_COL;
        
        if (isValidGridPosition(gridX, gridY)) {
            isDragging = true;
            startPoint = new Point(gridX, gridY);
            currentPoint = startPoint;
            selectedPoints.clear();
            selectedPoints.add(startPoint);
            displayPanel.repaint();
        }
    }
    
    private void updateDrag(Point p) {
        if (!isDragging) return;
        
        int gridX = (p.y - MARGIN) / CELL_SIZE - APPLE_START_ROW;
        int gridY = (p.x - MARGIN) / CELL_SIZE - APPLE_START_COL;
        
        if (isValidGridPosition(gridX, gridY)) {
            Point newPoint = new Point(gridX, gridY);
            if (!newPoint.equals(currentPoint)) {
                if (scoreChecker.isValidMove(selectedPoints.get(selectedPoints.size()-1), newPoint)) {
                    currentPoint = newPoint;
                    if (!selectedPoints.contains(currentPoint)) {
                        selectedPoints.add(currentPoint);
                    }
                }
            }
            displayPanel.repaint();
        }
    }
    
    private void endDrag() {
        if (!isDragging) return;
        
        if (scoreChecker.isSumTen(selectedPoints, board) && 
            scoreChecker.isValidPath(selectedPoints)) {
            // 선택된 사과들 제거
            for (Point p : selectedPoints) {
                board[p.x][p.y] = 0;
            }
            score += selectedPoints.size();
        }
        
        isDragging = false;
        selectedPoints.clear();
        displayPanel.repaint();
    }
    
    private boolean isValidGridPosition(int x, int y) {
        return x >= 0 && x < APPLE_ROWS && y >= 0 && y < APPLE_COLS && board[x][y] > 0;
    }
    
    private void endGame() {
        gameTimer.stop();
        JOptionPane.showMessageDialog(this, 
            "게임 종료!\n최종 점수: " + score, 
            "게임 종료", 
            JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    public static void startGame() {
        JFrame frame = new JFrame("사과 사천성");
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        
        int[][] board = new int[APPLE_ROWS][APPLE_COLS];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                board[i][j] = (int)(Math.random() * 3) + 1;
            }
        }
        
        frame.add(new Displayoutput(board, 0, 120, false, 
            new ArrayList<>(), new Point(0,0), new Point(0,0)));
        
        frame.setVisible(true);
    }
}
