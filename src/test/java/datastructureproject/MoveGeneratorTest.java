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
        ArrayList<String> moves = mg.getMoves();
        assertEquals(20, moves.size());
    }
    
}
