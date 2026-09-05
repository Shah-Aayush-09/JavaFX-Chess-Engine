package org.example;

import java.util.ArrayList;
import java.util.List;

public class Puzzles {

    public static class ChessPuzzle {
        public int id;
        public String description;
        public List<PuzzlePieceSetup> pieces;
        public int expectedFromRow, expectedFromCol;
        public int expectedToRow, expectedToCol;

        public ChessPuzzle(int id, String description, List<PuzzlePieceSetup> pieces,
                           int fromR, int fromC, int toR, int toC) {
            this.id = id;
            this.description = description;
            this.pieces = pieces;
            this.expectedFromRow = fromR;
            this.expectedFromCol = fromC;
            this.expectedToRow = toR;
            this.expectedToCol = toC;
        }
    }

    public static class PuzzlePieceSetup {
        public int row, col;
        public Piece piece;

        public PuzzlePieceSetup(int row, int col, Piece piece) {
            this.row = row;
            this.col = col;
            this.piece = piece;
        }
    }

    public static PuzzleStack getPredefinedPuzzles() {
        PuzzleStack puzzleStack = new PuzzleStack();

        List<PuzzlePieceSetup> p1Setup = new ArrayList<>();
        p1Setup.add(new PuzzlePieceSetup(0, 4, new Piece.King(false)));
        p1Setup.add(new PuzzlePieceSetup(1, 3, new Piece.Pawn(false)));
        p1Setup.add(new PuzzlePieceSetup(1, 4, new Piece.Pawn(false)));
        p1Setup.add(new PuzzlePieceSetup(1, 5, new Piece.Pawn(false)));
        p1Setup.add(new PuzzlePieceSetup(5, 0, new Piece.Rook(true)));
        p1Setup.add(new PuzzlePieceSetup(7, 4, new Piece.King(true)));

        List<PuzzlePieceSetup> p2Setup = new ArrayList<>();
        p2Setup.add(new PuzzlePieceSetup(0, 0, new Piece.King(false)));
        p2Setup.add(new PuzzlePieceSetup(1, 1, new Piece.Pawn(false)));
        p2Setup.add(new PuzzlePieceSetup(2, 0, new Piece.Queen(true)));
        p2Setup.add(new PuzzlePieceSetup(5, 0, new Piece.Rook(true)));
        p2Setup.add(new PuzzlePieceSetup(7, 4, new Piece.King(true)));

        List<PuzzlePieceSetup> p3Setup = new ArrayList<>();
        p3Setup.add(new PuzzlePieceSetup(0, 4, new Piece.King(false)));
        p3Setup.add(new PuzzlePieceSetup(1, 0, new Piece.Rook(true)));
        p3Setup.add(new PuzzlePieceSetup(5, 6, new Piece.Rook(true)));
        p3Setup.add(new PuzzlePieceSetup(7, 4, new Piece.King(true)));

        List<PuzzlePieceSetup> p4Setup = new ArrayList<>();
        p4Setup.add(new PuzzlePieceSetup(0, 3, new Piece.King(false)));
        p4Setup.add(new PuzzlePieceSetup(1, 2, new Piece.Pawn(false)));
        p4Setup.add(new PuzzlePieceSetup(1, 3, new Piece.Pawn(false)));
        p4Setup.add(new PuzzlePieceSetup(1, 4, new Piece.Pawn(false)));
        p4Setup.add(new PuzzlePieceSetup(2, 1, new Piece.Knight(true)));
        p4Setup.add(new PuzzlePieceSetup(6, 5, new Piece.Queen(true)));
        p4Setup.add(new PuzzlePieceSetup(7, 4, new Piece.King(true)));

        List<PuzzlePieceSetup> p5Setup = new ArrayList<>();
        p5Setup.add(new PuzzlePieceSetup(0, 0, new Piece.King(false)));
        p5Setup.add(new PuzzlePieceSetup(1, 0, new Piece.Pawn(false)));
        p5Setup.add(new PuzzlePieceSetup(0, 2, new Piece.Rook(false)));
        p5Setup.add(new PuzzlePieceSetup(1, 1, new Piece.Pawn(true)));
        p5Setup.add(new PuzzlePieceSetup(3, 3, new Piece.Bishop(true)));
        p5Setup.add(new PuzzlePieceSetup(7, 4, new Piece.King(true)));

        puzzleStack.push(new ChessPuzzle(5, "Puzzle 5: Promote to Mate!", p5Setup, 1, 1, 0, 2));
        puzzleStack.push(new ChessPuzzle(4, "Puzzle 4: The Knight's Silent Guard", p4Setup, 6, 5, 0, 5));
        puzzleStack.push(new ChessPuzzle(3, "Puzzle 3: Climb the Ladder!", p3Setup, 5, 6, 0, 6));
        puzzleStack.push(new ChessPuzzle(2, "Puzzle 2: The Cornered King", p2Setup, 2, 0, 1, 0));
        puzzleStack.push(new ChessPuzzle(1, "Puzzle 1: Exploit the Weak Back Rank!", p1Setup, 5, 0, 0, 0));

        return puzzleStack;
    }
}