# Week 6 Report

This week I wanted to implement move ordering to improve the performance of the alpha beta pruning. I first tried to store principal variations directly from the minimax search to be used, but this turned out harder to implement than I thought, so I decided to go for implementing a transposition table instead. I thought it may be somewhat complex but turned out to be quite quick to implement. I used Zobrist hashing to generate my hash values from the board. This was better to do anyway since the tranposition table would not only help me with move ordering, but also with reducing the number of nodes that need to be searched. Once I combined the transposition table with iterative deepening, the performance of the bot improved quite significantly based on the times it would take to come up with a best move at a fixed depth. 

I also did a some code commenting and updated the documentation.

In the final week I may try to implement some improvements to the bot, or then just polish up the project and documentation, depending on how the week unfolds.


Time spent this week: 8 hours