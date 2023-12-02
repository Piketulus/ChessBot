package datastructureproject;

import java.util.ArrayList;

import chess.model.Side;


/**
 * MoveGenerator class is used for generating valid moves for a given board.
 * It uses bitboards for storing the board state to make the move generation easier to code in some cases and faster to run.
 */
public class MoveGenerator {


    private String enpassantable;
    private String castlingRights;
    private Side sideToMove;

    private ArrayList<long[]> pinnedPieces; // [0] = rowPinned, [1] = colPinned, [2] = legal move 'ray'

    public int kingInCheck; // 0 = not in check, 1 = in check, 2 = double check
    private long attackers; // bitboard of checking pieces
    private long inCheckLegalMoves; // bitboard of legal moves when in check

    public long whitePieces = 0L;
    public long blackPieces = 0L;

    public long whitePawns = 0L;
    public long whiteKnights = 0L;
    public long whiteBishops = 0L;
    public long whiteRooks = 0L;
    public long whiteQueens = 0L;
    public long whiteKing = 0L;

    public long blackPawns = 0L;
    public long blackKnights = 0L;
    public long blackBishops = 0L;
    public long blackRooks = 0L;
    public long blackQueens = 0L;
    public long blackKing = 0L;


    public MoveGenerator(long[] board, String enpassantable, String castlingRights, Side sideToMove) {
        this.enpassantable = enpassantable;
        this.castlingRights = castlingRights;
        this.sideToMove = sideToMove;
        this.pinnedPieces = new ArrayList<>();

        this.fillBitboards(board);

        this.kingInCheck = this.kingInCheck();
        this.attackers = this.checkAttackers();
        this.getPinnedPieces();

        if (this.kingInCheck == 1) {
            this.inCheckLegalMoves = this.getLegalMovesForNonKingPiecesWhenChecked(this.attackers);
        }
    }


    /**
     * The one method to be called from outside the class to get the legal moves for the current board state.
     * @return ArrayList of legal moves in UCI string format
     */
    public ArrayList<String> getMoves() {

        ArrayList<String> legalMoves = new ArrayList<>();

        boolean inCheck = this.kingInCheck == 1;

        // if the king is in double check then only king moves are legal
        if (this.kingInCheck == 2) {
            ArrayList<int[]> kingCoords = getCoordinatesFromBitboard(this.sideToMove == Side.WHITE ? this.whiteKing : this.blackKing);
            int kingRow = kingCoords.get(0)[0];
            int kingCol = kingCoords.get(0)[1];
            long kingMoves = this.getKingMovesBitBoard(kingRow, kingCol);
            ArrayList<int[]> kingMovesCoords = getCoordinatesFromBitboard(kingMoves);
            ArrayList<String> kingMovesUCI = MoveParser.coordsToMoves(kingRow, kingCol, kingMovesCoords, false);
            legalMoves.addAll(kingMovesUCI);
        } else {
            // if not in double check then get moves for all pieces
            if (this.sideToMove == Side.WHITE) {
                ArrayList<int[]> pawnCoords = getCoordinatesFromBitboard(this.whitePawns);
                for (int[] coords : pawnCoords) {
                    boolean pinned = this.isPinned(coords[0], coords[1]);
                    boolean promote = false;
                    if (coords[0] == 6) {
                        promote = true;
                    }
                    long pawnMoves = this.getPawnMovesBitBoard(coords[0], coords[1], inCheck, pinned);
                    ArrayList<int[]> pawnMovesCoords = getCoordinatesFromBitboard(pawnMoves);
                    ArrayList<String> pawnMovesUCI = MoveParser.coordsToMoves(coords[0], coords[1], pawnMovesCoords, promote);
                    legalMoves.addAll(pawnMovesUCI);
                }
                ArrayList<int[]> knightCoords = getCoordinatesFromBitboard(this.whiteKnights);
                for (int[] coords : knightCoords) {
                    boolean pinned = this.isPinned(coords[0], coords[1]);
                    long knightMoves = this.getKnightMovesBitBoard(coords[0], coords[1], inCheck, pinned);
                    ArrayList<int[]> knightMovesCoords = getCoordinatesFromBitboard(knightMoves);
                    ArrayList<String> knightMovesUCI = MoveParser.coordsToMoves(coords[0], coords[1], knightMovesCoords, false);
                    legalMoves.addAll(knightMovesUCI);
                }
                ArrayList<int[]> bishopCoords = getCoordinatesFromBitboard(this.whiteBishops);
                for (int[] coords : bishopCoords) {
                    boolean pinned = this.isPinned(coords[0], coords[1]);
                    long bishopMoves = this.getBishopMovesBitBoard(coords[0], coords[1], inCheck, pinned);
                    ArrayList<int[]> bishopMovesCoords = getCoordinatesFromBitboard(bishopMoves);
                    ArrayList<String> bishopMovesUCI = MoveParser.coordsToMoves(coords[0], coords[1], bishopMovesCoords, false);
                    legalMoves.addAll(bishopMovesUCI);
                }
                ArrayList<int[]> rookCoords = getCoordinatesFromBitboard(this.whiteRooks);
                for (int[] coords : rookCoords) {
                    boolean pinned = this.isPinned(coords[0], coords[1]);
                    long rookMoves = this.getRookMovesBitBoard(coords[0], coords[1], inCheck, pinned);
                    ArrayList<int[]> rookMovesCoords = getCoordinatesFromBitboard(rookMoves);
                    ArrayList<String> rookMovesUCI = MoveParser.coordsToMoves(coords[0], coords[1], rookMovesCoords, false);
                    legalMoves.addAll(rookMovesUCI);
                }
                ArrayList<int[]> queenCoords = getCoordinatesFromBitboard(this.whiteQueens);
                for (int[] coords : queenCoords) {
                    boolean pinned = this.isPinned(coords[0], coords[1]);
                    long queenMoves = this.getQueenMovesBitBoard(coords[0], coords[1], inCheck, pinned);
                    ArrayList<int[]> queenMovesCoords = getCoordinatesFromBitboard(queenMoves);
                    ArrayList<String> queenMovesUCI = MoveParser.coordsToMoves(coords[0], coords[1], queenMovesCoords, false);
                    legalMoves.addAll(queenMovesUCI);
                }
                ArrayList<int[]> kingCoords = getCoordinatesFromBitboard(this.whiteKing);
                for (int[] coords : kingCoords) {
                    long kingMoves = this.getKingMovesBitBoard(coords[0], coords[1]);
                    ArrayList<int[]> kingMovesCoords = getCoordinatesFromBitboard(kingMoves);
                    ArrayList<String> kingMovesUCI = MoveParser.coordsToMoves(coords[0], coords[1], kingMovesCoords, false);
                    legalMoves.addAll(kingMovesUCI);
                }
            } else {
                ArrayList<int[]> pawnCoords = getCoordinatesFromBitboard(this.blackPawns);
                for (int[] coords : pawnCoords) {
                    boolean pinned = this.isPinned(coords[0], coords[1]);
                    boolean promote = false;
                    if (coords[0] == 1) {
                        promote = true;
                    }
                    long pawnMoves = this.getPawnMovesBitBoard(coords[0], coords[1], inCheck, pinned);
                    ArrayList<int[]> pawnMovesCoords = getCoordinatesFromBitboard(pawnMoves);
                    ArrayList<String> pawnMovesUCI = MoveParser.coordsToMoves(coords[0], coords[1], pawnMovesCoords, promote);
                    legalMoves.addAll(pawnMovesUCI);
                }
                ArrayList<int[]> knightCoords = getCoordinatesFromBitboard(this.blackKnights);
                for (int[] coords : knightCoords) {
                    boolean pinned = this.isPinned(coords[0], coords[1]);
                    long knightMoves = this.getKnightMovesBitBoard(coords[0], coords[1], inCheck, pinned);
                    ArrayList<int[]> knightMovesCoords = getCoordinatesFromBitboard(knightMoves);
                    ArrayList<String> knightMovesUCI = MoveParser.coordsToMoves(coords[0], coords[1], knightMovesCoords, false);
                    legalMoves.addAll(knightMovesUCI);
                }
                ArrayList<int[]> bishopCoords = getCoordinatesFromBitboard(this.blackBishops);
                for (int[] coords : bishopCoords) {
                    boolean pinned = this.isPinned(coords[0], coords[1]);
                    long bishopMoves = this.getBishopMovesBitBoard(coords[0], coords[1], inCheck, pinned);
                    ArrayList<int[]> bishopMovesCoords = getCoordinatesFromBitboard(bishopMoves);
                    ArrayList<String> bishopMovesUCI = MoveParser.coordsToMoves(coords[0], coords[1], bishopMovesCoords, false);
                    legalMoves.addAll(bishopMovesUCI);
                }
                ArrayList<int[]> rookCoords = getCoordinatesFromBitboard(this.blackRooks);
                for (int[] coords : rookCoords) {
                    boolean pinned = this.isPinned(coords[0], coords[1]);
                    long rookMoves = this.getRookMovesBitBoard(coords[0], coords[1], inCheck, pinned);
                    ArrayList<int[]> rookMovesCoords = getCoordinatesFromBitboard(rookMoves);
                    ArrayList<String> rookMovesUCI = MoveParser.coordsToMoves(coords[0], coords[1], rookMovesCoords, false);
                    legalMoves.addAll(rookMovesUCI);
                }
                ArrayList<int[]> queenCoords = getCoordinatesFromBitboard(this.blackQueens);
                for (int[] coords : queenCoords) {
                    boolean pinned = this.isPinned(coords[0], coords[1]);
                    long queenMoves = this.getQueenMovesBitBoard(coords[0], coords[1], inCheck, pinned);
                    ArrayList<int[]> queenMovesCoords = getCoordinatesFromBitboard(queenMoves);
                    ArrayList<String> queenMovesUCI = MoveParser.coordsToMoves(coords[0], coords[1], queenMovesCoords, false);
                    legalMoves.addAll(queenMovesUCI);
                }
                ArrayList<int[]> kingCoords = getCoordinatesFromBitboard(this.blackKing);
                for (int[] coords : kingCoords) {
                    long kingMoves = this.getKingMovesBitBoard(coords[0], coords[1]);
                    ArrayList<int[]> kingMovesCoords = getCoordinatesFromBitboard(kingMoves);
                    ArrayList<String> kingMovesUCI = MoveParser.coordsToMoves(coords[0], coords[1], kingMovesCoords, false);
                    legalMoves.addAll(kingMovesUCI);
                }
            }
        }
        
        return legalMoves;
        
    }


