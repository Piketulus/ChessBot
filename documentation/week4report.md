# Week 4 Report

This week I had more time to work on the project and managed reach my goals from last week. I finished up the move generation and tested it, created a position evaluator, and implemented a first version of the minimax algorithm with alpha beta pruning in order to get my bot running. The bot can now be played on lichess against the computer or another lichess user.

A big part of this week was spent on performance testing, to make sure that the movegenerator and minimax algorithms worked correctly and efficiently. After getting the basics done and having some fun playing with the bot on lichess, I started already started realizing ways in which I can improve the efficiency of my bot. Improvements is what I will be working on for the last few weeks of the course.

One thing that I have been working on implementing for a while now is an undo move function in my chess board. I got very close to it working perfectly this week, but it still causes some bugs when generating moves at higher depths. Still, I was able to see that if I can use an undo function it greatly improves the speed of my bot. Another thing I considered was redoing the implementation of the board to use bitboards instead of a 2d array, this would be more efficient and allow better continuity with the MoveGenerator and PositionEvaluator classes. This is something I will definitely consider doing in the following weeks to see if there is a significant improvement in performance. I also believe there are tweaks to be made to the minimax algorithm in terms of the pruning and evaluation.

There is also now a user manual and a testing document.


Time spent this week: 15 hours