package datastructureproject;

import java.util.ArrayList;
import org.junit.*;
import static org.junit.Assert.*;


public class MoveParserTest {

    @Test
    public void testGetFromRow() {
        String move = "a1a2";
        assertEquals(0, MoveParser.getFromRow(move));
    }

    @Test
    public void testGetFromCol() {
        String move = "a1a2";
        assertEquals(0, MoveParser.getFromCol(move));
    }

    @Test
    public void testGetToRow() {
        String move = "a1a2";
        assertEquals(1, MoveParser.getToRow(move));
    }

    @Test
    public void testGetToCol() {
        String move = "a1a2";
        assertEquals(0, MoveParser.getToCol(move));
    }

    @Test
    public void testIsPromotion() {
        String move = "a7a8q";
        assertTrue(MoveParser.isPromotion(move));
        String move2 = "a2a3";
        assertFalse(MoveParser.isPromotion(move2));
    }

    @Test
    public void testGetPromotionPiece() {
        String move = "a7a8q";
        assertEquals(PieceType.QUEEN, MoveParser.getPromotionPiece(move));
    }

    @Test
    public void testCoordsToMove() {
        int fromRow = 0;
        int fromCol = 0;
        ArrayList<int[]> toCoords = new ArrayList<>();
        toCoords.add(new int[]{1, 0});
        String move = "a1a2";
        assertEquals(move, MoveParser.coordsToMoves(fromRow, fromCol, toCoords).get(0));
    }

}
