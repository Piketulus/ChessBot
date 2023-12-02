## Testing

### Unit Tests

Unit testing is implemented for most of the crucial parts of the program. The tests are run everytime the project is built with the command `./gradlew build`. Reports of the test can be found at *ChessBot/build/reports/tests/test/index.html*

Test coverage can be viewed at [codecov](https://app.codecov.io/gh/Piketulus/ChessBot).

### Performance Testing

Although unit testing was useful for making sure small parts of the program work as intended, in order to make sure complicated classes such as the MoveGenerator works flawlessly, performance testing was needed. This way I could check for some given position and depth that the number of valid moves generated is the same as what some reference program such as Stockfish would give or numbers that can be found online. If the numbers were matching for different positions at large depths, I could be fairly certain that the program works as intended, and it there were discrepancies, I could easily follow the branch down the problematic positions and find the bug.

This was implemented in the PerformanceTest class, which can be run with the command `./gradlew performanceTest`. The testing is done manually with the use of the functions perft, nextMove (minimax with alpha beta pruning, same as what the bot actually uses in PiketulusBot class), and getEvaluation. The perft function is used to check the number of valid moves generated for a given position and depth, nextMove is used to check the best move generated for a given position and depth, and getEvaluation is used to check the evaluation of a given position. The results of the tests are printed to the terminal. Perft and nextMove are also timed to check the speed of the program.

All the above functions take in FEN strings as parameters that describe the position to be tested. FEN strings of board positions for testing can be found online, or by going for example onto lichess' analysis board, making any sort of moves and copying the FEN string shown below.

Currently with a time limit of 5 seconds per move, the bot can reach depths between 5 and 10 when coming up with the best move depending on the complexity of the position. This time limit allows the player to play fast paced games against the bot, with the bot still being hard to beat.