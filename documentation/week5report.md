# Week 5 Report

This week I started off with making a new implementation of the board using bitboards. This ended up being fairly simple and as I expected it improved the performance of the move generation and position evaluation. Although I did not compare the two implementations directly, I could see that the new implementation had a significant improvement in performance as times were much faster for move generation perft tests as well as getting the best move for a given position. This also simplified the structure of the project and some of the code. Also now the bot doesn't crash when games end but can be rematched and works properly without having to be restarted.

Then I worked on the suggestions given by the instructor for last week such as heuristic move ordering and iterative deepening to make the efficiency improvements in the minimax algorithm. At first I did some research to grasp the ideas and started with implementing iterative deepening. This does not directly improve the efficiency of the algorithm but I also made it so that instead of the bot searching to a fixed depth, it searches for a fixed time every move (5 seconds for example), and then returned the best move found. This way already the bot can play better in simpler game situations (such as the end game) where there are less possible moves, so the algorithm can search deeper in the given time. Then I ran out of time to work on the project for this week.

The next thing I want to add to the iterative deepening to make it improve performance is move ordering. It is most important to search the principal variation found in the previous iteration as the first path in the next iteration to minimize the search tree. 


Time spent this week: 8 hours