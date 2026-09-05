package org.example;

import java.util.EmptyStackException;


class PuzzleStack {
    public static final int CAPACITY = 5;

    private final Puzzles.ChessPuzzle[] elements;
    private int size;

    public PuzzleStack() {
        elements = new Puzzles.ChessPuzzle[CAPACITY];
        size = 0;
    }


    public void push(Puzzles.ChessPuzzle puzzle) {
        if (isFull()) {
            throw new IllegalStateException("PuzzleStack is full (capacity = " + CAPACITY + ")");
        }
        elements[size++] = puzzle;
    }


    public void pop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        Puzzles.ChessPuzzle top = elements[--size];
        elements[size] = null; // avoid memory leak
    }


    public Puzzles.ChessPuzzle peek() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        return elements[size - 1];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isFull() {
        return size == elements.length;
    }

    public int size() {
        return size;
    }
}