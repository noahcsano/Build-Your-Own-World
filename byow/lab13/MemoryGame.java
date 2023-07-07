package byow.lab13;

import byow.Core.RandomUtils;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.util.Random;

public class MemoryGame {
    /** The width of the window of this game. */
    private int width;
    /** The height of the window of this game. */
    private int height;
    /** The current round the user is on. */
    private int round;
    /** The Random object used to randomly generate Strings. */
    private Random rand;
    /** Whether or not the game is over. */
    private String randomWord;
    private int length = 0;
    private boolean gameOver;
    /** Whether or not it is the player's turn. Used in the last section of the
     * spec, 'Helpful UI'. */
    private boolean playerTurn;
    /** The characters we generate random Strings from. */
    private static final char[] CHARACTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    /** Encouraging phrases. Used in the last section of the spec, 'Helpful UI'. */
    private static final String[] ENCOURAGEMENT = {"You can do this!", "I believe in you!",
                                                   "You got this!", "You're a star!", "Go Bears!",
                                                   "Too easy for you!", "Wow, so impressive!"};

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter a seed");
            return;
        }
        long seed = Long.parseLong(args[0]);
        MemoryGame game = new MemoryGame(40, 40, seed);
        game.startGame();
    }

    public MemoryGame(int width, int height, long seed) {
        /* Sets up StdDraw so that it has a width by height grid of 16 by 16 squares as its canvas
         * Also sets up the scale so the top left is (0,0) and the bottom right is (width, height)
         */
        this.width = width;
        this.height = height;
        StdDraw.setCanvasSize(this.width * 16, this.height * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, this.width);
        StdDraw.setYscale(0, this.height);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();
        //TODO: Initialize random number generator
        rand = new Random(seed);
        StdDraw.setPenColor(Color.WHITE);
    }

    public String generateRandomString(int n) {
        //TODO: Generate random string of letters of length n
        String string = "";
        for (int i = 0; i < n; i += 1) {
            int randomStringIndex = RandomUtils.uniform(rand, CHARACTERS.length - 1);
            string += CHARACTERS[randomStringIndex];
        }
        return string;
    }

    public void drawFrame(String s) {
        //TODO: Take the string and display it in the center of the screen
        //TODO: If game is not over, display relevant game information at the top of the screen
        Font newFont = new Font("Arial", Font.BOLD, 20);
        String status = "Watch!";
        StdDraw.text(width / 2 ,height - 2, status);
        String roundCount = "Round: " + this.round;
        StdDraw.show();
        StdDraw.setFont(newFont);
        for (int i = 0; i < s.length(); i += 1) {
            StdDraw.clear(Color.BLACK);
            StdDraw.text(width / 2 ,height - 2, status);
            StdDraw.text(3 ,height - 2, roundCount);
            StdDraw.show();
            StdDraw.pause(500);
            flashSequence(s.substring(i, i + 1));
        }
    }

    public void flashSequence(String letters) {
        //TODO: Display each character in letters, making sure to blank the screen between letters
        Font font = new Font("Arial", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.text(width / 2,height / 2, letters.substring(0, 1));

        Font newFont = new Font("Arial", Font.BOLD, 20);
        StdDraw.setFont(newFont);
        String randomString = ENCOURAGEMENT[RandomUtils.uniform(rand, ENCOURAGEMENT.length - 1)];
        StdDraw.text(width - 5 ,height - 2, randomString);

        StdDraw.show();
        StdDraw.pause(1000);
        StdDraw.clear(Color.BLACK);
        String status = "Watch!";
        StdDraw.text(width / 2 ,height - 2, status);
        String roundCount = "Round: " + this.round;
        StdDraw.text(3 ,height - 2, roundCount);
        StdDraw.show();
    }

    public String solicitNCharsInput(int n) {
        String input = "";

        //TODO: Read n letters of player input
        while (StdDraw.hasNextKeyTyped()) {
            StdDraw.nextKeyTyped();
        }
        for (int i = 0; i < n;) {
            String status = "Type!";
            StdDraw.text(width / 2,height - 2, status);
            String roundCount = "Round: " + this.round;
            StdDraw.text(3 ,height - 2, roundCount);
            StdDraw.show();
            if (StdDraw.hasNextKeyTyped()) {
                StdDraw.clear(Color.BLACK);
                Font newFont = new Font("Arial", Font.BOLD, 20);
                StdDraw.setFont(newFont);
                String randomString = ENCOURAGEMENT[RandomUtils.uniform(rand, ENCOURAGEMENT.length - 1)];
                StdDraw.text(width - 5 ,height - 2, randomString);
                StdDraw.show();
                i += 1;
                String eachInput = Character.toString(StdDraw.nextKeyTyped());
                Font font = new Font("Arial", Font.BOLD, 30);
                StdDraw.setFont(font);
                StdDraw.text(width / 2,height / 2, eachInput);
                StdDraw.show();
                input += eachInput;
            }
        }
        StdDraw.pause(500);
        Font font = new Font("Arial", Font.BOLD, 30);
        StdDraw.setFont(font);
        if (input.equals(this.randomWord)) {
            this.round += 1;
            StdDraw.clear(Color.BLACK);
            StdDraw.text(width / 2,height / 2, "Round: " + this.round);
            StdDraw.show();
            StdDraw.pause(500);
            startGame();
        } else {
            StdDraw.clear(Color.BLACK);
            StdDraw.text(width / 2,height / 2, "Game Over! You made it to round: " + this.round);
            StdDraw.show();
        }
        return null;
    }

    public void startGame() {
        //TODO: Set any relevant variables before the game starts
        this.length += 1;
        this.randomWord = generateRandomString(this.length);
        this.drawFrame(this.randomWord);
        StdDraw.clear(Color.BLACK);
        StdDraw.show();
        solicitNCharsInput(this.randomWord.length());
        //TODO: Establish Engine loop
    }
}
