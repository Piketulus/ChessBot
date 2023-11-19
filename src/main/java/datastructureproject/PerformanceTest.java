package datastructureproject;

import chess.bot.ChessBot;
import chess.engine.GameState;
import java.util.ArrayList;
import java.util.List;

import chess.model.Side;

/**
 * Use this class to write performance tests for your bot.
 * 
 */
public class PerformanceTest {

    private ChessBot bot;
    private List<GameState> gsList = new ArrayList();

    public void setGsList(List<GameState> gsList) {
        this.gsList = gsList;
    }


    public static void main(String[] args) {
        // create a new board and then check how fast the move generator is byt checking how long it takes to generate all moves to depth 6, and report the time and the amoun tof nodes generated
        ChessBoard board = new ChessBoard();
        int depth = 7;
        PerformanceTest pt = new PerformanceTest();
        long startTime = System.nanoTime();
        long nodes = pt.getNodesGenerated(depth, board, Side.WHITE);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;
        System.out.println("Time taken: " + duration + "ms");
        System.out.println("Nodes generated: " + nodes);

        //test a position and see moves generated
        ChessBoard board2 = new ChessBoard();
        String[] moves = {"e2e4", "e7e5", "f1c4", "b8c6", "d1h5", "g8f6", "h5f7"};
        board2.makeMoves(moves);
        MoveGenerator mg = new MoveGenerator(board2.getBoard(), board2.getEnpassantable(), Side.BLACK);
        ArrayList<String> responses = mg.getMoves();
        for (String move : responses) {
            System.out.println(move);
        }

        
    }

    // recursive method to generate all moves to a certain depth
    public long getNodesGenerated(int depth, ChessBoard board, Side side) {

        if (depth == 0) {
            return 1;
        }

        MoveGenerator mg = new MoveGenerator(board.getBoard(), board.getEnpassantable(), side);
        Side opposite = side == Side.WHITE ? Side.BLACK : Side.WHITE;
        ArrayList<String> moves = mg.getMoves();
        long nodes = 0;
        for (String move : moves) {
            ChessBoard newBoard = new ChessBoard(board);
            newBoard.makeMove(move);
            nodes += getNodesGenerated(depth - 1, newBoard, opposite);
        }

        return nodes;
        
    }

}
