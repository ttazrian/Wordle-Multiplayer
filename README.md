# 🎮 Wordle-Multiplayer

**Wordle-Multiplayer** is a Java-based multiplayer word-guessing game where players join rooms to play one-on-one games. The game includes color-coded feedback, flexible room management, and real-time interaction.

## 🌟 Features
- **Room-Based Multiplayer:** Players join specific rooms for two-player game sessions. The server can host multiple rooms for concurrent games.
- **Turn-Based Guessing with Feedback:** Players alternate turns to guess each other's secret word with color-coded feedback:
  - 🟩 **Green:** Correct letter in the correct position.
  - 🟨 **Yellow:** Correct letter but wrong position.
  - ⬜ **Grey:** Letter not in the word.
- **Flexible Player Exit:** Players can exit at any time, and the remaining player can wait or leave.

## 🔧 Tech Stack
- Java (Core OOP)
- Java Networking (Sockets)
- Command-line user interface (CLI)

## 🚀 Running the Game

### 1️⃣ Compile
```bash
javac Server.java
javac Client.java
````

### 2️⃣ Start the Server

```bash
java Server localhost
```

### 3️⃣ Start Clients (in separate terminals)

```bash
java Client 12345
```

## 🎲 How to Play

1. **Start the Server:** Run the server to manage game rooms and wait for players.
2. **Join a Room:** Players connect and join a room. Each player sets a 5-letter secret word.
3. **Guess and Get Feedback:** Players take turns guessing the other’s word, with feedback to help refine guesses:

   * 🟩 Green: Correct letter and position.
   * 🟨 Yellow: Correct letter, wrong position.
   * ⬜ Grey: Letter not in the word.
4. **Exit Anytime:** Players can type "exit" to leave the game. The remaining player can wait or exit.

## 📂 Project Structure

```
/ (root)
  Server.java      # Manages clients, rooms, and game sessions
  Client.java      # Client-side logic and user interaction
  Room.java        # Room-specific game logic
  README.md        # Project documentation
  .gitignore       # Exclude unnecessary files (optional)
```

## 🚀 Future Improvements

* Implement a graphical interface (JavaFX or Swing) for improved user experience.
* Add authentication and player profiles.
* Introduce leaderboards and scoring.
* Expand support for more than two players per room.
* Enable web or desktop deployment.

## 📄 License

This project is licensed under the MIT License.

```

