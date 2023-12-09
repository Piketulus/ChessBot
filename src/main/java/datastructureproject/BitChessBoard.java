package datastructureproject;


/**
 * A class for representing a chess board using bitboards.
 * Keeps track of the position for the bot in order to search for next moves.
 */
public class BitChessBoard {

    public String enpassantable;
    public String castlingRights;

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


    public BitChessBoard() {
        this.enpassantable = "-";
        this.castlingRights = "KQkq";
        this.initializeBoard();
    }


    // copy constructor
    public BitChessBoard(BitChessBoard board) {
        this.enpassantable = board.enpassantable;
        this.castlingRights = board.castlingRights;
        this.whitePawns = board.whitePawns;
        this.whiteKnights = board.whiteKnights;
        this.whiteBishops = board.whiteBishops;
        this.whiteRooks = board.whiteRooks;
        this.whiteQueens = board.whiteQueens;
        this.whiteKing = board.whiteKing;
        this.blackPawns = board.blackPawns;
        this.blackKnights = board.blackKnights;
        this.blackBishops = board.blackBishops;
        this.blackRooks = board.blackRooks;
        this.blackQueens = board.blackQueens;
        this.blackKing = board.blackKing;
    }


    public long[] getBoard() {
        long[] board = {this.whitePawns, this.whiteKnights, this.whiteBishops, 
                        this.whiteRooks, this.whiteQueens, this.whiteKing,
                        this.blackPawns, this.blackKnights, this.blackBishops, 
                        this.blackRooks, this.blackQueens, this.blackKing};
        return board;
    }


