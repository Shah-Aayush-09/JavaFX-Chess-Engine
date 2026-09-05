package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ChessLogic {
    public static int enPassantCol = -1;

    public Piece[][] board = new Piece[8][8];
    public boolean isWhiteTurn = true;
    public boolean gameOver = false;
    public int gameMode = 0;
    public List<String> movesList = new ArrayList<>();

    public int whiteScore = 39;
    public int blackScore = 39;

    public void initializeStandardBoard() {
        for (int row = 0; row < 8; row++) {
            Arrays.fill(board[row], null);
        }

        board[0][0] = new Piece.Rook(false);
        board[0][1] = new Piece.Knight(false);
        board[0][2] = new Piece.Bishop(false);
        board[0][3] = new Piece.Queen(false);
        board[0][4] = new Piece.King(false);
        board[0][5] = new Piece.Bishop(false);
        board[0][6] = new Piece.Knight(false);
        board[0][7] = new Piece.Rook(false);

        for (int i = 0; i < 8; i++) {
            board[1][i] = new Piece.Pawn(false);
            board[6][i] = new Piece.Pawn(true);
        }

        board[7][0] = new Piece.Rook(true);
        board[7][1] = new Piece.Knight(true);
        board[7][2] = new Piece.Bishop(true);
        board[7][3] = new Piece.Queen(true);
        board[7][4] = new Piece.King(true);
        board[7][5] = new Piece.Bishop(true);
        board[7][6] = new Piece.Knight(true);
        board[7][7] = new Piece.Rook(true);
    }

    public void calculateLiveScores() {
        int whiteMaterial = 0;
        int blackMaterial = 0;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null) {
                    if (p.isWhite) {
                        whiteMaterial += p.getValue();
                    } else {
                        blackMaterial += p.getValue();
                    }
                }
            }
        }
        whiteScore = whiteMaterial;
        blackScore = blackMaterial;
    }

    public void recordAndLogMove(int fromRow, int fromCol, int toRow, int toCol,String promotionChoice) {
        Piece piece = board[fromRow][fromCol];
        boolean isCapture = (board[toRow][toCol] != null);
        String notation = "";

        if (piece instanceof Piece.Pawn) {
            if (isCapture) {
                notation += (char) ('a' + fromCol);
            }
        } else {
            notation += piece.getNotationLetter();
        }

        if (piece instanceof Piece.King && Math.abs(fromCol - toCol) == 2) {
            executeRookCastlingJump(fromRow, toCol);
            notation = (toCol == 6) ? "O-O" : "O-O-O";
        } else if (piece instanceof Piece.Pawn && fromCol != toCol && board[toRow][toCol] == null) {
            board[fromRow][toCol] = null; // Remove en-passant captured pawn
            isCapture = true;
            notation += "x" + (char) ('a' + toCol) + (8 - toRow) + " e.p.";
        }

        if (!notation.startsWith("O-O")) {
            if (isCapture) {
                notation += "x";
            }
            notation += (char) ('a' + toCol);
            notation += String.valueOf(8 - toRow);
        }

        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = null;
        piece.hasMoved = true;

        if (piece instanceof Piece.Pawn && Math.abs(fromRow - toRow) == 2) {
            enPassantCol = toCol;
        } else {
            enPassantCol = -1;
        }

        if (piece instanceof Piece.Pawn && (toRow == 0 || toRow == 7)) {

            switch (promotionChoice.toUpperCase()){
                        case "KNIGHT":
                            board[toRow][toCol] = new Piece.Knight(piece.isWhite);
                            notation += "=N";
                            break;
                        case "BISHOP":
                            board[toRow][toCol] = new Piece.Bishop(piece.isWhite);
                            notation += "=B";
                            break;
                        case "ROOK":
                            board[toRow][toCol] = new Piece.Rook(piece.isWhite);
                            notation += "=R";
                            break;
                        default:
                            board[toRow][toCol] = new Piece.Queen(piece.isWhite);
                            notation += "=Q";
                            break;
                    }
                }




        boolean opponentColor = !piece.isWhite;
        if (isInCheck(opponentColor, board)) {
            notation += hasNoLegalMoves(opponentColor) ? "#" : "+";
        }

        movesList.add(notation);
    }

    private void executeRookCastlingJump(int kingRow, int kingTargetCol) {
        int rookSourceCol = (kingTargetCol == 6) ? 7 : 0;
        int rookTargetCol = (kingTargetCol == 6) ? 5 : 3;

        board[kingRow][rookTargetCol] = board[kingRow][rookSourceCol];
        board[kingRow][rookSourceCol] = null;

        if (board[kingRow][rookTargetCol] != null) {
            board[kingRow][rookTargetCol].hasMoved = true;
        }
    }

    public int[] getBotMove() {
        List<int[]> legalMoves = new ArrayList<>();

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = board[r][c];
                if (piece != null && !piece.isWhite) {
                    collectLegalMovesForPiece(r, c, piece, legalMoves);
                }
            }
        }

        if (legalMoves.isEmpty()) {
            return null;
        }

        int maxCaptureValue = -100;
        for (int[] move : legalMoves) {
            int targetValue = board[move[2]][move[3]] != null ? board[move[2]][move[3]].getValue() : -1;
            if (targetValue > maxCaptureValue) {
                maxCaptureValue = targetValue;
            }
        }

        List<int[]> bestMoves = new ArrayList<>();
        for (int[] move : legalMoves) {
            int targetValue = board[move[2]][move[3]] != null ? board[move[2]][move[3]].getValue() : -1;
            if (targetValue == maxCaptureValue) {
                bestMoves.add(move);
            }
        }

        return bestMoves.get(new Random().nextInt(bestMoves.size()));
    }

    private void collectLegalMovesForPiece(int r, int c, Piece piece, List<int[]> movesListCollector) {
        for (int tr = 0; tr < 8; tr++) {
            for (int tc = 0; tc < 8; tc++) {
                if (piece.isValidMove(r, c, tr, tc, board)) {
                    if (willMoveResolveCheck(r, c, tr, tc, false)) {
                        movesListCollector.add(new int[]{r, c, tr, tc});
                    }
                }
            }
        }
    }

    public boolean isInCheck(boolean isWhiteSide, Piece[][] b) {
        int kingRow = -1, kingCol = -1;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (b[r][c] instanceof Piece.King && b[r][c].isWhite == isWhiteSide) {
                    kingRow = r;
                    kingCol = c;
                    break;
                }
            }
        }
        if (kingRow == -1) return false;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece attacker = b[r][c];
                if (attacker != null && attacker.isWhite != isWhiteSide) {
                    if (attacker.isValidMove(r, c, kingRow, kingCol, b)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean willMoveResolveCheck(int fromRow, int fromCol, int toRow, int toCol, boolean isWhiteSide) {
        Piece[][] temporaryBoard = new Piece[8][8];
        for (int i = 0; i < 8; i++) {
            System.arraycopy(board[i], 0, temporaryBoard[i], 0, 8);
        }

        if (temporaryBoard[fromRow][fromCol] instanceof Piece.Pawn && fromCol != toCol && temporaryBoard[toRow][toCol] == null) {
            temporaryBoard[fromRow][toCol] = null;
        }

        temporaryBoard[toRow][toCol] = temporaryBoard[fromRow][fromCol];
        temporaryBoard[fromRow][fromCol] = null;

        return !isInCheck(isWhiteSide, temporaryBoard);
    }

    public boolean hasNoLegalMoves(boolean isWhiteSide) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null && p.isWhite == isWhiteSide) {
                    if (hasAnyValidDestinations(r, c, p, isWhiteSide)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean hasAnyValidDestinations(int row, int col, Piece piece, boolean isWhiteSide) {
        for (int tr = 0; tr < 8; tr++) {
            for (int tc = 0; tc < 8; tc++) {
                if (piece.isValidMove(row, col, tr, tc, board)) {
                    if (willMoveResolveCheck(row, col, tr, tc, isWhiteSide)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isPathClear(int startRow, int startCol, int targetRow, int targetCol, Piece[][] b) {
        int stepRow = Integer.compare(targetRow, startRow);
        int stepCol = Integer.compare(targetCol, startCol);

        int currentRow = startRow + stepRow;
        int currentCol = startCol + stepCol;

        while (currentRow != targetRow || currentCol != targetCol) {
            if (b[currentRow][currentCol] != null) {
                return false;
            }
            currentRow += stepRow;
            currentCol += stepCol;
        }
        return true;
    }
}