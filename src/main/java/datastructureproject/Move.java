package datastructureproject;


/**
 * Move class is an object for the moves made in the game.
 * It stores the move as a UCI string and has methods for getting the move's starting and ending coordinates.
 */
public class Move {

    private String move;

    public Move(String move) {
        this.move = move;
    }

    public String getMove() {
        return this.move;
    }

    public int getFromRow() {
        //take the first number from the move string and subtract 1 from it
        return Character.getNumericValue(this.move.charAt(1)) - 1;
    }


    public int getFromCol() {
        //take the first letter from the move string and convert it to a number
        return letterToNumber(this.move.charAt(0));
    }

    public int getToRow() {
        //take the second number from the move string and subtract 1 from it
        return Character.getNumericValue(this.move.charAt(3)) - 1;
    }

    public int getToCol() {
        //take the second letter from the move string and convert it to a number
        return letterToNumber(this.move.charAt(2));
    }

    public boolean isPromotion() {
        //check if the move is a promotion
        return this.move.length() == 5;
    }

    public PieceType getPromotionPiece() {
        //return the promotion piece
        switch (move.charAt(4)) {
            case 'q':
                return PieceType.QUEEN;
            case 'r':
                return PieceType.ROOK;
            case 'b':
                return PieceType.BISHOP;
            case 'n':
                return PieceType.KNIGHT;
            default:
                throw new IllegalArgumentException("Invalid promotion piece: " + move.charAt(4));
        }
    }


    public int letterToNumber(char letter) {
        switch (letter) {
            case 'a':
                return 0;
            case 'b':
                return 1;
            case 'c':
                return 2;
            case 'd':
                return 3;
            case 'e':
                return 4;
            case 'f':
                return 5;
            case 'g':
                return 6;
            case 'h':
                return 7;
            default:
                throw new IllegalArgumentException("Invalid letter: " + letter);
        }
    }

}