    private boolean isPinned(int row, int col) {
        //check if the piece is pinned by seeing if row and col match any of the pinned pieces
        for (long[] pinnedPiece : this.pinnedPieces) {
            if (pinnedPiece[0] == row && pinnedPiece[1] == col) {
                return true;
            }
        }
        return false;
    }


    /**
     * Checks if the king is in check by checking the number of checking pieces.
     * @return the number of checking pieces (if more than 0 then in check), this is useful for detecting double check
     */
    private int kingInCheck() {

        long attackers = this.checkAttackers();

        return Long.bitCount(attackers);
    }


    /**
     * Checks for checking pieces of the enemy side to move by seeing if moves from the king by different pieces intersect with opponent pieces.
     * @return bitboard of checking pieces
     */
    private long checkAttackers() {

        ArrayList<int[]> kingCoords = getCoordinatesFromBitboard(this.sideToMove == Side.WHITE ? this.whiteKing : this.blackKing);

        int kingRow = kingCoords.get(0)[0];
        int kingCol = kingCoords.get(0)[1];

        long attackers = 0L;

        if (this.sideToMove == Side.WHITE) {
            attackers = (this.getBishopMovesBitBoard(kingRow, kingCol, false, false) & (this.blackBishops | this.blackQueens))
                | (this.getRookMovesBitBoard(kingRow, kingCol, false, false) & (this.blackRooks | this.blackQueens))
                | (this.getKnightMovesBitBoard(kingRow, kingCol, false, false) & this.blackKnights)
                | (this.getPawnMovesBitBoard(kingRow, kingCol, false, false) & this.blackPawns);
        } else {
            attackers = (this.getBishopMovesBitBoard(kingRow, kingCol, false, false) & (this.whiteBishops | this.whiteQueens))
                | (this.getRookMovesBitBoard(kingRow, kingCol, false, false) & (this.whiteRooks | this.whiteQueens))
                | (this.getKnightMovesBitBoard(kingRow, kingCol, false, false) & this.whiteKnights)
                | (this.getPawnMovesBitBoard(kingRow, kingCol, false, false) & this.whitePawns);
        }

        return attackers;
    }


