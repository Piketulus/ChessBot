package datastructureproject;

import java.util.LinkedList;

import chess.model.Side;


/**
 * ChessBoard class is an object for the chess board.
 * It stores the board as a 2D array of Piece objects.
 */
public class ChessBoard {

    private Piece[][] board;
    private String enpassantable;
    private String castlingRights;

    private LinkedList<String> previousMoves = new LinkedList<String>();
    private LinkedList<String> previousEnps = new LinkedList<String>();
    private LinkedList<Piece> previousCaptures = new LinkedList<Piece>();
    private LinkedList<String> previousCastlingRights = new LinkedList<String>();


    public ChessBoard() {
        this.board = new Piece[8][8];
        this.enpassantable = "";
        this.castlingRights = "KQkq";
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
        this.castlingRights = other.getCastlingRights();
    }

    public Piece[][] getBoard() {
        return this.board;
    }

    public String getEnpassantable() {
        return this.enpassantable;
    }

    public void setEnpassantable(String enpassantable) {
        this.enpassantable = enpassantable;
    }

    public String getCastlingRights() {
        return this.castlingRights;
    }

    public void setCastlingRights(String castlingRights) {
        this.castlingRights = castlingRights;
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
     * @param move a string of a move in UCI format
     */
    public void makeMove(String move) {

        int fromRow = MoveParser.getFromRow(move);
        int fromCol = MoveParser.getFromCol(move);
        int toRow = MoveParser.getToRow(move);
        int toCol = MoveParser.getToCol(move);

        /*
        //add move to previous moves list
        this.previousMoves.addLast(move);
        this.previousEnps.addLast(this.enpassantable);
        this.previousCastlingRights.addLast(this.castlingRights);

        //add captured piece to previous captures list
        Piece captured = this.getPiece(toRow, toCol);
        if (captured != null) {
            this.previousCaptures.addLast(captured);
        } else {
            this.previousCaptures.addLast(null);
        }
         */
        //special case for promotion:

        if (MoveParser.isPromotion(move)) { 
            Piece piece = new Piece(MoveParser.getPromotionPiece(move), 
                                    this.getPiece(fromRow, fromCol).getSide());
            this.setPiece(toRow, toCol, piece);
            this.removePiece(fromRow, fromCol);
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
                && move.charAt(1) == this.enpassantable.charAt(1) 
                && move.charAt(2) == this.enpassantable.charAt(0) 
                && this.getPiece(fromRow, fromCol).getType() 
                == PieceType.PAWN) {
                
                //this.previousCaptures.removeLast();
                //this.previousCaptures.addLast(new Piece(this.getPiece(fromRow, toCol)));

                this.removePiece(fromRow, toCol);
            }
        }

        //set enpassantable piece:
        /*
        if (this.getPiece(fromRow, fromCol) == null) {
            this.printBoardNicely();
            System.out.println(this.previousMoves);
            throw new IllegalArgumentException("No piece at " + move);
            
        }
         */
        if (this.getPiece(fromRow, fromCol).getType() == PieceType.PAWN 
            && Math.abs(fromRow - toRow) == 2) {

            this.enpassantable = move.substring(2);

        } else {
            //reset after any move that is not a double pawn move
            this.enpassantable = "";
        }

        Piece piece = this.getPiece(fromRow, fromCol);
        if (!piece.getHasMoved()) {
            piece.setHasMoved();
            // if the piece is a rook, remove castling rights for that side, if piece is king, remove all castling rights
            /*
            if (piece.getType() == PieceType.ROOK) {
                if (piece.getSide() == Side.WHITE) {
                    if (fromRow == 0 && fromCol == 0) {
                        this.castlingRights = this.castlingRights.replace("Q", "");
                    } else if (fromRow == 0 && fromCol == 7) {
                        this.castlingRights = this.castlingRights.replace("K", "");
                    }
                } else {
                    if (fromRow == 7 && fromCol == 0) {
                        this.castlingRights = this.castlingRights.replace("q", "");
                    } else if (fromRow == 7 && fromCol == 7) {
                        this.castlingRights = this.castlingRights.replace("k", "");
                    }
                }
            } else if (piece.getType() == PieceType.KING) {
                if (piece.getSide() == Side.WHITE) {
                    this.castlingRights = this.castlingRights.replace("Q", "");
                    this.castlingRights = this.castlingRights.replace("K", "");
                } else {
                    this.castlingRights = this.castlingRights.replace("q", "");
                    this.castlingRights = this.castlingRights.replace("k", "");
                }
            }
             */
        }
        this.setPiece(toRow, toCol, piece);
        this.removePiece(fromRow, fromCol);
        
    }

