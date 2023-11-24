package datastructureproject;

import chess.model.Side;


/**
 * ChessBoard class is an object for the chess board.
 * It stores the board as a 2D array of Piece objects.
 */
public class ChessBoard {

    private Piece[][] board;
    private String enpassantable;


    public ChessBoard() {
        this.board = new Piece[8][8];
        this.enpassantable = "";
        this.addWhitePieces();
        this.addBlackPieces();
    }

    public ChessBoard(ChessBoard other) {
        this.board = new Piece[8][8];
        Piece[][] otherBoard = other.getBoard();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (otherBoard[i][j] != null) {
                    this.board[i][j] = new Piece(otherBoard[i][j]);
                }
            }
        }
        this.enpassantable = other.getEnpassantable();
    }

    public Piece[][] getBoard() {
        return this.board;
    }

    public void setBoard(Piece[][] board) {
        this.board = board;
    }

    public String getEnpassantable() {
        return this.enpassantable;
    }

    public void setEnpassantable(String enpassantable) {
        this.enpassantable = enpassantable;
    }

    public Piece getPiece(int row, int col) {
        return this.board[row][col];
    }

    public void setPiece(int row, int col, Piece piece) {
        this.board[row][col] = piece;
    }

    public void removePiece(int row, int col) {
        this.board[row][col] = null;
    }

    
    /**
     * Given a UCI move, this method makes the move on the board.
     * Expects given move to be valid (does not check for validity)
     * @param move a Move object containing the move in UCI format
     */
    public void makeMove(String move) {

        //special case for promotion:

        if (MoveParser.isPromotion(move)) { 
            Piece piece = new Piece(MoveParser.getPromotionPiece(move), 
                                    this.getPiece(MoveParser.getFromRow(move), MoveParser.getFromCol(move)).getSide());
            this.setPiece(MoveParser.getToRow(move), MoveParser.getToCol(move), piece);
            this.removePiece(MoveParser.getFromRow(move), MoveParser.getFromCol(move));
            piece.setHasMoved();
            return;
        } 

        //special cases for castling moves:

        if (move.equals("e1c1") && this.getPiece(0, 4).getType() == PieceType.KING) {
            //move the rook from a1 to d1
            Piece piece = this.getPiece(0, 0);
            this.setPiece(0, 3, piece);
            this.removePiece(0, 0);
            piece.setHasMoved();
        } else if (move.equals("e1g1") && this.getPiece(0, 4).getType() == PieceType.KING) {
            //move the rook from h1 to f1
            Piece piece = this.getPiece(0, 7);
            this.setPiece(0, 5, piece);
            this.removePiece(0, 7);
            piece.setHasMoved();
        } else if (move.equals("e8c8") && this.getPiece(7, 4).getType() == PieceType.KING) {
            //move the rook from a8 to d8
            Piece piece = this.getPiece(7, 0);
            this.setPiece(7, 3, piece);
            this.removePiece(7, 0);
            piece.setHasMoved();
        } else if (move.equals("e8g8") && this.getPiece(7, 4).getType() == PieceType.KING) {
            //move the rook from h8 to f8
            Piece piece = this.getPiece(7, 7);
            this.setPiece(7, 5, piece);
            this.removePiece(7, 7);
            piece.setHasMoved();                
        }
        
        //special case for en passant:

        if (!this.enpassantable.equals("")) {
            if (move.charAt(0) != move.charAt(2) 
                && move.charAt(1) == enpassantable.charAt(1) 
                && move.charAt(2) == enpassantable.charAt(0) 
                && this.getPiece(MoveParser.getFromRow(move), MoveParser.getFromCol(move)).getType() 
                == PieceType.PAWN) {

                this.removePiece(MoveParser.getFromRow(move), MoveParser.getToCol(move));
            }
        }

        //set enpassantable piece:

        if (this.getPiece(MoveParser.getFromRow(move), MoveParser.getFromCol(move)).getType() == PieceType.PAWN 
            && Math.abs(MoveParser.getFromRow(move) - MoveParser.getToRow(move)) == 2) {

            this.enpassantable = move.substring(2);

        } else {
            //reset after any move that is not a double pawn move
            this.enpassantable = "";
        }

        Piece piece = this.getPiece(MoveParser.getFromRow(move), MoveParser.getFromCol(move));
        this.setPiece(MoveParser.getToRow(move), MoveParser.getToCol(move), piece);
        this.removePiece(MoveParser.getFromRow(move), MoveParser.getFromCol(move));
        if (!piece.getHasMoved()) {
            piece.setHasMoved();
        }
    }

    public void makeMoves(String[] moves) {
        for (String move : moves) {
            this.makeMove(move);
        }
    }


    private void addWhitePieces() {
        //Adds white pieces to the board

        this.board[0][0] = new Piece(PieceType.ROOK, Side.WHITE);
        this.board[0][1] = new Piece(PieceType.KNIGHT, Side.WHITE);
        this.board[0][2] = new Piece(PieceType.BISHOP, Side.WHITE);
        this.board[0][3] = new Piece(PieceType.QUEEN, Side.WHITE);
        this.board[0][4] = new Piece(PieceType.KING, Side.WHITE);
        this.board[0][5] = new Piece(PieceType.BISHOP, Side.WHITE);
        this.board[0][6] = new Piece(PieceType.KNIGHT, Side.WHITE);
        this.board[0][7] = new Piece(PieceType.ROOK, Side.WHITE);
        for (int i = 0; i < 8; i++) {
            this.board[1][i] = new Piece(PieceType.PAWN, Side.WHITE);
        }
    }

    private void addBlackPieces() {
        //Adds black pieces to the board

        this.board[7][0] = new Piece(PieceType.ROOK, Side.BLACK);
        this.board[7][1] = new Piece(PieceType.KNIGHT, Side.BLACK);
        this.board[7][2] = new Piece(PieceType.BISHOP, Side.BLACK);
        this.board[7][3] = new Piece(PieceType.QUEEN, Side.BLACK);
        this.board[7][4] = new Piece(PieceType.KING, Side.BLACK);
        this.board[7][5] = new Piece(PieceType.BISHOP, Side.BLACK);
        this.board[7][6] = new Piece(PieceType.KNIGHT, Side.BLACK);
        this.board[7][7] = new Piece(PieceType.ROOK, Side.BLACK);
        for (int i = 0; i < 8; i++) {
            this.board[6][i] = new Piece(PieceType.PAWN, Side.BLACK);
        }
    }


    public void fenToBoard(String fen) {
        //converts a FEN string to a board
        this.board = new Piece[8][8];

        String[] fenParts = fen.split(" ");

        //castling rights
        String castlingRights = fenParts[2];
        boolean whiteKingSide = castlingRights.contains("K");
        boolean whiteQueenSide = castlingRights.contains("Q");
        boolean blackKingSide = castlingRights.contains("k");
        boolean blackQueenSide = castlingRights.contains("q");

        String[] rows = fenParts[0].split("/");
        for (int i = 0; i < 8; i++) {
            int col = 0;
            for (int j = 0; j < rows[7 - i].length(); j++) {
                char c = rows[7 - i].charAt(j);
                if (Character.isDigit(c)) {
                    col += Character.getNumericValue(c);
                } else {
                    switch (c) {
                        case 'p':
                            this.board[i][col] = new Piece(PieceType.PAWN, Side.BLACK);
                            col++;
                            break;
                        case 'r':
                            this.board[i][col] = new Piece(PieceType.ROOK, Side.BLACK);
                            if (i == 7 && col == 0 && !blackQueenSide) {
                                this.board[i][col].setHasMoved();
                            } else if (i == 7 && col == 7 && !blackKingSide) {
                                this.board[i][col].setHasMoved();
                            }
                            col++;
                            break;
                        case 'n':
                            this.board[i][col] = new Piece(PieceType.KNIGHT, Side.BLACK);
                            col++;
                            break;
                        case 'b':
                            this.board[i][col] = new Piece(PieceType.BISHOP, Side.BLACK);
                            col++;
                            break;
                        case 'q':
                            this.board[i][col] = new Piece(PieceType.QUEEN, Side.BLACK);
                            col++;
                            break;
                        case 'k':
                            this.board[i][col] = new Piece(PieceType.KING, Side.BLACK);
                            if (!blackKingSide && !blackQueenSide) {
                                this.board[i][col].setHasMoved();
                            }
                            col++;
                            break;
                        case 'P':
                            this.board[i][col] = new Piece(PieceType.PAWN, Side.WHITE);
                            col++;
                            break;
                        case 'R':
                            this.board[i][col] = new Piece(PieceType.ROOK, Side.WHITE);
                            if (i == 0 && col == 0 && !whiteQueenSide) {
                                this.board[i][col].setHasMoved();
                            } else if (i == 0 && col == 7 && !whiteKingSide) {
                                this.board[i][col].setHasMoved();
                            }
                            col++;
                            break;
                        case 'N':
                            this.board[i][col] = new Piece(PieceType.KNIGHT, Side.WHITE);
                            col++;
                            break;
                        case 'B':
                            this.board[i][col] = new Piece(PieceType.BISHOP, Side.WHITE);
                            col++;
                            break;
                        case 'Q':
                            this.board[i][col] = new Piece(PieceType.QUEEN, Side.WHITE);
                            col++;
                            break;
                        case 'K':
                            this.board[i][col] = new Piece(PieceType.KING, Side.WHITE);
                            if (!whiteKingSide && !whiteQueenSide) {
                                this.board[i][col].setHasMoved();
                            }
                            col++;
                            break;
                    }
                }
            }
        }
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
            this.enpassantable = "";
        }

    }
    
}
