package datastructureproject;

import org.junit.*;
import static org.junit.Assert.*;

public class BitChessBoardTest {

    BitChessBoard board;

    @Before
    public void setUp() {
        board = new BitChessBoard();
    }

    @After
    public void tearDown() {
        board = null;
    }

    @Test
    public void testBoardInitialization() {
        assertNotEquals(0L, ((board.whiteKing >> (0 * 8 + 4)) & 1L));
        assertNotEquals(0L, ((board.whiteQueens >> (0 * 8 + 3)) & 1L));
    }

    @Test
    public void testMakeMove() {
        board.makeMove("e2e4");
        assertEquals(0L, ((board.whitePawns >> (1 * 8 + 4)) & 1L));
        assertEquals(1L, ((board.whitePawns >> (3 * 8 + 4)) & 1L));
    }

    @Test
    public void testFenToBoard() {
        board.makeMove("e2e4");
        board.fenToBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        assertEquals(1L, ((board.whitePawns >> (1 * 8 + 4)) & 1L));
        assertEquals(0L, ((board.whitePawns >> (3 * 8 + 4)) & 1L));
        assertNotEquals(0L, ((board.whiteKing >> (0 * 8 + 4)) & 1L));
        assertNotEquals(0L, ((board.whiteQueens >> (0 * 8 + 3)) & 1L));
    }
    
}
