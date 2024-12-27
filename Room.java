import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

//class to manage individual game sessions, storing player information and game state
// each room operates independently, allowing separate gameplay for each pair of players
public class Room {
    private String roomName;
    private List<Server.ClientHandler> players;
    private static final int ROOM_CAPACITY = 2;  // Room capacity of 2 players

    // Room-specific game state variables
    private String[] secretWords = new String[2];  // Secret words for both players
    private int currentPlayerIndex = 0;  // Track whose turn it is

    public Room(String name) {
        this.roomName = name;
        this.players = new ArrayList<>();
    }

    public String getRoomName() {
        return roomName;
    }

    // Add a player to the room if it isn't full
    public boolean addPlayer(Server.ClientHandler player) {
        if (players.size() < ROOM_CAPACITY) {
            players.add(player);
            return true;
        } else {
            return false;  // Room is full
        }
    }

    // Get the list of players in the room
    public List<Server.ClientHandler> getPlayers() {
        return players;
    }

    // Check if the room is full
    public boolean isFull() {
        return players.size() == ROOM_CAPACITY;
    }

    // Broadcast a message to all players in the room
    public void broadcastMessage(String message) {
        for (Server.ClientHandler player : players) {
            player.sendMessage(message);
        }
    }

    // Get the secret words for the game
    public String[] getSecretWords() {
        return secretWords;
    }

    // Get which player's turn it currently is
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    // Switch to the next player's turn
    public void switchTurn() {
        currentPlayerIndex = 1 - currentPlayerIndex;  // Switch turn
    }

    // Remove a player from the room and notify the other player
    public void removePlayer(Server.ClientHandler player) {
        players.remove(player);
        broadcastMessage(player.getPlayerName() + " has left the room.");

        // Check if there's still one player left in the room
        if (players.size() == 1) {
            Server.ClientHandler remainingPlayer = players.get(0);
            remainingPlayer.sendMessage("The other player has left the game. Would you like to wait for a new player to join or exit?");
            handleRemainingPlayerChoice(remainingPlayer);  // Prompt remaining player to wait or exit
        }
    }

    // Handle choice for remaining player to wait or exit
    private void handleRemainingPlayerChoice(Server.ClientHandler remainingPlayer) {
        try {
            remainingPlayer.sendMessage("Type 'wait' to wait for a new player or 'exit' to leave:");
            String choice = remainingPlayer.getIn().readLine();

            if (choice.equalsIgnoreCase("exit")) {
                remainingPlayer.sendMessage("You have exited the game.");
                removePlayer(remainingPlayer);
            } else if (choice.equalsIgnoreCase("wait")) {
                remainingPlayer.sendMessage("Waiting for a new player to join...");
                // The room remains open, and the server will allow a new player to join this room.
            } else {
                remainingPlayer.sendMessage("Invalid choice. Please type 'wait' or 'exit'.");
                handleRemainingPlayerChoice(remainingPlayer);  // Retry on invalid input
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Check if the room is empty
    public boolean isEmpty() {
        return players.isEmpty();
    }

    // Check if the game can start (both players have set their words)
    public boolean canStartGame() {
        return secretWords[0] != null && secretWords[1] != null;  // Both players have set their secret words
    }
}