    /**
     * Makes a move on the board, assumes that the move is legal.
     * @param move move to be made
     */
    public void makeMove(String move) {

        int fromRow = MoveParser.getFromRow(move);
        int fromCol = MoveParser.getFromCol(move);
        int toRow = MoveParser.getToRow(move);
        int toCol = MoveParser.getToCol(move);

        //special case for promotion:

        if (MoveParser.isPromotion(move)) { 
            //if fromrow is less than torow, then it's a white pawn
            long toPiece = 1L << (8 * toRow + toCol);
            if (fromRow < toRow) {
                this.whitePawns &= ~(1L << (8 * fromRow + fromCol));
                if ((this.blackPawns & toPiece) != 0L) {
                    this.blackPawns &= ~toPiece;
                } else if ((this.blackKnights & toPiece) != 0L) {
                    this.blackKnights &= ~toPiece;
                } else if ((this.blackBishops & toPiece) != 0L) {
                    this.blackBishops &= ~toPiece;
                } else if ((this.blackRooks & toPiece) != 0L) {
                    this.blackRooks &= ~toPiece;
                } else if ((this.blackQueens & toPiece) != 0L) {
                    this.blackQueens &= ~toPiece;
                }
                switch (move.charAt(4)) {
                    case 'q':
                        this.whiteQueens |= 1L << (8 * toRow + toCol);
                        break;
                    case 'r':
                        this.whiteRooks |= 1L << (8 * toRow + toCol);
                        break;
                    case 'b':
                        this.whiteBishops |= 1L << (8 * toRow + toCol);
                        break;
                    case 'n':
                        this.whiteKnights |= 1L << (8 * toRow + toCol);
                        break;
                }
            } else {
                this.blackPawns &= ~(1L << (8 * fromRow + fromCol));
                if ((this.whitePawns & toPiece) != 0L) {
                    this.whitePawns &= ~toPiece;
                } else if ((this.whiteKnights & toPiece) != 0L) {
                    this.whiteKnights &= ~toPiece;
                } else if ((this.whiteBishops & toPiece) != 0L) {
                    this.whiteBishops &= ~toPiece;
                } else if ((this.whiteRooks & toPiece) != 0L) {
                    this.whiteRooks &= ~toPiece;
                } else if ((this.whiteQueens & toPiece) != 0L) {
                    this.whiteQueens &= ~toPiece;
                }
                switch (move.charAt(4)) {
                    case 'q':
                        this.blackQueens |= 1L << (8 * toRow + toCol);
                        break;
                    case 'r':
                        this.blackRooks |= 1L << (8 * toRow + toCol);
                        break;
                    case 'b':
                        this.blackBishops |= 1L << (8 * toRow + toCol);
                        break;
                    case 'n':
                        this.blackKnights |= 1L << (8 * toRow + toCol);
                        break;
                }
            }
            this.enpassantable = "-";
            return;
        } 

        //special cases for castling moves:
        //since we need to move both the king and the rook

        if ((((this.whiteKing >> (fromRow * 8 + fromCol)) & 1L) > 0)) {
            if (move.equals("e1g1")) {
                this.whiteKing &= ~(1L << (8 * fromRow + fromCol));
                this.whiteKing |= 1L << (8 * toRow + toCol);
                this.whiteRooks &= ~(1L << (8 * 0 + 7));
                this.whiteRooks |= 1L << (8 * 0 + 5);
                this.castlingRights = this.castlingRights.replace("K", "");
                this.castlingRights = this.castlingRights.replace("Q", "");
                this.enpassantable = "-";
                return;
            } else if (move.equals("e1c1")) {
                this.whiteKing &= ~(1L << (8 * fromRow + fromCol));
                this.whiteKing |= 1L << (8 * toRow + toCol);
                this.whiteRooks &= ~(1L << (8 * 0 + 0));
                this.whiteRooks |= 1L << (8 * 0 + 3);
                this.castlingRights = this.castlingRights.replace("K", "");
                this.castlingRights = this.castlingRights.replace("Q", "");
                this.enpassantable = "-";
                return;
            }
        } else if ((((blackKing >> (fromRow * 8 + fromCol)) & 1L) > 0)) {
            if (move.equals("e8g8")) {
                this.blackKing &= ~(1L << (8 * fromRow + fromCol));
                this.blackKing |= 1L << (8 * toRow + toCol);
                this.blackRooks &= ~(1L << (8 * 7 + 7));
                this.blackRooks |= 1L << (8 * 7 + 5);
                this.castlingRights = this.castlingRights.replace("k", "");
                this.castlingRights = this.castlingRights.replace("q", "");
                this.enpassantable = "-";
                return;
            } else if (move.equals("e8c8")) {
                this.blackKing &= ~(1L << (8 * fromRow + fromCol));
                this.blackKing |= 1L << (8 * toRow + toCol);
                this.blackRooks &= ~(1L << (8 * 7 + 0));
                this.blackRooks |= 1L << (8 * 7 + 3);
                this.castlingRights = this.castlingRights.replace("k", "");
                this.castlingRights = this.castlingRights.replace("q", "");
                this.enpassantable = "-";
                return;
            }
        }
        
        //special case for en passant:

        if (!this.enpassantable.equals("-")) {
            if (move.charAt(0) != move.charAt(2) 
                    && move.charAt(1) == this.enpassantable.charAt(1) 
                    && move.charAt(2) == this.enpassantable.charAt(0) 
                    && ((((this.whitePawns >> (fromRow * 8 + fromCol)) & 1L) > 0) 
                        || (((this.blackPawns >> (fromRow * 8 + fromCol)) & 1L) > 0))) {
                if (fromRow < toRow) {
                    this.whitePawns &= ~(1L << (8 * fromRow + fromCol));
                    this.whitePawns |= 1L << (8 * toRow + toCol);
                    this.blackPawns &= ~(1L << (8 * fromRow + toCol));
                } else {
                    this.blackPawns &= ~(1L << (8 * fromRow + fromCol));
                    this.blackPawns |= 1L << (8 * toRow + toCol);
                    this.whitePawns &= ~(1L << (8 * fromRow + toCol));
                }
                this.enpassantable = "-";
                return;
            }
        }

        //set enpassantable piece:

        if (Math.abs(fromRow - toRow) == 2 
                && ((((this.whitePawns >> (fromRow * 8 + fromCol)) & 1L) > 0) 
                    || (((this.blackPawns >> (fromRow * 8 + fromCol)) & 1L) > 0))) {

            this.enpassantable = move.substring(2);

        } else {
            //reset after any move that is not a double pawn move
            this.enpassantable = "-";
        }

        //update castling rights in case of king or rook move:

        if ((((this.whiteRooks >> (fromRow * 8 + fromCol)) & 1L) > 0)) {
            if (fromRow == 0 && fromCol == 0 && this.castlingRights.contains("Q")) {
                this.castlingRights = this.castlingRights.replace("Q", "");
            } else if (fromRow == 0 && fromCol == 7 && castlingRights.contains("K")) {
                this.castlingRights = this.castlingRights.replace("K", "");
            }
        } else if ((((this.blackRooks >> (fromRow * 8 + fromCol)) & 1L) > 0)) {
            if (fromRow == 7 && fromCol == 0 && this.castlingRights.contains("q")) {
                this.castlingRights = this.castlingRights.replace("q", "");
            } else if (fromRow == 7 && fromCol == 7 && castlingRights.contains("k")) {
                this.castlingRights = this.castlingRights.replace("k", "");
            }
        } else if ((((this.whiteKing >> (fromRow * 8 + fromCol)) & 1L) > 0)) {
            if (fromRow == 0 && fromCol == 4) {
                if (this.castlingRights.contains("K")) {
                    this.castlingRights = this.castlingRights.replace("K", "");
                }
                if (this.castlingRights.contains("Q")) {
                    this.castlingRights = this.castlingRights.replace("Q", "");
                }
            }
        } else if ((((this.blackKing >> (fromRow * 8 + fromCol)) & 1L) > 0)) {
            if (fromRow == 7 && fromCol == 4) {
                if (this.castlingRights.contains("k")) {
                    this.castlingRights = this.castlingRights.replace("k", "");
                }
                if (this.castlingRights.contains("q")) {
                    this.castlingRights = this.castlingRights.replace("q", "");
                }
            }
        }

        //For non special cases, just move the piece:

        long fromPiece = 1L << (8 * fromRow + fromCol);
        long toPiece = 1L << (8 * toRow + toCol);

        // remove piece at toPiece if there is already one

        if ((this.whitePawns & toPiece) != 0L) {
            this.whitePawns &= ~toPiece;
        } else if ((this.whiteKnights & toPiece) != 0L) {
            this.whiteKnights &= ~toPiece;
        } else if ((this.whiteBishops & toPiece) != 0L) {
            this.whiteBishops &= ~toPiece;
        } else if ((this.whiteRooks & toPiece) != 0L) {
            this.whiteRooks &= ~toPiece;
        } else if ((this.whiteQueens & toPiece) != 0L) {
            this.whiteQueens &= ~toPiece;
        } else if ((this.blackPawns & toPiece) != 0L) {
            this.blackPawns &= ~toPiece;
        } else if ((this.blackKnights & toPiece) != 0L) {
            this.blackKnights &= ~toPiece;
        } else if ((this.blackBishops & toPiece) != 0L) {
            this.blackBishops &= ~toPiece;
        } else if ((this.blackRooks & toPiece) != 0L) {
            this.blackRooks &= ~toPiece;
        } else if ((this.blackQueens & toPiece) != 0L) {
            this.blackQueens &= ~toPiece;
        }

        // move the moving piece

        if ((this.whitePawns & fromPiece) != 0L) {
            this.whitePawns &= ~fromPiece;
            this.whitePawns |= toPiece;
        } else if ((this.whiteKnights & fromPiece) != 0L) {
            this.whiteKnights &= ~fromPiece;
            this.whiteKnights |= toPiece;
        } else if ((this.whiteBishops & fromPiece) != 0L) {
            this.whiteBishops &= ~fromPiece;
            this.whiteBishops |= toPiece;
        } else if ((this.whiteRooks & fromPiece) != 0L) {
            this.whiteRooks &= ~fromPiece;
            this.whiteRooks |= toPiece;
        } else if ((this.whiteQueens & fromPiece) != 0L) {
            this.whiteQueens &= ~fromPiece;
            this.whiteQueens |= toPiece;
        } else if ((this.whiteKing & fromPiece) != 0L) {
            this.whiteKing &= ~fromPiece;
            this.whiteKing |= toPiece;
        } else if ((this.blackPawns & fromPiece) != 0L) {
            this.blackPawns &= ~fromPiece;
            this.blackPawns |= toPiece;
        } else if ((this.blackKnights & fromPiece) != 0L) {
            this.blackKnights &= ~fromPiece;
            this.blackKnights |= toPiece;
        } else if ((this.blackBishops & fromPiece) != 0L) {
            this.blackBishops &= ~fromPiece;
            this.blackBishops |= toPiece;
        } else if ((this.blackRooks & fromPiece) != 0L) {
            this.blackRooks &= ~fromPiece;
            this.blackRooks |= toPiece;
        } else if ((this.blackQueens & fromPiece) != 0L) {
            this.blackQueens &= ~fromPiece;
            this.blackQueens |= toPiece;
        } else if ((this.blackKing & fromPiece) != 0L) {
            this.blackKing &= ~fromPiece;
            this.blackKing |= toPiece;
        }

    }


