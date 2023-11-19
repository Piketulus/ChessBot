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
    private int value;

    public Piece(PieceType type, Side side) {
        this.type = type;
        this.side = side;
        this.hasMoved = false;
        
        if (type == PieceType.PAWN) {
            this.value = 1;
        } else if (type == PieceType.ROOK) {
            this.value = 5;
        } else if (type == PieceType.KNIGHT) {
            this.value = 3;
        } else if (type == PieceType.BISHOP) {
            this.value = 3;
        } else if (type == PieceType.QUEEN) {
            this.value = 9;
        } else if (type == PieceType.KING) {
            this.value = 1000;
        }
    }

    public Piece(Piece other) {
        this.type = other.getType();
        this.side = other.getSide();
        this.hasMoved = other.getHasMoved();
        this.value = other.getValue();
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

    public int getValue() {
        return this.value;
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