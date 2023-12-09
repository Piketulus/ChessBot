# Implementation Document

## Project Structure

The project contains the following classes:

- `BitChessBoard` - represents the chess board. Contains information about the current state of the board, and methods for moving pieces.
- `MoveParser` - parses UCI move strings into row and column coordinates to be used by other classes.
- `MoveGenerator` - generates all legal moves for a given board state.
- `PositionEvaluator` - evaluates the current board state and returns a score.
- `Hasher` - generates a hash value for a given board state using Zobrist hashing. Used for transposition table.
- `PerformanceTest`- contains methods for testing the performance of the program, such as speed of move generation and speed of minimax search.
- `PiketulusBot` - contains the implementation of ChessBot that plays chess using iteratively deepening minimax algorithm with alpha-beta pruning.

## Implemented algorithms

The bot finds the best move using the minimax algorithm with alpha-beta pruning, enchanced with a transposition table and iterative deepening. The naive complexity of the minimax algorithm is O(b^d), where b is the branching factor and d is the depth of the search tree. Alpha-beta pruning can reduce the number of nodes that need to be searched, but this requires that best/better moves are searched first. If the best move is always searched first, the complexity is O(b^(d/2)).

The transposition table is used to store the best move and its score for previously searched board states, so either the position does not need to be searched again if the depth of the previous search was enough. In chess transpositions are common when searching through all moves, so this greatly reduces the number of nodes needed to be searched. With iterative deepening, after having searched the previous depth, the best moves for all positions are stored in the transposition table, so when searching the next depth, when the same position is encountered, the best move can be retrieved from the transposition table and searched first. This way the previously found best move is always searched first, decreasing the number of nodes that need to be searched in total. Since the previously found best move may not actually be the best move, the algorithm does not actually reach the complexity of O(b^(d/2)), but should hopefully be closer to it than the naive minimax algorithm complexity of O(b^d).

Another simple algorithm used is Zobrist hashing, which is used to generate a hash value for a given board state.

## Comparative Performance

I have personally played the bot and would say that it is quite hard to beat for any casual player.

Against stockfish engines on lichess, the bot is able to beat level 5 (lichess rating 2000) most of the time (and therefore any levels below comfortably), but has not managed to beat level 6 (lichess rating 2300) yet. A rough estimate of my bots lichess rating would then maybe be around 2100-2200. And from the games played against stockfish, a rough estimate of the bots average accuracy would be somewhere around 85-90%. Stockfish level 6 seems to play well over 90% accuracy and has not made a single blunder when played against, so it is not surprising that my bot crumbles to it.

In general, my bot simply does not come close to the calculation speed and position evaluation of stockfish, which is expected. But I am still very happy with the performance I was able to achieve with my bot, and have gained a new level of respect for the stockfish engine and other top chess engines.

Example game against stockfish level 5 (Computer analysis gives 91% accuracy for my bot in this game): https://lichess.org/Mwy3Bd02

Lichess approximate bot ratings from: https://lichess.org/@/MagoGG/blog/stockfish-level-and-its-rating/CvL5k0jL

## Possible Flaws and Improvements

The bot is not able to detect draws by repetition or the 50 move rule, as I have neglected to implement these as of now for some reason. In most cases this is not noticeable, but in some rare cases the bot may get threefold repetition even when in a winning position, and therefore draw the game. 

Another thing that could improve the gameplay of my bot is refining the position evaluation function. Currently the bot only evaluates the position based on the material value of the pieces and piece square tables. This works well enough but sometimes the bot makes moves that I can immediately see were probably not optimal. The bot can reach quite good search depths, which is why I believe that the simple position evaluation function is the main reason for the bot not playing as well as it could.

I am sure there are also many efficinecy improvements that could be made to the code and algorithms to increase possible search depth and speed.

## References

Most of the algorithms and techniques used in this project were learned from: https://www.chessprogramming.org/