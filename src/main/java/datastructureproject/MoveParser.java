package datastructureproject;


/**
 * Move class contains methods for parsing moves in UCI format.
 */
public class MoveParser {


    public static int getFromRow(String move) {
        //take the first number from the move string and subtract 1 from it
        return Character.getNumericValue(move.charAt(1)) - 1;
    }


    public static int getFromCol(String move) {
        //take the first letter from the move string and convert it to a number
        return letterToNumber(move.charAt(0));
    }

    public static int getToRow(String move) {
        //take the second number from the move string and subtract 1 from it
        return Character.getNumericValue(move.charAt(3)) - 1;
    }

    public static int getToCol(String move) {
        //take the second letter from the move string and convert it to a number
        return letterToNumber(move.charAt(2));
    }

    public static boolean isPromotion(String move) {
        //check if the move is a promotion
        return move.length() == 5;
    }

    public static PieceType getPromotionPiece(String move) {
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


    public static int letterToNumber(char letter) {
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
