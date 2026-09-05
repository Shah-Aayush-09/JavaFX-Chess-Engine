package org.example;

public abstract class Piece {
    public boolean isWhite;
    public boolean hasMoved = false;

    public Piece(boolean w) {
        this.isWhite = w;
    }

    public abstract boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b);
    public abstract String getEmoji();
    public abstract String getNotationLetter();
    public abstract int getValue();

    protected boolean basicCheck(int toRow, int toCol, Piece[][] b) {
        return b[toRow][toCol] == null || b[toRow][toCol].isWhite != isWhite;
    }

    public static class Pawn extends Piece {
        public Pawn(boolean w) {
            super(w);
        }

        public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b) {
            if (!basicCheck(toRow, toCol, b)) {
                return false;
            }

            int dir = isWhite ? -1 : 1;
            int start = isWhite ? 6 : 1;

            if (fromCol == toCol && toRow == fromRow + dir && b[toRow][toCol] == null) {
                return true;
            }

            // starting two-square forward move
            if (fromCol == toCol && fromRow == start && toRow == fromRow + (2 * dir)) {
                if (b[fromRow + dir][toCol] == null && b[toRow][toCol] == null) {
                    return true;
                }
            }

            //capture logic
            if (Math.abs(fromCol - toCol) == 1 && toRow == fromRow + dir && b[toRow][toCol] != null) {
                return true;
            }

            // En Passant
            if (Math.abs(fromCol - toCol) == 1 && toRow == fromRow + dir && b[toRow][toCol] == null) {
                return ChessLogic.enPassantCol == toCol && fromRow == (isWhite ? 3 : 4);
            }

            return false;
        }

        public String getEmoji() {
            return isWhite ? "♙" : "♟";
        }

        public String getNotationLetter() {
            return "";
        }

        public int getValue() {
            return 1;
        }
    }

    public static class Rook extends Piece {
        public Rook(boolean w) {
            super(w);
        }

        public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b) {
            if (basicCheck(toRow, toCol, b) && (fromRow == toRow || fromCol == toCol)) {
                return ChessLogic.isPathClear(fromRow, fromCol, toRow, toCol, b);
            }
            return false;
        }

        public String getEmoji() {
            return isWhite ? "♖" : "♜";
        }

        public String getNotationLetter() {
            return "R";
        }

        public int getValue() {
            return 5;
        }
    }

    public static class Knight extends Piece {
        public Knight(boolean w) {
            super(w);
        }

        public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b) {
            if (!basicCheck(toRow, toCol, b)) {
                return false;
            }

            int dR = Math.abs(fromRow - toRow);
            int dC = Math.abs(fromCol - toCol);

            return (dR == 2 && dC == 1) || (dR == 1 && dC == 2);
        }

        public String getEmoji() {
            return isWhite ? "♘" : "♞";
        }

        public String getNotationLetter() {
            return "N";
        }

        public int getValue() {
            return 3;
        }
    }

    public static class Bishop extends Piece {
        public Bishop(boolean w) {
            super(w);
        }

        public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b) {
            if (basicCheck(toRow, toCol, b) && Math.abs(fromRow - toRow) == Math.abs(fromCol - toCol)) {
                return ChessLogic.isPathClear(fromRow, fromCol, toRow, toCol, b);
            }
            return false;
        }

        public String getEmoji() {
            return isWhite ? "♗" : "♝";
        }

        public String getNotationLetter() {
            return "B";
        }

        public int getValue() {
            return 3;
        }
    }

    public static class Queen extends Piece {
        public Queen(boolean w) {
            super(w);
        }

        public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b) {
            if (!basicCheck(toRow, toCol, b)) {
                return false;
            }

            boolean diagonal = Math.abs(fromRow - toRow) == Math.abs(fromCol - toCol);
            boolean straight = (fromRow == toRow || fromCol == toCol);

            if (diagonal || straight) {
                return ChessLogic.isPathClear(fromRow, fromCol, toRow, toCol, b);
            }

            return false;
        }

        public String getEmoji() {
            return isWhite ? "♕" : "♛";
        }

        public String getNotationLetter() {
            return "Q";
        }

        public int getValue() {
            return 9;
        }
    }

    public static class King extends Piece {
        public King(boolean w) {
            super(w);
        }

        public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b) {
            if (!basicCheck(toRow, toCol, b)) {
                return false;
            }

            if (Math.abs(fromRow - toRow) <= 1 && Math.abs(fromCol - toCol) <= 1) {
                return true;
            }

           //castling logic
            if (!hasMoved && fromRow == toRow && Math.abs(fromCol - toCol) == 2) {
                int rookCol = toCol == 6 ? 7 : 0;

                if (b[fromRow][rookCol] instanceof Rook && !b[fromRow][rookCol].hasMoved) {
                    return ChessLogic.isPathClear(fromRow, fromCol, fromRow, rookCol, b);
                }
            }

            return false;
        }

        public String getEmoji() {
            return isWhite ? "♔" : "♚";
        }

        public String getNotationLetter() {
            return "K";
        }

        public int getValue() {
            return 0;
        }
    }
}