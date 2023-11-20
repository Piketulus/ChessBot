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
        
        PerformanceTest pt = new PerformanceTest();

        pt.perft("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10", 4, false);
        
    }


    public void perft(String fen, int depth, boolean divide) {
        //check how many positions are generated in a certain depth and how long it takes (Perft)
        if (!divide) {
            ChessBoard board = new ChessBoard();
            board.fenToBoard(fen);
            
            Side side = fen.split(" ")[1].equals("w") ? Side.WHITE : Side.BLACK;

            PerformanceTest pt = new PerformanceTest();

            long startTime = System.nanoTime();
            long nodes = pt.getNodesGenerated(depth, board, side);
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000;

            System.out.println("Time taken: " + duration + "ms");
            System.out.println("Positions generated: " + nodes);
        } else {
            //display(print) the number of positions generated for each move from the initial position
            ChessBoard board = new ChessBoard();
            board.fenToBoard(fen);

            Side side = fen.split(" ")[1].equals("w") ? Side.WHITE : Side.BLACK;
            Side opSide = side == Side.WHITE ? Side.BLACK : Side.WHITE;

            PerformanceTest pt = new PerformanceTest();

            MoveGenerator mg = new MoveGenerator(board.getBoard(), board.getEnpassantable(), side);
            ArrayList<String> moves = mg.getMoves();

            for (String move : moves) {
                ChessBoard newBoard = new ChessBoard(board);
                newBoard.makeMove(move);
                long nodes = pt.getNodesGenerated(depth - 1, newBoard, opSide);
                System.out.println(move + ": " + nodes);
            }
            System.out.println("Total: " + moves.size());
        }

    }


    public long getNodesGenerated(int depth, ChessBoard board, Side side) {
        // recursive method to generate all moves to a certain depth
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
