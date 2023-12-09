package datastructureproject;

import java.util.ArrayList;
import java.util.HashMap;

import chess.bot.ChessBot;
import chess.engine.GameState;
import chess.model.Side;

public class PiketulusBot implements ChessBot {

    private BitChessBoard board;
    private int maxDepth = 10; // max depth for calculating a move
    private int maxTime = 5000; // max time for calculating a move in milliseconds
    private boolean start = true;

    private HashMap<Long, String[]> tranpositionTable = new HashMap<>();
    private Hasher hasher = new Hasher();


    public PiketulusBot() {
        this.board = new BitChessBoard();
    }
    
    /**
     * Returns the next move for the bot given the current game state.
     * @param gs current game state
     * @return next move for the bot
     */
    public String nextMove(GameState gs) {
        
        if (gs.moves.size() == 0 && gs.playing == Side.WHITE) {
            this.board = new BitChessBoard();
            this.start = true;
        } else if (gs.moves.size() == 1 && gs.playing == Side.BLACK) {
            this.board = new BitChessBoard();
            this.start = true;
        }

        if (start) {
            for (String move : gs.moves) {
                board.makeMove(move);
            }
            this.start = false;
        } else if (gs.getMoveCount() > 0) {
            String lastMove = gs.getLatestMove();
            board.makeMove(lastMove);
        }

        String bestMove = iterDeepNextMove(maxDepth, board, gs.playing, gs.playing);

        if (bestMove == null) {
            return null;
        } else {
            board.makeMove(bestMove);
            return bestMove;
        }
    }

    /**
     * Finds the best move for the bot using iterative deepening.
     * @param maxDepth maximum depth for the search
     * @param board current board state
     * @param turn side to move
     * @param playing side that bot is playing
     * @return best move for the bot
     */
    private String iterDeepNextMove(int maxDepth, BitChessBoard board, Side turn, Side playing) {
        String bestFoundMove = null;
        MoveGenerator mg = new MoveGenerator(board.getBoard(), board.enpassantable, board.castlingRights, turn);
        Side opposite = turn == Side.WHITE ? Side.BLACK : Side.WHITE;
        ArrayList<String> moves = mg.getMoves();
        if (moves.size() == 0) {
            return null;
        }

        this.tranpositionTable.clear();

        long startTime = System.currentTimeMillis();
        for (int d = 2; d <= maxDepth; d++) {
            String bestMove = null;
            int bestScore = Integer.MIN_VALUE;
            for (String move : moves) {
                if (System.currentTimeMillis() - startTime > this.maxTime) {
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

    /**
     * Finds the best move for the bot using alpha-beta pruning at a given depth.
     * @param d depth of the search
     * @param board current board state
     * @param alpha alpha value
     * @param beta beta value
     * @param turn side to move
     * @param playing side that bot is playing
     * @return score of the best move
     */
    private int alphaBetaMinimax(int d, BitChessBoard board, int alpha, int beta, Side turn, Side playing) {
        
        if (d == 0) {
            return PositionEvaluator.evaluatePosition(board.getBoard(), playing);
        }

        long hash = this.hasher.getHash(board, turn);

        String lastFoundBestMove = null;

        if (this.tranpositionTable.containsKey(hash)) {
            String[] entry = this.tranpositionTable.get(hash);
            if (Integer.parseInt(entry[2]) >= d) {
                return Integer.parseInt(entry[1]);
            } else {
                lastFoundBestMove = entry[0];
            }
        }

        MoveGenerator mg = new MoveGenerator(board.getBoard(), board.enpassantable, board.castlingRights, turn);
        Side opposite = turn == Side.WHITE ? Side.BLACK : Side.WHITE;
        ArrayList<String> moves = mg.getMoves();

        if (moves.size() == 0) {
            if (mg.kingInCheck > 0 && turn == playing) {
                return Integer.MIN_VALUE + (this.maxDepth - d);
            } else if (mg.kingInCheck > 0 && turn != playing) {
                return Integer.MAX_VALUE - (this.maxDepth - d);
            } else {
                return 0;
            }
        }

        if (turn == playing) {
            int bestScore = Integer.MIN_VALUE;
            String bestMove = null;

            if (lastFoundBestMove != null) {
                BitChessBoard newBoard = new BitChessBoard(board);
                newBoard.makeMove(lastFoundBestMove);
                int score = alphaBetaMinimax(d - 1, newBoard, alpha, beta, opposite, playing);
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = lastFoundBestMove;
                }
                alpha = Math.max(alpha, score);
                if (beta <= alpha) {
                    this.tranpositionTable.put(hash, 
                            new String[]{bestMove, Integer.toString(bestScore), Integer.toString(d)});
                    return bestScore;
                }
            }

            for (String move : moves) {
                if (move.equals(lastFoundBestMove)) {
                    continue;
                }
                BitChessBoard newBoard = new BitChessBoard(board);
                newBoard.makeMove(move);
                int score = alphaBetaMinimax(d - 1, newBoard, alpha, beta, opposite, playing);
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
                alpha = Math.max(alpha, score);
                if (beta <= alpha) {
                    break;
                }
            }
            this.tranpositionTable.put(hash, new String[]{bestMove, Integer.toString(bestScore), Integer.toString(d)});
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            String bestMove = null;

            if (lastFoundBestMove != null) {
                BitChessBoard newBoard = new BitChessBoard(board);
                newBoard.makeMove(lastFoundBestMove);
                int score = alphaBetaMinimax(d - 1, newBoard, alpha, beta, opposite, playing);
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = lastFoundBestMove;
                }
                beta = Math.min(beta, score);
                if (beta <= alpha) {
                    this.tranpositionTable.put(hash, 
                            new String[]{bestMove, Integer.toString(bestScore), Integer.toString(d)});
                    return bestScore;
                }
            }

            for (String move : moves) {
                if (move.equals(lastFoundBestMove)) {
                    continue;
                }
                BitChessBoard newBoard = new BitChessBoard(board);
                newBoard.makeMove(move);
                int score = alphaBetaMinimax(d - 1, newBoard, alpha, beta, opposite, playing);
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
                beta = Math.min(beta, score);
                if (beta <= alpha) {
                    break;
                }
            }
            this.tranpositionTable.put(hash, new String[]{bestMove, Integer.toString(bestScore), Integer.toString(d)});
            return bestScore;
        }
    }
}
