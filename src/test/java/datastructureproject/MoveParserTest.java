package datastructureproject;

import org.junit.*;
import static org.junit.Assert.*;


public class MoveParserTest {
    
    MoveParser mp;

    @Before
    public void setUp() {
        mp = new MoveParser();
    }

    @After
    public void tearDown() {
        mp = null;
    }

    @Test
    public void testGetFromRow() {
        String move = "a1a2";
        assertEquals(0, mp.getFromRow(move));
    }

    @Test
    public void testGetFromCol() {
        String move = "a1a2";
        assertEquals(0, mp.getFromCol(move));
    }

    @Test
    public void testGetToRow() {
        String move = "a1a2";
        assertEquals(1, mp.getToRow(move));
    }

    @Test
    public void testGetToCol() {
        String move = "a1a2";
        assertEquals(0, mp.getToCol(move));
    }

    @Test
    public void testIsPromotion() {
        String move = "a7a8q";
        assertTrue(mp.isPromotion(move));
        String move2 = "a2a3";
        assertFalse(mp.isPromotion(move2));
    }

    @Test
    public void testGetPromotionPiece() {
        String move = "a7a8q";
        assertEquals(PieceType.QUEEN, mp.getPromotionPiece(move));
    }

}
