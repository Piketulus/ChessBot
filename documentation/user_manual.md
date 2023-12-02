## User Manual

The project is made to work with the lichess.org chess interface. To play the bot against the lichess computer you will need a lichess bot account and lichess API-token at minimum. You can also play against the bot yourself on lichess, this can be done on an incognito browser window for example, or another lichess account.

The bot can play as both white and black, and currently the max time per move is fixed at 5 seconds which allows the bot to play at a fast speed so there is no need to wait forever for the bot to make a move.

### Setting up you lichess account

*Excerpt from: https://github.com/TiraLabra/chess/blob/master/documentation/Beginners_guide.md*

1. Register to [Lichess](https://lichess.org/signup). Here, you need to agree to the four given points.

**Note:** If you have played even a single game as a human player and now want to try playing as a bot, you will have to DELETE your [token](https://lichess.org/account/oauth/token) and also close your account to register it as a bot.

2. Create [New personal API access token](https://lichess.org/account/oauth/token/create) and choose all the scopes.

**Note:** Never put your personal token on github or other public files, but store it somewhere as you will not see it again.

**Note:** if you copy your access token to a Word document (or another similar text processing document), and from there to your chess bot program, a hidden character may be added in the end of your token. If your token does not work, check with backspace if a hidden character was added.

3. Upgrade your account to a bot with the following command:

    $ curl -d '' https<span></span>://lichess.org/api/bot/account/upgrade -H "Authorization: Bearer INSERT YOUR TOKEN HERE"

### Starting up the bot

1. Clone the project and run in the root directory: `./gradlew build`

2. 1. To play bot against bot: Go on lichess with the bot account and select *Play with the computer*, choose the computer level (the bot will probably have a hard time beating anything 6 or above), choose side and start the game.

   2. To play against the bot yourself: Go on lichess with your bot account and select *Play with a friend*, choose side and start game. A link will be generated, copy the link and open it in an incognito browser window (so not signed in to lichess) or another device on another lichess account. You can now play against the bot.

3. After starting a game, run in the project root directory: `./gradlew run --args="--lichess --token=YOUR_LICHESS_TOKEN" `

4. The bot will now start playing. If you want to stop the bot, press `ctrl + c` in the terminal.

**Note:** Rematching and starting new games should work fine without quitting or restarting the bot, but in the case no moves are being made or there is some error, restarting the bot should fix it. The bot can continue playing from any position.