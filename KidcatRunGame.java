import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;

import java.awt.event.*;
import java.net.URL;

public class KidcatRunGame extends JPanel implements ActionListener {

    private long timePenalty = 0;
    private long levelStartTime;
    private long levelTimeLimit;

    private List<Coin> coins;
    private int score;
    private Timer timer;
    private Player player;
    private List<Obstacle> obstacles;
    private Background background;
    private boolean jumpRequested;
    private Random random;
    private int currentLevel = 1;
    private boolean gameStarted = false;
    private long speedMultiplier = 1; // ตัวคูณความเร็ว

    // เพิ่มตัวแปรสำหรับจัดเก็บเวลา
    private JLabel timeLabel;
    private JLabel levelNameLabel; // ประกาศ JLabel

    private boolean blinkRed = false; // ตัวแปรสำหรับควบคุมการกระพริบสี
    private int blinkCounter = 0; // ตัวนับการกระพริบสี

    private boolean blinkScoreRed = false; // ตัวแปรสำหรับควบคุมการกระพริบสีของคะแนน
    private int blinkScoreCounter = 0; // ตัวนับการกระพริบสีของคะแนน

    public KidcatRunGame() {
        this.setPreferredSize(new Dimension(800, 600));
        this.setFocusable(true);

        player = new Player();
        obstacles = new ArrayList<>();
        coins = new ArrayList<>();
        background = new Background();
        jumpRequested = false;
        random = new Random();

        timer = new Timer(10, this);

        // สร้าง JLabel สำหรับแสดงเวลาปัจจุบัน
        timeLabel = new JLabel("Time: 0.00s");
        timeLabel.setFont(new Font("Monospaced", Font.BOLD, 18)); // ตั้งฟอนต์

        // add(timeLabel);

        levelNameLabel = new JLabel();
        levelTimeLimit = 120000; // 2 นาทีต่อด่าน (ในหน่วยมิลลิวินาที)

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!gameStarted)
                    return;

