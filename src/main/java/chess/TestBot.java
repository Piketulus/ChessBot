/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess;

import chess.model.GameState;
import com.github.bhlangonijr.chesslib.move.Move;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestBot {
    private String token;
    
    public TestBot(String token) {
        this.token = token;
    }
    
    public String nextMove(GameState gs) {
        Move myMove;
        try {
            myMove = gs.engine.getMove();
            
            if (myMove != null) {
                return myMove.toString();
            }
            
            
        } catch (Exception ex) {
            Logger.getLogger(TestBot.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    public String getToken() {
        return this.token;
    }
}