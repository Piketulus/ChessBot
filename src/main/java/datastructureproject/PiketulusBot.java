package datastructureproject;

import java.util.ArrayList;

import chess.bot.ChessBot;
import chess.engine.GameState;
import chess.model.Side;

public class PiketulusBot implements ChessBot {

    private BitChessBoard board;
    private int depth = 5;
    private boolean start = true;


    public PiketulusBot() {
        this.board = new BitChessBoard();
    }
    

    public String nextMove(GameState gs) {
        
        Side opSide = gs.playing == Side.WHITE ? Side.BLACK : Side.WHITE;

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

        MoveGenerator mg = new MoveGenerator(board.getBoard(), board.enpassantable, board.castlingRights, gs.playing);
        ArrayList<String> moves = mg.getMoves();

        if (moves.size() != 0) {
            String bestMove = moves.get(0);
            int bestScore = Integer.MIN_VALUE;
            for (String move : moves) {
                BitChessBoard newBoard = new BitChessBoard(board);
                newBoard.makeMove(move);
                int score = alphaBetaMinimax(depth - 1, newBoard, Integer.MIN_VALUE, 
                                             Integer.MAX_VALUE, opSide, gs.playing);
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            }
            board.makeMove(bestMove);
            return bestMove;
        }
        
        return null;
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
                return Integer.MIN_VALUE + 1;
            } else if (mg.kingInCheck > 0 && turn != playing) {
                return Integer.MAX_VALUE - 1;
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