                int key = e.getKeyCode();
                if (key == KeyEvent.VK_SPACE) {
                    if (!player.isJumping() && !jumpRequested) {
                        jumpRequested = true;
                    }
                } else if (key == KeyEvent.VK_S) {
                    player.startDuck();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (!gameStarted)
                    return;

                int key = e.getKeyCode();
                if (key == KeyEvent.VK_SPACE) {
                    jumpRequested = false;
                } else if (key == KeyEvent.VK_S) {
                    player.stopDuck();
                }
            }
        });
    }

    public void setBlinkRed(boolean blinkRed) {
        this.blinkRed = blinkRed;
    }

    public void setBlinkScoreRed(boolean blinkScoreRed) {
        this.blinkScoreRed = blinkScoreRed;
    }

    public void reduceTimePenalty(long penalty) {
        this.timePenalty += penalty;
    }

    public void reduceScore(int amount) {
        this.score = Math.max(0, this.score - amount);
    }

    public void startLevel() {
        levelStartTime = System.currentTimeMillis();
        timePenalty = 0;
    }

    public void updateTime() {
        if (isLevelTimeUp()) {
            showGameOverDialog(); // แสดง Game Over เมื่อเวลาหมด
        }
    }

    public void setLevelTimeLimit(long timeLimit) {
        this.levelTimeLimit = timeLimit;
    }

    private boolean isLevelTimeUp() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - levelStartTime;
        return elapsedTime >= levelTimeLimit;
    }

    private void showGameOverDialog() {

        timer.stop(); // หยุดทำงานของ timer
        gameStarted = false;

        if (!SwingUtilities.getWindowAncestor(this).isShowing()) {
            return;
        }
        // Create custom dialog
        JDialog gameOverDialog = new JDialog();
        gameOverDialog.setUndecorated(true);
        gameOverDialog.setSize(400, 300);
        gameOverDialog.setLocationRelativeTo(null);

        // Set dialog to be transparent
        gameOverDialog.setBackground(new Color(0, 0, 0, 0));

        // Main panel with transparent background
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackground(new Color(0, 0, 0, 0));

        // Game Over panel (yellow box)
        JPanel gameOverPanel = new JPanel();
        gameOverPanel.setBackground(Color.YELLOW);
        gameOverPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        gameOverPanel.setBounds(75, 80, 250, 60);

        // Game Over text
        JLabel gameOverLabel = new JLabel("GAME OVER!!");
        gameOverLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        gameOverPanel.add(gameOverLabel);

        // Try Again button
        JButton tryAgainButton = new JButton("TRY AGAIN");
        tryAgainButton.setFont(new Font("Monospaced", Font.BOLD, 16));
        tryAgainButton.setBackground(new Color(255, 99, 71)); // Coral/red color
        tryAgainButton.setForeground(Color.WHITE);
        tryAgainButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        tryAgainButton.setBounds(150, 160, 100, 40);
        tryAgainButton.setFocusPainted(false);

        // Button action
        tryAgainButton.addActionListener(e -> {
            gameOverDialog.dispose();
            resetGame(); // Restart the game
            showMainMenu(); // Show the main menu
        });

        // Add components to main panel
        mainPanel.add(gameOverPanel);
        mainPanel.add(tryAgainButton);

        // Add main panel to dialog
        gameOverDialog.add(mainPanel);

        // Show dialog
        gameOverDialog.setModal(true);
        gameOverDialog.setVisible(true);

    }

    private void resetGame() {
        // รีเซ็ตค่าต่างๆ ให้กลับไปเป็นค่าเริ่มต้น
        score = 0;
        currentLevel = 1;
        obstacles.clear();
        coins.clear();
        player = new Player(); // สร้างตัวผู้เล่นใหม่
        gameStarted = false; // เกมยังไม่เริ่ม
        timer.stop(); // หยุด timer

        // รีเซ็ตเวลาและเวลาสูงสุด
        timeLabel.setText("Time: 0.00s");

        gameStarted = false;
    }

    public void startGame() {
        gameStarted = true;
        player = new Player(); // สร้าง player ใหม่
        score = 0; // รีเซ็ตคะแนน
        currentLevel = 1; // รีเซ็ตระดับ
        obstacles.clear(); // ล้างสิ่งกีดขวาง
        coins.clear(); // ล้างเหรียญ
        generateObstacles(5); // สร้างสิ่งกีดขวางใหม่
        generateCoins(1); // สร้างเหรียญใหม่
        timer.start(); // เริ่ม timer

        // เริ่มจับเวลา
        levelStartTime = System.currentTimeMillis();

        requestFocusInWindow();
    }

    private void showMainMenu() {
        // สร้างและแสดง MainMenu ใหม่
        MainMenu mainMenu = new MainMenu();
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);

        mainMenu.addStartGameListener(e -> {
            frame.getContentPane().remove(mainMenu);
            frame.getContentPane().add(this);
            frame.pack();
            startGame();
            frame.revalidate();
            frame.repaint();
        });

        frame.getContentPane().remove(this);
        frame.getContentPane().add(mainMenu);
        frame.pack();
        frame.revalidate();
        frame.repaint();
    }

    private void generateObstacles(int count) {
        URL simpleObstacleImage;
        URL tallObstacleImage;
        URL movingObstacleImage;

        // Set images based on current level
        switch (currentLevel) {
            case 2: // สวนหลังบ้าน (Backyard)
                simpleObstacleImage = getClass().getResource("/resources/Obstacles/Level1/fence.png"); // รั้ว
                tallObstacleImage = getClass().getResource("resources/Obstacles/Level1/bee.png"); // นก
                movingObstacleImage = getClass().getResource("resources/Obstacles/Level1/flower.png"); // หิน

                break;

            case 3: // ป่า (Forest)
                simpleObstacleImage = getClass().getResource("resources/Obstacles/Level2/mushroom.png"); // ตอไม้
                tallObstacleImage = getClass().getResource("resources/Obstacles/Level2/owl.png"); // ต้นสน
                movingObstacleImage = getClass().getResource("resources/Obstacles/Level2/frog.png"); // นกฮูก

                break;

            case 4: // เมือง (City)
                simpleObstacleImage = getClass().getResource("resources/Obstacles/Level3/trashcan.png"); // ถังขยะ
                tallObstacleImage = getClass().getResource("resources/Obstacles/Level3/pigeon.png"); // เสาไฟ
                movingObstacleImage = getClass().getResource("resources/Obstacles/Level3/car.png"); // สเก็ตบอร์ด

                break;

            case 5: // หิมะ (Snow)
                simpleObstacleImage = getClass().getResource("resources/Obstacles/Level4/snowman.png"); // ตุ๊กตาหิมะ
                tallObstacleImage = getClass().getResource("resources/Obstacles/Level4/snowflake.png"); // น้ำแข็ง
                movingObstacleImage = getClass().getResource("resources/Obstacles/Level4/penguin.png"); // เพนกวิน

                break;

            default:
                simpleObstacleImage = getClass().getResource("resources/Obstacles/Level1/fence.png"); // รั้ว
                tallObstacleImage = getClass().getResource("resources/Obstacles/Level1/bee.png"); // นก
                movingObstacleImage = getClass().getResource("resources/Obstacles/Level1/flower.png"); // หิน

        }

        for (int i = 0; i < count; i++) {
            int x = 800 + i * 300;
            int y = 500;
            int type = random.nextInt(4);

            if (type == 0) {
                obstacles.add(new SimpleObstacle(x, y, simpleObstacleImage));
            } else if (type == 1) {
                obstacles.add(new TallObstacle(x, y, tallObstacleImage));
            } else if (type == 2) {
                obstacles.add(new MovingObstacle(x, y, movingObstacleImage));
            }
        }
    }

    private void generateCoins(int count) {
        List<Integer> obstacleXPositions = new ArrayList<>();
        for (Obstacle obstacle : obstacles) {
            obstacleXPositions.add(obstacle.getX());
        }

        int coinsGenerated = 0;
        int lastCoinX = -1;
        int minCoinDistance = 100;

        while (coinsGenerated < count) {
            int x = 800 + random.nextInt(600);

            boolean collision = false;
            for (int obstacleX : obstacleXPositions) {
                if (Math.abs(x - obstacleX) < 50) {
                    collision = true;
                    break;
                }
            }

            if (!collision && (lastCoinX == -1 || Math.abs(x - lastCoinX) >= minCoinDistance)) {
                int y = 400;
                coins.add(new Coin(x, y));
                lastCoinX = x;
                coinsGenerated++;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!gameStarted) {
            background.draw(g);
            return;
        }

        background.draw(g);
        Image playerImage = player.isDucking() ? player.getImage() : player.getImage();
        g.drawImage(playerImage, player.getX(), player.getY(), player.getWidth(), player.getHeight(), this);

        for (Obstacle obstacle : obstacles) {
            obstacle.draw(g);
        }

        for (Coin coin : coins) {
            coin.draw(g);
        }

        drawHPBar(g);
        drawScore(g);
        drawMapName(g);

        // แสดงเวลาปัจจุบันและเวลาสูงสุด
        drawTime(g);
    }

    private void drawHPBar(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        int heartSize = 30; // ขนาดของหัวใจแต่ละดวง
        int x = 10;
        int y = 10;
        int spacing = heartSize + 5; // ระยะห่างระหว่างหัวใจ

        int maxHearts = 5; // แสดง 5 หัวใจ (HP 100 = 5 หัวใจ, หัวใจละ 20 HP)
        int currentHearts = (int) Math.ceil(player.getHP() / 20.0); // คำนวณจำนวนหัวใจที่เหลือ

        for (int i = 0; i < maxHearts; i++) {
            int heartX = x + (spacing * i);

            if (i < currentHearts) {
                // วาดหัวใจสีแดง (HP ที่เหลืออยู่)
                drawPixelHeart(g2d, heartX, y, heartSize,
                        new Color(255, 83, 83), // สีแดงหลัก
                        new Color(255, 0, 0), // สีแดงเข้ม (ขอบ)
                        new Color(255, 136, 136) // สีแดงอ่อน (ไฮไลท์)
                );
            } else {
                // วาดหัวใจสีเทา (HP ที่หมดไป)
                drawPixelHeart(g2d, heartX, y, heartSize,
                        new Color(200, 200, 200), // สีเทาหลัก
                        new Color(160, 160, 160), // สีเทาเข้ม
                        new Color(220, 220, 220) // สีเทาอ่อน
                );
            }
        }
    }

    private void drawPixelHeart(Graphics2D g2d, int x, int y, int size, Color mainColor, Color darkColor,
            Color lightColor) {
        int pixelSize = size / 8; // ขนาดของแต่ละ pixel

        // รูปแบบ pixel สำหรับหัวใจ (1 = ขอบเข้ม, 2 = สีหลัก, 3 = ไฮไลท์)
        int[][] heartPattern = {
                { 0, 1, 1, 0, 0, 1, 1, 0 },
                { 1, 2, 2, 1, 1, 2, 2, 1 },
                { 1, 2, 2, 2, 2, 2, 2, 1 },
                { 1, 2, 2, 2, 2, 2, 2, 1 },
                { 0, 1, 2, 2, 2, 2, 1, 0 },
                { 0, 0, 1, 2, 2, 1, 0, 0 },
                { 0, 0, 0, 1, 1, 0, 0, 0 }
        };

        // วาดแต่ละ pixel ตามรูปแบบ
        for (int i = 0; i < heartPattern.length; i++) {
            for (int j = 0; j < heartPattern[0].length; j++) {
                if (heartPattern[i][j] > 0) {
                    Color pixelColor;
                    switch (heartPattern[i][j]) {
                        case 1:
                            pixelColor = darkColor;
                            break; // ขอบเข้ม
                        case 2:
                            pixelColor = mainColor;
                            break; // สีหลัก
                        case 3:
                            pixelColor = lightColor;
                            break; // ไฮไลท์
                        default:
                            pixelColor = mainColor;
                    }
                    g2d.setColor(pixelColor);
                    g2d.fillRect(x + (j * pixelSize), y + (i * pixelSize), pixelSize, pixelSize);
                }
            }
        }
    }

    private void drawScore(Graphics g) {
        g.setFont(new Font("Monospaced", Font.BOLD, 30));

        // ตรวจสอบสถานะการกระพริบสี
        if (blinkScoreRed) {
            // สลับสีระหว่างแดงและสีปกติทุกๆ 10 เฟรม
            g.setColor((blinkScoreCounter / 10) % 2 == 0 ? Color.RED : Color.BLACK);
            blinkScoreCounter++;
            if (blinkScoreCounter > 10) { // ให้กระพริบ 5 รอบแล้วหยุด
                blinkScoreRed = false;
                blinkScoreCounter = 0;
            }
        } else {
            g.setColor(Color.BLACK);
        }

        g.drawString("Score: " + score, 10, 60);
    }

    private void drawMapName(Graphics g) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Monospaced", Font.BOLD, 30));
        // แสดงชื่อของระดับในแทนที่คะแนน
        g.drawString(levelNameLabel.getText(), 280, 45); // ใช้ levelNameLabel แทน score
    }

    private void drawTime(Graphics g) {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = (currentTime - levelStartTime + timePenalty) * speedMultiplier;
        long remainingTime = Math.max(0, levelTimeLimit - elapsedTime);
        double seconds = remainingTime / 1000.0;

        if (remainingTime == 0) {
            showGameOverDialog();
            return;
        }

        g.setFont(new Font("Monospaced", Font.BOLD, 25));

        // ตรวจสอบว่าเหลือน้อยกว่า 10 วินาที หรือว่ากำลังอยู่ในสถานะกระพริบสีแดง
        if (seconds < 10 || blinkRed) {
            if (blinkRed) {
                // สลับสีระหว่างแดงและสีปกติทุกๆ 10 เฟรม
                g.setColor((blinkCounter / 10) % 2 == 0 ? Color.RED : Color.BLACK);
                blinkCounter++;
                if (blinkCounter > 10) { // ให้กระพริบ 5 รอบแล้วหยุด
                    blinkRed = false;
                    blinkCounter = 0;
                }
            } else {
                g.setColor(Color.RED);
            }
        } else {
            g.setColor(Color.BLACK);
        }

        g.drawString(String.format("Time: %.0fs", seconds), 10, 90);
    }

    public void setTimeSpeed(long multiplier) {
        this.speedMultiplier = multiplier;
        setTimeSpeed(2);
    }

    ActionListener timerListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            updateTime(); // ตรวจสอบเวลาและแสดง Game Over เมื่อเวลาหมด
        }
    };

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameStarted)
            return;

        List<Obstacle> obstaclesToRemove = new ArrayList<>();
        List<Coin> coinsToRemove = new ArrayList<>();

        // ตรวจสอบเงื่อนไขเวลา
        // ตรวจสอบเงื่อนไขเวลา
        if (isLevelTimeUp() && gameStarted) {
            // Stop the game
            timer.stop();
            gameStarted = false;

            // Show Game Over dialog immediately
            showGameOverDialog();
            return;
        }
        if (score >= 0 && currentLevel == 1) {
            currentLevel++;
            changeLevel();
        } else if (score >= 60 && currentLevel == 2) {
            currentLevel++;
            changeLevel();
        } else if (score >= 120 && currentLevel == 3) {
            currentLevel++;
            changeLevel();
        } else if (score >= 180 && currentLevel == 4) {
            currentLevel++;
            changeLevel();
        } else if (score >= 240) {
            showSuccessDialog();
        }

        for (Obstacle obstacle : obstacles) {
            obstacle.move();
            if (obstacle.getX() < -obstacle.getWidth()) {
                obstaclesToRemove.add(obstacle);
            }
        }
        obstacles.removeAll(obstaclesToRemove);

        for (Coin coin : coins) {
            coin.move();
            if (coin.getX() < -coin.getWidth()) {
                coinsToRemove.add(coin);
            }
            if (checkCollision(player, coin) && !coin.isCollected()) {
                coin.collect();
                score += 10;
            }
        }
        coins.removeAll(coinsToRemove);

        if (obstacles.isEmpty()) {
            generateObstacles(100);
            generateCoins(5);
        }

        if (jumpRequested) {
            player.startJump();
            jumpRequested = false;
        }
        player.updateMovement();
        checkCollisions();
        repaint();
    }

    private void showSuccessDialog() {
        JDialog successDialog = new JDialog();
        successDialog.setUndecorated(true);
        successDialog.setSize(400, 300);
        successDialog.setLocationRelativeTo(null);

        // Set dialog to be transparent
        successDialog.setBackground(new Color(0, 0, 0, 0));

        // Main panel with transparent background
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackground(new Color(0, 0, 0, 0));

        // Success panel (yellow box)
        JPanel successPanel = new JPanel();
        successPanel.setBackground(Color.YELLOW);
        successPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        successPanel.setBounds(75, 80, 250, 60);

        // Success text
        JLabel successLabel = new JLabel("SUCCESS !!");
        successLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        successPanel.add(successLabel);

        // Return to Home button
        JButton returnHomeButton = new JButton("RETURN TO HOME");
        returnHomeButton.setFont(new Font("Monospaced", Font.BOLD, 16));
        returnHomeButton.setBackground(new Color(47, 204, 90)); // Coral/red color
        returnHomeButton.setForeground(Color.WHITE);
        returnHomeButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        returnHomeButton.setBounds(125, 160, 150, 40);
        returnHomeButton.setFocusPainted(false);

        // Button action
        returnHomeButton.addActionListener(e -> {
            successDialog.dispose();
            resetGame();
            showMainMenu(); // Method to go back to the main menu
        });

        // Add components to main panel
        mainPanel.add(successPanel);
        mainPanel.add(returnHomeButton);

        // Add main panel to dialog
        successDialog.add(mainPanel);

        // Show dialog
        successDialog.setModal(true);
        successDialog.setVisible(true);
    }

    private void handleCollision(Obstacle obstacle) {
        if (player.getHP() <= 0) {
            showGameOverDialog();
            return;
        }

        if (!player.getInvincible()) {
            obstacle.applyEffect(this, player);
            player.setInvisible(true);

            // Timer for invincibility
            final Timer invincibleTimer = new Timer(1000, null);
            invincibleTimer.addActionListener((e) -> {
                player.setInvisible(false);
                invincibleTimer.stop();
            });
            invincibleTimer.start();
        }
    }

    private void changeLevel() {
        obstacles.clear();
        coins.clear();
        levelNameLabel.setText("");

        // กำหนดพื้นหลังและอุปสรรคตามด่านที่เลือก
        if (currentLevel == 1) {

            background.setBackground(getClass().getResource("/resources/Bg_Backyard.png"));
            generateObstacles(5);
            generateCoins(5);
            levelNameLabel.setText("Level 1: Backyard");
            levelNameLabel.setBounds(500, 500, 50, 50);// ตั้งชื่อด่าน

        } else if (currentLevel == 2) {

            background.setBackground(getClass().getResource("resources/Bg_Backyard.png"));
            generateObstacles(5);
            generateCoins(5);
            levelNameLabel.setText("Level 1: Backyard");
            levelNameLabel.setBounds(500, 500, 50, 50);// ตั้งชื่อด่าน
        } else if (currentLevel == 3) {
            background.setBackground(getClass().getResource("resources/Bg_Forest.png"));
            generateObstacles(5);
            generateCoins(5);
            levelNameLabel.setText("Level 2: Forest"); // ตั้งชื่อด่าน
        } else if (currentLevel == 4) {
            background.setBackground(getClass().getResource("resources/Bg_City.png"));
            generateObstacles(7);
            generateCoins(5);
            levelNameLabel.setText("Level 3: City"); // ตั้งชื่อด่าน
        } else if (currentLevel == 5) {
            background.setBackground(getClass().getResource("resources/Bg_Snow.png"));
            generateObstacles(7);
            generateCoins(5);
            levelNameLabel.setText("Level 4: Snow"); // ตั้งชื่อด่าน
        }

        switch (currentLevel) {
            case 1:
                levelTimeLimit = 100000; // 2 นาที
                break;
            case 2:
                levelTimeLimit = 100000; // 1 นาที 40 วินาที
                break;
            case 3:
                levelTimeLimit = 80000; // 1 นาที 30 วินาที
                break;
            case 4:
                levelTimeLimit = 70000; // 1 นาที 20 วินาที
                break;
            case 5:
                levelTimeLimit = 60000; // 1 นาที
                break;
        }

        startLevel();
    }

    private boolean checkCollision(Player player, Coin coin) {
        Rectangle playerBounds = new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight());
        Rectangle coinBounds = new Rectangle(coin.getX(), coin.getY(), coin.getWidth(), coin.getHeight());
        return playerBounds.intersects(coinBounds);
    }

    private void checkCollisions() {
        Rectangle playerBounds = new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight());

        for (Obstacle obstacle : obstacles) {
            Rectangle obstacleBounds = new Rectangle(obstacle.getX(), obstacle.getY(), obstacle.getWidth(),
                    obstacle.getHeight());

            if (playerBounds.intersects(obstacleBounds)) {
                handleCollision(obstacle);
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Kidcat Run Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false); // ป้องกันการขยายหน้าจอ

        MainMenu mainMenu = new MainMenu();
        KidcatRunGame game = new KidcatRunGame();

        mainMenu.addStartGameListener(e -> {
            frame.getContentPane().remove(mainMenu);
            frame.getContentPane().add(game);
            frame.pack();
            game.startGame();
            frame.revalidate();
            frame.repaint();
        });

        frame.getContentPane().add(mainMenu);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

// Player class
class Player {
    private int x, y;
    private double yVelocity; // Vertical velocity for jump and fall
    private final double jumpVelocity = -12; // Initial jump velocity
    private final double gravity = 0.5; // Gravity acceleration
    private final int groundLevel = 510; // Ground level y-coordinate
    private boolean isJumping; // Check if jumping
    private boolean isDucking;
    private int width = 100;
    private int height = 100;
    private final int normalHeight = 100; // Normal height
    private final int duckingHeight = 30; // Height when ducking
    private int hp = 100; // Maximum HP
    private boolean invincible = false;
    private ImageIcon[] playerRunning;
    private int currentFrame;
    private int frameDelay = 5; // Delay for switching frames
    private int frameCount = 0; // Counter for delay

    // Load the images using ImageIcon
    private ImageIcon playerDucking; // Image for ducking
    private ImageIcon playerJumping; // Image for jumping

    public Player() {
        this.x = 100; // Starting position
        this.y = groundLevel - normalHeight; // Set y to ground level minus height
        this.yVelocity = 0;
        this.isJumping = false;
        this.isDucking = false;

        playerDucking = new ImageIcon(getClass().getResource("resources/Kidcat_duck.png"));
        playerJumping = new ImageIcon(getClass().getResource("resources/Kidcat_jump.png"));

        // Load running images into the array
        playerRunning = new ImageIcon[4]; // Assuming you have 4 frames for running
        playerRunning[0] = new ImageIcon(getClass().getResource("resources/Kidcat_run1.png"));
        playerRunning[1] = new ImageIcon(getClass().getResource("resources/Kidcat_run2.png"));
        playerRunning[2] = new ImageIcon(getClass().getResource("resources/Kidcat_run3.png"));
        playerRunning[3] = new ImageIcon(getClass().getResource("resources/Kidcat_run4.png"));
    }

    public boolean isJumping() {
        return isJumping;
    }

    public void startJump() {
        if (!isJumping && !isDucking) { // Cannot jump while ducking
            this.yVelocity = jumpVelocity;
            isJumping = true;
        }
    }

    public void startDuck() {
        if (!isJumping) { // Cannot duck while jumping
            this.isDucking = true;
            this.height = duckingHeight; // Reduce height when ducking
            this.y = groundLevel - duckingHeight; // Move player closer to ground when ducking
        }
    }

    public void stopDuck() {
        if (isDucking) { // Stop ducking when the key is released
            this.isDucking = false;
            this.height = normalHeight; // Restore normal height
            this.y = groundLevel - normalHeight; // Restore normal y position
        }
    }

    public void updateMovement() {
        if (isJumping) {
            yVelocity += gravity; // Apply gravity
            y += yVelocity; // Update position

            if (y >= groundLevel - normalHeight) { // Check if player has landed
                y = groundLevel - normalHeight;
                yVelocity = 0;
                isJumping = false;
            }
        } else {
            // Update running animation frame
            frameCount++;
            if (frameCount >= frameDelay) {
                currentFrame = (currentFrame + 1) % playerRunning.length; // Loop through frames
                frameCount = 0;
            }
        }
    }

    public int getHP() {
        return hp;
    }

    public void reduceHP(int amount) {
        hp -= amount; // Reduce HP by the given amount
        if (hp < 0) {
            hp = 0; // Ensure HP doesn't go below zero
        }
    }

    public boolean getInvincible() {
        return this.invincible;
    }

    public void setInvisible(boolean value) {
        this.invincible = value;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Image getImage() {
        if (isJumping) {
            return playerJumping.getImage(); // Return jumping image
        } else if (isDucking) {
            return playerDucking.getImage(); // Return ducking image
        } else {
            return playerRunning[currentFrame].getImage(); // Return current running frame
        }
    }

    // Check if the player is ducking
    public boolean isDucking() {
        return isDucking;
    }
}

// Obstacle class
abstract class Obstacle {
    protected int x, y;
    protected final int width = 50;
    protected int height;
    protected final int groundLevel;
    protected ImageIcon obstacleImage;
    protected ObstacleEffect effect; // เพิ่มผลกระทบของอุปสรรค

    public Obstacle(int startX, int groundLevel) {
        this.x = startX;
        this.groundLevel = groundLevel;
        this.height = 50;
        this.y = groundLevel - height;
    }

    public abstract void move();

    public abstract void applyEffect(KidcatRunGame game, Player player); // เมธอดใหม่สำหรับใช้ผลกระทบ

    public void draw(Graphics g) {
        if (obstacleImage != null) {
            g.drawImage(obstacleImage.getImage(), x, y, width, height, null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(x, y, width, height);
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}

enum ObstacleEffect {
    DAMAGE, // ลดพลังชีวิต
    TIME_PENALTY, // ลดเวลา
    SCORE_PENALTY // ลดคะแนน
}

class SimpleObstacle extends Obstacle {
    public SimpleObstacle(int startX, int groundLevel, URL imagePath) {
        super(startX, groundLevel);
        this.obstacleImage = new ImageIcon(imagePath);
        this.effect = ObstacleEffect.DAMAGE;
    }

    @Override
    public void move() {
        x -= 5;
    }

    @Override
    public void applyEffect(KidcatRunGame game, Player player) {
        player.reduceHP(20); // ลดพลังชีวิต 20 หน่วย
    }
}

class TallObstacle extends Obstacle {
    public TallObstacle(int startX, int groundLevel, URL imagePath) {
        super(startX, groundLevel);
        this.height = 50;
        this.y = groundLevel - height - 70;
        this.obstacleImage = new ImageIcon(imagePath);
        this.effect = ObstacleEffect.TIME_PENALTY;
    }

    @Override
    public void move() {
        x -= 5;
    }

    @Override
    public void applyEffect(KidcatRunGame game, Player player) {
        game.reduceTimePenalty(5000); // ลดเวลา 5 วินาที
        game.setBlinkRed(true); // สั่งให้เวลาเริ่มกระพริบ
    }
}

class MovingObstacle extends Obstacle {
    private int moveDirection = 1;

    public MovingObstacle(int startX, int groundLevel, URL imagePath) {
        super(startX, groundLevel);
        this.obstacleImage = new ImageIcon(imagePath);
        this.effect = ObstacleEffect.SCORE_PENALTY;
    }

    @Override
    public void move() {
        x -= 5;
        y += moveDirection * 1;
        if (y <= groundLevel - height - 50 || y >= groundLevel - height) {
            moveDirection *= -1;
        }
    }

    @Override
    public void applyEffect(KidcatRunGame game, Player player) {
        game.reduceScore(10); // ลดคะแนน 10 แต้ม
        game.setBlinkScoreRed(true);
    }
}

// Coin class
class Coin {
    private int x, y;
    private final int width = 60; // เพิ่มขนาดให้ใหญ่ขึ้น
    private final int height = 60; // เพิ่มขนาดให้ใหญ่ขึ้น
    private boolean collected = false; // Check if coin is collected
    private ImageIcon coinImage; // เพิ่มตัวแปรสำหรับรูปภาพเหรียญ

    public Coin(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.coinImage = new ImageIcon(getClass().getResource("resources/Pratoo.png")); // โหลดรูปภาพเหรียญ
    }

    public boolean isCollected() {
        return collected;
    }

    public void move() {
        x -= 5; // Move speed
    }

    public void draw(Graphics g) {
        if (!collected) {
            g.drawImage(coinImage.getImage(), x, y, width, height, null); // วาดรูปเหรียญ
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void collect() {
        collected = true; // Mark coin as collected
    }
}

// Background class
class Background {
    private int x1;

    private ImageIcon backgroundImage;

    public Background() {
        this.x1 = 0;
    }

    public void setBackground(URL imagePath) {
        System.out.println("Changing background to: " + imagePath);
        this.backgroundImage = new ImageIcon(imagePath); // Change background image
    }

    public void draw(Graphics g) {
        g.drawImage(backgroundImage.getImage(), x1, 0, 800, 600, null);

    }
}

class GameButton extends JButton {
    private Color startColor;
    private Color hoverColor;
    private Color pressedColor;
    private int shadowOffset = 5;
    private float glowIntensity = 0f;
    private Timer glowTimer;
    private boolean isHovered = false;

    public GameButton(String text, Color mainColor) {
        super(text);
        this.startColor = mainColor;
        this.hoverColor = mainColor.brighter();
        this.pressedColor = mainColor.darker();

        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setFont(new Font("Monospaced", Font.BOLD, 24));
        setForeground(Color.BLACK); // เปลี่ยนสีตัวอักษรเป็นดำสำหรับปุ่มสีเหลือง

        // สร้างเอฟเฟกต์เรืองแสง
        glowTimer = new Timer(50, e -> {
            if (isHovered) {
                glowIntensity = Math.min(1f, glowIntensity + 0.1f);
            } else {
                glowIntensity = Math.max(0f, glowIntensity - 0.1f);
            }
            repaint();
        });

        // เพิ่ม Mouse Listeners
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                glowTimer.start();
                shake();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                glowTimer.start();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                shadowOffset = 2;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                shadowOffset = 5;
                repaint();
            }
        });
    }

    private void shake() {
        final int originalX = getX();
        final Timer timer = new Timer(50, null);
        final int[] counter = { 0 };

        timer.addActionListener(e -> {
            int offset = (counter[0] % 2 == 0) ? 2 : -2;
            setLocation(originalX + offset, getY());
            counter[0]++;

            if (counter[0] >= 6) {
                timer.stop();
                setLocation(originalX, getY());
            }
        });

        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // วาดเงา
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillRoundRect(shadowOffset, shadowOffset, getWidth() - 1, getHeight() - 1, 20, 20);

        // สร้างไล่สีแบบ gradient
        GradientPaint gradient;
        if (getModel().isPressed()) {
            gradient = new GradientPaint(0, 0, pressedColor, 0, getHeight(), pressedColor.darker());
        } else if (isHovered) {
            gradient = new GradientPaint(0, 0, hoverColor, 0, getHeight(), hoverColor.darker());
        } else {
            gradient = new GradientPaint(0, 0, startColor, 0, getHeight(), startColor.darker());
        }

        // วาดพื้นหลังปุ่ม
        g2d.setPaint(gradient);
        g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

        // เพิ่มขอบมันวาว
        if (glowIntensity > 0) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glowIntensity * 0.5f));
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
        }

        // วาดเอฟเฟกต์แสงด้านบน
        GradientPaint shine = new GradientPaint(0, 0,
                new Color(255, 255, 255, 100),
                0, getHeight() / 2,
                new Color(255, 255, 255, 0));
        g2d.setPaint(shine);
        g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() / 2, 20, 20);

        // วาดข้อความ
        FontMetrics metrics = g2d.getFontMetrics(getFont());
        int x = (getWidth() - metrics.stringWidth(getText())) / 2;
        int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();

        // เงาข้อความ
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawString(getText(), x + 1, y + 1);

        // ข้อความหลัก
        g2d.setColor(getForeground());
        g2d.drawString(getText(), x, y);

        g2d.dispose();
    }
}

