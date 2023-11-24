package datastructureproject;

import chess.model.Side;


/**
 * Piece class is an object for the pieces on the board.
 * It stores the piece's type and side and has a boolean for whether the piece has moved.
 */
public class Piece {
     
    private PieceType type;
    private Side side;
    private boolean hasMoved;

    public Piece(PieceType type, Side side) {
        this.type = type;
        this.side = side;
        this.hasMoved = false;
    }

    public Piece(Piece other) {
        this.type = other.getType();
        this.side = other.getSide();
        this.hasMoved = other.getHasMoved();
    }

    public PieceType getType() {
        return this.type;
    }

    public Side getSide() {
        return this.side;
    }

    public boolean getHasMoved() {
        return this.hasMoved;
    }

    public void setHasMoved() {
        this.hasMoved = true;
    }

    public void setHasNotMoved() {
        this.hasMoved = false;
    }

}


enum PieceType {
    /*
     * PieceType enum is used to store the different types of pieces.
     */
    PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING
}