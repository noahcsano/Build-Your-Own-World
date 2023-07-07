package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import edu.princeton.cs.introcs.StdDraw;

import javax.swing.plaf.ColorUIResource;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 60;
    public static final int HEIGHT = 34;

    private static final int MAX_ROOM_WIDTH = 20;
    private static final int MAX_ROOM_HEIGHT = 20;

    private static final int ROOM_ATTEMPTS = 1000;
    private LinkedList<int[]> entranceCoordinates = new LinkedList<int[]>();

    private Random random;

    private String keyBoardInputs = "";

    private int[] avatarLocation = new int[2];

    private int[][] boardAsGraph = new int[HEIGHT][WIDTH];

    private int doorsFound = 0;

    TETile[][] finalWorldFrame = new TETile[WIDTH][HEIGHT];

    private int roomCount;

    private static final File CWD = new File(System.getProperty("user.dir"));

    private static final File BOARD_FILE = Utils.join(CWD, "board.txt");

    private static final File DOORS_FOUND_FILE = Utils.join(CWD, "doorsFound.txt");

    private static final File ROOM_COUNT_FILE = Utils.join(CWD, "roomCount.txt");

    private static final File AVATAR_LOCATION_FILE = Utils.join(CWD, "avatarFile.txt");

    private boolean gameActive = true;

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        gameMenu();
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.

//        ter.initialize(WIDTH, HEIGHT);

        initEmptyBoard(finalWorldFrame);

        input = input.toLowerCase();

        if (input.charAt(0) == 'n') {
            random = new Random(Long.parseLong(input.substring(1, input.indexOf("s"))));

            keyBoardInputs = input.substring(input.indexOf("s") + 1);

            placeRooms(finalWorldFrame);

            generateGraphFromBoard(finalWorldFrame);

            generateHallways(finalWorldFrame);

            placeRandomObjects(finalWorldFrame);
        } else if (input.charAt(0) == 'l') {
            loadGame();
            if (input.indexOf(":Q") == -1) {
                keyBoardInputs = input.substring(1);
            } else {
                keyBoardInputs = input.substring(1, input.indexOf(":Q"));
            }
        }

        moveAvatar(finalWorldFrame);

