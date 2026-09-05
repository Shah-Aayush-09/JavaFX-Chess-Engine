package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class JavaFXChess extends Application {

    static final int TILE_SIZE = 75;
    static final int MODE_PVP = 0;
    static final int MODE_BOT = 1;
    static final int MODE_PUZZLE = 2;
    static final int SIDEBAR_WIDTH = 350;

    static final int BOARD_OFFSET = 30;
    static final int TOTAL_BOARD_SIZE = (TILE_SIZE * 8) + (BOARD_OFFSET * 2);

    ChessLogic engine = new ChessLogic();
    StackPane[][] visualTiles = new StackPane[8][8];

    int lastFromRow = -1, lastFromCol = -1;
    int lastToRow = -1, lastToCol = -1;
    int selectedRow = -1, selectedCol = -1;

    PuzzleStack puzzleStack = Puzzles.getPredefinedPuzzles();

    BorderPane rootLayout;
    GridPane chessGrid = new GridPane();
    Label statusLabel = new Label();
    Label scoreLabel = new Label();
    VBox notationContainer = new VBox(6);
    VBox controlsContainer = new VBox(10);
    ScrollPane notationScrollPane;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        rootLayout = new BorderPane();
        rootLayout.setStyle("-fx-background-color: #1e1e1e;");

        chessGrid.setPrefSize(TOTAL_BOARD_SIZE, TOTAL_BOARD_SIZE);
        chessGrid.setMaxSize(TOTAL_BOARD_SIZE, TOTAL_BOARD_SIZE);
        chessGrid.setMinSize(TOTAL_BOARD_SIZE, TOTAL_BOARD_SIZE);

        showMainMenu();
        loadWindowIcon(stage);

        Scene scene = new Scene(rootLayout, TOTAL_BOARD_SIZE + SIDEBAR_WIDTH, TOTAL_BOARD_SIZE);
        stage.setScene(scene);
        stage.setTitle("Interactive Chess Engine");
        stage.setResizable(false);
        stage.show();
    }

    private void loadWindowIcon(Stage stage) {
        InputStream iconStream = getClass().getResourceAsStream("/logo.png");
        if (iconStream != null) {
            stage.getIcons().add(new Image(iconStream));
        }
    }

    void showMainMenu() {
        engine.gameOver = false;
        VBox menuBox = new VBox(25);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setMinWidth(TOTAL_BOARD_SIZE + SIDEBAR_WIDTH);

        Label title = new Label("CHESS ENGINE");
        title.setFont(new Font("Segoe UI Bold", 36));
        title.setTextFill(Color.web("#eeeed2"));

        Button pvpBtn = createMenuButton("Local Arena (Player vs Player)");
        Button botBtn = createMenuButton("Bot Match (Player vs Computer)");
        Button puzzleBtn = createMenuButton("One Move Puzzles");
        Button profileBtn = createMenuButton("Check Profile Stats");

        pvpBtn.setOnAction(e -> startNewGame(MODE_PVP));
        botBtn.setOnAction(e -> startNewGame(MODE_BOT));
        puzzleBtn.setOnAction(e -> startPuzzleMode());
        profileBtn.setOnAction(e -> showProfileModal());

        menuBox.getChildren().addAll(title, pvpBtn, botBtn, puzzleBtn, profileBtn);
        rootLayout.setCenter(menuBox);
        rootLayout.setRight(null);
    }

    void showProfileModal() {
        Stage popupStage = new Stage();
        popupStage.setTitle("User Arena Profile");
        popupStage.setResizable(false);

        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(25));
        container.setStyle("-fx-background-color: #151515; -fx-border-color: #bacb46; -fx-border-width: 2px;");

        Label statsLabel = new Label(DatabaseManager.fetchProfileStats());
        statsLabel.setFont(new Font("Consolas", 14));
        statsLabel.setTextFill(Color.web("#eeeed2"));
        statsLabel.setWrapText(true);

        Button closeBtn = new Button("Close Profile");
        closeBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #eeeed2; -fx-font-weight: bold; -fx-padding: 8 20 8 20; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> popupStage.close());

        container.getChildren().addAll(statsLabel, closeBtn);
        popupStage.setScene(new Scene(container, 500, 420));
        popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popupStage.showAndWait();
    }

    Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setFont(new Font("Segoe UI Semibold", 15));
        String base = "-fx-background-color: #2d2d2d; -fx-text-fill: #eeeed2; -fx-min-width: 320px; -fx-max-width: 320px; -fx-padding: 14px; -fx-background-radius: 6px; -fx-cursor: hand;";
        String hover = "-fx-background-color: #3d3d3d; -fx-text-fill: #bacb46; -fx-min-width: 320px; -fx-max-width: 320px; -fx-padding: 14px; -fx-background-radius: 6px; -fx-cursor: hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        return btn;
    }

    private VBox createBaseSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setStyle("-fx-background-color: #151515; -fx-padding: 20px; -fx-border-color: #2d2d2d; -fx-border-width: 0 0 0 2px;");
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setMinWidth(SIDEBAR_WIDTH);
        sidebar.setMaxWidth(SIDEBAR_WIDTH);
        sidebar.setPrefWidth(SIDEBAR_WIDTH);
        return sidebar;
    }

    void startNewGame(int mode) {
        resetMatchState(mode);

        rootLayout.setCenter(chessGrid);
        VBox sidebar = createBaseSidebar();

        statusLabel.setFont(new Font("Segoe UI Bold", 16));
        statusLabel.setTextFill(Color.web("#bacb46"));
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(SIDEBAR_WIDTH - 40);
        statusLabel.setText(mode == MODE_PVP ? "Player 1's Turn" : "Player's Turn (White)");

        scoreLabel.setFont(new Font("Segoe UI Semibold", 13));
        scoreLabel.setTextFill(Color.web("#eeeed2"));
        updateScoreDisplay();

        Button menuBtn = new Button("Return to Main Menu");
        menuBtn.setStyle("-fx-background-color: #c33737; -fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 310px; -fx-padding: 10px; -fx-background-radius: 4px; -fx-cursor: hand;");
        menuBtn.setOnAction(e -> showMainMenu());

        notationScrollPane = new ScrollPane(notationContainer);
        notationScrollPane.setFitToWidth(true);
        notationScrollPane.setStyle("-fx-background: #1e1e1e; -fx-border-color: #2d2d2d;");
        notationScrollPane.setPrefHeight(260);

        sidebar.getChildren().addAll(statusLabel, scoreLabel, notationScrollPane, controlsContainer, menuBtn);
        rootLayout.setRight(sidebar);

        engine.initializeStandardBoard();
        buildGraphicBoard();
    }

    void startPuzzleMode() {
        resetMatchState(MODE_PUZZLE);
        setupPuzzleLayout();
    }

    private void resetMatchState(int mode) {
        engine.gameMode = mode;
        engine.gameOver = false;
        engine.isWhiteTurn = true;
        selectedRow = -1; selectedCol = -1;
        lastFromRow = -1; lastFromCol = -1;
        lastToRow = -1; lastToCol = -1;
        ChessLogic.enPassantCol = -1;
        engine.whiteScore = 39;
        engine.blackScore = 39;
        engine.movesList.clear();
        notationContainer.getChildren().clear();
        controlsContainer.getChildren().clear();
    }

    void setupPuzzleLayout() {
        if (puzzleStack.isEmpty()) {
            statusLabel.setText("Amazing! You solved all available puzzles!");
            showMainMenu();
            return;
        }

        rootLayout.setCenter(chessGrid);
        Puzzles.ChessPuzzle currentPuzzle = puzzleStack.peek();

        for (int row = 0; row < 8; row++) {
            Arrays.fill(engine.board[row], null);
        }
        for (Puzzles.PuzzlePieceSetup setup : currentPuzzle.pieces) {
            engine.board[setup.row][setup.col] = setup.piece;
        }

        VBox sidebar = createBaseSidebar();

        statusLabel.setFont(new Font("Segoe UI Bold", 16));
        statusLabel.setTextFill(Color.web("#bacb46"));
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(SIDEBAR_WIDTH - 40);
        statusLabel.setText(currentPuzzle.description);

        scoreLabel.setFont(new Font("Segoe UI Semibold", 13));
        scoreLabel.setTextFill(Color.web("#eeeed2"));
        scoreLabel.setText("Find the single best move to Checkmate!");

        Button exitBtn = new Button("Exit Puzzles");
        exitBtn.setStyle("-fx-background-color: #c33737; -fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 310px; -fx-padding: 10px; -fx-background-radius: 4px; -fx-cursor: hand;");
        exitBtn.setOnAction(e -> showMainMenu());

        sidebar.getChildren().addAll(statusLabel, scoreLabel, exitBtn);
        rootLayout.setRight(sidebar);

        buildGraphicBoard();
    }

    void updateScoreDisplay() {
        scoreLabel.setText(String.format("Material — White: %d  |  Black: %d", engine.whiteScore, engine.blackScore));
    }

    void buildGraphicBoard() {
        clearAndSetupGridConstraints();
        drawBoardBordersAndCoordinates();

        boolean whiteInCheck = engine.isInCheck(true, engine.board);
        boolean blackInCheck = engine.isInCheck(false, engine.board);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane tile = createTile(row, col, whiteInCheck, blackInCheck);
                visualTiles[row][col] = tile;
                chessGrid.add(tile, col + 1, row + 1);
            }
        }
        overlayLegalMoveHints();
    }

    private void clearAndSetupGridConstraints() {
        chessGrid.getChildren().clear();
        chessGrid.getRowConstraints().clear();
        chessGrid.getColumnConstraints().clear();

        chessGrid.getColumnConstraints().add(new ColumnConstraints(BOARD_OFFSET));
        for (int i = 0; i < 8; i++) chessGrid.getColumnConstraints().add(new ColumnConstraints(TILE_SIZE));
        chessGrid.getColumnConstraints().add(new ColumnConstraints(BOARD_OFFSET));

        chessGrid.getRowConstraints().add(new RowConstraints(BOARD_OFFSET));
        for (int i = 0; i < 8; i++) chessGrid.getRowConstraints().add(new RowConstraints(TILE_SIZE));
        chessGrid.getRowConstraints().add(new RowConstraints(BOARD_OFFSET));
    }

    private void drawBoardBordersAndCoordinates() {
        for (int row = 0; row < 8; row++) {
            String rank = String.valueOf(8 - row);
            chessGrid.add(createCoordinateLabel(rank), 0, row + 1);
            chessGrid.add(createCoordinateLabel(rank), 9, row + 1);
        }
        for (int col = 0; col < 8; col++) {
            String file = String.valueOf((char) ('a' + col));
            chessGrid.add(createCoordinateLabel(file), col + 1, 0);
            chessGrid.add(createCoordinateLabel(file), col + 1, 9);
        }
    }

    private StackPane createCoordinateLabel(String labelText) {
        StackPane pane = new StackPane();
        Label label = new Label(labelText);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        label.setTextFill(Color.web("#8b8b8b"));
        pane.getChildren().add(label);
        pane.setAlignment(Pos.CENTER);
        return pane;
    }

    private StackPane createTile(int row, int col, boolean whiteInCheck, boolean blackInCheck) {
        StackPane tile = new StackPane();
        Rectangle square = new Rectangle(TILE_SIZE, TILE_SIZE);

        Color baseColor = (row + col) % 2 == 0 ? Color.web("#eeeed2") : Color.web("#739552");

        if ((row == lastFromRow && col == lastFromCol) || (row == lastToRow && col == lastToCol)) {
            baseColor = Color.web("#f7f48b");
        }

        Piece piece = engine.board[row][col];
        if (piece instanceof Piece.King) {
            if ((piece.isWhite && whiteInCheck) || (!piece.isWhite && blackInCheck)) {
                baseColor = Color.web("#e05353");
            }
        }
        square.setFill(baseColor);
        tile.getChildren().add(square);

        Label pieceLabel = new Label(piece != null ? piece.getEmoji() : "");
        pieceLabel.setFont(new Font("Arial", 52));

        if (piece != null) {
            if (piece.isWhite) {
                pieceLabel.setTextFill(Color.web("#ffffff"));
                pieceLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(61, 61, 61, 0.85), 2, 0.9, 0, 0);");
            } else {
                pieceLabel.setTextFill(Color.web("#3d3d3d"));
                pieceLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(255, 255, 255, 0.9), 1.5, 0.95, 0, 0);");
            }
        }

        tile.getChildren().add(pieceLabel);
        tile.setOnMouseClicked(e -> handleTileClick(row, col));
        return tile;
    }

    private void overlayLegalMoveHints() {
        if (selectedRow == -1 || selectedCol == -1) return;

        Piece currentPiece = engine.board[selectedRow][selectedCol];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (currentPiece.isValidMove(selectedRow, selectedCol, r, c, engine.board)) {
                    if (engine.willMoveResolveCheck(selectedRow, selectedCol, r, c, engine.isWhiteTurn)) {
                        Circle dot = new Circle(9, Color.web("#3d3d3d", 0.45));
                        visualTiles[r][c].getChildren().add(dot);
                    }
                }
            }
        }
    }

    void handleTileClick(int row, int col) {
        if (engine.gameOver || (engine.gameMode == MODE_BOT && !engine.isWhiteTurn)) return;

        if (selectedRow == -1) {
            selectPieceOnTile(row, col);
        } else {
            executeSelectedMovement(row, col);
        }
    }

    private void selectPieceOnTile(int row, int col) {
        if (engine.board[row][col] != null && engine.board[row][col].isWhite == engine.isWhiteTurn) {
            selectedRow = row;
            selectedCol = col;
            buildGraphicBoard();

            Rectangle rect = (Rectangle) visualTiles[row][col].getChildren().get(0);
            rect.setFill(Color.web("#bacb46"));
        }
    }

    private void executeSelectedMovement(int row, int col) {
        int fromRow = selectedRow;
        int fromCol = selectedCol;
        selectedRow = -1;

        if (fromRow == row && fromCol == col) {
            buildGraphicBoard();
            return;
        }
        Piece movingPiece = engine.board[fromRow][fromCol];

        if (movingPiece.isValidMove(fromRow, fromCol, row, col, engine.board)) {
            if (engine.willMoveResolveCheck(fromRow, fromCol, row, col, engine.isWhiteTurn)) {

                if (engine.gameMode == MODE_PUZZLE) {
                    evaluatePuzzleMoveAttempt(fromRow, fromCol, row, col);
                    return;
                }
                String promotionChoice="Queen";
                if(movingPiece instanceof Piece.Pawn && (row == 0 || row == 7)) {
                    if(engine.gameMode==MODE_PVP){
                        promotionChoice = showPromotionPopup(engine.isWhiteTurn);
                    } else if (engine.isWhiteTurn) {
                        promotionChoice = showPromotionPopup(engine.isWhiteTurn);
                    }

                }
                engine.recordAndLogMove(fromRow, fromCol, row, col,promotionChoice);
                lastFromRow = fromRow; lastFromCol = fromCol;
                lastToRow = row; lastToCol = col;

                engine.calculateLiveScores();
                engine.isWhiteTurn = !engine.isWhiteTurn;
                postMoveEvaluation();

                triggerDelayedBotAction();
                return;
            }
        }
        statusLabel.setText("Illegal Move Attempted!");
        buildGraphicBoard();
    }


    // Global variable to capture the choice from inside event handlers
    private String emojiChoiceResult = "Queen";

    private String showPromotionPopup(boolean isWhitePiece) {
        // Reset our result variable to default
        emojiChoiceResult = "Queen";

        // 1. Create a modal pop-up window
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL); // Blocks main window until clicked
        popupStage.initStyle(StageStyle.UNDECORATED);       // Cleans layout by removing close/minimize bars
        popupStage.setTitle("Pawn Promotion");

        // 2. Setup structural container
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #151515; -fx-border-color: #bacb46; -fx-border-width: 3px; -fx-background-radius: 8px; -fx-border-radius: 8px;");

        Label titleLabel = new Label("PAWN PROMOTION");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#bacb46"));

        // 3. Create a horizontal line to store choice buttons
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER);

        // Retrieve corresponding unicode piece symbols
        String qEmoji = isWhitePiece ? "♕" : "♛"; //
        String rEmoji = isWhitePiece ? "♖" : "♜"; //
        String bEmoji = isWhitePiece ? "♗" : "♝"; //
        String nEmoji = isWhitePiece ? "  ♘" : "♞"; //

        // Create styled graphical buttons
        Button queenBtn = createEmojiButton(qEmoji, isWhitePiece);
        Button rookBtn = createEmojiButton(rEmoji, isWhitePiece);
        Button bishopBtn = createEmojiButton(bEmoji, isWhitePiece);
        Button knightBtn = createEmojiButton(nEmoji, isWhitePiece);
        queenBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        rookBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        knightBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        bishopBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        // 4. Map actions to store click event selections
        queenBtn.setOnAction(e -> { emojiChoiceResult = "Queen"; popupStage.close(); });
        rookBtn.setOnAction(e -> { emojiChoiceResult = "Rook"; popupStage.close(); });
        bishopBtn.setOnAction(e -> { emojiChoiceResult = "Bishop"; popupStage.close(); });
        knightBtn.setOnAction(e -> { emojiChoiceResult = "Knight"; popupStage.close(); });

        buttonBox.getChildren().addAll(queenBtn, rookBtn, bishopBtn, knightBtn);
        container.getChildren().addAll(titleLabel, buttonBox);

        // 5. Render Scene constraints
        Scene scene = new Scene(container, 340, 140);
        popupStage.setScene(scene);
        popupStage.showAndWait(); // Pauses execution loop until user clicks an emoji

        return emojiChoiceResult;
    }

    // Helper styling method to generate matching grid buttons for our emojis
    private Button createEmojiButton(String emoji, boolean isWhitePiece) {
        Button btn = new Button(emoji);

        // Explicit font family ensures the emojis render correctly
        btn.setFont(Font.font("Segoe UI Symbol", FontWeight.BOLD, 36));
        btn.setPrefSize(70, 70);

        if (isWhitePiece) {
            // White Pieces: White text on a sleek dark gray button background
            btn.setTextFill(Color.web("#ffffff"));
            btn.setStyle("-fx-background-color: #2d2d2d; -fx-background-radius: 6px; -fx-cursor: hand;");

            // Hover effects for White choices
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #444444; -fx-border-color: #bacb46; -fx-border-width: 2px; -fx-border-radius: 6px; -fx-cursor: hand; -fx-text-fill: #ffffff;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #2d2d2d; -fx-background-radius: 6px; -fx-cursor: hand; -fx-text-fill: #ffffff;"));
        } else {
            // FIX: Black Pieces: Dark gray text on a bright, high-contrast cream background
            btn.setTextFill(Color.web("#1e1e1e"));
            btn.setStyle("-fx-background-color: #eeeed2; -fx-background-radius: 6px; -fx-cursor: hand;");

            // Hover effects for Black choices (glows red/green when hovered for high visibility)
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #e2e2b9; -fx-border-color: #bacb46; -fx-border-width: 2px; -fx-border-radius: 6px; -fx-cursor: hand; -fx-text-fill: #1e1e1e;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #eeeed2; -fx-background-radius: 6px; -fx-cursor: hand; -fx-text-fill: #1e1e1e;"));
        }

        return btn;
    }



    private void evaluatePuzzleMoveAttempt(int fromRow, int fromCol, int toRow, int toCol) {
        Puzzles.ChessPuzzle activePuzzle = puzzleStack.peek();

        if (fromRow == activePuzzle.expectedFromRow && fromCol == activePuzzle.expectedFromCol &&
                toRow == activePuzzle.expectedToRow && toCol == activePuzzle.expectedToCol) {

            engine.board[toRow][toCol] = engine.board[fromRow][fromCol];
            engine.board[fromRow][fromCol] = null;
            lastFromRow = fromRow; lastFromCol = fromCol;
            lastToRow = toRow; lastToCol = toCol;
            buildGraphicBoard();

            DatabaseManager.savePuzzleSolve(activePuzzle.id);
            statusLabel.setText("CORRECT! CHECKMATE delivered successfully.");
            engine.gameOver = true;

            puzzleStack.pop();
            PauseTransition menuReturnDelay = new PauseTransition(Duration.millis(2000));
            menuReturnDelay.setOnFinished(ev -> showMainMenu());
            menuReturnDelay.play();
        } else {
            statusLabel.setText("Incorrect Move. Try looking again!");
            buildGraphicBoard();
        }
    }

    private void triggerDelayedBotAction() {
        if (engine.gameMode == MODE_BOT && !engine.isWhiteTurn && !engine.gameOver) {
            PauseTransition pause = new PauseTransition(Duration.millis(500));
            pause.setOnFinished(e -> {
                executeBotAction();
                engine.calculateLiveScores();
            });
            pause.play();
        }
    }

    void executeBotAction() {
        int[] choice = engine.getBotMove();
        if (choice == null) {
            postMoveEvaluation();
            return;
        }
        engine.recordAndLogMove(choice[0], choice[1], choice[2], choice[3],"Queen");
        lastFromRow = choice[0]; lastFromCol = choice[1];
        lastToRow = choice[2]; lastToCol = choice[3];

        engine.isWhiteTurn = true;
        postMoveEvaluation();
    }

    void updateLiveNotationUI() {
        notationContainer.getChildren().clear();
        for (int i = 0; i < engine.movesList.size(); i += 2) {
            HBox row = new HBox(20);
            row.setPadding(new Insets(3, 8, 3, 8));

            Label numLabel = new Label((i / 2 + 1) + ".");
            numLabel.setTextFill(Color.GRAY);
            numLabel.setPrefWidth(30);

            Label whiteLabel = new Label(engine.movesList.get(i));
            whiteLabel.setTextFill(Color.web("#eeeed2"));
            whiteLabel.setPrefWidth(80);

            String blackMove = i + 1 < engine.movesList.size() ? engine.movesList.get(i + 1) : "";
            Label blackLabel = new Label(blackMove);
            blackLabel.setTextFill(Color.web("#eeeed2"));

            row.getChildren().addAll(numLabel, whiteLabel, blackLabel);
            notationContainer.getChildren().add(row);
        }
        notationScrollPane.setVvalue(1.0);
    }

    void enableEndGameLayout() {
        controlsContainer.getChildren().clear();
        Button btn = new Button("Launch New Match");
        btn.setStyle("-fx-background-color: #bacb46; -fx-text-fill: #151515; -fx-font-weight: bold; -fx-min-width: 310px; -fx-max-width: 310px; -fx-padding: 10px; -fx-cursor: hand;");
        btn.setOnAction(e -> startNewGame(engine.gameMode));
        controlsContainer.getChildren().add(btn);
    }

    void postMoveEvaluation() {
        updateLiveNotationUI();
        buildGraphicBoard();

        if (engine.hasNoLegalMoves(engine.isWhiteTurn)) {
            processTerminalMatchState();
        } else {
            statusLabel.setText(engine.isWhiteTurn ? "Player 1's Turn" : (engine.gameMode == MODE_PVP ? "Player 2's Turn" : "Bot's Turn (Black)"));
        }
    }

    private void processTerminalMatchState() {
        engine.gameOver = true;
        String outcomeType;
        String winnerSide;

        if (engine.isInCheck(engine.isWhiteTurn, engine.board)) {
            outcomeType = "CHECKMATE";
            if (engine.gameMode == MODE_PVP) {
                winnerSide = engine.isWhiteTurn ? "Player 2" : "Player 1";
            } else {
                winnerSide = engine.isWhiteTurn ? "Bot" : "Player";
            }
            statusLabel.setText("CHECKMATE! " + winnerSide + " wins.");
        } else {
            outcomeType = "STALEMATE";
            winnerSide = "Draw";
            statusLabel.setText("STALEMATE!");
        }

        enableEndGameLayout();

        String tableType = (engine.gameMode == MODE_BOT) ? "PVE" : "PVP";
        String dbWinnerValue = winnerSide;

        if (engine.gameMode == MODE_BOT) {
            if (!winnerSide.equals("Draw")) {
                dbWinnerValue = winnerSide.equals("White") ? "Player" : "Bot";
            }
        } else {
            if (!winnerSide.equals("Draw")) {
                dbWinnerValue = winnerSide.equals("White") ? "Player 1" : "Player 2";
            }
        }

        DatabaseManager.saveGame(tableType, engine.whiteScore, engine.blackScore, outcomeType, dbWinnerValue, engine.movesList);
    }
}