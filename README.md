# Multiplayer Word-Guessing Game

A Java-based multiplayer word-guessing game where players join rooms to play one-on-one guessing games. The game includes color-coded feedback, flexible room management, and real-time interaction.

## Features

- **Room-Based Multiplayer**: Players join specific rooms for two-player game sessions. The server can host multiple rooms for concurrent games.
- **Turn-Based Guessing with Feedback**: Players alternate turns to guess each other’s secret word with color-coded feedback:
  - **Green**: Correct letter in the correct position.
  - **Yellow**: Correct letter but in the wrong position.
  - **Grey**: Letter not in the word.
- **Exit Flexibility**: Players can exit anytime, and the remaining player can choose to wait or exit.

### Running the Game

Compile and Run:
1. **Server**
- *javac Server.java*
- *java Server localhost*

2. **Client**(open a separate terminal for each player):
- *javac Client.java*
- *java Client 12345*

### How to Play
1. Start the Server: Run the server to create game rooms and await players.
2. Join a Room: Players connect and join a room. Each player sets a 5-letter secret word.
3. Guess and Get Feedback: Players take turns guessing the other’s word, with feedback to help refine guesses.
4. Exit Anytime: Players can type "exit" to leave. The remaining player is given options to wait or exit.

### Project Structure
- **Server.java**: Manages clients, rooms, and game sessions.
- **Client.java**: Client-side interface for players to connect, join rooms, and play.
- **Room.java**: Handles room-specific game logic.