    /**
     * Makes a series of moves on the board, assumes that the moves are legal.
     * @param moves moves to be made
     */
    public void makeMoves(String[] moves) {
        for (String move : moves) {
            this.makeMove(move);
        }
    }


    private void initializeBoard() {
        //white pieces
        this.whiteRooks |= 1L << 0;
        this.whiteKnights |= 1L << 1;
        this.whiteBishops |= 1L << 2;
        this.whiteQueens |= 1L << 3;
        this.whiteKing |= 1L << 4;
        this.whiteBishops |= 1L << 5;
        this.whiteKnights |= 1L << 6;
        this.whiteRooks |= 1L << 7;
        for (int i = 8; i < 16; i++) {
            this.whitePawns |= 1L << i;
        }

        //black pieces
        this.blackRooks |= 1L << 56;
        this.blackKnights |= 1L << 57;
        this.blackBishops |= 1L << 58;
        this.blackQueens |= 1L << 59;
        this.blackKing |= 1L << 60;
        this.blackBishops |= 1L << 61;
        this.blackKnights |= 1L << 62;
        this.blackRooks |= 1L << 63;
        for (int i = 48; i < 56; i++) {
            this.blackPawns |= 1L << i;
        }

    }


    // Helper method for Hasher class
    public int getPieceTypeAtIndex (int index) {
        if ((((this.whitePawns >> index) & 1L) > 0)) {
            return 0;
        } else if ((((this.whiteKnights >> index) & 1L) > 0)) {
            return 1;
        } else if ((((this.whiteBishops >> index) & 1L) > 0)) {
            return 2;
        } else if ((((this.whiteRooks >> index) & 1L) > 0)) {
            return 3;
        } else if ((((this.whiteQueens >> index) & 1L) > 0)) {
            return 4;
        } else if ((((this.whiteKing >> index) & 1L) > 0)) {
            return 5;
        } else if ((((this.blackPawns >> index) & 1L) > 0)) {
            return 6;
        } else if ((((this.blackKnights >> index) & 1L) > 0)) {
            return 7;
        } else if ((((this.blackBishops >> index) & 1L) > 0)) {
            return 8;
        } else if ((((this.blackRooks >> index) & 1L) > 0)) {
            return 9;
        } else if ((((this.blackQueens >> index) & 1L) > 0)) {
            return 10;
        } else if ((((this.blackKing >> index) & 1L) > 0)) {
            return 11;
        } else {
            return -1;
        }
    }


