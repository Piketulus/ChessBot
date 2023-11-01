# Project Specifications

### Info

* Program: Bachelor's in Science
* Programming language: Java
* Documentation language: English

### Project description

Creating a chess AI and making it as strong as possible within the given time frame (7 weeks), while keeping in mind the efficiency of the program to make sure it does not take too long to make a move.

### Algorithms and data structrures

The chess AI will be implented using the minimax algorithm with alpha-beta pruning. The minimax algorithm is a recursive algorithm for choosing the next move in an n-player game, usually a two-player, zero-sum game. A value is associated with each position or state of the game. This value is computed by means of a position evaluation function (heuristic) and it indicates how good it would be for a player to reach that position. The player then makes the move that maximizes the minimum value of the position resulting from the opponent's possible following moves. 

Researched time complexities of the above mentioned algorithms:
* Minimax: O(b^m)
* With alpha-beta pruning: O(b^(m/2))

Where b is the branching factor and m is the maximum depth of the tree.

Possible data structures to be implemented are:
* The chess board itself
* Transposition table
* Hash table

### Sources

* https://en.wikipedia.org/wiki/Minimax
* https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning

The project is based on the [chess](https://github.com/TiraLabra/chess) base provided by the course.