    /**
     * Finds all pinned pieces for the side to move and stores them in the pinnedPieces list.
     * Does this by finding sliding moves from the kings position if it were opposite side to move and intersecting with the pieces of the side to move.
     * Any pieces in the overlap are potentially pinned.
     * The piece can then be removed and we can see if it results in check then it is pinned.
     */
    private void getPinnedPieces() {

        ArrayList<int[]> kingCoords = getCoordinatesFromBitboard(this.sideToMove == Side.WHITE ? this.whiteKing : this.blackKing);

        int kingRow = kingCoords.get(0)[0];
        int kingCol = kingCoords.get(0)[1];

        long kingSlidingMoves = 0L;

        this.sideToMove = this.sideToMove == Side.WHITE ? Side.BLACK : Side.WHITE;
        kingSlidingMoves = this.getQueenMovesBitBoard(kingRow, kingCol, false, false);
        this.sideToMove = this.sideToMove == Side.WHITE ? Side.BLACK : Side.WHITE;

        long pinnedPiecesMaybe;

        if (this.sideToMove == Side.WHITE) {
            pinnedPiecesMaybe = kingSlidingMoves & this.whitePieces;
        } else {
            pinnedPiecesMaybe = kingSlidingMoves & this.blackPieces;
        }

        if (pinnedPiecesMaybe == 0L) {
            return;
        }

        ArrayList<int[]> pinnedPieceCoords = getCoordinatesFromBitboard(pinnedPiecesMaybe);

        for (int[] coords : pinnedPieceCoords) {
            /*for each pinned piece, remove the pinned piece from the board, 
            find the attackers coords and type, intersect the moves of the attacker with moves 
            of the same type from the king plus the attackers coords to make the legal moves 'ray', 
            then put the pinned piece back on the board
             */ 
            long attackersBefore = this.checkAttackers();

            long pinned = 1L << (coords[0] * 8 + coords[1]);

            if (this.sideToMove == Side.WHITE) {
                this.whitePieces = this.whitePieces & ~pinned;
            } else {
                this.blackPieces = this.blackPieces & ~pinned;
            }

            long attackersAfter = this.checkAttackers();
            long attacker = attackersBefore ^ attackersAfter;

            if (attacker == 0L) {
                if (this.sideToMove == Side.WHITE) {
                    this.whitePieces |= pinned;
                } else {
                    this.blackPieces |= pinned;
                }
                continue;
            }

            long legalMoves = this.getLegalMovesForNonKingPiecesWhenChecked(attacker);

            if (this.sideToMove == Side.WHITE) {
                this.whitePieces |= pinned;
            } else {
                this.blackPieces |= pinned;
            }

            this.pinnedPieces.add(new long[]{coords[0], coords[1], legalMoves});
        }
    }


    /**
     * Helper function for getPinnedPieces()
     * intersects the moves of the attacker with moves of the 
     * same piece type from the king plus the attackers coords to make the legal moves 'ray'
     * @return bitboard of legal moves
     */
    private long getLegalMovesForNonKingPiecesWhenChecked(long attacker) {

        ArrayList<int[]> kingCoords = getCoordinatesFromBitboard(this.sideToMove == Side.WHITE ? this.whiteKing : this.blackKing);

        int kingRow = kingCoords.get(0)[0];
        int kingCol = kingCoords.get(0)[1];

        ArrayList<int[]> attackerCoords = getCoordinatesFromBitboard(attacker);

        int attackerRow = attackerCoords.get(0)[0];
        int attackerCol = attackerCoords.get(0)[1];

        long legalMoves = 0L;

        if ((attacker & this.blackRooks) != 0L || (attacker & this.whiteRooks) != 0L) {
            legalMoves = this.getRookMovesBitBoard(kingRow, kingCol, false, false);
            legalMoves &= this.getRookMovesBitBoard(attackerRow, attackerCol, false, false) | attacker;
        } else if ((attacker & this.blackBishops) != 0L || (attacker & this.whiteBishops) != 0L) {
            legalMoves = this.getBishopMovesBitBoard(kingRow, kingCol, false, false);
            legalMoves &= this.getBishopMovesBitBoard(attackerRow, attackerCol, false, false) | attacker;
        } else if ((attacker & this.blackQueens) != 0L || (attacker & this.whiteQueens) != 0L) {
            //if queen is not on same file or rank, the bishop moves, otherwise the rook moves
            if (attackerRow != kingRow && attackerCol != kingCol) {
                legalMoves = this.getBishopMovesBitBoard(kingRow, kingCol, false, false);
                legalMoves &= this.getBishopMovesBitBoard(attackerRow, attackerCol, false, false) | attacker;
            } else {
                legalMoves = this.getRookMovesBitBoard(kingRow, kingCol, false, false);
                legalMoves &= this.getRookMovesBitBoard(attackerRow, attackerCol, false, false) | attacker;
            }
        } else if ((attacker & this.blackKnights) != 0L || (attacker & this.whiteKnights) != 0L) {
            legalMoves = attacker;
        } else if ((attacker & this.blackPawns) != 0L || (attacker & this.whitePawns) != 0L) {
            legalMoves = attacker;
        }

        return legalMoves;
    }


