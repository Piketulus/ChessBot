package datastructureproject;

import org.junit.*;
import static org.junit.Assert.*;

import chess.model.Side;


public class PieceTest {
    
    Piece piece;

    @Before
    public void setUp() {
        piece = new Piece(PieceType.PAWN, Side.WHITE);
    }

    @After
    public void tearDown() {
        piece = null;
    }

    @Test
    public void testGetType() {
        assertEquals(PieceType.PAWN, piece.getType());
    }

    @Test
    public void testGetSide() {
        assertEquals(Side.WHITE, piece.getSide());
    }

    @Test
    public void testGetHasMoved() {
        assertFalse(piece.getHasMoved());
    }

    @Test
    public void testSetHasMoved() {
        piece.setHasMoved();
        assertTrue(piece.getHasMoved());
    }

    @Test
    public void testSetHasNotMoved() {
        piece.setHasMoved();
        piece.setHasNotMoved();
        assertFalse(piece.getHasMoved());
    }

}
