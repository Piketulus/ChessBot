package datastructureproject;

import java.util.ArrayList;
import org.junit.*;
import static org.junit.Assert.*;

import chess.model.Side;


public class MoveGeneratorTest {

    MoveGenerator mg;
    ChessBoard board;

    @Before
    public void setUp() {
        board = new ChessBoard();
        mg = new MoveGenerator(board.getBoard(), board.getEnpassantable(), Side.WHITE);
    }

    @After
    public void tearDown() {
        board = null;
        mg = null;
    }

    @Test
    public void testFillBitboards() {
        assertEquals(1L << (0 * 8 + 4), mg.whiteKing);
        assertEquals(1L << (7 * 8 + 4), mg.blackKing);
    }

    @Test
    public void testGetMoves() {
        String[] moves = {"e2e4", "d7d5", "f1b5"};
        board.makeMoves(moves);
        mg = new MoveGenerator(board.getBoard(), board.getEnpassantable(), Side.BLACK);
        ArrayList<String> responses = mg.getMoves();
        assertEquals(5, responses.size());
    }

    @Test
    public void testCheckmate() {
        String[] moves = {"e2e4", "e7e5", "f1c4", "b8c6", "d1h5", "g8f6", "h5f7"};
        board.makeMoves(moves);
        mg = new MoveGenerator(board.getBoard(), board.getEnpassantable(), Side.BLACK);
        ArrayList<String> responses = mg.getMoves();
        assertEquals(0, responses.size());
    }
    
}
