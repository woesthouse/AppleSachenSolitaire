import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class AppleSachenSolitaire extends JFrame {
    private static final int BOARD_ROWS = 14;    // 전체 게임판 행 수정
    private static final int BOARD_COLS = 21;    // 전체 게임판 열 수정
    private static final int APPLE_ROWS = 10;    // 사과 배치 행
    private static final int APPLE_COLS = 17;    // 사과 배치 열
    private static final int CELL_SIZE = 50;     // 셀 크기
    private static final int MARGIN = 50;        // 외부 여백
    
    // 사과 시작 위치 계산 (중앙 정렬)
    private static final int APPLE_START_ROW = (BOARD_ROWS - APPLE_ROWS) / 2;
    private static final int APPLE_START_COL = (BOARD_COLS - APPLE_COLS) / 2;
    
    private int[][] board;
    private int score;
    private int timeLeft;  
    private boolean isDragging;
    private Point startPoint;
    private Point currentPoint;
    private ArrayList<Point> selectedPoints;
    private Timer gameTimer;
    
    public AppleSachenSolitaire() {
        setTitle("Apple Sachen Solitaire");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize((BOARD_COLS * CELL_SIZE) + 250, (BOARD_ROWS * CELL_SIZE) + MARGIN * 2);
        
        initializeGame();
        GamePanel gamePanel = new GamePanel();
        add(gamePanel);
        addMouseListeners(gamePanel);
        setupTimer();
    }
    
    private void initializeGame() {
        board = new int[APPLE_ROWS][APPLE_COLS];
        score = 0;
        timeLeft = 120;
        isDragging = false;
        selectedPoints = new ArrayList<>();
        
        // 보드 초기화 (1~9 랜덤 배치)
        Random random = new Random();
        for(int i = 0; i < APPLE_ROWS; i++) {
            for(int j = 0; j < APPLE_COLS; j++) {
                board[i][j] = random.nextInt(9) + 1;
            }
        }
    }
    
    private void addMouseListeners(GamePanel gamePanel) {
        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startDrag(e.getPoint());
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                endDrag();
            }
        });
        
        gamePanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                updateDrag(e.getPoint());
            }
        });
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
    
    private void startDrag(Point p) {
        isDragging = true;
        startPoint = convertToGridPoint(p);
        selectedPoints.clear();
        selectedPoints.add(startPoint);
        currentPoint = startPoint;
    }
    
    private void updateDrag(Point p) {
        if(!isDragging) return;
        
        Point gridPoint = convertToGridPoint(p);
        if(isValidMove(gridPoint)) {
            currentPoint = gridPoint;
            if(!selectedPoints.contains(gridPoint)) {
                selectedPoints.add(gridPoint);
            }
        }
        repaint();
    }
    
    private void endDrag() {
        if (isDragging) {
            // 모든 연결이 유효하고 합이 10일 때만 사과 제거
            if (isValidPath() && isSumTen()) {
                int removedCount = removeSelectedApples(); // 제거된 사과 개수 반환
                updateScore(removedCount); // 제거된 사과 개수에 따라 점수 업데이트
            } else {
                // 드래그가 유효하지 않은 경우 점수 업데이트를 하지 않음
                System.out.println("Invalid path or sum not equal to 10. No score update.");
            }
        }
        isDragging = false;
        selectedPoints.clear();
        repaint();
    }
    
    
    private Point convertToGridPoint(Point p) {
        // 전체 게임판 기준으로 좌표 변환
        int x = (p.y - MARGIN) / CELL_SIZE; // y 좌표를 기준으로 행 계산
        int y = (p.x - MARGIN) / CELL_SIZE; // x 좌표를 기준으로 열 계산
        
        // 전체 게임판 영역 내의 좌표로 제한
        if (x >= 0 && x < BOARD_ROWS && y >= 0 && y < BOARD_COLS) {
            return new Point(x, y);  // 게임판 좌표 반환
        }
        return new Point(-1, -1);
    }
    
    private boolean isValidMove(Point p) {
        if(selectedPoints.isEmpty()) return true;
        Point last = selectedPoints.get(selectedPoints.size()-1);
        // 가로나 세로로만 이동 가능
        return (last.x == p.x && Math.abs(last.y - p.y) == 1) ||
               (last.y == p.y && Math.abs(last.x - p.x) == 1);
    }
    
    private boolean isSumTen() {
        int sum = 0;
        for(Point p : selectedPoints) {
            sum += getAppleValue(p);
        }
        return sum == 10;
    }
    
    private boolean isValidPath() {
        if (selectedPoints.size() < 2) return true;
    
        // 전체 드래그에서 꺾인 점의 갯수 체크
        int turns = 0;
        for (int i = 0; i < selectedPoints.size() - 1; i++) {
            Point current = selectedPoints.get(i);
            Point next = selectedPoints.get(i + 1);
            
            // 두 점 사이의 꺾임 횟수 계산
            turns += countTurns(current, next); // countTurns 호출
        }
    
        return turns < 3; // 꺾인 점이 3번 이상이면 false
    }
    
    private int countTurns(Point start, Point end) {
        int turns = 0;
        Point prevDirection = null;
        
        // 현재 위치
        int x = start.x;
        int y = start.y;
        
        // 목표 위치까지 이동하면서 꺾임 횟수 계산
        while (x != end.x || y != end.y) {
            // 다음 위치 결정
            int nextX = x;
            int nextY = y;
            
            if (x != end.x) {
                nextX += Integer.compare(end.x, x);
            }
            if (y != end.y) {
                nextY += Integer.compare(end.y, y);
            }
            
            // 현재 방향 계산
            Point currentDirection = new Point(
                Integer.compare(nextX, x),
                Integer.compare(nextY, y)
            );
            
            // 첫 번째 이동일 경우 prevDirection을 초기화
            if (prevDirection == null) {
                prevDirection = currentDirection;
            }
            
            if (currentDirection.x != prevDirection.x || currentDirection.y != prevDirection.y) {
                turns++;
                System.out.println("Turn detected! Total turns so far: " + turns);
            }
            
            // 이전 방향 업데이트
            prevDirection = currentDirection;
            x = nextX;
            y = nextY;
        }
        
        System.out.println("Total turns from " + start + " to " + end + ": " + turns);
        return turns;
    }
    
    private int removeSelectedApples() {
        int removedCount = 0; // 제거된 사과의 개수

        for (Point p : selectedPoints) {
            if (isApplePosition(p)) {
                int appleX = p.x - APPLE_START_ROW;
                int appleY = p.y - APPLE_START_COL;
                if (board[appleX][appleY] > 0) { // 사과가 존재하는 경우에만 제거
                    board[appleX][appleY] = 0; // 사과 제거
                    removedCount++; // 제거된 사과 개수 증가
                }
            }
        }

        return removedCount; // 제거된 사과 개수 반환
    }
    
    private void updateScore(int removedCount) {
        score += removedCount; // 제거된 사과 개수만큼 점수 증가
    }
    
    private void endGame() {
        gameTimer.stop();
        JOptionPane.showMessageDialog(this, 
            "Game Over!\nFinal Score: " + score, 
            "Game Over", 
            JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }
    
    private boolean isApplePosition(Point p) {
        int appleX = p.x - APPLE_START_ROW;
        int appleY = p.y - APPLE_START_COL;
        return appleX >= 0 && appleX < APPLE_ROWS && 
               appleY >= 0 && appleY < APPLE_COLS;
    }
    
    private int getAppleValue(Point p) {
        if (isApplePosition(p)) {
            int appleX = p.x - APPLE_START_ROW;
            int appleY = p.y - APPLE_START_COL;
            return board[appleX][appleY];
        }
        return 0;
    }
    
    private Color getPathColor() {
        //사과 사이에 3번 이상 꺾인 경우가 있으면 검은색
        for (int i = 0; i < selectedPoints.size() - 1; i++) {
            Point current = selectedPoints.get(i);
            Point next = selectedPoints.get(i + 1);
            if (countTurns(current, next) > 2) {
                return Color.BLACK;
            }
        }
        // 그렇지 않으면 합에 따라 색상 결정
        return isSumTen() ? Color.ORANGE : Color.YELLOW;
    }
    
    private class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setStroke(new BasicStroke(3));
            
            // 전체 게임판 테두리 그리기
            g2d.setColor(Color.BLACK);
            g2d.drawRect(MARGIN, MARGIN, 
                        BOARD_COLS * CELL_SIZE, BOARD_ROWS * CELL_SIZE);
            
            drawBoard(g2d);
            drawUI(g2d);
            if(isDragging) {
                drawSelection(g2d);
                drawDragPath(g2d);
            }
        }
        
        private void drawBoard(Graphics2D g) {
            for(int i = 0; i < APPLE_ROWS; i++) {
                for(int j = 0; j < APPLE_COLS; j++) {
                    if(board[i][j] > 0) {
                        // 중앙 정렬된 위치에 사과 그리기
                        drawApple(g, 
                                (j + APPLE_START_COL) * CELL_SIZE, 
                                (i + APPLE_START_ROW) * CELL_SIZE, 
                                board[i][j]);
                    }
                }
            }
        }
        
        private void drawApple(Graphics2D g, int x, int y, int value) {
            int drawX = x + MARGIN;
            int drawY = y + MARGIN;
            
            g.setColor(Color.RED);
            g.fillOval(drawX+5, drawY+5, CELL_SIZE-10, CELL_SIZE-10);
            
            Point gridPoint = new Point(y/CELL_SIZE, x/CELL_SIZE);
            if(selectedPoints.contains(gridPoint)) {
                g.setColor(getPathColor());  // 색상 처리 통합
                g.drawOval(drawX+5, drawY+5, CELL_SIZE-10, CELL_SIZE-10);
            }
            
            g.setStroke(new BasicStroke(1));
            g.setColor(Color.WHITE);
            g.drawString(String.valueOf(value), drawX+20, drawY+30);
            g.setStroke(new BasicStroke(3));
        }
        
        private void drawSelection(Graphics2D g) {
            g.setColor(new Color(255, 255, 255, 0));
            
            for(int i = 1; i < selectedPoints.size(); i++) {
                Point p = selectedPoints.get(i);
                g.fillRect(p.y * CELL_SIZE, p.x * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
            
            if(currentPoint != null && !currentPoint.equals(startPoint)) {
                g.fillRect(currentPoint.y * CELL_SIZE, currentPoint.x * CELL_SIZE, 
                          CELL_SIZE, CELL_SIZE);
            }
        }
        
        private void drawDragPath(Graphics2D g) {
            if (selectedPoints.size() < 2) return;

            g.setColor(getPathColor());  // 색상 처리 통합

            for (int i = 0; i < selectedPoints.size() - 1; i++) {
                Point p1 = selectedPoints.get(i);
                Point p2 = selectedPoints.get(i + 1);
                g.drawLine(p1.y * CELL_SIZE + CELL_SIZE / 2 + MARGIN, 
                           p1.x * CELL_SIZE + CELL_SIZE / 2 + MARGIN,
                           p2.y * CELL_SIZE + CELL_SIZE / 2 + MARGIN, 
                           p2.x * CELL_SIZE + CELL_SIZE / 2 + MARGIN);
            }

            if (currentPoint != null && !selectedPoints.isEmpty()) {
                Point last = selectedPoints.get(selectedPoints.size() - 1);
                g.drawLine(last.y * CELL_SIZE + CELL_SIZE / 2 + MARGIN, 
                           last.x * CELL_SIZE + CELL_SIZE / 2 + MARGIN,
                           currentPoint.y * CELL_SIZE + CELL_SIZE / 2 + MARGIN, 
                           currentPoint.x * CELL_SIZE + CELL_SIZE / 2 + MARGIN);
            }
        }
        
        private void drawUI(Graphics2D g) {
            g.setStroke(new BasicStroke(1));
            g.setColor(Color.BLACK);
            g.drawString("Score: " + score, (BOARD_COLS * CELL_SIZE) + MARGIN + 40, 30);
            
            // 시간 게이지
            int gaugeHeight = getHeight() - 80;
            int remainingHeight = gaugeHeight * timeLeft / 120;
            int startY = getHeight() - 40;
            
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect((BOARD_COLS * CELL_SIZE) + MARGIN + 40, 
                      startY - gaugeHeight, 20, gaugeHeight);
            
            g.setColor(Color.GREEN);
            g.fillRect((BOARD_COLS * CELL_SIZE) + MARGIN + 40, 
                      startY - remainingHeight, 20, remainingHeight);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AppleSachenSolitaire().setVisible(true);
        });
    }
}
