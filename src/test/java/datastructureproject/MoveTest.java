package datastructureproject;

import org.junit.*;

import static org.junit.Assert.*;


public class MoveTest {
    
    Move move;
    Move promotionMove;

    @Before
    public void setUp() {
        move = new Move("a1a2");
        promotionMove = new Move("h7h8q");
    }

    @After
    public void tearDown() {
        move = null;
        promotionMove = null;
    }

    @Test
    public void testGetFromRow() {
        assertEquals(0, move.getFromRow());
    }

    @Test
    public void testGetFromCol() {
        assertEquals(0, move.getFromCol());
    }

    @Test
    public void testGetToRow() {
        assertEquals(1, move.getToRow());
    }

    @Test
    public void testGetToCol() {
        assertEquals(0, move.getToCol());
    }

    @Test
    public void testIsPromotion() {
        assertTrue(promotionMove.isPromotion());
    }

    @Test
    public void testGetPromotionPiece() {
        assertEquals(PieceType.QUEEN, promotionMove.getPromotionPiece());
    }

    @Test
    public void testGetMove() {
        assertEquals("a1a2", move.getMove());
    }

}