    /**
     * Converts a FEN string to a board.
     * Useful for quickly setting up a board for testing.
     * @param fen FEN string
     */
    public void fenToBoard(String fen) {

        this.resetBoard();

        String[] fenParts = fen.split(" ");
        String[] rows = fenParts[0].split("/");

        for (int i = 0; i < rows.length; i++) {
            String row = rows[7 - i];
            int j = 0;
            for (int k = 0; k < row.length(); k++) {
                char c = row.charAt(k);
                if (Character.isDigit(c)) {
                    j += Character.getNumericValue(c);
                } else {
                    switch (c) {
                        case 'P':
                            this.whitePawns |= 1L << (8 * i + j);
                            break;
                        case 'N':
                            this.whiteKnights |= 1L << (8 * i + j);
                            break;
                        case 'B':
                            this.whiteBishops |= 1L << (8 * i + j);
                            break;
                        case 'R':
                            this.whiteRooks |= 1L << (8 * i + j);
                            break;
                        case 'Q':
                            this.whiteQueens |= 1L << (8 * i + j);
                            break;
                        case 'K':
                            this.whiteKing |= 1L << (8 * i + j);
                            break;
                        case 'p':
                            this.blackPawns |= 1L << (8 * i + j);
                            break;
                        case 'n':
                            this.blackKnights |= 1L << (8 * i + j);
                            break;
                        case 'b':
                            this.blackBishops |= 1L << (8 * i + j);
                            break;
                        case 'r':
                            this.blackRooks |= 1L << (8 * i + j);
                            break;
                        case 'q':
                            this.blackQueens |= 1L << (8 * i + j);
                            break;
                        case 'k':
                            this.blackKing |= 1L << (8 * i + j);
                            break;
                    }
                    j++;
                }
            }
        }

        this.castlingRights = fenParts[2];
        
        //set enpassantable
        String toMove = fenParts[1];
        String enpassantable = fenParts[3];

        if (!enpassantable.equals("-")) {
            if (toMove.equals("w")) {
                this.enpassantable = enpassantable.charAt(0) 
                    + Integer.toString((Character.getNumericValue(enpassantable.charAt(1)) - 1));
            } else {
                this.enpassantable = enpassantable.charAt(0) 
                    + Integer.toString((Character.getNumericValue(enpassantable.charAt(1)) + 1));
            }
        } else {
            this.enpassantable = "-";
        }

    }

    
    private void resetBoard() {
        this.whitePawns = 0L;
        this.whiteKnights = 0L;
        this.whiteBishops = 0L;
        this.whiteRooks = 0L;
        this.whiteQueens = 0L;
        this.whiteKing = 0L;
        this.blackPawns = 0L;
        this.blackKnights = 0L;
        this.blackBishops = 0L;
        this.blackRooks = 0L;
        this.blackQueens = 0L;
        this.blackKing = 0L;
    }