//        ter.renderFrame(finalWorldFrame);

        return finalWorldFrame;
    }

    private void placeRooms(TETile[][] board) {
        for (int i = 0; i < ROOM_ATTEMPTS; i++) {
            int x = RandomUtils.uniform(random, WIDTH);
            int y = RandomUtils.uniform(random, HEIGHT);

            int roomWidth = RandomUtils.uniform(random, 3, MAX_ROOM_WIDTH);
            int roomHeight = RandomUtils.uniform(random, 3, MAX_ROOM_HEIGHT);
            createRectangularRoom(x, y, roomWidth, roomHeight, board);
        }
    }

    private void createRectangularRoom(int x, int y, int width, int height, TETile[][] board) {
        if (x + width >= WIDTH - 1 || y + height >= HEIGHT - 3 || x <= 1 || y <= 3) {
            // Spill over perimeter
            return;
        } else {
            // Checking to see if there's anything in the way
            for (int i = x; i < x + width; i++) {
                for (int j = y; j < y + height; j++) {
                    if (!board[i][j].equals(Tileset.NOTHING)
                            || i != WIDTH && !board[i + 1][j].equals(Tileset.NOTHING)
                            || i != 0 && !board[i - 1][j].equals(Tileset.NOTHING)
                            || j != HEIGHT && !board[i][j + 1].equals(Tileset.NOTHING)
                            || j != 0 && !board[i][j - 1].equals(Tileset.NOTHING)) {
                        return;
                    }
                }
            }
            roomCount += 1;
            // Build room
            int perimeter = (2 * width) + (2 * height) - 4;
            int entranceLocation = RandomUtils.uniform(random, 2, perimeter);
            while (entranceLocation == height || entranceLocation == perimeter - height + 1) {
                entranceLocation = RandomUtils.uniform(random, 2, perimeter);
            }
            int count = 1;
            for (int i = x; i < x + width; i++) {
                for (int j = y; j < y + height; j++) {
                    if (i == x || i == (x + width - 1) || j == y || j == (y + height - 1)) {
                        if (count == entranceLocation) {
                            board[i][j] = Tileset.FLOOR;
                            int[] currentEntranceCoordinate = {i, j};
                            entranceCoordinates.add(currentEntranceCoordinate);
                        } else {
                            board[i][j] = Tileset.WALL;
                        }
                        count++;
                    } else {
                        board[i][j] = Tileset.FLOOR;
                    }
                }
            }
        }
    }

    private void initEmptyBoard(TETile[][] board) {
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                board[i][j] = Tileset.NOTHING;
            }
        }
    }

    private void generateGraphFromBoard(TETile[][] board) {
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                if (board[i][j].equals(Tileset.WALL)) {
                    this.boardAsGraph[HEIGHT - 1 - j][i] = Integer.MAX_VALUE;
                } else {
                    this.boardAsGraph[HEIGHT - 1 - j][i] = 1;
                }
            }
        }
    }

    private void generateHallways(TETile[][] board) {
        entranceCoordinates.addLast(entranceCoordinates.get(0));
        for (int i = 0; i < entranceCoordinates.size() - 1; i++) {
            int[] currentEntrance = entranceCoordinates.get(i);
            int[] destinationEntrance = entranceCoordinates.get(i + 1);

            if (currentEntrance[0] < destinationEntrance[0]) {
                buildHorizontalHallway(board, currentEntrance[0], currentEntrance[1],
                        destinationEntrance[0] - currentEntrance[0]);
            } else if (currentEntrance[0] > destinationEntrance[0]) {
                buildHorizontalHallway(board, destinationEntrance[0], destinationEntrance[1],
                        currentEntrance[0] - destinationEntrance[0]);
            }
            if (currentEntrance[1] < destinationEntrance[1]) {
                buildVerticalHallway(board, destinationEntrance[0], currentEntrance[1],
                        destinationEntrance[1] - currentEntrance[1]);
            } else if (currentEntrance[1] > destinationEntrance[1]) {
                buildVerticalHallway(board, destinationEntrance[0], destinationEntrance[1],
                        currentEntrance[1] - destinationEntrance[1]);
            }
        }
        closeHallways(board);
    }

    private void closeHallways(TETile[][] board) {
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                if (board[i][j].equals(Tileset.NOTHING)) {
                    int floorCount = 0;
                    if (i > 0 && (board[i - 1][j].equals(Tileset.FLOOR)
                            || board[i - 1][j].equals(Tileset.LOCKED_DOOR))) {
                        floorCount += 1;
                    }
                    if (i < WIDTH - 1 && (board[i + 1][j].equals(Tileset.FLOOR)
                            || board[i + 1][j].equals(Tileset.LOCKED_DOOR))) {
                        floorCount += 1;
                    }
                    if (j < HEIGHT - 1 && (board[i][j + 1].equals(Tileset.FLOOR)
                            || board[i][j + 1].equals(Tileset.LOCKED_DOOR))) {
                        floorCount += 1;
                    }
                    if (j > 0 && (board[i][j - 1].equals(Tileset.FLOOR)
                            ||  board[i][j - 1].equals(Tileset.LOCKED_DOOR))) {
                        floorCount += 1;
                    }
                    if (floorCount == 1) {
                        board[i][j] = Tileset.WALL;
                    } else if (floorCount > 1) {
                        board[i][j] = Tileset.FLOOR;
                        if (i < WIDTH - 1 && board[i + 1][j].equals(Tileset.NOTHING)
                                || board[i + 1][j].equals(Tileset.LOCKED_DOOR)) {
                            board[i + 1][j] = Tileset.WALL;
                        }
                        if (i > 0 && board[i - 1][j].equals(Tileset.NOTHING)
                                || board[i - 1][j].equals(Tileset.LOCKED_DOOR)) {
                            board[i - 1][j] = Tileset.WALL;
                        }
                        if (j < HEIGHT - 1 && board[i][j + 1].equals(Tileset.NOTHING)
                                || board[i][j + 1].equals(Tileset.LOCKED_DOOR)) {
                            board[i][j + 1] = Tileset.WALL;
                        }
                        if (j > 0 && board[i][j - 1].equals(Tileset.NOTHING)
                                || board[i][j - 1].equals(Tileset.LOCKED_DOOR)) {
                            board[i][j - 1] = Tileset.WALL;
                        }
                    }
                }
            }
        }
    }

    private void buildHorizontalHallway(TETile[][] board, int startingXPos, int yPos, int length) {
        if (startingXPos + length >= WIDTH) {
            return;
        }
        for (int i = 0; i <= length + 1; i++) {
            board[startingXPos + i][yPos] = Tileset.FLOOR;
            if (board[startingXPos + i][yPos - 1].equals(Tileset.NOTHING)) {
                board[startingXPos + i][yPos - 1] = Tileset.WALL;
            }
            if (board[startingXPos + i][yPos + 1].equals(Tileset.NOTHING)) {
                board[startingXPos + i][yPos + 1] = Tileset.WALL;
            }
        }
        if (board[startingXPos + length][yPos].equals(Tileset.WALL)) {
            board[startingXPos + length][yPos] = Tileset.FLOOR;
        }
        if (board[startingXPos - 1][yPos].equals(Tileset.WALL)) {
            board[startingXPos - 1][yPos] = Tileset.FLOOR;
        }
    }

    private void buildVerticalHallway(TETile[][] board, int xPos, int startingYPos, int length) {
        if (startingYPos + length >= HEIGHT) {
            return;
        }
        for (int i = 0; i <= length + 1; i++) {
            board[xPos][startingYPos + i] = Tileset.FLOOR;
            if (board[xPos - 1][startingYPos + i].equals(Tileset.NOTHING)) {
                board[xPos - 1][startingYPos + i] = Tileset.WALL;
            }
            if (board[xPos + 1][startingYPos + i].equals(Tileset.NOTHING)) {
                board[xPos + 1][startingYPos + i] = Tileset.WALL;
            }
        }
        if (board[xPos][startingYPos + length + 1].equals(Tileset.WALL)) {
            board[xPos][startingYPos + length + 1] = Tileset.FLOOR;
        }
        if (board[xPos][startingYPos + length - 1].equals(Tileset.WALL)) {
            board[xPos][startingYPos + length - 1] = Tileset.FLOOR;
        }
    }

    private void placeRandomObjects(TETile[][] board) {
        for (int i = 0; i < roomCount; i++) {
            int randomX = RandomUtils.uniform(random, 0, WIDTH);
            int randomY = RandomUtils.uniform(random, 0, HEIGHT);
            while (!board[randomX][randomY].equals(Tileset.FLOOR)) {
                randomX = RandomUtils.uniform(random, 0, WIDTH);
                randomY = RandomUtils.uniform(random, 0, HEIGHT);
            }
            board[randomX][randomY] = Tileset.LOCKED_DOOR;
        }
        placeAvatar(board);
    }

    private void placeAvatar(TETile[][] board) {
        int randomX = RandomUtils.uniform(random, 0, WIDTH);
        int randomY = RandomUtils.uniform(random, 0, HEIGHT);
        while (!board[randomX][randomY].equals(Tileset.FLOOR)) {
            randomX = RandomUtils.uniform(random, 0, WIDTH);
            randomY = RandomUtils.uniform(random, 0, HEIGHT);
        }
        board[randomX][randomY] = Tileset.AVATAR;
        avatarLocation[0] = randomX;
        avatarLocation[1] = randomY;
    }

    private void moveAvatar(TETile[][] board) {
        boolean waitingForQ = false;
        for (int i = 0; i < keyBoardInputs.length(); i++) {
            char direction = keyBoardInputs.charAt(i);
            if (direction == 'w' && gameActive) {
                if (avatarLocation[1] + 1 != HEIGHT
                        && !board[avatarLocation[0]][avatarLocation[1] + 1].equals(Tileset.WALL)) {
                    if (board[avatarLocation[0]][avatarLocation[1] + 1]
                            .equals(Tileset.LOCKED_DOOR)) {
                        board[avatarLocation[0]][avatarLocation[1] + 1] = Tileset.AVATAR;
                        board[avatarLocation[0]][avatarLocation[1]] = Tileset.FLOOR;
                        doorsFound++;
                    } else {
                        board[avatarLocation[0]][avatarLocation[1] + 1] = Tileset.AVATAR;
                        board[avatarLocation[0]][avatarLocation[1]] = Tileset.FLOOR;
                    }
                    avatarLocation[1] += 1;
                }
                waitingForQ = false;
            } else if (direction == 's' && gameActive) {
                if (avatarLocation[1] - 1 >= 0
                        && !board[avatarLocation[0]][avatarLocation[1] - 1].equals(Tileset.WALL)) {
                    if (board[avatarLocation[0]][avatarLocation[1] - 1]
                            .equals(Tileset.LOCKED_DOOR)) {
                        board[avatarLocation[0]][avatarLocation[1] - 1] = Tileset.AVATAR;
                        board[avatarLocation[0]][avatarLocation[1]] = Tileset.FLOOR;
                        doorsFound++;
                    } else {
                        board[avatarLocation[0]][avatarLocation[1] - 1] = Tileset.AVATAR;
                        board[avatarLocation[0]][avatarLocation[1]] = Tileset.FLOOR;
                    }
                    avatarLocation[1] -= 1;
                }
                waitingForQ = false;
            } else if (direction == 'a' && gameActive) {
                if (avatarLocation[0] - 1 >= 0
                        && !board[avatarLocation[0] - 1][avatarLocation[1]].equals(Tileset.WALL)) {
                    if (board[avatarLocation[0] - 1][avatarLocation[1]]
                            .equals(Tileset.LOCKED_DOOR)) {
                        board[avatarLocation[0] - 1][avatarLocation[1]] = Tileset.AVATAR;
                        board[avatarLocation[0]][avatarLocation[1]] = Tileset.FLOOR;
                        doorsFound++;
                    } else {
                        board[avatarLocation[0] - 1][avatarLocation[1]] = Tileset.AVATAR;
                        board[avatarLocation[0]][avatarLocation[1]] = Tileset.FLOOR;
                    }
                    avatarLocation[0] -= 1;
                }
                waitingForQ = false;
            } else if (direction == 'd' && gameActive) {
                if (avatarLocation[0] + 1 != WIDTH
                        && !board[avatarLocation[0] + 1][avatarLocation[1]].equals(Tileset.WALL)) {
                    if (board[avatarLocation[0] + 1][avatarLocation[1]]
                            .equals(Tileset.LOCKED_DOOR)) {
                        board[avatarLocation[0] + 1][avatarLocation[1]] = Tileset.AVATAR;
                        board[avatarLocation[0]][avatarLocation[1]] = Tileset.FLOOR;
                        doorsFound++;
                    } else {
                        board[avatarLocation[0] + 1][avatarLocation[1]] = Tileset.AVATAR;
                        board[avatarLocation[0]][avatarLocation[1]] = Tileset.FLOOR;
                    }
                    avatarLocation[0] += 1;
                }
                waitingForQ = false;
            } else if (direction == 'l') {
                waitingForQ = false;
                loadGame();
                gameActive = true;
            } else if (direction == ':') {
                waitingForQ = true;
            } else if (direction == 'q' && waitingForQ) {
                saveGame(board);
                gameActive = false;
            }
        }
    }

    private void moveAvatar(TETile[][] board, char direction) {
        if (direction == 'w' && gameActive) {
            if (avatarLocation[1] + 1 != HEIGHT
                    && !board[avatarLocation[0]][avatarLocation[1] + 1].equals(Tileset.WALL)) {
                if (board[avatarLocation[0]][avatarLocation[1] + 1].equals(Tileset.LOCKED_DOOR)) {
                    board[avatarLocation[0]][avatarLocation[1] + 1] = Tileset.AVATAR;
                    board[avatarLocation[0]][avatarLocation[1]] = Tileset.FLOOR;
                    doorsFound++;
                } else {
                    board[avatarLocation[0]][avatarLocation[1] + 1] = Tileset.AVATAR;
                    board[avatarLocation[0]][avatarLocation[1]] = Tileset.FLOOR;
                }
                avatarLocation[1] += 1;
            }
        } else if (direction == 's' && gameActive) {
            if (avatarLocation[1] - 1 >= 0
                    && !board[avatarLocation[0]][avatarLocation[1] - 1].equals(Tileset.WALL)) {
                if (board[avatarLocation[0]][avatarLocation[1] - 1].equals(Tileset.LOCKED_DOOR)) {
                    board[avatarLocation[0]][avatarLocation[1] - 1] = Tileset.AVATAR;
                    board[avatarLocation[0]][avatarLocation[1]] = Tileset.FLOOR;
                    doorsFound++;
                } else {
                    board[avatarLocation[0]][avatarLocation[1] - 1] = Tileset.AVATAR;
                    board[avatarLocation[0]][avatarLocation[1]] = Tileset.FLOOR;
                }
                avatarLocation[1] -= 1;
            }
        } else if (direction == 'a' && gameActive) {
            if (avatarLocation[0] - 1 >= 0
                    && !board[avatarLocation[0] - 1][avatarLocation[1]].equals(Tileset.WALL)) {
                if (board[avatarLocation[0] - 1][avatarLocation[1]].equals(Tileset.LOCKED_DOOR)) {
                    board[avatarLocation[0] - 1][avatarLocation[1]] = Tileset.AVATAR;
                    board[avatarLocation[0]][avatarLocation[1]] = Tileset.FLOOR;
                    doorsFound++;
                } else {
                    board[avatarLocation[0] - 1][avatarLocation[1]] = Tileset.AVATAR;
                    board[avatarLocation[0]][avatarLocation[1]] = Tileset.FLOOR;
                }
                avatarLocation[0] -= 1;
            }
        } else if (direction == 'd' && gameActive) {
            if (avatarLocation[0] + 1 != WIDTH
                    && !board[avatarLocation[0] + 1][avatarLocation[1]].equals(Tileset.WALL)) {
                if (board[avatarLocation[0] + 1][avatarLocation[1]].equals(Tileset.LOCKED_DOOR)) {
                    board[avatarLocation[0] + 1][avatarLocation[1]] = Tileset.AVATAR;
                    board[avatarLocation[0]][avatarLocation[1]] = Tileset.FLOOR;
                    doorsFound++;
                } else {
                    board[avatarLocation[0] + 1][avatarLocation[1]] = Tileset.AVATAR;
                    board[avatarLocation[0]][avatarLocation[1]] = Tileset.FLOOR;
                }
                avatarLocation[0] += 1;
            }
        }
    }

    private void saveGame(TETile[][] board) {
        try {
            if (!BOARD_FILE.exists()) {
                BOARD_FILE.createNewFile();
            }
            if (!DOORS_FOUND_FILE.exists()) {
                DOORS_FOUND_FILE.createNewFile();
            }
            if (!AVATAR_LOCATION_FILE.exists()) {
                AVATAR_LOCATION_FILE.createNewFile();
            }
            if (!ROOM_COUNT_FILE.exists()) {
                ROOM_COUNT_FILE.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String boardRepresentation = "";
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                if (board[i][j].equals(Tileset.WALL)) {
                    boardRepresentation += "w";
                } else if (board[i][j].equals(Tileset.NOTHING)) {
                    boardRepresentation += "n";
                } else if (board[i][j].equals(Tileset.FLOOR)) {
                    boardRepresentation += "f";
                } else if (board[i][j].equals(Tileset.AVATAR)) {
                    boardRepresentation += "a";
                } else if (board[i][j].equals(Tileset.LOCKED_DOOR)) {
                    boardRepresentation += "d";
                }
            }
        }
        Utils.writeContents(BOARD_FILE, boardRepresentation);
        Utils.writeObject(DOORS_FOUND_FILE, doorsFound);
        Integer[] avatarLocationLocal = new Integer[2];
        avatarLocationLocal[0] = this.avatarLocation[0];
        avatarLocationLocal[1] = this.avatarLocation[1];
        Utils.writeObject(AVATAR_LOCATION_FILE, avatarLocationLocal);
        Utils.writeObject(ROOM_COUNT_FILE, roomCount);
    }

    private void loadGame() {
        String boardRepresentation = Utils.readContentsAsString(BOARD_FILE);
        initEmptyBoard(finalWorldFrame);
        int count = 0;
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                if (boardRepresentation.charAt(count) == 'w') {
                    finalWorldFrame[i][j] = Tileset.WALL;
                } else if (boardRepresentation.charAt(count) == 'f') {
                    finalWorldFrame[i][j] = Tileset.FLOOR;
                } else if (boardRepresentation.charAt(count) == 'n') {
                    finalWorldFrame[i][j] = Tileset.NOTHING;
                } else if (boardRepresentation.charAt(count) == 'a') {
                    finalWorldFrame[i][j] = Tileset.AVATAR;
                } else if (boardRepresentation.charAt(count) == 'd') {
                    finalWorldFrame[i][j] = Tileset.LOCKED_DOOR;
                }
                count += 1;
            }
        }

        doorsFound = Utils.readObject(DOORS_FOUND_FILE, Integer.class);
        Integer[] avatarLocationLocal = Utils.readObject(AVATAR_LOCATION_FILE, Integer[].class);
        this.avatarLocation[0] = avatarLocationLocal[0];
        this.avatarLocation[1] = avatarLocationLocal[1];
        roomCount = Utils.readObject(ROOM_COUNT_FILE, Integer.class);
    }

    private void gameMenu() {
        StdDraw.setCanvasSize(WIDTH * 16, HEIGHT * 18);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();

        StdDraw.clear(Color.BLACK);
        int centerX = WIDTH / 2;
        int titleY = (int) (HEIGHT * 0.75);
        StdDraw.setFont(new Font("Serif", Font.BOLD, 40));
        StdDraw.setPenColor(ColorUIResource.WHITE);
        StdDraw.text(centerX, titleY, "CS61B: The Game");

        int centerY = HEIGHT / 2;
        StdDraw.setFont(new Font("Serif", Font.BOLD, 25));
        StdDraw.setPenColor(ColorUIResource.WHITE);
        StdDraw.text(centerX, centerY, "New Game (N) \nLoad Game (L) \nQuit (Q)");

        StdDraw.show();

        String playerInput = "";
        while (playerInput.length() < 1) {
            if (StdDraw.hasNextKeyTyped()) {
                playerInput += StdDraw.nextKeyTyped();
            }
        }

        playerInput.toLowerCase();

        if (playerInput.equals("n")) {
            boolean seedTyped = false;
            String seed = "";
            while (!seedTyped) {
                StdDraw.clear(Color.BLACK);
                StdDraw.text(centerX, titleY, "Please enter input seed");
                StdDraw.text(centerX, centerY, "Seed: " + seed);
                if (StdDraw.hasNextKeyTyped()) {
                    char key = StdDraw.nextKeyTyped();
                    if (key == 's' && seed.length() > 0) {
                        seedTyped = true;
                        StdDraw.clear(Color.BLACK);
                    } else if (Character.isDigit(key)) {
                        seed += key;
                    }
                }
                StdDraw.show();
            }
            startNewGame(Long.parseLong(seed));
        } else if (playerInput.equals("l")) {
            resumeGame();
        } else if (playerInput.equals("q")) {
            StdDraw.clear(StdDraw.BLACK);
            StdDraw.show();
        } else {
            gameMenu();
        }
    }

    private void resumeGame() {
        ter.initialize(WIDTH, HEIGHT);

        loadGame();

        controlGame();

        StdDraw.clear(StdDraw.BLACK);
        StdDraw.show();
    }

    private void startNewGame(long seed) {
        ter.initialize(WIDTH, HEIGHT);

        initEmptyBoard(finalWorldFrame);

        random = new Random(seed);

        placeRooms(finalWorldFrame);

        generateGraphFromBoard(finalWorldFrame);

        generateHallways(finalWorldFrame);

        placeRandomObjects(finalWorldFrame);

        controlGame();

        StdDraw.clear(StdDraw.BLACK);
        StdDraw.show();
    }

    private void controlGame() {
        boolean gameOver = false;
        boolean lineOfSightMode = false;
        while (!gameOver) {
            StdDraw.clear(StdDraw.BLACK);
            renderBoard(finalWorldFrame, lineOfSightMode);

            StdDraw.setPenColor(Color.WHITE);
            StdDraw.setFont(new Font("Serif", Font.PLAIN, 16));
            StdDraw.textLeft(0, HEIGHT - 1, "Found " + doorsFound + " of " + roomCount
                    + " total doors");

            Date date = new Date();
            StdDraw.text(WIDTH / 2, HEIGHT - 1, date.toString());

            int mouseX = (int) StdDraw.mouseX();
            int mouseY = (int) StdDraw.mouseY();

            if (mouseX >= WIDTH) {
                mouseX = WIDTH - 1;
            }
            if (mouseY >= HEIGHT) {
                mouseY = HEIGHT - 1;
            }
            TETile currentTileMouse = finalWorldFrame[mouseX][mouseY];
            StdDraw.textRight(WIDTH - 1, HEIGHT - 1, currentTileMouse.description());

            StdDraw.text(WIDTH / 2, 1, "Press SPACE to toggle Line of Sight View");
            StdDraw.textLeft(0, 1, "Collect all doors to complete game!");

            StdDraw.show();

            String input = "";
            if (doorsFound == this.roomCount) {
                gameOver = true;
            }

            if (StdDraw.hasNextKeyTyped()) {
                input = String.valueOf(StdDraw.nextKeyTyped());
                input.toLowerCase();
                if (input.equals("w") || input.equals("s") || input.equals("a")
                        || input.equals("d")) {
                    moveAvatar(finalWorldFrame, input.charAt(0));
                } else if (input.equals(":")) {
                    while (!StdDraw.hasNextKeyTyped()) {
                        // Wait for next key to be typed
                        input += "";
                    }
                    input += StdDraw.nextKeyTyped();
                    input.toLowerCase();
                    if (input.charAt(1) == 'q') {
                        saveGame(finalWorldFrame);
                        gameOver = true;
                    }
                } else if (input.equals(" ")) {
                    lineOfSightMode = !lineOfSightMode;
                }
            }
            StdDraw.pause(75);
        }
    }

    private void renderBoard(TETile[][] board, boolean lineOfSight) {
        if (!lineOfSight) {
            ter.renderFrame(board);
        } else {
            TETile[][] lineOfSightBoard = new TETile[WIDTH][HEIGHT];
            int viewRadius = 5;
            for (int i = 0; i < WIDTH; i++) {
                for (int j = 0; j < HEIGHT; j++) {
                    if (findDistance(i, j) < viewRadius) {
                        lineOfSightBoard[i][j] = board[i][j];
                    } else {
                        lineOfSightBoard[i][j] = Tileset.NOTHING;
                    }
                }
            }
            ter.renderFrame(lineOfSightBoard);
        }
    }

    private int findDistance(int x, int y) {
        return (int) Math.hypot(avatarLocation[0] - x, avatarLocation[1] - y);
    }
}
