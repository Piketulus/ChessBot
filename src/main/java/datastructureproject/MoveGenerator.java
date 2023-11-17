package datastructureproject;

import java.util.List;
import chess.model.Side;


/**
 * MoveGenerator class is used for generating valid moves for a given board.
 * It uses bitboards for storing the board state to make the move generation easier to code in some cases and faster to run.
 */
public class MoveGenerator {


    private Piece[][] board;
    private String enpassantable;
    private Side sideToMove;

    private List<long[]> pinnedPieces; // [0] = rowPinned, [1] = colPinned, [2] = legal move 'ray'

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
        this.fillBitboards();
    }


    /**
     * Checks if the king of the side to move is in check by seeing if moves from the king by different pieces intersect with opponent pieces.
     * @return the number of checking pieces (if more than 0 then in check), this is useful for detecting double check
     */
    private int kingInCheck() {

        int[] kingCoords = getCoordinatesFromBitboard(this.sideToMove == Side.WHITE ? this.whiteKing : this.blackKing);

        long attackers = 0L;

        if (this.sideToMove == Side.WHITE) {
            attackers = (this.getBishopMovesBitBoard(kingCoords[0], kingCoords[1]) & this.blackBishops & this.blackQueens)
                | (this.getRookMovesBitBoard(kingCoords[0], kingCoords[1]) & this.blackRooks & this.blackQueens)
                | (this.getKnightMovesBitBoard(kingCoords[0], kingCoords[1]) & this.blackKnights)
                | (this.getPawnMovesBitBoard(kingCoords[0], kingCoords[1]) & this.blackPawns);
        } else {
            attackers = (this.getBishopMovesBitBoard(kingCoords[0], kingCoords[1]) & this.whiteBishops & this.whiteQueens)
                | (this.getRookMovesBitBoard(kingCoords[0], kingCoords[1]) & this.whiteRooks & this.whiteQueens)
                | (this.getKnightMovesBitBoard(kingCoords[0], kingCoords[1]) & this.whiteKnights)
                | (this.getPawnMovesBitBoard(kingCoords[0], kingCoords[1]) & this.whitePawns);
        }

        return Long.bitCount(attackers);
    }


    /**
     * Finds all pinned pieces for the side to move and stores them in the pinnedPieces list.
     * Does this by finding moves of sliding opponent pieces and overlaps it with sliding moves from the kings position.
     * Any pieces of the side to move that are in the overlap are pinned.
     * The piece can then be removed and the moves of the sliding piece can be used to generate the legal moves 'ray' for the pinned piece.
     */
    private void getPinnedPieces() {
        // TODO
    }


    private long getPawnMovesBitBoard(int row, int col) {
        long pawnMoves = 0L;

        // go straight
        if (this.sideToMove == Side.WHITE) {
            if (row + 1 < 8) {
                if (this.whitePieces >> ((row + 1) * 8 + col) == 0L && this.blackPieces >> ((row + 1) * 8 + col) == 0L) {
                    pawnMoves |= 1L << ((row + 1) * 8 + col);
                }
            }
            if (row == 1 && this.whitePieces >> ((row + 1) * 8 + col) == 0L && this.blackPieces >> ((row + 1) * 8 + col) == 0L) {
                if (this.whitePieces >> ((row + 2) * 8 + col) == 0L && this.blackPieces >> ((row + 2) * 8 + col) == 0L) {
                    pawnMoves |= 1L << ((row + 2) * 8 + col);
                }
            }
        } else {
            if (row - 1 >= 0) {
                if (this.whitePieces >> ((row - 1) * 8 + col) == 0L && this.blackPieces >> ((row - 1) * 8 + col) == 0L) {
                    pawnMoves |= 1L << ((row - 1) * 8 + col);
                }
            }
            if (row == 6 && this.whitePieces >> ((row - 1) * 8 + col) == 0L && this.blackPieces >> ((row - 1) * 8 + col) == 0L) {
                if (this.whitePieces >> ((row - 2) * 8 + col) == 0L && this.blackPieces >> ((row - 2) * 8 + col) == 0L) {
                    pawnMoves |= 1L << ((row - 2) * 8 + col);
                }
            }
        }

        // capture
        if (this.sideToMove == Side.WHITE) {
            if (row + 1 < 8 && col - 1 >= 0) {
                if (this.blackPieces >> ((row + 1) * 8 + col - 1) == 1L) {
                    pawnMoves |= 1L << ((row + 1) * 8 + col - 1);
                }
            }
            if (row + 1 < 8 && col + 1 < 8) {
                if (this.blackPieces >> ((row + 1) * 8 + col + 1) == 1L) {
                    pawnMoves |= 1L << ((row + 1) * 8 + col + 1);
                }
            }
        } else {
            if (row - 1 >= 0 && col - 1 >= 0) {
                if (this.whitePieces >> ((row - 1) * 8 + col - 1) == 1L) {
                    pawnMoves |= 1L << ((row - 1) * 8 + col - 1);
                }
            }
            if (row - 1 >= 0 && col + 1 < 8) {
                if (this.whitePieces >> ((row - 1) * 8 + col + 1) == 1L) {
                    pawnMoves |= 1L << ((row - 1) * 8 + col + 1);
                }
            }
        }

        // en passant
        if (this.sideToMove == Side.WHITE) {
            if (row == 4) {
                if (col - 1 >= 0) {
                    if (this.blackPawns >> (row * 8 + col - 1) == 1L) {
                        if (MoveParser.getFromCol(this.enpassantable) == (col - 1) && MoveParser.getFromRow(this.enpassantable) == row) {
                            pawnMoves |= 1L << ((row + 1) * 8 + col - 1);
                        }
                    }
                }
                if (col + 1 < 8) {
                    if (this.blackPawns >> (row * 8 + col + 1) == 1L) {
                        if (MoveParser.getFromCol(this.enpassantable) == (col + 1) && MoveParser.getFromRow(this.enpassantable) == row) {
                            pawnMoves |= 1L << ((row + 1) * 8 + col + 1);
                        }
                    }
                }
            }
        } else {
            if (row == 3) {
                if (col - 1 >= 0) {
                    if (this.whitePawns >> (row * 8 + col - 1) == 1L) {
                        if (MoveParser.getFromCol(this.enpassantable) == (col - 1) && MoveParser.getFromRow(this.enpassantable) == row) {
                            pawnMoves |= 1L << ((row - 1) * 8 + col - 1);
                        }
                    }
                }
                if (col + 1 < 8) {
                    if (this.whitePawns >> (row * 8 + col + 1) == 1L) {
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
            if (this.whitePieces >> (i * 8 + j) == 1L) {
                if (this.sideToMove == Side.WHITE) {
                    break;
                } else {
                    bishopMoves |= 1L << (i * 8 + j);
                    break;
                }
            } else if (this.blackPieces >> (i * 8 + j) == 1L) {
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
            if (this.whitePieces >> (i * 8 + j) == 1L) {
                if (this.sideToMove == Side.WHITE) {
                    break;
                } else {
                    bishopMoves |= 1L << (i * 8 + j);
                    break;
                }
            } else if (this.blackPieces >> (i * 8 + j) == 1L) {
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
            if (this.whitePieces >> (i * 8 + j) == 1L) {
                if (this.sideToMove == Side.WHITE) {
                    break;
                } else {
                    bishopMoves |= 1L << (i * 8 + j);
                    break;
                }
            } else if (this.blackPieces >> (i * 8 + j) == 1L) {
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
            if (this.whitePieces >> (i * 8 + j) == 1L) {
                if (this.sideToMove == Side.WHITE) {
                    break;
                } else {
                    bishopMoves |= 1L << (i * 8 + j);
                    break;
                }
            } else if (this.blackPieces >> (i * 8 + j) == 1L) {
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
            if (this.whitePieces >> (i * 8 + col) == 1L) {
                if (this.sideToMove == Side.WHITE) {
                    break;
                } else {
                    rookMoves |= 1L << (i * 8 + col);
                    break;
                }
            } else if (this.blackPieces >> (i * 8 + col) == 1L) {
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
            if (this.whitePieces >> (i * 8 + col) == 1L) {
                if (this.sideToMove == Side.WHITE) {
                    break;
                } else {
                    rookMoves |= 1L << (i * 8 + col);
                    break;
                }
            } else if (this.blackPieces >> (i * 8 + col) == 1L) {
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
            if (this.whitePieces >> (row * 8 + i) == 1L) {
                if (this.sideToMove == Side.WHITE) {
                    break;
                } else {
                    rookMoves |= 1L << (row * 8 + i);
                    break;
                }
            } else if (this.blackPieces >> (row * 8 + i) == 1L) {
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
            if (this.whitePieces >> (row * 8 + i) == 1L) {
                if (this.sideToMove == Side.WHITE) {
                    break;
                } else {
                    rookMoves |= 1L << (row * 8 + i);
                    break;
                }
            } else if (this.blackPieces >> (row * 8 + i) == 1L) {
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
            if (this.whitePieces >> ((row - 2) * 8 + col - 1) == 1L) {
                if (this.sideToMove == Side.BLACK) {
                    knightMoves |= 1L << ((row - 2) * 8 + col - 1);
                }
            } else if (this.blackPieces >> ((row - 2) * 8 + col - 1) == 1L) {
                if (this.sideToMove == Side.WHITE) {
                    knightMoves |= 1L << ((row - 2) * 8 + col - 1);
                }
            } else {
                knightMoves |= 1L << ((row - 2) * 8 + col - 1);
            }
        }

        if (row - 2 >= 0 && col + 1 < 8) {
            if (this.whitePieces >> ((row - 2) * 8 + col + 1) == 1L) {
                if (this.sideToMove == Side.BLACK) {
                    knightMoves |= 1L << ((row - 2) * 8 + col + 1);
                }
            } else if (this.blackPieces >> ((row - 2) * 8 + col + 1) == 1L) {
                if (this.sideToMove == Side.WHITE) {
                    knightMoves |= 1L << ((row - 2) * 8 + col + 1);
                }
            } else {
                knightMoves |= 1L << ((row - 2) * 8 + col + 1);
            }
        }

        if (row - 1 >= 0 && col - 2 >= 0) {
            if (this.whitePieces >> ((row - 1) * 8 + col - 2) == 1L) {
                if (this.sideToMove == Side.BLACK) {
                    knightMoves |= 1L << ((row - 1) * 8 + col - 2);
                }
            } else if (this.blackPieces >> ((row - 1) * 8 + col - 2) == 1L) {
                if (this.sideToMove == Side.WHITE) {
                    knightMoves |= 1L << ((row - 1) * 8 + col - 2);
                }
            } else {
                knightMoves |= 1L << ((row - 1) * 8 + col - 2);
            }
        }

        if (row - 1 >= 0 && col + 2 < 8) {
            if (this.whitePieces >> ((row - 1) * 8 + col + 2) == 1L) {
                if (this.sideToMove == Side.BLACK) {
                    knightMoves |= 1L << ((row - 1) * 8 + col + 2);
                }
            } else if (this.blackPieces >> ((row - 1) * 8 + col + 2) == 1L) {
                if (this.sideToMove == Side.WHITE) {
                    knightMoves |= 1L << ((row - 1) * 8 + col + 2);
                }
            } else {
                knightMoves |= 1L << ((row - 1) * 8 + col + 2);
            }
        }

        if (row + 1 < 8 && col - 2 >= 0) {
            if (this.whitePieces >> ((row + 1) * 8 + col - 2) == 1L) {
                if (this.sideToMove == Side.BLACK) {
                    knightMoves |= 1L << ((row + 1) * 8 + col - 2);
                }
            } else if (this.blackPieces >> ((row + 1) * 8 + col - 2) == 1L) {
                if (this.sideToMove == Side.WHITE) {
                    knightMoves |= 1L << ((row + 1) * 8 + col - 2);
                }
            } else {
                knightMoves |= 1L << ((row + 1) * 8 + col - 2);
            }
        }

        if (row + 1 < 8 && col + 2 < 8) {
            if (this.whitePieces >> ((row + 1) * 8 + col + 2) == 1L) {
                if (this.sideToMove == Side.BLACK) {
                    knightMoves |= 1L << ((row + 1) * 8 + col + 2);
                }
            } else if (this.blackPieces >> ((row + 1) * 8 + col + 2) == 1L) {
                if (this.sideToMove == Side.WHITE) {
                    knightMoves |= 1L << ((row + 1) * 8 + col + 2);
                }
            } else {
                knightMoves |= 1L << ((row + 1) * 8 + col + 2);
            }
        }

        if (row + 2 < 8 && col - 1 >= 0) {
            if (this.whitePieces >> ((row + 2) * 8 + col - 1) == 1L) {
                if (this.sideToMove == Side.BLACK) {
                    knightMoves |= 1L << ((row + 2) * 8 + col - 1);
                }
            } else if (this.blackPieces >> ((row + 2) * 8 + col - 1) == 1L) {
                if (this.sideToMove == Side.WHITE) {
                    knightMoves |= 1L << ((row + 2) * 8 + col - 1);
                }
            } else {
                knightMoves |= 1L << ((row + 2) * 8 + col - 1);
            }
        }

        if (row + 2 < 8 && col + 1 < 8) {
            if (this.whitePieces >> ((row + 2) * 8 + col + 1) == 1L) {
                if (this.sideToMove == Side.BLACK) {
                    knightMoves |= 1L << ((row + 2) * 8 + col + 1);
                }
            } else if (this.blackPieces >> ((row + 2) * 8 + col + 1) == 1L) {
                if (this.sideToMove == Side.WHITE) {
                    knightMoves |= 1L << ((row + 2) * 8 + col + 1);
                }
            } else {
                knightMoves |= 1L << ((row + 2) * 8 + col + 1);
            }
        }

        return knightMoves;
        
    }



    private static int[] getCoordinatesFromBitboard(long bitboard) {    // Function to get coordinates from a bitboard for one piece
        int index = Long.numberOfTrailingZeros(bitboard); // Find the position of the least significant set bit
        int row = index / 8; // Convert index to row
        int col = index % 8; // Convert index to column

        return new int[]{row, col};
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