    public void printBoardNicely() {
        // print out a chess board in a nice way
        String[] rows = new String[8];
        for (int i = 0; i < 8; i++) {
            rows[i] = "";

        }

        for (int i = 0; i < 64; i++) {
            if ((((this.whitePawns >> i) & 1L) > 0)) {
                rows[i / 8] += "P";
            } else if ((((this.whiteKnights >> i) & 1L) > 0)) {
                rows[i / 8] += "N";
            } else if ((((this.whiteBishops >> i) & 1L) > 0)) {
                rows[i / 8] += "B";
            } else if ((((this.whiteRooks >> i) & 1L) > 0)) {
                rows[i / 8] += "R";
            } else if ((((this.whiteQueens >> i) & 1L) > 0)) {
                rows[i / 8] += "Q";
            } else if ((((this.whiteKing >> i) & 1L) > 0)) {
                rows[i / 8] += "K";
            } else if ((((this.blackPawns >> i) & 1L) > 0)) {
                rows[i / 8] += "p";
            } else if ((((this.blackKnights >> i) & 1L) > 0)) {
                rows[i / 8] += "n";
            } else if ((((this.blackBishops >> i) & 1L) > 0)) {
                rows[i / 8] += "b";
            } else if ((((this.blackRooks >> i) & 1L) > 0)) {
                rows[i / 8] += "r";
            } else if ((((this.blackQueens >> i) & 1L) > 0)) {
                rows[i / 8] += "q";
            } else if ((((this.blackKing >> i) & 1L) > 0)) {
                rows[i / 8] += "k";
            } else {
                rows[i / 8] += ".";
            }
        }

        for (int i = 0; i < 8; i++) {
            System.out.println(rows[7 - i]);
        }

    }
    
}
