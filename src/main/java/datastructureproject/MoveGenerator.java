package datastructureproject;

import java.util.ArrayList;

import chess.model.Side;


/**
 * MoveGenerator class is used for generating valid moves for a given board.
 * It uses bitboards for storing the board state to make the move generation easier to code in some cases and faster to run.
 */
public class MoveGenerator {


    private Piece[][] board;
    private String enpassantable;
    private Side sideToMove;

    private ArrayList<long[]> pinnedPieces; // [0] = rowPinned, [1] = colPinned, [2] = legal move 'ray'

    private int kingInCheck; // 0 = not in check, 1 = in check, 2 = double check
    private long attackers; // bitboard of checking pieces
    private long inCheckLegalMoves; // bitboard of legal moves when in check

    public long whitePieces = 0L;
    public long blackPieces = 0L;

    public long whitePawns = 0L;
    public long blackPawns = 0L;
    public long whiteKnights = 0L;
    public long blackKnights = 0L;
    public long whiteBishops = 0L;
    public long blackBishops = 0L;
    public long whiteRooks = 0L;
    public long blackRooks = 0L;
    public long whiteQueens = 0L;
    public long blackQueens = 0L;
    public long whiteKing = 0L;
    public long blackKing = 0L;


    public MoveGenerator(Piece[][] board, String enpassantable, Side sideToMove) {
        this.board = board;
        this.enpassantable = enpassantable;
        this.sideToMove = sideToMove;
        this.pinnedPieces = new ArrayList<>();

        this.fillBitboards();

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
            //loop through the board and find all legal moves for each piece and add them to the legalMoves list
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if (this.board[row][col] != null && this.board[row][col].getSide() == this.sideToMove) {
                        boolean pinned = this.isPinned(row, col);
                        if (this.board[row][col].getType() == PieceType.PAWN) {
                            boolean promote = false;
                            if (row == 6 && this.sideToMove == Side.WHITE) {
                                promote = true;
                            } else if (row == 1 && this.sideToMove == Side.BLACK) {
                                promote = true;
                            }
                            long pawnMoves = this.getPawnMovesBitBoard(row, col, inCheck, pinned);
                            ArrayList<int[]> pawnMovesCoords = getCoordinatesFromBitboard(pawnMoves);
                            ArrayList<String> pawnMovesUCI = MoveParser.coordsToMoves(row, col, pawnMovesCoords, promote);
                            legalMoves.addAll(pawnMovesUCI);
                        } else if (this.board[row][col].getType() == PieceType.KNIGHT) {
                            long knightMoves = this.getKnightMovesBitBoard(row, col, inCheck, pinned);
                            ArrayList<int[]> knightMovesCoords = getCoordinatesFromBitboard(knightMoves);
                            ArrayList<String> knightMovesUCI = MoveParser.coordsToMoves(row, col, knightMovesCoords, false);
                            legalMoves.addAll(knightMovesUCI);
                        } else if (this.board[row][col].getType() == PieceType.BISHOP) {
                            long bishopMoves = this.getBishopMovesBitBoard(row, col, inCheck, pinned);
                            ArrayList<int[]> bishopMovesCoords = getCoordinatesFromBitboard(bishopMoves);
                            ArrayList<String> bishopMovesUCI = MoveParser.coordsToMoves(row, col, bishopMovesCoords, false);
                            legalMoves.addAll(bishopMovesUCI);
                        } else if (this.board[row][col].getType() == PieceType.ROOK) {
                            long rookMoves = this.getRookMovesBitBoard(row, col, inCheck, pinned);
                            ArrayList<int[]> rookMovesCoords = getCoordinatesFromBitboard(rookMoves);
                            ArrayList<String> rookMovesUCI = MoveParser.coordsToMoves(row, col, rookMovesCoords, false);
                            legalMoves.addAll(rookMovesUCI);
                        } else if (this.board[row][col].getType() == PieceType.QUEEN) {
                            long queenMoves = this.getQueenMovesBitBoard(row, col, inCheck, pinned);
                            ArrayList<int[]> queenMovesCoords = getCoordinatesFromBitboard(queenMoves);
                            ArrayList<String> queenMovesUCI = MoveParser.coordsToMoves(row, col, queenMovesCoords, false);
                            legalMoves.addAll(queenMovesUCI);
                        } else if (this.board[row][col].getType() == PieceType.KING) {
                            long kingMoves = this.getKingMovesBitBoard(row, col);
                            ArrayList<int[]> kingMovesCoords = getCoordinatesFromBitboard(kingMoves);
                            ArrayList<String> kingMovesUCI = MoveParser.coordsToMoves(row, col, kingMovesCoords, false);
                            legalMoves.addAll(kingMovesUCI);
                        }
                    }
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

        if (this.board[attackerRow][attackerCol].getType() == PieceType.ROOK) {
            legalMoves = this.getRookMovesBitBoard(kingRow, kingCol, false, false);
            legalMoves &= this.getRookMovesBitBoard(attackerRow, attackerCol, false, false) | attacker;
        } else if (this.board[attackerRow][attackerCol].getType() == PieceType.BISHOP) {
            legalMoves = this.getBishopMovesBitBoard(kingRow, kingCol, false, false);
            legalMoves &= this.getBishopMovesBitBoard(attackerRow, attackerCol, false, false) | attacker;
        } else if (this.board[attackerRow][attackerCol].getType() == PieceType.QUEEN) {
            //if queen is not on same file or rank, the bishop moves, otherwise the rook moves
            if (attackerRow != kingRow && attackerCol != kingCol) {
                legalMoves = this.getBishopMovesBitBoard(kingRow, kingCol, false, false);
                legalMoves &= this.getBishopMovesBitBoard(attackerRow, attackerCol, false, false) | attacker;
            } else {
                legalMoves = this.getRookMovesBitBoard(kingRow, kingCol, false, false);
                legalMoves &= this.getRookMovesBitBoard(attackerRow, attackerCol, false, false) | attacker;
            }
        } else if (this.board[attackerRow][attackerCol].getType() == PieceType.KNIGHT) {
            legalMoves = attacker;
        } else if (this.board[attackerRow][attackerCol].getType() == PieceType.PAWN) {
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
            if (row == 0 && col == 4 && !this.board[0][4].getHasMoved()) {
                if (this.board[0][7] != null) {
                    if (this.board[0][7].getType() == PieceType.ROOK && !this.board[0][7].getHasMoved()) {
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
                }
                if (this.board[0][0] != null) {
                    if (this.board[0][0].getType() == PieceType.ROOK && !this.board[0][0].getHasMoved()) {
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
                
            }
        } else {
            if (row == 7 && col == 4 && !this.board[7][4].getHasMoved()) {
                if (this.board[7][7] != null) {
                    if (this.board[7][7].getType() == PieceType.ROOK && !this.board[7][7].getHasMoved()) {
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
                }
                if (this.board[7][0] != null) {
                    if (this.board[7][0].getType() == PieceType.ROOK && !this.board[7][0].getHasMoved()) {
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
        if (!this.enpassantable.equals("")) {

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

            ArrayList<int[]> attackerCoords = getCoordinatesFromBitboard(this.attackers);
            int attackerRow = attackerCoords.get(0)[0];
            int attackerCol = attackerCoords.get(0)[1];

            if (this.board[attackerRow][attackerCol].getType() == PieceType.PAWN) {
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
            pawnMoves &= this.inCheckLegalMoves;
            pawnMoves |= enpassantGetRidOfCheck;
            return pawnMoves;
        }
        
    }


    private boolean enPassantLeadsToCheck(int row, int col, int shift, int side) {
        
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
    private static ArrayList<int[]> getCoordinatesFromBitboard(long bitboard) {
        ArrayList<int[]> bitboardCoordinates = new ArrayList<>();

        for (int i = 0; i < 64; i++) {
            if (((bitboard >> i) & 1L) > 0) {
                bitboardCoordinates.add(new int[]{i / 8, i % 8});
            }
        }

        return bitboardCoordinates;
    }


    private void fillBitboards() {
        //fill the bitboards by looping through the board
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = this.board[row][col];
                if (piece != null) {
                    if (piece.getSide() == Side.WHITE) {
                        this.whitePieces |= 1L << (row * 8 + col);
                    } else {
                        this.blackPieces |= 1L << (row * 8 + col);
                    }
                    if (piece.getType() == PieceType.PAWN) {
                        if (piece.getSide() == Side.WHITE) {
                            this.whitePawns |= 1L << (row * 8 + col);
                        } else {
                            this.blackPawns |= 1L << (row * 8 + col);
                        }
                    } else if (piece.getType() == PieceType.KNIGHT) {
                        if (piece.getSide() == Side.WHITE) {
                            this.whiteKnights |= 1L << (row * 8 + col);
                        } else {
                            this.blackKnights |= 1L << (row * 8 + col);
                        }
                    } else if (piece.getType() == PieceType.BISHOP) {
                        if (piece.getSide() == Side.WHITE) {
                            this.whiteBishops |= 1L << (row * 8 + col);
                        } else {
                            this.blackBishops |= 1L << (row * 8 + col);
                        }
                    } else if (piece.getType() == PieceType.ROOK) {
                        if (piece.getSide() == Side.WHITE) {
                            this.whiteRooks |= 1L << (row * 8 + col);
                        } else {
                            this.blackRooks |= 1L << (row * 8 + col);
                        }
                    } else if (piece.getType() == PieceType.QUEEN) {
                        if (piece.getSide() == Side.WHITE) {
                            this.whiteQueens |= 1L << (row * 8 + col);
                        } else {
                            this.blackQueens |= 1L << (row * 8 + col);
                        }
                    } else if (piece.getType() == PieceType.KING) {
                        if (piece.getSide() == Side.WHITE) {
                            this.whiteKing |= 1L << (row * 8 + col);
                        } else {
                            this.blackKing |= 1L << (row * 8 + col);
                        }
                    }
                }
            }
        }

    }

}
