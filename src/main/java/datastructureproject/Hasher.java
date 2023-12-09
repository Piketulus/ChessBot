package datastructureproject;

import java.util.ArrayList;
import java.util.Random;

import chess.model.Side;

public class Hasher {

    // https://www.chessprogramming.org/Zobrist_Hashing

    private long[][] pieceSquareHashes = new long[12][64];
    private long sideHash = 0;
    private long[] castlingHashes = new long[4];
    private long[] enPassantHash = new long[8];

    public Hasher() {
        initPieceSquareHashes();
        initSideHash();
        initCastlingHashes();
        initEnPassantHash();
    }


    public long getHash(BitChessBoard board, Side turn) {
        long hash = 0;
        int piecetype = 0;
        for (long pieces : board.getBoard()) {
            ArrayList<int[]> coordinates = MoveGenerator.getCoordinatesFromBitboard(pieces);
            for (int[] coordinate : coordinates) {
                int index = coordinate[0] * 8 + coordinate[1];
                hash ^= pieceSquareHashes[piecetype][index];
            }
            piecetype++;
        }

        if (turn == Side.BLACK) {
            hash ^= sideHash;
        }

        if (board.castlingRights.contains("K")) {
            hash ^= castlingHashes[0];
        }
        if (board.castlingRights.contains("Q")) {
            hash ^= castlingHashes[1];
        }
        if (board.castlingRights.contains("k")) {
            hash ^= castlingHashes[2];
        }
        if (board.castlingRights.contains("q")) {
            hash ^= castlingHashes[3];
        }

        if (!board.enpassantable.equals("-")) {
            int index = MoveParser.letterToNumber(board.enpassantable.charAt(0));
            hash ^= enPassantHash[index];
        }

        return hash;
    }


    public long updateHash(long oldHash, BitChessBoard oldBoard, 
                           String newCastling, String newEnpassant, String move) {
        long hash = oldHash;

        if (oldBoard.castlingRights.contains("K") && !newCastling.contains("K")) {
            hash ^= castlingHashes[0];
        }
        if (oldBoard.castlingRights.contains("Q") && !newCastling.contains("Q")) {
            hash ^= castlingHashes[1];
        }
        if (oldBoard.castlingRights.contains("k") && !newCastling.contains("k")) {
            hash ^= castlingHashes[2];
        }
        if (oldBoard.castlingRights.contains("q") && !newCastling.contains("q")) {
            hash ^= castlingHashes[3];
        }

        if (oldBoard.enpassantable.equals("-") && !newEnpassant.equals("-")) {
            int index = MoveParser.letterToNumber(newEnpassant.charAt(0));
            hash ^= enPassantHash[index];
        } else if (!oldBoard.enpassantable.equals("-") && newEnpassant.equals("-")) {
            int index = MoveParser.letterToNumber(oldBoard.enpassantable.charAt(0));
            hash ^= enPassantHash[index];
        } else if (!oldBoard.enpassantable.equals("-") && !newEnpassant.equals("-")) {
            int oldIndex = MoveParser.letterToNumber(oldBoard.enpassantable.charAt(0));
            int newIndex = MoveParser.letterToNumber(newEnpassant.charAt(0));
            hash ^= enPassantHash[oldIndex];
            hash ^= enPassantHash[newIndex];
        }

        hash ^= sideHash;

        int fromIndex = MoveParser.getFromRow(move) * 8 + MoveParser.getFromCol(move);
        int toIndex = MoveParser.getToRow(move) * 8 + MoveParser.getToCol(move);

        int fromPieceType = oldBoard.getPieceTypeAtIndex(fromIndex);
        int toPieceType = oldBoard.getPieceTypeAtIndex(toIndex);

        hash ^= pieceSquareHashes[fromPieceType][fromIndex];
        hash ^= pieceSquareHashes[fromPieceType][toIndex];

        if (toPieceType != -1) {
            hash ^= pieceSquareHashes[toPieceType][toIndex];
        }

        return hash;

    }


    private void initPieceSquareHashes() {
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 64; j++) {
                pieceSquareHashes[i][j] = randomLong();
            }
        }
    }


    private void initSideHash() {
        sideHash = randomLong();
    }

    
    private void initCastlingHashes() {
        for (int i = 0; i < 4; i++) {
            castlingHashes[i] = randomLong();
        }
    }


    private void initEnPassantHash() {
        for (int i = 0; i < 8; i++) {
            enPassantHash[i] = randomLong();
        }
    }

    
    private long randomLong() {
        Random random = new Random();
        return random.nextLong();
    }
    
    
}
