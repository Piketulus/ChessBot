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

        //https://www.chessprogramming.org/Perft_Results for perft test positions and results

        //pt.perft("rn2kb1r/pp3ppp/2p2n2/q3pb2/B7/2NPBN2/PPP2PPP/R2QK2R w KQkq - 3 9", 5, false);

        //pt.getEvaluation("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P2N/P1NP4/1PP1bPPP/R4RK1 w - - 0 11");

        
        long startTime = System.nanoTime();
        String move = pt.nextMove("8/p7/8/8/1p6/nP1K1k2/P7/6q1 b - - 7 62", 8);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;
        System.out.println("Time taken: " + duration + "ms");
        System.out.println("Move: " + move);
        

    }


    public void perft(String fen, int depth, boolean divide) {
        //check how many positions are generated in a certain depth and how long it takes (Perft)
        if (!divide) {
            BitChessBoard board = new BitChessBoard();
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
            BitChessBoard board = new BitChessBoard();
            board.fenToBoard(fen);

            Side side = fen.split(" ")[1].equals("w") ? Side.WHITE : Side.BLACK;
            Side opSide = side == Side.WHITE ? Side.BLACK : Side.WHITE;

            PerformanceTest pt = new PerformanceTest();

            MoveGenerator mg = new MoveGenerator(board.getBoard(), board.enpassantable, board.castlingRights, side);
            ArrayList<String> moves = mg.getMoves();

            for (String move : moves) {
                BitChessBoard newBoard = new BitChessBoard(board);
                newBoard.makeMove(move);
                long nodes = pt.getNodesGenerated(depth - 1, newBoard, opSide);
                //board.undoMove();
                System.out.println(move + ": " + nodes);
            }
            System.out.println("Total: " + moves.size());
        }

    }


    public long getNodesGenerated(int depth, BitChessBoard board, Side side) {
        // recursive method to generate all moves to a certain depth
        if (depth == 0) {
            return 1;
        }

        MoveGenerator mg = new MoveGenerator(board.getBoard(), board.enpassantable, board.castlingRights, side);
        Side opposite = side == Side.WHITE ? Side.BLACK : Side.WHITE;
        ArrayList<String> moves = mg.getMoves();
        long nodes = 0;
        for (String move : moves) {
            BitChessBoard newBoard = new BitChessBoard(board);
            newBoard.makeMove(move);
            nodes += getNodesGenerated(depth - 1, newBoard, opposite);
            //board.undoMove();
        }

        return nodes;
        
    }


    public void getEvaluation(String fen) {
        //get the evaluation of a position
        BitChessBoard board = new BitChessBoard();
        board.fenToBoard(fen);
        Side side = fen.split(" ")[1].equals("w") ? Side.WHITE : Side.BLACK;
        int score = PositionEvaluator.evaluatePosition(board.getBoard(), side);
        System.out.println("Evaluation: " + score);
    }


    /**
     * Methods copied (only slightly modified) from PiketulusBot.java for testing purposes
     */
    public String nextMove(String fen, int depth) {

        BitChessBoard board = new BitChessBoard();
        board.fenToBoard(fen);
        Side playing = fen.split(" ")[1].equals("w") ? Side.WHITE : Side.BLACK;
        
        String bestMove = iterDeepNextMove(depth, board, playing, playing);

        return bestMove;

    }


    private String iterDeepNextMove(int maxDepth, BitChessBoard board, Side turn, Side playing) {
        String bestFoundMove = null;
        MoveGenerator mg = new MoveGenerator(board.getBoard(), board.enpassantable, board.castlingRights, turn);
        Side opposite = turn == Side.WHITE ? Side.BLACK : Side.WHITE;
        ArrayList<String> moves = mg.getMoves();
        if (moves.size() == 0) {
            return null;
        }
        long startTime = System.currentTimeMillis();
        for (int d = 1; d <= maxDepth; d++) {
            String bestMove = null;
            int bestScore = Integer.MIN_VALUE;
            for (String move : moves) {
                if (System.currentTimeMillis() - startTime > 5000) {
                    return bestFoundMove;
                }
                BitChessBoard newBoard = new BitChessBoard(board);
                newBoard.makeMove(move);
                int score = alphaBetaMinimax(d - 1, newBoard, Integer.MIN_VALUE, 
                                             Integer.MAX_VALUE, opposite, playing);
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            }
            bestFoundMove = bestMove;
        }
        return bestFoundMove;
    }


    private int alphaBetaMinimax(int d, BitChessBoard board, int alpha, int beta, Side turn, Side playing) {
        if (d == 0) {
            return PositionEvaluator.evaluatePosition(board.getBoard(), playing);
        }

        MoveGenerator mg = new MoveGenerator(board.getBoard(), board.enpassantable, board.castlingRights, turn);
        Side opposite = turn == Side.WHITE ? Side.BLACK : Side.WHITE;
        ArrayList<String> moves = mg.getMoves();

        if (moves.size() == 0) {
            if (mg.kingInCheck > 0 && turn == playing) {
                return Integer.MIN_VALUE + (8 - d);
            } else if (mg.kingInCheck > 0 && turn != playing) {
                return Integer.MAX_VALUE - (8 - d);
            } else {
                return 0;
            }
        }

        if (turn == playing) {
            int bestScore = Integer.MIN_VALUE;
            for (String move : moves) {
                BitChessBoard newBoard = new BitChessBoard(board);
                newBoard.makeMove(move);
                int score = alphaBetaMinimax(d - 1, newBoard, alpha, beta, opposite, playing);
                bestScore = Math.max(bestScore, score);
                alpha = Math.max(alpha, score);
                if (beta <= alpha) {
                    break;
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (String move : moves) {
                BitChessBoard newBoard = new BitChessBoard(board);
                newBoard.makeMove(move);
                int score = alphaBetaMinimax(d - 1, newBoard, alpha, beta, opposite, playing);
                bestScore = Math.min(bestScore, score);
                beta = Math.min(beta, score);
                if (beta <= alpha) {
                    break;
                }
            }
            return bestScore;
        }
    }    

}