    public void makeMoves(String[] moves) {
        for (String move : moves) {
            this.makeMove(move);
        }
    }


    /**
     * Undoes the last move made on the board.
     * Expects that there is a move to undo.
     */
    public void undoMove() {

        String move = this.previousMoves.removeLast();
        String prevEnpassant = this.previousEnps.removeLast();
        Piece captured = this.previousCaptures.removeLast();
        String castlingRights = this.previousCastlingRights.removeLast();

        int fromRow = MoveParser.getFromRow(move);
        int fromCol = MoveParser.getFromCol(move);
        int toRow = MoveParser.getToRow(move);
        int toCol = MoveParser.getToCol(move);

        //special case for promotion:

        if (MoveParser.isPromotion(move)) { 
            Piece piece = new Piece(PieceType.PAWN, this.getPiece(toRow, toCol).getSide());
            this.setPiece(fromRow, fromCol, piece);
            this.setPiece(toRow, toCol, captured);
            this.setEnpassantable(prevEnpassant);
            this.setCastlingRights(castlingRights);
            return;
        } 

        //special cases for castling moves:

        if (move.equals("e1c1") && this.getPiece(0, 2).getType() == PieceType.KING) {
            //move the rook from d1 to a1
            Piece piece = this.getPiece(0, 3);
            piece.setHasNotMoved();
            this.setPiece(0, 0, piece);
            this.removePiece(0, 3);
            //move king
            Piece piece2 = this.getPiece(toRow, toCol);
            piece2.setHasNotMoved();
            this.setPiece(fromRow, fromCol, piece2);
            this.removePiece(toRow, toCol);
            this.setEnpassantable(prevEnpassant);
            this.setCastlingRights(castlingRights);
            return;
        } else if (move.equals("e1g1") && this.getPiece(0, 6).getType() == PieceType.KING) {
            //move the rook from f1 to h1
            Piece piece = this.getPiece(0, 5);
            piece.setHasNotMoved();
            this.setPiece(0, 7, piece);
            this.removePiece(0, 5);
            //move king
            Piece piece2 = this.getPiece(toRow, toCol);
            piece2.setHasNotMoved();
            this.setPiece(fromRow, fromCol, piece2);
            this.removePiece(toRow, toCol);
            this.setEnpassantable(prevEnpassant);
            this.setCastlingRights(castlingRights);
            return;
        } else if (move.equals("e8c8") && this.getPiece(7, 2).getType() == PieceType.KING) {
            //move the rook from d8 to a8
            Piece piece = this.getPiece(7, 3);
            piece.setHasNotMoved();
            this.setPiece(7, 0, piece);
            this.removePiece(7, 3);
            //move king
            Piece piece2 = this.getPiece(toRow, toCol);
            piece2.setHasNotMoved();
            this.setPiece(fromRow, fromCol, piece2);
            this.removePiece(toRow, toCol);
            this.setEnpassantable(prevEnpassant);
            this.setCastlingRights(castlingRights);
            return;
        } else if (move.equals("e8g8") && this.getPiece(7, 6).getType() == PieceType.KING) {
            //move the rook from f8 to h8
            Piece piece = this.getPiece(7, 5);
            piece.setHasNotMoved();
            this.setPiece(7, 7, piece);
            this.removePiece(7, 5);
            //move king
            Piece piece2 = this.getPiece(toRow, toCol);
            piece2.setHasNotMoved();
            this.setPiece(fromRow, fromCol, piece2);
            this.removePiece(toRow, toCol);
            this.setEnpassantable(prevEnpassant);
            this.setCastlingRights(castlingRights);
            return;
        }

        //special case for en passant:
        
        if (!prevEnpassant.equals("") && captured != null) {
            if (move.charAt(0) != move.charAt(2) 
                && move.charAt(1) == prevEnpassant.charAt(1) 
                && move.charAt(2) == prevEnpassant.charAt(0) 
                && this.getPiece(toRow, toCol).getType() == PieceType.PAWN
                && captured.getType() == PieceType.PAWN) {
                
                this.setPiece(fromRow, toCol, captured);
                this.setPiece(fromRow, fromCol, this.getPiece(toRow, toCol));
                this.setPiece(toRow, toCol, null);
                this.setEnpassantable(prevEnpassant);
                this.setCastlingRights(castlingRights);
                return;
            }
        }

        //otherwise:

        Piece piece = this.getPiece(toRow, toCol);

        // check previous castling rights and set hasMoved accordingly
        if (piece.getType() == PieceType.KING) {
            if (piece.getSide() == Side.WHITE) {
                if (castlingRights.contains("K") || castlingRights.contains("Q")) {
                    piece.setHasNotMoved();
                }
            } else {
                if (castlingRights.contains("k") || castlingRights.contains("q")) {
                    piece.setHasNotMoved();
                }
            }
        } else if (piece.getType() == PieceType.ROOK) {
            if (piece.getSide() == Side.WHITE) {
                if (fromRow == 0 && fromCol == 0) {
                    if (castlingRights.contains("Q")) {
                        piece.setHasNotMoved();
                    }
                } else if (fromRow == 0 && fromCol == 7) {
                    if (castlingRights.contains("K")) {
                        piece.setHasNotMoved();
                    }
                }
            } else {
                if (fromRow == 7 && fromCol == 0) {
                    if (castlingRights.contains("q")) {
                        piece.setHasNotMoved();
                    }
                } else if (fromRow == 7 && fromCol == 7) {
                    if (castlingRights.contains("k")) {
                        piece.setHasNotMoved();
                    }
                }
            }
        }

        this.setPiece(fromRow, fromCol, piece);
        this.setPiece(toRow, toCol, captured);
        this.setEnpassantable(prevEnpassant);
        this.setCastlingRights(castlingRights);
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


    public void printBoardNicely() {
        //prints the board in a nice format
        System.out.println("  a b c d e f g h");
        for (int i = 7; i >= 0; i--) {
            System.out.print((i + 1) + " ");
            for (int j = 0; j < 8; j++) {
                if (this.board[i][j] == null) {
                    System.out.print("- ");
                } else {
                    if (this.board[i][j].getSide() == Side.WHITE) {
                        switch (this.board[i][j].getType()) {
                            case PAWN:
                                System.out.print("P ");
                                break;
                            case ROOK:
                                System.out.print("R ");
                                break;
                            case KNIGHT:
                                System.out.print("N ");
                                break;
                            case BISHOP:
                                System.out.print("B ");
                                break;
                            case QUEEN:
                                System.out.print("Q ");
                                break;
                            case KING:
                                System.out.print("K ");
                                break;
                        }
                    } else {
                        switch (this.board[i][j].getType()) {
                            case PAWN:
                                System.out.print("p ");
                                break;
                            case ROOK:
                                System.out.print("r ");
                                break;
                            case KNIGHT:
                                System.out.print("n ");
                                break;
                            case BISHOP:
                                System.out.print("b ");
                                break;
                            case QUEEN:
                                System.out.print("q ");
                                break;
                            case KING:
                                System.out.print("k ");
                                break;
                        }
                    }
                }
            }
            System.out.println(i + 1);
        }
        System.out.println("  a b c d e f g h");
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

        this.setCastlingRights(castlingRights);

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
