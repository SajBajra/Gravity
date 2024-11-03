package main;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Random;

public class GravitySwapGame extends JPanel implements ActionListener {
    private static final int SCREEN_WIDTH = 800;
    private static final int SCREEN_HEIGHT = 600;
    private static final int PLAYER_SIZE = 50;
    private static final int COLLECTIBLE_SIZE = 30;
    private static final int OBSTACLE_WIDTH = 50;
    private static final int OBSTACLE_HEIGHT = 100;
    private static final int GAME_SPEED = 120;
    private static final int INITIAL_OBSTACLE_SPEED = 5; // Initial speed of obstacles
    private static final int SPEED_INCREASE_INTERVAL = 100; // Interval for speed increase
    private static final int MAX_OBSTACLE_SPEED = 15; // Maximum speed for obstacles

    private int playerX; // X-coordinate of player
    private boolean gravityFlipped; // True if player is at the top
    private Timer timer;
    private ArrayList<Rectangle> collectibles;
    private ArrayList<Rectangle> obstacles;
    private int score = 0;
    private Random rand = new Random();
    private boolean gameOver = false;
    private int obstacleSpeed = INITIAL_OBSTACLE_SPEED; // Current speed of obstacles
    private int frameCount = 0; // Frame count for generating obstacles
    private boolean horizontalLayout; // True if the game is in horizontal layout

    public GravitySwapGame() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    gravityFlipped = !gravityFlipped; // Swap gravity
                } else if (e.getKeyCode() == KeyEvent.VK_R && gameOver) {
                    restartGame(); // Restart the game
                }
            }
        });

        initializeGame();
        timer = new Timer(1000 / GAME_SPEED, this);
        timer.start();
    }

    private void initializeGame() {
        playerX = SCREEN_WIDTH / 2 - PLAYER_SIZE / 2;
        gravityFlipped = false; // Start with player at the bottom
        collectibles = new ArrayList<>();
        obstacles = new ArrayList<>();
        score = 0;
        gameOver = false;
        horizontalLayout = rand.nextBoolean(); // Randomly choose layout
        obstacleSpeed = INITIAL_OBSTACLE_SPEED; // Reset obstacle speed
        createCollectibles();
    }

    private void createCollectibles() {
        // Create a collectible at the top or bottom
        int x = SCREEN_WIDTH; // Start off-screen
        int y = gravityFlipped ? 50 : SCREEN_HEIGHT - COLLECTIBLE_SIZE - 50; // Top or bottom
        collectibles.add(new Rectangle(x, y, COLLECTIBLE_SIZE, COLLECTIBLE_SIZE));
    }

    private void createObstacles() {
        // Create obstacles at random positions and layout
        int x = SCREEN_WIDTH; // Start off-screen
        int y;
        
        if (horizontalLayout) {
            y = gravityFlipped ? 50 : SCREEN_HEIGHT - OBSTACLE_HEIGHT - 50; // Bottom or top
        } else {
            y = rand.nextInt(SCREEN_HEIGHT - OBSTACLE_HEIGHT); // Random Y for vertical obstacles
        }
        
        obstacles.add(new Rectangle(x, y, horizontalLayout ? OBSTACLE_WIDTH : OBSTACLE_HEIGHT, 
                                    horizontalLayout ? OBSTACLE_HEIGHT : OBSTACLE_WIDTH));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawPlayer(g);
        drawCollectibles(g);
        drawObstacles(g);
        drawScore(g);
        if (gameOver) {
            drawGameOver(g);
        }
    }

    private void drawPlayer(Graphics g) {
        g.setColor(Color.CYAN);
        g.fillRect(playerX, gravityFlipped ? 50 : SCREEN_HEIGHT - PLAYER_SIZE - 50, PLAYER_SIZE, PLAYER_SIZE);
    }

    private void drawCollectibles(Graphics g) {
        g.setColor(Color.YELLOW);
        for (Rectangle collectible : collectibles) {
            g.fillRect(collectible.x, collectible.y, COLLECTIBLE_SIZE, COLLECTIBLE_SIZE);
        }
    }

    private void drawObstacles(Graphics g) {
        g.setColor(Color.RED);
        for (Rectangle obstacle : obstacles) {
            g.fillRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
        }
    }

    private void drawScore(Graphics g) {
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, 10, 20);
    }

    private void drawGameOver(Graphics g) {
        g.setColor(Color.WHITE);
        g.drawString("Game Over!", SCREEN_WIDTH / 2 - 30, SCREEN_HEIGHT / 2);
        g.drawString("Final Score: " + score, SCREEN_WIDTH / 2 - 40, SCREEN_HEIGHT / 2 + 20);
        g.drawString("Press R to Restart", SCREEN_WIDTH / 2 - 60, SCREEN_HEIGHT / 2 + 40);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            moveObjects();
            checkCollisions();
            repaint();
            frameCount++; // Increment frame count
        }
    }

    private void moveObjects() {
        // Move collectibles to the left
        for (int i = 0; i < collectibles.size(); i++) {
            Rectangle collectible = collectibles.get(i);
            collectible.x -= 5; // Move collectible left

            // Remove collectible if it goes off-screen
            if (collectible.x < -COLLECTIBLE_SIZE) {
                collectibles.remove(i);
                i--;
            }
        }

        // Move obstacles to the left
        for (int i = 0; i < obstacles.size(); i++) {
            Rectangle obstacle = obstacles.get(i);
            obstacle.x -= obstacleSpeed; // Move obstacle left

            // Remove obstacle if it goes off-screen
            if (obstacle.x < -OBSTACLE_WIDTH) {
                obstacles.remove(i);
                i--;
                score += 10; // Increment score for avoiding obstacles
            }
        }

        // Control the generation of new obstacles and collectibles
        if (frameCount % 50 == 0) { // Generate new collectibles every 50 frames
            createCollectibles();
        }
        if (frameCount % 100 == 0) { // Generate new obstacles every 100 frames
            createObstacles();
        }

        // Randomly switch layout after every few frames
        if (frameCount % 150 == 0) {
            horizontalLayout = rand.nextBoolean(); // Randomly switch layout
        }

        // Increase obstacle speed based on score
        if (score / SPEED_INCREASE_INTERVAL > 0) {
            obstacleSpeed = Math.min(INITIAL_OBSTACLE_SPEED + (score / SPEED_INCREASE_INTERVAL), MAX_OBSTACLE_SPEED);
        }
    }

    private void checkCollisions() {
        Rectangle playerRect = new Rectangle(playerX, gravityFlipped ? 50 : SCREEN_HEIGHT - PLAYER_SIZE - 50, PLAYER_SIZE, PLAYER_SIZE);

        // Check for collectible collision
        for (int i = 0; i < collectibles.size(); i++) {
            Rectangle collectible = collectibles.get(i);
            if (playerRect.intersects(collectible)) {
                collectibles.remove(i);
                score += 10; // Increment score for collecting items
                i--; // Adjust index after removal
            }
        }

        // Check for obstacle collision
        for (Rectangle obstacle : obstacles) {
            if (playerRect.intersects(obstacle)) {
                gameOver = true; // End the game on collision
            }
        }
    }

    private void restartGame() {
        initializeGame(); // Reset the game state
        frameCount = 0; // Reset frame count
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Gravity Swap Game");
        GravitySwapGame game = new GravitySwapGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
    }
}