    private long getKingMovesBitBoard(int row, int col) {

        long kingmoves = 0L;

        long coveredByOppKing = this.getOppositeKingCoveredSquares();

        if (this.sideToMove == Side.WHITE) {
            if (row + 1 < 8) {
                if (((this.whitePieces >> ((row + 1) * 8 + col)) & 1L) == 0L) {
                    if (((coveredByOppKing >> ((row + 1) * 8 + col)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row + 1, col)) {
                            kingmoves |= 1L << ((row + 1) * 8 + col);
                        }
                    }
                }
            }
            if (row - 1 >= 0) {
                if (((this.whitePieces >> ((row - 1) * 8 + col)) & 1L) == 0L) {
                    if (((coveredByOppKing >> ((row - 1) * 8 + col)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row - 1, col)) {
                            kingmoves |= 1L << ((row - 1) * 8 + col);
                        }
                    }
                }
            }
            if (col + 1 < 8) {
                if (((this.whitePieces >> (row * 8 + col + 1)) & 1L) == 0L) {
                    if (((coveredByOppKing >> (row * 8 + col + 1)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row, col + 1)) {
                            kingmoves |= 1L << (row * 8 + col + 1);
                        }
                    }
                }
            }
            if (col - 1 >= 0) {
                if (((this.whitePieces >> (row * 8 + col - 1)) & 1L) == 0L) {
                    if (((coveredByOppKing >> (row * 8 + col - 1)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row, col - 1)) {
                            kingmoves |= 1L << (row * 8 + col - 1);
                        }
                    }
                }
            }
            if (row + 1 < 8 && col + 1 < 8) {
                if (((this.whitePieces >> ((row + 1) * 8 + col + 1)) & 1L) == 0L) {
                    if (((coveredByOppKing >> ((row + 1) * 8 + col + 1)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row + 1, col + 1)) {
                            kingmoves |= 1L << ((row + 1) * 8 + col + 1);
                        }
                    }
                }
            }
            if (row + 1 < 8 && col - 1 >= 0) {
                if (((this.whitePieces >> ((row + 1) * 8 + col - 1)) & 1L) == 0L) {
                    if (((coveredByOppKing >> ((row + 1) * 8 + col - 1)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row + 1, col - 1)) {
                            kingmoves |= 1L << ((row + 1) * 8 + col - 1);
                        }
                    }
                }
            }
            if (row - 1 >= 0 && col + 1 < 8) {
                if (((this.whitePieces >> ((row - 1) * 8 + col + 1)) & 1L) == 0L) {
                    if (((coveredByOppKing >> ((row - 1) * 8 + col + 1)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row - 1, col + 1)) {
                            kingmoves |= 1L << ((row - 1) * 8 + col + 1);
                        }
                    }
                }
            }
            if (row - 1 >= 0 && col - 1 >= 0) {
                if (((this.whitePieces >> ((row - 1) * 8 + col - 1)) & 1L) == 0L) {
                    if (((coveredByOppKing >> ((row - 1) * 8 + col - 1)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row - 1, col - 1)) {
                            kingmoves |= 1L << ((row - 1) * 8 + col - 1);
                        }
                    }
                }
            }
        } else {
            if (row + 1 < 8) {
                if (((this.blackPieces >> ((row + 1) * 8 + col)) & 1L) == 0L) {
                    if (((coveredByOppKing >> ((row + 1) * 8 + col)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row + 1, col)) {
                            kingmoves |= 1L << ((row + 1) * 8 + col);
                        }
                    }
                }
            }
            if (row - 1 >= 0) {
                if (((this.blackPieces >> ((row - 1) * 8 + col)) & 1L) == 0L) {
                    if (((coveredByOppKing >> ((row - 1) * 8 + col)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row - 1, col)) {
                            kingmoves |= 1L << ((row - 1) * 8 + col);
                        }
                    }
                }
            }
            if (col + 1 < 8) {
                if (((this.blackPieces >> (row * 8 + col + 1)) & 1L) == 0L) {
                    if (((coveredByOppKing >> (row * 8 + col + 1)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row, col + 1)) {
                            kingmoves |= 1L << (row * 8 + col + 1);
                        }
                    }
                }
            }
            if (col - 1 >= 0) {
                if (((this.blackPieces >> (row * 8 + col - 1)) & 1L) == 0L) {
                    if (((coveredByOppKing >> (row * 8 + col - 1)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row, col - 1)) {
                            kingmoves |= 1L << (row * 8 + col - 1);
                        }
                    }
                }
            }
            if (row + 1 < 8 && col + 1 < 8) {
                if (((this.blackPieces >> ((row + 1) * 8 + col + 1)) & 1L) == 0L) {
                    if (((coveredByOppKing >> ((row + 1) * 8 + col + 1)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row + 1, col + 1)) {
                            kingmoves |= 1L << ((row + 1) * 8 + col + 1);
                        }
                    }
                }
            }
            if (row + 1 < 8 && col - 1 >= 0) {
                if (((this.blackPieces >> ((row + 1) * 8 + col - 1)) & 1L) == 0L) {
                    if (((coveredByOppKing >> ((row + 1) * 8 + col - 1)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row + 1, col - 1)) {
                            kingmoves |= 1L << ((row + 1) * 8 + col - 1);
                        }
                    }
                }
            }
            if (row - 1 >= 0 && col + 1 < 8) {
                if (((this.blackPieces >> ((row - 1) * 8 + col + 1)) & 1L) == 0L) {
                    if (((coveredByOppKing >> ((row - 1) * 8 + col + 1)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row - 1, col + 1)) {
                            kingmoves |= 1L << ((row - 1) * 8 + col + 1);
                        }
                    }
                }
            }
            if (row - 1 >= 0 && col - 1 >= 0) {
                if (((this.blackPieces >> ((row - 1) * 8 + col - 1)) & 1L) == 0L) {
                    if (((coveredByOppKing >> ((row - 1) * 8 + col - 1)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row - 1, col - 1)) {
                            kingmoves |= 1L << ((row - 1) * 8 + col - 1);
                        }
                    }
                }
            }
        }

        // check if king is in check so need to find castling moves
        if (this.kingInCheck > 0) {
            return kingmoves;
        }

        //if king is not in check then check for castling moves
        if (this.sideToMove == Side.WHITE) {
            if (row == 0 && col == 4 && (this.castlingRights.contains("K") || this.castlingRights.contains("Q"))) {
                if ((((this.whiteRooks >> (0 * 8 + 7)) & 1L) > 0) && this.castlingRights.contains("K")) {
                    if (((this.whitePieces >> (0 * 8 + 5)) & 1L) == 0L && ((this.whitePieces >> (0 * 8 + 6)) & 1L) == 0L) {
                        if (((this.blackPieces >> (0 * 8 + 5)) & 1L) == 0L && ((this.blackPieces >> (0 * 8 + 6)) & 1L) == 0L) {
                            if (((coveredByOppKing >> (0 * 8 + 5)) & 1L) == 0L && ((coveredByOppKing >> (0 * 8 + 6)) & 1L) == 0L) {
                                if (!this.kingInCheckAfterMove(0, 5) && !this.kingInCheckAfterMove(0, 6)) {
                                    kingmoves |= 1L << (0 * 8 + 6);
                                }
                            }
                        }
                    }
                }
                if ((((this.whiteRooks >> (0 * 8 + 0)) & 1L) > 0) && this.castlingRights.contains("Q")) {
                    if (((this.whitePieces >> (0 * 8 + 3)) & 1L) == 0L && ((this.whitePieces >> (0 * 8 + 2)) & 1L) == 0L && ((this.whitePieces >> (0 * 8 + 1)) & 1L) == 0L) {
                        if (((this.blackPieces >> (0 * 8 + 3)) & 1L) == 0L && ((this.blackPieces >> (0 * 8 + 2)) & 1L) == 0L && ((this.blackPieces >> (0 * 8 + 1)) & 1L) == 0L) {
                            if (((coveredByOppKing >> (0 * 8 + 3)) & 1L) == 0L && ((coveredByOppKing >> (0 * 8 + 2)) & 1L) == 0L) {
                                if (!this.kingInCheckAfterMove(0, 3) && !this.kingInCheckAfterMove(0, 2)) {
                                    kingmoves |= 1L << (0 * 8 + 2);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (row == 7 && col == 4 && (this.castlingRights.contains("k") || this.castlingRights.contains("q"))) {
                if ((((this.blackRooks >> (7 * 8 + 7)) & 1L) > 0) && this.castlingRights.contains("k")) {
                    if (((this.blackPieces >> (7 * 8 + 5)) & 1L) == 0L && ((this.blackPieces >> (7 * 8 + 6)) & 1L) == 0L) {
                        if (((this.whitePieces >> (7 * 8 + 5)) & 1L) == 0L && ((this.whitePieces >> (7 * 8 + 6)) & 1L) == 0L) {
                            if (((coveredByOppKing >> (7 * 8 + 5)) & 1L) == 0L && ((coveredByOppKing >> (7 * 8 + 6)) & 1L) == 0L) {
                                if (!this.kingInCheckAfterMove(7, 5) && !this.kingInCheckAfterMove(7, 6)) {
                                    kingmoves |= 1L << (7 * 8 + 6);
                                }
                            }
                        }
                    }
                }
                if ((((this.blackRooks >> (7 * 8 + 0)) & 1L) > 0) && this.castlingRights.contains("q")) {
                    if (((this.blackPieces >> (7 * 8 + 3)) & 1L) == 0L && ((this.blackPieces >> (7 * 8 + 2)) & 1L) == 0L && ((this.blackPieces >> (7 * 8 + 1)) & 1L) == 0L) {
                        if (((this.whitePieces >> (7 * 8 + 3)) & 1L) == 0L && ((this.whitePieces >> (7 * 8 + 2)) & 1L) == 0L && ((this.whitePieces >> (7 * 8 + 1)) & 1L) == 0L) {
                            if (((coveredByOppKing >> (7 * 8 + 3)) & 1L) == 0L && ((coveredByOppKing >> (7 * 8 + 2)) & 1L) == 0L) {
                                if (!this.kingInCheckAfterMove(7, 3) && !this.kingInCheckAfterMove(7, 2)) {
                                    kingmoves |= 1L << (7 * 8 + 2);
                                }
                            }
                        }
                    }
                }
            } 
        }

        return kingmoves;

    }


    /**
     * Helper function for getKingMovesBitBoard()
     * Checks if the king is in check after moving it to the given square
     * @param row of the square to move the king to
     * @param col of the square to move the king to
     * @return true if the king is in check after the move, false otherwise
     */
    private boolean kingInCheckAfterMove(int row, int col) {

        // remove the king from the board and place it in the new square by row and col

        long king = this.sideToMove == Side.WHITE ? this.whiteKing : this.blackKing;

        if (this.sideToMove == Side.WHITE) {
            this.whitePieces = this.whitePieces & ~king;
            this.whiteKing = 1L << (row * 8 + col);
            this.whitePieces |= this.whiteKing;
        } else {
            this.blackPieces = this.blackPieces & ~king;
            this.blackKing = 1L << (row * 8 + col);
            this.blackPieces |= this.blackKing;
        }

        // check if the king is in check

        int kingInCheck = this.kingInCheck();

        // put the king back on the board

        if (this.sideToMove == Side.WHITE) {
            this.whitePieces = this.whitePieces & ~this.whiteKing;
            this.whiteKing = king;
            this.whitePieces |= this.whiteKing;
        } else {
            this.blackPieces = this.blackPieces & ~this.blackKing;
            this.blackKing = king;
            this.blackPieces |= this.blackKing;
        }

        return kingInCheck > 0;

    }


    /**
     * Helper function for getKingMovesBitBoard()
     * Finds all squares covered by the opposite king
     * @return bitboard of covered squares
     */
    private long getOppositeKingCoveredSquares() {
        ArrayList<int[]> oppositeKingCoords = getCoordinatesFromBitboard(this.sideToMove == Side.WHITE ? this.blackKing : this.whiteKing);

        long oppositeKingCoveredSquares = 0L;

        int oppositeKingRow = oppositeKingCoords.get(0)[0];
        int oppositeKingCol = oppositeKingCoords.get(0)[1];

        if (oppositeKingRow + 1 < 8) {
            oppositeKingCoveredSquares |= 1L << ((oppositeKingRow + 1) * 8 + oppositeKingCol);
        }
        if (oppositeKingRow - 1 >= 0) {
            oppositeKingCoveredSquares |= 1L << ((oppositeKingRow - 1) * 8 + oppositeKingCol);
        }
        if (oppositeKingCol + 1 < 8) {
            oppositeKingCoveredSquares |= 1L << (oppositeKingRow * 8 + oppositeKingCol + 1);
        }
        if (oppositeKingCol - 1 >= 0) {
            oppositeKingCoveredSquares |= 1L << (oppositeKingRow * 8 + oppositeKingCol - 1);
        }
        if (oppositeKingRow + 1 < 8 && oppositeKingCol + 1 < 8) {
            oppositeKingCoveredSquares |= 1L << ((oppositeKingRow + 1) * 8 + oppositeKingCol + 1);
        }
        if (oppositeKingRow + 1 < 8 && oppositeKingCol - 1 >= 0) {
            oppositeKingCoveredSquares |= 1L << ((oppositeKingRow + 1) * 8 + oppositeKingCol - 1);
        }
        if (oppositeKingRow - 1 >= 0 && oppositeKingCol + 1 < 8) {
            oppositeKingCoveredSquares |= 1L << ((oppositeKingRow - 1) * 8 + oppositeKingCol + 1);
        }
        if (oppositeKingRow - 1 >= 0 && oppositeKingCol - 1 >= 0) {
            oppositeKingCoveredSquares |= 1L << ((oppositeKingRow - 1) * 8 + oppositeKingCol - 1);
        }

        return oppositeKingCoveredSquares;

    }


    private long getPawnMovesBitBoard(int row, int col, boolean inCheck, boolean pinned) {
        long pawnMoves = 0L;

        // go straight
        if (this.sideToMove == Side.WHITE) {
            if (row + 1 < 8) {
                if (((this.whitePieces >> ((row + 1) * 8 + col)) & 1L) == 0L && ((this.blackPieces >> ((row + 1) * 8 + col)) & 1L) == 0L) {
                    pawnMoves |= 1L << ((row + 1) * 8 + col);
                }
            }
            if (row == 1 && ((this.whitePieces >> ((row + 1) * 8 + col)) & 1L) == 0L && ((this.blackPieces >> ((row + 1) * 8 + col)) & 1L) == 0L) {
                if (((this.whitePieces >> ((row + 2) * 8 + col)) & 1L) == 0L && ((this.blackPieces >> ((row + 2) * 8 + col)) & 1L) == 0L) {
                    pawnMoves |= 1L << ((row + 2) * 8 + col);
                }
            }
        } else {
            if (row - 1 >= 0) {
                if (((this.whitePieces >> ((row - 1) * 8 + col)) & 1L) == 0L && ((this.blackPieces >> ((row - 1) * 8 + col)) & 1L) == 0L) {
                    pawnMoves |= 1L << ((row - 1) * 8 + col);
                }
            }
            if (row == 6 && ((this.whitePieces >> ((row - 1) * 8 + col)) & 1L) == 0L && ((this.blackPieces >> ((row - 1) * 8 + col)) & 1L) == 0L) {
                if (((this.whitePieces >> ((row - 2) * 8 + col)) & 1L) == 0L && ((this.blackPieces >> ((row - 2) * 8 + col)) & 1L) == 0L) {
                    pawnMoves |= 1L << ((row - 2) * 8 + col);
                }
            }
        }

        // capture
        if (this.sideToMove == Side.WHITE) {
            if (row + 1 < 8 && col - 1 >= 0) {
                if (((this.blackPieces >> ((row + 1) * 8 + col - 1)) & 1L) > 0) {
                    pawnMoves |= 1L << ((row + 1) * 8 + col - 1);
                }
            }
            if (row + 1 < 8 && col + 1 < 8) {
                if (((this.blackPieces >> ((row + 1) * 8 + col + 1)) & 1L) > 0) {
                    pawnMoves |= 1L << ((row + 1) * 8 + col + 1);
                }
            }
        } else {
            if (row - 1 >= 0 && col - 1 >= 0) {
                if (((this.whitePieces >> ((row - 1) * 8 + col - 1)) & 1L) > 0) {
                    pawnMoves |= 1L << ((row - 1) * 8 + col - 1);
                }
            }
            if (row - 1 >= 0 && col + 1 < 8) {
                if (((this.whitePieces >> ((row - 1) * 8 + col + 1)) & 1L) > 0) {
                    pawnMoves |= 1L << ((row - 1) * 8 + col + 1);
                }
            }
        }

        // en passant
        if (!this.enpassantable.equals("-")) {

            if (this.sideToMove == Side.WHITE) {
                if (row == 4) {
                    if (col - 1 >= 0) {
                        if (((this.blackPawns >> (row * 8 + col - 1)) & 1L) > 0) {
                            if (MoveParser.getFromCol(this.enpassantable) == (col - 1) && MoveParser.getFromRow(this.enpassantable) == row) {
                                if (!this.enPassantLeadsToCheck(row, col, -1, 1)) {
                                    pawnMoves |= 1L << ((row + 1) * 8 + col - 1);
                                }
                            }
                        }
                    }
                    if (col + 1 < 8) {
                        if (((this.blackPawns >> (row * 8 + col + 1)) & 1L) > 0) {
                            if (MoveParser.getFromCol(this.enpassantable) == (col + 1) && MoveParser.getFromRow(this.enpassantable) == row) {
                                if (!this.enPassantLeadsToCheck(row, col, 1, 1)) {
                                    pawnMoves |= 1L << ((row + 1) * 8 + col + 1);
                                }
                            }
                        }
                    }
                }
            } else {
                if (row == 3) {
                    if (col - 1 >= 0) {
                        if (((this.whitePawns >> (row * 8 + col - 1)) & 1L) > 0) {
                            if (MoveParser.getFromCol(this.enpassantable) == (col - 1) && MoveParser.getFromRow(this.enpassantable) == row) {
                                if (!this.enPassantLeadsToCheck(row, col, -1, -1)) {
                                    pawnMoves |= 1L << ((row - 1) * 8 + col - 1);
                                }
                            }
                        }
                    }
                    if (col + 1 < 8) {
                        if (((this.whitePawns >> (row * 8 + col + 1)) & 1L) > 0) {
                            if (MoveParser.getFromCol(this.enpassantable) == (col + 1) && MoveParser.getFromRow(this.enpassantable) == row) {
                                if (!this.enPassantLeadsToCheck(row, col, 1, -1)) {
                                    pawnMoves |= 1L << ((row - 1) * 8 + col + 1);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (pinned) {
            for (long[] pinnedPiece : this.pinnedPieces) {
                if (pinnedPiece[0] == row && pinnedPiece[1] == col) {
                    pawnMoves &= pinnedPiece[2];
                    break;
                }
            }
        }

        if (!inCheck) {
            return pawnMoves;
        } else {
            // special check for if an enpassant capture gets rid of check
            long enpassantGetRidOfCheck = 0L;
            
            if (!this.enpassantable.equals("-")) {
                ArrayList<int[]> attackerCoords = getCoordinatesFromBitboard(this.attackers);
                int attackerRow = attackerCoords.get(0)[0];
                int attackerCol = attackerCoords.get(0)[1];

                if (((((whitePawns >> (attackerRow * 8 + attackerCol)) & 1L) > 0) 
                        || (((blackPawns >> (attackerRow * 8 + attackerCol)) & 1L) > 0))) {
                    if (MoveParser.getFromRow(this.enpassantable) == attackerRow && MoveParser.getFromCol(this.enpassantable) == attackerCol) {
                        if (this.sideToMove == Side.WHITE) {
                            if (((pawnMoves >> ((attackerRow + 1) * 8 + attackerCol)) & 1L) > 0) {
                                enpassantGetRidOfCheck = 1L << ((attackerRow + 1) * 8 + attackerCol);
                            }
                        } else {
                            if (((pawnMoves >> ((attackerRow - 1) * 8 + attackerCol)) & 1L) > 0) {
                                enpassantGetRidOfCheck = 1L << ((attackerRow - 1) * 8 + attackerCol);
                            }
                        }
                    }
                }
            }
            
            pawnMoves &= this.inCheckLegalMoves;
            pawnMoves |= enpassantGetRidOfCheck;
            return pawnMoves;
        }
        
    }


    private boolean enPassantLeadsToCheck(int row, int col, int shift, int side) {

        //if at row col there is king (in case we are looking for pawn 
        // moves from the king position in other functions we don't 
        // want to place extra pawns), then return false

        if (side == 1) {
            if (((this.whiteKing >> (row * 8 + col)) & 1L) > 0) {
                return false;
            }
        } else {
            if (((this.blackKing >> (row * 8 + col)) & 1L) > 0) {
                return false;
            }
        }
        
        //remove pawns off the board
        if (side == 1) {
            this.whitePawns = this.whitePawns & ~(1L << (row * 8 + col));
            this.whitePieces = this.whitePieces & ~(1L << (row * 8 + col));
            this.blackPawns = this.blackPawns & ~(1L << (row * 8 + col + shift));
            this.blackPieces = this.blackPieces & ~(1L << (row * 8 + col + shift));
            this.whitePawns |= 1L << ((row + side) * 8 + col + shift);
            this.whitePieces |= 1L << ((row + side) * 8 + col + shift);
        } else {
            this.blackPawns = this.blackPawns & ~(1L << (row * 8 + col));
            this.blackPieces = this.blackPieces & ~(1L << (row * 8 + col));
            this.whitePawns = this.whitePawns & ~(1L << (row * 8 + col + shift));
            this.whitePieces = this.whitePieces & ~(1L << (row * 8 + col + shift));
            this.blackPawns |= 1L << ((row + side) * 8 + col + shift);
            this.blackPieces |= 1L << ((row + side) * 8 + col + shift);
        }

        //check if king is in check
        int check = this.kingInCheck();

        //put pawns back
        if (side == 1) {
            this.whitePawns = this.whitePawns & ~(1L << ((row + side) * 8 + col + shift));
            this.whitePieces = this.whitePieces & ~(1L << ((row + side) * 8 + col + shift));
            this.whitePawns |= 1L << (row * 8 + col);
            this.whitePieces |= 1L << (row * 8 + col);
            this.blackPawns |= 1L << (row * 8 + col + shift);
            this.blackPieces |= 1L << (row * 8 + col + shift);
        } else {
            this.blackPawns = this.blackPawns & ~(1L << ((row + side) * 8 + col + shift));
            this.blackPieces = this.blackPieces & ~(1L << ((row + side) * 8 + col + shift));
            this.blackPawns |= 1L << (row * 8 + col);
            this.blackPieces |= 1L << (row * 8 + col);
            this.whitePawns |= 1L << (row * 8 + col + shift);
            this.whitePieces |= 1L << (row * 8 + col + shift);
        }

        return check > 0;

    }


    private long getBishopMovesBitBoard(int row, int col, boolean inCheck, boolean pinned) {
        long bishopMoves = 0L;

        // down-left
        for (int i = row - 1, j = col - 1; i >= 0 && j >= 0; i--, j--) {
            if (((this.whitePieces >> (i * 8 + j)) & 1L) > 0) {
                if (this.sideToMove == Side.WHITE) {
                    break;
                } else {
                    bishopMoves |= 1L << (i * 8 + j);
                    break;
                }
            } else if (((this.blackPieces >> (i * 8 + j)) & 1L) > 0) {
                if (this.sideToMove == Side.BLACK) {
                    break;
                } else {
                    bishopMoves |= 1L << (i * 8 + j);
                    break;
                }
            } else {
                bishopMoves |= 1L << (i * 8 + j);
            }
        }

        // down-right
        for (int i = row - 1, j = col + 1; i >= 0 && j < 8; i--, j++) {
            if (((this.whitePieces >> (i * 8 + j)) & 1L) > 0) {
                if (this.sideToMove == Side.WHITE) {
                    break;
                } else {
                    bishopMoves |= 1L << (i * 8 + j);
                    break;
                }
            } else if (((this.blackPieces >> (i * 8 + j)) & 1L) > 0) {
                if (this.sideToMove == Side.BLACK) {
                    break;
                } else {
                    bishopMoves |= 1L << (i * 8 + j);
                    break;
                }
            } else {
                bishopMoves |= 1L << (i * 8 + j);
            }
        }

        // up-left
        for (int i = row + 1, j = col - 1; i < 8 && j >= 0; i++, j--) {
            if (((this.whitePieces >> (i * 8 + j)) & 1L) > 0) {
                if (this.sideToMove == Side.WHITE) {
                    break;
                } else {
                    bishopMoves |= 1L << (i * 8 + j);
                    break;
                }
            } else if (((this.blackPieces >> (i * 8 + j)) & 1L) > 0) {
                if (this.sideToMove == Side.BLACK) {
                    break;
                } else {
                    bishopMoves |= 1L << (i * 8 + j);
                    break;
                }
            } else {
                bishopMoves |= 1L << (i * 8 + j);
            }
        }

        // up-right
        for (int i = row + 1, j = col + 1; i < 8 && j < 8; i++, j++) {
            if (((this.whitePieces >> (i * 8 + j)) & 1L) > 0) {
                if (this.sideToMove == Side.WHITE) {
                    break;
                } else {
                    bishopMoves |= 1L << (i * 8 + j);
                    break;
                }
            } else if (((this.blackPieces >> (i * 8 + j)) & 1L) > 0) {
                if (this.sideToMove == Side.BLACK) {
                    break;
                } else {
                    bishopMoves |= 1L << (i * 8 + j);
                    break;
                }
            } else {
                bishopMoves |= 1L << (i * 8 + j);
            }
        }

        if (pinned) {
            for (long[] pinnedPiece : this.pinnedPieces) {
                if (pinnedPiece[0] == row && pinnedPiece[1] == col) {
                    bishopMoves &= pinnedPiece[2];
                    break;
                }
            }
        }

        if (!inCheck) {
            return bishopMoves;
        } else {
            bishopMoves &= this.inCheckLegalMoves;
            return bishopMoves;
        }

    }


    private long getRookMovesBitBoard(int row, int col, boolean inCheck, boolean pinned) {
        long rookMoves = 0L;

        // down
        for (int i = row - 1; i >= 0; i--) {
            if (((this.whitePieces >> (i * 8 + col)) & 1L) > 0) {
                if (this.sideToMove == Side.WHITE) {
                    break;
                } else {
                    rookMoves |= 1L << (i * 8 + col);
                    break;
                }
            } else if (((this.blackPieces >> (i * 8 + col)) & 1L) > 0) {
                if (this.sideToMove == Side.BLACK) {
                    break;
                } else {
                    rookMoves |= 1L << (i * 8 + col);
                    break;
                }
            } else {
                rookMoves |= 1L << (i * 8 + col);
            }
        }

        // up
        for (int i = row + 1; i < 8; i++) {
            if (((this.whitePieces >> (i * 8 + col)) & 1L) > 0) {
                if (this.sideToMove == Side.WHITE) {
                    break;
                } else {
                    rookMoves |= 1L << (i * 8 + col);
                    break;
                }
            } else if (((this.blackPieces >> (i * 8 + col)) & 1L) > 0) {
                if (this.sideToMove == Side.BLACK) {
                    break;
                } else {
                    rookMoves |= 1L << (i * 8 + col);
                    break;
                }
            } else {
                rookMoves |= 1L << (i * 8 + col);
            }
        }

        // left
        for (int i = col - 1; i >= 0; i--) {
            if (((this.whitePieces >> (row * 8 + i)) & 1L) > 0) {
                if (this.sideToMove == Side.WHITE) {
                    break;
                } else {
                    rookMoves |= 1L << (row * 8 + i);
                    break;
                }
            } else if (((this.blackPieces >> (row * 8 + i)) & 1L) > 0) {
                if (this.sideToMove == Side.BLACK) {
                    break;
                } else {
                    rookMoves |= 1L << (row * 8 + i);
                    break;
                }
            } else {
                rookMoves |= 1L << (row * 8 + i);
            }
        }

        // right
        for (int i = col + 1; i < 8; i++) {
            if (((this.whitePieces >> (row * 8 + i)) & 1L) > 0) {
                if (this.sideToMove == Side.WHITE) {
                    break;
                } else {
                    rookMoves |= 1L << (row * 8 + i);
                    break;
                }
            } else if (((this.blackPieces >> (row * 8 + i)) & 1L) > 0) {
                if (this.sideToMove == Side.BLACK) {
                    break;
                } else {
                    rookMoves |= 1L << (row * 8 + i);
                    break;
                }
            } else {
                rookMoves |= 1L << (row * 8 + i);
            }
        }

        if (pinned) {
            for (long[] pinnedPiece : this.pinnedPieces) {
                if (pinnedPiece[0] == row && pinnedPiece[1] == col) {
                    rookMoves &= pinnedPiece[2];
                    break;
                }
            }
        }

        if (!inCheck) {
            return rookMoves;
        } else {
            rookMoves &= this.inCheckLegalMoves;
            return rookMoves;
        }

    }


    private long getQueenMovesBitBoard(int row, int col, boolean inCheck, boolean pinned) {
        return this.getRookMovesBitBoard(row, col, inCheck, pinned) | this.getBishopMovesBitBoard(row, col, inCheck, pinned);
    }


    private long getKnightMovesBitBoard(int row, int col, boolean inCheck, boolean pinned) {
        long knightMoves = 0L;

        if (row - 2 >= 0 && col - 1 >= 0) {
            if (((this.whitePieces >> ((row - 2) * 8 + col - 1)) & 1L) > 0) {
                if (this.sideToMove == Side.BLACK) {
                    knightMoves |= 1L << ((row - 2) * 8 + col - 1);
                }
            } else if (((this.blackPieces >> ((row - 2) * 8 + col - 1)) & 1L) > 0) {
                if (this.sideToMove == Side.WHITE) {
                    knightMoves |= 1L << ((row - 2) * 8 + col - 1);
                }
            } else {
                knightMoves |= 1L << ((row - 2) * 8 + col - 1);
            }
        }

        if (row - 2 >= 0 && col + 1 < 8) {
            if (((this.whitePieces >> ((row - 2) * 8 + col + 1)) & 1L) > 0) {
                if (this.sideToMove == Side.BLACK) {
                    knightMoves |= 1L << ((row - 2) * 8 + col + 1);
                }
            } else if (((this.blackPieces >> ((row - 2) * 8 + col + 1)) & 1L) > 0) {
                if (this.sideToMove == Side.WHITE) {
                    knightMoves |= 1L << ((row - 2) * 8 + col + 1);
                }
            } else {
                knightMoves |= 1L << ((row - 2) * 8 + col + 1);
            }
        }

        if (row - 1 >= 0 && col - 2 >= 0) {
            if (((this.whitePieces >> ((row - 1) * 8 + col - 2)) & 1L) > 0) {
                if (this.sideToMove == Side.BLACK) {
                    knightMoves |= 1L << ((row - 1) * 8 + col - 2);
                }
            } else if (((this.blackPieces >> ((row - 1) * 8 + col - 2)) & 1L) > 0) {
                if (this.sideToMove == Side.WHITE) {
                    knightMoves |= 1L << ((row - 1) * 8 + col - 2);
                }
            } else {
                knightMoves |= 1L << ((row - 1) * 8 + col - 2);
            }
        }

        if (row - 1 >= 0 && col + 2 < 8) {
            if (((this.whitePieces >> ((row - 1) * 8 + col + 2)) & 1L) > 0) {
                if (this.sideToMove == Side.BLACK) {
                    knightMoves |= 1L << ((row - 1) * 8 + col + 2);
                }
            } else if (((this.blackPieces >> ((row - 1) * 8 + col + 2)) & 1L) > 0) {
                if (this.sideToMove == Side.WHITE) {
                    knightMoves |= 1L << ((row - 1) * 8 + col + 2);
                }
            } else {
                knightMoves |= 1L << ((row - 1) * 8 + col + 2);
            }
        }

        if (row + 1 < 8 && col - 2 >= 0) {
            if (((this.whitePieces >> ((row + 1) * 8 + col - 2)) & 1L) > 0) {
                if (this.sideToMove == Side.BLACK) {
                    knightMoves |= 1L << ((row + 1) * 8 + col - 2);
                }
            } else if (((this.blackPieces >> ((row + 1) * 8 + col - 2)) & 1L) > 0) {
                if (this.sideToMove == Side.WHITE) {
                    knightMoves |= 1L << ((row + 1) * 8 + col - 2);
                }
            } else {
                knightMoves |= 1L << ((row + 1) * 8 + col - 2);
            }
        }

        if (row + 1 < 8 && col + 2 < 8) {
            if (((this.whitePieces >> ((row + 1) * 8 + col + 2)) & 1L) > 0) {
                if (this.sideToMove == Side.BLACK) {
                    knightMoves |= 1L << ((row + 1) * 8 + col + 2);
                }
            } else if (((this.blackPieces >> ((row + 1) * 8 + col + 2)) & 1L) > 0) {
                if (this.sideToMove == Side.WHITE) {
                    knightMoves |= 1L << ((row + 1) * 8 + col + 2);
                }
            } else {
                knightMoves |= 1L << ((row + 1) * 8 + col + 2);
            }
        }

        if (row + 2 < 8 && col - 1 >= 0) {
            if (((this.whitePieces >> ((row + 2) * 8 + col - 1)) & 1L) > 0) {
                if (this.sideToMove == Side.BLACK) {
                    knightMoves |= 1L << ((row + 2) * 8 + col - 1);
                }
            } else if (((this.blackPieces >> ((row + 2) * 8 + col - 1)) & 1L) > 0) {
                if (this.sideToMove == Side.WHITE) {
                    knightMoves |= 1L << ((row + 2) * 8 + col - 1);
                }
            } else {
                knightMoves |= 1L << ((row + 2) * 8 + col - 1);
            }
        }

        if (row + 2 < 8 && col + 1 < 8) {
            if (((this.whitePieces >> ((row + 2) * 8 + col + 1)) & 1L) > 0) {
                if (this.sideToMove == Side.BLACK) {
                    knightMoves |= 1L << ((row + 2) * 8 + col + 1);
                }
            } else if (((this.blackPieces >> ((row + 2) * 8 + col + 1)) & 1L) > 0) {
                if (this.sideToMove == Side.WHITE) {
                    knightMoves |= 1L << ((row + 2) * 8 + col + 1);
                }
            } else {
                knightMoves |= 1L << ((row + 2) * 8 + col + 1);
            }
        }

        if (pinned) {
            for (long[] pinnedPiece : this.pinnedPieces) {
                if (pinnedPiece[0] == row && pinnedPiece[1] == col) {
                    knightMoves &= pinnedPiece[2];
                    break;
                }
            }
        }

        if (!inCheck) {
            return knightMoves;
        } else {
            knightMoves &= this.inCheckLegalMoves;
            return knightMoves;
        }
        
    }


    // Function to get the coordinates of the bits on a bitboard
    public static ArrayList<int[]> getCoordinatesFromBitboard(long bitboard) {
        ArrayList<int[]> bitboardCoordinates = new ArrayList<>();

        while (bitboard != 0L) {
            int index = Long.numberOfTrailingZeros(bitboard);
            bitboardCoordinates.add(new int[]{index / 8, index % 8});
            bitboard &= ~(1L << index);
        }
        
        return bitboardCoordinates;
    }


    private void fillBitboards(long[] board) {
        this.whitePawns = board[0];
        this.whiteKnights = board[1];
        this.whiteBishops = board[2];
        this.whiteRooks = board[3];
        this.whiteQueens = board[4];
        this.whiteKing = board[5];

        this.blackPawns = board[6];
        this.blackKnights = board[7];
        this.blackBishops = board[8];
        this.blackRooks = board[9];
        this.blackQueens = board[10];
        this.blackKing = board[11];

        this.whitePieces = this.whitePawns | this.whiteKnights | this.whiteBishops | this.whiteRooks | this.whiteQueens | this.whiteKing;
        this.blackPieces = this.blackPawns | this.blackKnights | this.blackBishops | this.blackRooks | this.blackQueens | this.blackKing;
        
    }
    

}
