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
            attackers = (this.getBishopMovesBitBoard(kingRow, kingCol) & this.blackBishops & this.blackQueens)
                | (this.getRookMovesBitBoard(kingRow, kingCol) & this.blackRooks & this.blackQueens)
                | (this.getKnightMovesBitBoard(kingRow, kingCol) & this.blackKnights)
                | (this.getPawnMovesBitBoard(kingRow, kingCol) & this.blackPawns);
        } else {
            attackers = (this.getBishopMovesBitBoard(kingRow, kingCol) & this.whiteBishops & this.whiteQueens)
                | (this.getRookMovesBitBoard(kingRow, kingCol) & this.whiteRooks & this.whiteQueens)
                | (this.getKnightMovesBitBoard(kingRow, kingCol) & this.whiteKnights)
                | (this.getPawnMovesBitBoard(kingRow, kingCol) & this.whitePawns);
        }

        return attackers;
    }


    /**
     * Finds all pinned pieces for the side to move and stores them in the pinnedPieces list.
     * Does this by finding moves of sliding opponent pieces and overlaps it with sliding moves from the kings position.
     * Any pieces of the side to move that are in the overlap are pinned.
     * The piece can then be removed and the moves of the sliding piece can be used to generate the legal moves 'ray' for the pinned piece.
     */
    private void getPinnedPieces() {

        ArrayList<int[]> kingCoords = getCoordinatesFromBitboard(this.sideToMove == Side.WHITE ? this.whiteKing : this.blackKing);

        int kingRow = kingCoords.get(0)[0];
        int kingCol = kingCoords.get(0)[1];

        long kingSlidingMoves = 0L;

        this.sideToMove = this.sideToMove == Side.WHITE ? Side.BLACK : Side.WHITE;

        kingSlidingMoves = this.getQueenMovesBitBoard(kingRow, kingCol);

        long opponentSlidingMoves = 0L;

        ArrayList<int[]> opponentRookCoords = getCoordinatesFromBitboard(this.sideToMove == Side.WHITE ? this.whiteRooks : this.blackRooks);
        for (int[] coords : opponentRookCoords) {
            opponentSlidingMoves |= this.getRookMovesBitBoard(coords[0], coords[1]);
        }
        ArrayList<int[]> opponentBishopCoords = getCoordinatesFromBitboard(this.sideToMove == Side.WHITE ? this.whiteBishops : this.blackBishops);
        for (int[] coords : opponentBishopCoords) {
            opponentSlidingMoves |= this.getBishopMovesBitBoard(coords[0], coords[1]);
        }
        ArrayList<int[]> opponentQueenCoords = getCoordinatesFromBitboard(this.sideToMove == Side.WHITE ? this.whiteQueens : this.blackQueens);
        for (int[] coords : opponentQueenCoords) {
            opponentSlidingMoves |= this.getQueenMovesBitBoard(coords[0], coords[1]);
        }

        long pinnedPieces = kingSlidingMoves & opponentSlidingMoves;

        this.sideToMove = this.sideToMove == Side.WHITE ? Side.BLACK : Side.WHITE;

        if (pinnedPieces == 0L) {
            return;
        }

        ArrayList<int[]> pinnedPieceCoords = getCoordinatesFromBitboard(pinnedPieces);

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
            legalMoves = this.getRookMovesBitBoard(kingRow, kingCol);
            this.sideToMove = this.sideToMove == Side.WHITE ? Side.BLACK : Side.WHITE;
            legalMoves &= this.getRookMovesBitBoard(attackerRow, attackerCol) | attacker;
            this.sideToMove = this.sideToMove == Side.WHITE ? Side.BLACK : Side.WHITE;
        } else if (this.board[attackerRow][attackerCol].getType() == PieceType.BISHOP) {
            legalMoves = this.getBishopMovesBitBoard(kingRow, kingCol);
            this.sideToMove = this.sideToMove == Side.WHITE ? Side.BLACK : Side.WHITE;
            legalMoves &= this.getBishopMovesBitBoard(attackerRow, attackerCol) | attacker;
            this.sideToMove = this.sideToMove == Side.WHITE ? Side.BLACK : Side.WHITE;
        } else if (this.board[attackerRow][attackerCol].getType() == PieceType.QUEEN) {
            legalMoves = this.getQueenMovesBitBoard(kingRow, kingCol);
            this.sideToMove = this.sideToMove == Side.WHITE ? Side.BLACK : Side.WHITE;
            legalMoves &= this.getQueenMovesBitBoard(attackerRow, attackerCol) | attacker;
            this.sideToMove = this.sideToMove == Side.WHITE ? Side.BLACK : Side.WHITE;
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
                        if (!this.kingInCheckAfterMove(row + 1, col))
                            kingmoves |= 1L << ((row + 1) * 8 + col);
                    }
                }
            }
            if (row - 1 >= 0) {
                if (((this.whitePieces >> ((row - 1) * 8 + col)) & 1L) == 0L) {
                    if (((coveredByOppKing >> ((row - 1) * 8 + col)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row - 1, col))
                            kingmoves |= 1L << ((row - 1) * 8 + col);
                    }
                }
            }
            if (col + 1 < 8) {
                if (((this.whitePieces >> (row * 8 + col + 1)) & 1L) == 0L) {
                    if (((coveredByOppKing >> (row * 8 + col + 1)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row, col + 1))
                            kingmoves |= 1L << (row * 8 + col + 1);
                    }
                }
            }
            if (col - 1 >= 0) {
                if (((this.whitePieces >> (row * 8 + col - 1)) & 1L) == 0L) {
                    if (((coveredByOppKing >> (row * 8 + col - 1)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row, col - 1))
                            kingmoves |= 1L << (row * 8 + col - 1);
                    }
                }
            }
            if (row + 1 < 8 && col + 1 < 8) {
                if (((this.whitePieces >> ((row + 1) * 8 + col + 1)) & 1L) == 0L) {
                    if (((coveredByOppKing >> ((row + 1) * 8 + col + 1)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row + 1, col + 1))
                            kingmoves |= 1L << ((row + 1) * 8 + col + 1);
                    }
                }
            }
            if (row + 1 < 8 && col - 1 >= 0) {
                if (((this.whitePieces >> ((row + 1) * 8 + col - 1)) & 1L) == 0L) {
                    if (((coveredByOppKing >> ((row + 1) * 8 + col - 1)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row + 1, col - 1))
                            kingmoves |= 1L << ((row + 1) * 8 + col - 1);
                    }
                }
            }
            if (row - 1 >= 0 && col + 1 < 8) {
                if (((this.whitePieces >> ((row - 1) * 8 + col + 1)) & 1L) == 0L) {
                    if (((coveredByOppKing >> ((row - 1) * 8 + col + 1)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row - 1, col + 1))
                            kingmoves |= 1L << ((row - 1) * 8 + col + 1);
                    }
                }
            }
            if (row - 1 >= 0 && col - 1 >= 0) {
                if (((this.whitePieces >> ((row - 1) * 8 + col - 1)) & 1L) == 0L) {
                    if (((coveredByOppKing >> ((row - 1) * 8 + col - 1)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row - 1, col - 1))
                            kingmoves |= 1L << ((row - 1) * 8 + col - 1);
                    }
                }
            }
        } else {
            if (row + 1 < 8) {
                if (((this.blackPieces >> ((row + 1) * 8 + col)) & 1L) == 0L) {
                    if (((coveredByOppKing >> ((row + 1) * 8 + col)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row + 1, col))
                            kingmoves |= 1L << ((row + 1) * 8 + col);
                    }
                }
            }
            if (row - 1 >= 0) {
                if (((this.blackPieces >> ((row - 1) * 8 + col)) & 1L) == 0L) {
                    if (((coveredByOppKing >> ((row - 1) * 8 + col)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row - 1, col))
                            kingmoves |= 1L << ((row - 1) * 8 + col);
                    }
                }
            }
            if (col + 1 < 8) {
                if (((this.blackPieces >> (row * 8 + col + 1)) & 1L) == 0L) {
                    if (((coveredByOppKing >> (row * 8 + col + 1)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row, col + 1))
                            kingmoves |= 1L << (row * 8 + col + 1);
                    }
                }
            }
            if (col - 1 >= 0) {
                if (((this.blackPieces >> (row * 8 + col - 1)) & 1L) == 0L) {
                    if (((coveredByOppKing >> (row * 8 + col - 1)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row, col - 1))
                            kingmoves |= 1L << (row * 8 + col - 1);
                    }
                }
            }
            if (row + 1 < 8 && col + 1 < 8) {
                if (((this.blackPieces >> ((row + 1) * 8 + col + 1)) & 1L) == 0L) {
                    if (((coveredByOppKing >> ((row + 1) * 8 + col + 1)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row + 1, col + 1))
                            kingmoves |= 1L << ((row + 1) * 8 + col + 1);
                    }
                }
            }
            if (row + 1 < 8 && col - 1 >= 0) {
                if (((this.blackPieces >> ((row + 1) * 8 + col - 1)) & 1L) == 0L) {
                    if (((coveredByOppKing >> ((row + 1) * 8 + col - 1)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row + 1, col - 1))
                            kingmoves |= 1L << ((row + 1) * 8 + col - 1);
                    }
                }
            }
            if (row - 1 >= 0 && col + 1 < 8) {
                if (((this.blackPieces >> ((row - 1) * 8 + col + 1)) & 1L) == 0L) {
                    if (((coveredByOppKing >> ((row - 1) * 8 + col + 1)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row - 1, col + 1))
                            kingmoves |= 1L << ((row - 1) * 8 + col + 1);
                    }
                }
            }
            if (row - 1 >= 0 && col - 1 >= 0) {
                if (((this.blackPieces >> ((row - 1) * 8 + col - 1)) & 1L) == 0L) {
                    if (((coveredByOppKing >> ((row - 1) * 8 + col - 1)) & 1L) == 0L) {
                        if (!this.kingInCheckAfterMove(row - 1, col - 1))
                            kingmoves |= 1L << ((row - 1) * 8 + col - 1);
                    }
                }
            }
        }

        // Check for castling moves

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


    private long getPawnMovesBitBoard(int row, int col) {
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
        if (this.sideToMove == Side.WHITE) {
            if (row == 4) {
                if (col - 1 >= 0) {
                    if (((this.blackPawns >> (row * 8 + col - 1)) & 1L) > 0) {
                        if (MoveParser.getFromCol(this.enpassantable) == (col - 1) && MoveParser.getFromRow(this.enpassantable) == row) {
                            pawnMoves |= 1L << ((row + 1) * 8 + col - 1);
                        }
                    }
                }
                if (col + 1 < 8) {
                    if (((this.blackPawns >> (row * 8 + col + 1)) & 1L) > 0) {
                        if (MoveParser.getFromCol(this.enpassantable) == (col + 1) && MoveParser.getFromRow(this.enpassantable) == row) {
                            pawnMoves |= 1L << ((row + 1) * 8 + col + 1);
                        }
                    }
                }
            }
        } else {
            if (row == 3) {
                if (col - 1 >= 0) {
                    if (((this.whitePawns >> (row * 8 + col - 1)) & 1L) > 0) {
                        if (MoveParser.getFromCol(this.enpassantable) == (col - 1) && MoveParser.getFromRow(this.enpassantable) == row) {
                            pawnMoves |= 1L << ((row - 1) * 8 + col - 1);
                        }
                    }
                }
                if (col + 1 < 8) {
                    if (((this.whitePawns >> (row * 8 + col + 1)) & 1L) > 0) {
                        if (MoveParser.getFromCol(this.enpassantable) == (col + 1) && MoveParser.getFromRow(this.enpassantable) == row) {
                            pawnMoves |= 1L << ((row - 1) * 8 + col + 1);
                        }
                    }
                }
            }
        }

        return pawnMoves;

    }


    private long getBishopMovesBitBoard(int row, int col) {
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

        return bishopMoves;
    }


    private long getRookMovesBitBoard(int row, int col) {
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

        return rookMoves;

    }


    private long getQueenMovesBitBoard(int row, int col) {
        return this.getRookMovesBitBoard(row, col) | this.getBishopMovesBitBoard(row, col);
    }


    private long getKnightMovesBitBoard(int row, int col) {
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

        return knightMoves;
        
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


    private void resetBitBoards() {
        this.whitePieces = 0L;
        this.blackPieces = 0L;

        this.whitePawns = 0L;
        this.blackPawns = 0L;
        this.whiteKnights = 0L;
        this.blackKnights = 0L;
        this.whiteBishops = 0L;
        this.blackBishops = 0L;
        this.whiteRooks = 0L;
        this.blackRooks = 0L;
        this.whiteQueens = 0L;
        this.blackQueens = 0L;
        this.whiteKing = 0L;
        this.blackKing = 0L;
    }
    
}
