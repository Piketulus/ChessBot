package datastructureproject;

import org.junit.*;
import static org.junit.Assert.*;

import chess.model.Side;

public class PositionEvaluatorTest {

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
    public void testStartingPosition() {
        assertEquals(0, PositionEvaluator.evaluatePosition(board.getBoard(), Side.WHITE));
    }

    @Test
    public void testOneMove() {
        board.makeMove("e2e4");
        assertEquals(32, PositionEvaluator.evaluatePosition(board.getBoard(), Side.WHITE));
    }
    
}
