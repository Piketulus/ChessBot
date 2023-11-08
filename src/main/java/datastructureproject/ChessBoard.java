package datastructureproject;

import chess.model.Side;


/**
 * ChessBoard class is an object for the chess board.
 * It stores the board as a 2D array of Piece objects.
 */
public class ChessBoard {

    private Piece[][] board;


    public ChessBoard() {
        this.board = new Piece[8][8];
        this.addWhitePieces();
        this.addBlackPieces();
    }

    public Piece[][] getBoard() {
        return this.board;
    }

    public void setBoard(Piece[][] board) {
        this.board = board;
    }

    public Piece getPiece(int row, int col) {
        return this.board[row][col];
    }

    public void setPiece(int row, int col, Piece piece) {
        this.board[row][col] = piece;
    }

    public void removePiece(int row, int col) {
        this.board[row][col] = null;
    }

    public boolean isOccupiedBySide(int row, int col, Side side) {
        return this.board[row][col] != null && this.board[row][col].getSide() == side;
    }

    
    /**
     * Given a UCI move, this method makes the move on the board.
     * Expects given move to be valid (does not check for validity)
     * @param move a Move object containing the move in UCI format
     */
    public void makeMove(Move move) {

        if (move.isPromotion()) { //special case for promotion
            Piece piece = new Piece(move.getPromotionPiece(), this.getPiece(move.getFromRow(), move.getFromCol()).getSide());
            this.setPiece(move.getToRow(), move.getToCol(), piece);
            this.removePiece(move.getFromRow(), move.getFromCol());
            piece.setHasMoved();
            return;
        
        //special cases for castling moves:

        } else if (move.getMove().equals("e1c1") && this.getPiece(0, 4).getType() == PieceType.KING) {
            //move the rook from a1 to d1
            Piece piece = this.getPiece(0, 0);
            this.setPiece(0, 3, piece);
            this.removePiece(0, 0);
            piece.setHasMoved();
        } else if (move.getMove().equals("e1g1") && this.getPiece(0, 4).getType() == PieceType.KING) {
            //move the rook from h1 to f1
            Piece piece = this.getPiece(0, 7);
            this.setPiece(0, 5, piece);
            this.removePiece(0, 7);
            piece.setHasMoved();
        } else if (move.getMove().equals("e8c8") && this.getPiece(7, 4).getType() == PieceType.KING) {
            //move the rook from a8 to d8
            Piece piece = this.getPiece(7, 0);
            this.setPiece(7, 3, piece);
            this.removePiece(7, 0);
            piece.setHasMoved();
        } else if (move.getMove().equals("e8g8") && this.getPiece(7, 4).getType() == PieceType.KING) {
            //move the rook from h8 to f8
            Piece piece = this.getPiece(7, 7);
            this.setPiece(7, 5, piece);
            this.removePiece(7, 7);
            piece.setHasMoved();
        }

        Piece piece = this.getPiece(move.getFromRow(), move.getFromCol());
        this.setPiece(move.getToRow(), move.getToCol(), piece);
        this.removePiece(move.getFromRow(), move.getFromCol());
        if (!piece.getHasMoved()) {
            piece.setHasMoved();
        }
    }

    public void makeMoves(Move[] moves) {
        for (Move move : moves) {
            this.makeMove(move);
        }
    }


    private void addWhitePieces() {
        //Adds white pieces to the board

        this.board[0][0] = new Piece(PieceType.ROOK, Side.WHITE);
        this.board[0][1] = new Piece(PieceType.KNIGHT, Side.WHITE);
        this.board[0][2] = new Piece(PieceType.BISHOP, Side.WHITE);
        this.board[0][3] = new Piece(PieceType.QUEEN, Side.WHITE);
        this.board[0][4] = new Piece(PieceType.KING, Side.WHITE);
        this.board[0][5] = new Piece(PieceType.BISHOP, Side.WHITE);
        this.board[0][6] = new Piece(PieceType.KNIGHT, Side.WHITE);
        this.board[0][7] = new Piece(PieceType.ROOK, Side.WHITE);
        for (int i = 0; i < 8; i++) {
            this.board[1][i] = new Piece(PieceType.PAWN, Side.WHITE);
        }
    }

    private void addBlackPieces() {
        //Adds black pieces to the board

        this.board[7][0] = new Piece(PieceType.ROOK, Side.BLACK);
        this.board[7][1] = new Piece(PieceType.KNIGHT, Side.BLACK);
        this.board[7][2] = new Piece(PieceType.BISHOP, Side.BLACK);
        this.board[7][3] = new Piece(PieceType.QUEEN, Side.BLACK);
        this.board[7][4] = new Piece(PieceType.KING, Side.BLACK);
        this.board[7][5] = new Piece(PieceType.BISHOP, Side.BLACK);
        this.board[7][6] = new Piece(PieceType.KNIGHT, Side.BLACK);
        this.board[7][7] = new Piece(PieceType.ROOK, Side.BLACK);
        for (int i = 0; i < 8; i++) {
            this.board[6][i] = new Piece(PieceType.PAWN, Side.BLACK);
        }
    }
    
}
