package datastructureproject;

import org.junit.*;
import static org.junit.Assert.*;

import chess.model.Side;


public class ChessBoardTest {
    
    ChessBoard board;

    @Before
    public void setUp() {
        board = new ChessBoard();
    }

    @After
    public void tearDown() {
        board = null;
    }

    @Test
    public void testBoardInitialization() {
        assertEquals(PieceType.ROOK, board.getPiece(0, 0).getType());
        assertEquals(PieceType.KNIGHT, board.getPiece(0, 1).getType());
        assertEquals(PieceType.BISHOP, board.getPiece(0, 2).getType());
        assertEquals(PieceType.QUEEN, board.getPiece(0, 3).getType());
        assertEquals(PieceType.KING, board.getPiece(0, 4).getType());
        assertEquals(PieceType.BISHOP, board.getPiece(0, 5).getType());
        assertEquals(PieceType.KNIGHT, board.getPiece(0, 6).getType());
        assertEquals(PieceType.ROOK, board.getPiece(0, 7).getType());
        assertEquals(PieceType.PAWN, board.getPiece(1, 0).getType());
        assertEquals(PieceType.PAWN, board.getPiece(1, 1).getType());
        assertEquals(PieceType.PAWN, board.getPiece(1, 2).getType());
        assertEquals(PieceType.PAWN, board.getPiece(1, 3).getType());
        assertEquals(PieceType.PAWN, board.getPiece(1, 4).getType());
        assertEquals(PieceType.PAWN, board.getPiece(1, 5).getType());
        assertEquals(PieceType.PAWN, board.getPiece(1, 6).getType());
        assertEquals(PieceType.PAWN, board.getPiece(1, 7).getType());
        assertEquals(PieceType.ROOK, board.getPiece(7, 0).getType());
        assertEquals(PieceType.KNIGHT, board.getPiece(7, 1).getType());
        assertEquals(PieceType.BISHOP, board.getPiece(7, 2).getType());
        assertEquals(PieceType.QUEEN, board.getPiece(7, 3).getType());
        assertEquals(PieceType.KING, board.getPiece(7, 4).getType());
        assertEquals(PieceType.BISHOP, board.getPiece(7, 5).getType());
        assertEquals(PieceType.KNIGHT, board.getPiece(7, 6).getType());
        assertEquals(PieceType.ROOK, board.getPiece(7, 7).getType());
        assertEquals(PieceType.PAWN, board.getPiece(6, 0).getType());
        assertEquals(PieceType.PAWN, board.getPiece(6, 1).getType());
        assertEquals(PieceType.PAWN, board.getPiece(6, 2).getType());
        assertEquals(PieceType.PAWN, board.getPiece(6, 3).getType());
        assertEquals(PieceType.PAWN, board.getPiece(6, 4).getType());
        assertEquals(PieceType.PAWN, board.getPiece(6, 5).getType());
        assertEquals(PieceType.PAWN, board.getPiece(6, 6).getType());
        assertEquals(PieceType.PAWN, board.getPiece(6, 7).getType());
    }

    @Test
    public void testSetPiece() {
        board.setPiece(0, 0, new Piece(PieceType.QUEEN, Side.WHITE));
        assertEquals(PieceType.QUEEN, board.getPiece(0, 0).getType());
    }

    @Test
    public void testRemovePiece() {
        board.removePiece(0, 0);
        assertEquals(null, board.getPiece(0, 0));
    }

    @Test
    public void testIsOccupiedBySide() {
        assertTrue(board.isOccupiedBySide(7, 0, Side.BLACK));
    }

    @Test
    public void testMakeMove() {
        String move = "a2a3";
        board.makeMove(move);
        assertEquals(PieceType.PAWN, board.getPiece(2, 0).getType());
    }

    @Test
    public void testMakeMoves() {
        String[] moves = {"a2a3", "a7a6"};
        board.makeMoves(moves);
        assertEquals(PieceType.PAWN, board.getPiece(2, 0).getType());
        assertEquals(PieceType.PAWN, board.getPiece(5, 0).getType());
        assertEquals(Side.BLACK, board.getPiece(5, 0).getSide());
    }

    @Test
    public void testCastling() {
        String[] moves = {"e2e4", "e7e5", "g1f3", "g8f6", "f1c4", "f8c5", "e1g1", "e8g8"};
        board.makeMoves(moves);
        assertEquals(PieceType.KING, board.getPiece(7, 6).getType());
        assertEquals(PieceType.ROOK, board.getPiece(7, 5).getType());
        assertEquals(PieceType.KING, board.getPiece(0, 6).getType());
        assertEquals(PieceType.ROOK, board.getPiece(0, 5).getType());
    }

    @Test
    public void testEnPassant() {
        String[] moves = {"e2e4", "a7a6", "e4e5", "f7f5"};
        board.makeMoves(moves);
        assertEquals("f5", board.getEnpassantable());
        board.makeMove("e5f6");
        assertEquals(PieceType.PAWN, board.getPiece(5, 5).getType());
        assertEquals(null, board.getPiece(4, 5));
    }

}