class MainMenu extends JPanel {
    private GameButton startButton;
    private GameButton exitButton;
    private GameButton howToPlayButton; // New button
    private ImageIcon backgroundImage;

    public MainMenu() {
        setLayout(null);
        setPreferredSize(new Dimension(800, 600));

        backgroundImage = new ImageIcon(getClass().getResource("resources/Bg_Main.png"));

        // Start Game button (Yellow)
        startButton = new GameButton("Start Game", new Color(47, 204, 90));
        startButton.setBounds(450, 250, 200, 50);
        startButton.setForeground(Color.WHITE);
        add(startButton);

        // How to Play button (Blue)
        howToPlayButton = new GameButton("How to Play", new Color(224, 219, 38));
        howToPlayButton.setBounds(450, 320, 200, 50);
        howToPlayButton.setForeground(Color.WHITE);
        add(howToPlayButton);

        // Exit button (Red)
        exitButton = new GameButton("Exit", new Color(231, 76, 60));
        exitButton.setBounds(450, 390, 200, 50);
        exitButton.setForeground(Color.WHITE);
        add(exitButton);

        // Exit button action
        exitButton.addActionListener(e -> System.exit(0));

        // How to Play button action
        // "How to Play" Button Action
        howToPlayButton.addActionListener(e -> {
            // Create a new dialog for "How to Play"
            JDialog howToPlayDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "How to Play", true);
            howToPlayDialog.setSize(400, 300);
            howToPlayDialog.setLayout(null); // Use null layout for custom positioning

            // Instructions text
            JLabel instructionsLabel = new JLabel("<html><center><b>How to Play:</b></center><br>"
                    + "<ul style='margin-left:10px;'>"
                    + "<li>Press 'Space' to jump</li>"
                    + "<li>Press 'S' to slide</li>"
                    + "</ul></html>");
            instructionsLabel.setFont(new Font("Monospaced", Font.PLAIN, 22));
            instructionsLabel.setBounds(40, 20, 320, 180); // Position text within the dialog
            instructionsLabel.setHorizontalAlignment(SwingConstants.CENTER);

            // "BACK" Button
            JButton backButton = new JButton("BACK");
            backButton.setFont(new Font("Monospaced", Font.BOLD, 16));
            backButton.setBackground(new Color(255, 215, 0)); // Yellow color
            backButton.setForeground(Color.BLACK);
            backButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            backButton.setBounds(150, 220, 100, 40); // Position the button within the dialog
            backButton.setFocusPainted(false);

            // Button action to close the dialog
            backButton.addActionListener(event -> howToPlayDialog.dispose());

            // Add components to the dialog
            howToPlayDialog.add(instructionsLabel);
            howToPlayDialog.add(backButton);

            // Set dialog position and visibility
            howToPlayDialog.setLocationRelativeTo(this);
            howToPlayDialog.setVisible(true);
        });
    }

    public void addStartGameListener(ActionListener listener) {
        startButton.addActionListener(listener);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null && backgroundImage.getImage() != null) {
            g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
        }
    }
}