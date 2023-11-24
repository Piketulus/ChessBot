package datastructureproject;

import java.util.ArrayList;

/**
 * Move class contains methods for parsing moves from and to UCI format.
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


    public static String numberToLetter(int number) {
        switch (number) {
            case 0:
                return "a";
            case 1:
                return "b";
            case 2:
                return "c";
            case 3:
                return "d";
            case 4:
                return "e";
            case 5:
                return "f";
            case 6:
                return "g";
            default:
                return "h";
        }
    }


    /**
     * Converts given starting coordinates and list of destination coordinates to a list of move strings.
     * @param fromRow starting row
     * @param fromCol starting column
     * @param destinations list of destination coordinates
     * @return list of move strings
     */
    public static ArrayList<String> coordsToMoves(int fromRow, int fromCol, 
                                                  ArrayList<int[]> destinations, boolean isPromotion) {
        ArrayList<String> moves = new ArrayList<>();
        if (!isPromotion) {
            for (int[] destination : destinations) {
                moves.add(numberToLetter(fromCol) + (fromRow + 1) 
                          + numberToLetter(destination[1]) + (destination[0] + 1));
            }
        } else {
            for (int[] destination : destinations) {
                moves.add(numberToLetter(fromCol) + (fromRow + 1) 
                                         + numberToLetter(destination[1]) + (destination[0] + 1) + "q");
                moves.add(numberToLetter(fromCol) + (fromRow + 1) 
                                         + numberToLetter(destination[1]) + (destination[0] + 1) + "r");
                moves.add(numberToLetter(fromCol) + (fromRow + 1) 
                                         + numberToLetter(destination[1]) + (destination[0] + 1) + "b");
                moves.add(numberToLetter(fromCol) + (fromRow + 1) 
                                         + numberToLetter(destination[1]) + (destination[0] + 1) + "n");
            }
        }
        return moves;
    }

}
