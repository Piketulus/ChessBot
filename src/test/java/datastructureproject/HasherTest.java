package datastructureproject;

import org.junit.*;

import chess.model.Side;

import static org.junit.Assert.*;

public class HasherTest {

    BitChessBoard board;
    Hasher hasher;

    @Before
    public void setUp() {
        board = new BitChessBoard();
        hasher = new Hasher();
    }

    @After
    public void tearDown() {
        board = null;
        hasher = null;
    }

    @Test
    public void testHash() {
        board.fenToBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        long hash1 = hasher.getHash(board, Side.WHITE);
        BitChessBoard newBoard = new BitChessBoard(board);
        newBoard.makeMove("e2e4");
        long hash2 = hasher.updateHash(hash1, board, newBoard.castlingRights, newBoard.enpassantable, "e2e4");
        System.out.println(hash1);
        assertNotEquals(hash1, hash2);
    }
    
}
