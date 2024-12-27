import java.io.*;
import java.net.*;
import java.util.*;

// server class to that manages client connections, room assignments, and game sessions
//this server supports multiple rooms anf uses multithreading to handle multiple games at the same time
public class Server {
    private static final int PORT = 12345;
    private static List<ClientHandler> clients = new ArrayList<>();
    private static List<Room> rooms = new ArrayList<>();
    private static ServerSocket serverSocket;
    private static boolean isRunning = true;

    public static void main(String[] args) {
        System.out.println("Server started, waiting for clients to connect...");

        // Initialize rooms for sessions
        rooms.add(new Room("Room 1"));
        rooms.add(new Room("Room 2"));
        rooms.add(new Room("Room 3"));

        try {
            serverSocket = new ServerSocket(PORT);
            new Thread(Server::listenForShutdownCommand).start();

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            if (isRunning) {
                e.printStackTrace();
            }
        } finally {
            shutdown();
        }
    }

    //shutdown command from server console to close the server
    private static void listenForShutdownCommand() {
        try (BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {
            while (isRunning) {
                String command = consoleReader.readLine();
                if ("shutdown".equalsIgnoreCase(command.trim())) {
                    isRunning = false;
                    shutdown();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
// shuts down the server
    private static void shutdown() {
        try {
            isRunning = false;
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            for (ClientHandler client : clients) {
                client.sendMessage("Server is shutting down. Disconnecting...");
                client.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // handles client connection and manages game room interactions
    public static class ClientHandler implements Runnable {
        private PrintWriter out;
        private BufferedReader in;
        private Socket socket;
        private Room assignedRoom;
        private String playerName;

        //initializing client comms streams
        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                this.out = new PrintWriter(socket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //sends message to client that are connected
        public void sendMessage(String message) {
            out.println(message);
        }

        //disconnects client
        public void disconnect() {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //gets and stores player name from user input
        public String getPlayerName() {
            return playerName;
        }

        public BufferedReader getIn() {
            return in;
        }

        //primary method to take care of initial interactions(i.e. welcome msg, name, room list, etc.)
        @Override
        public void run() {
            try {
                // Prompt for non-empty name
                while (true) {
                    out.println("Please enter your name:");
                    playerName = in.readLine();
                    if (playerName != null && !playerName.trim().isEmpty()) {
                        System.out.println("Player connected with name: " + playerName);
                        break;
                    }
                    out.println("Name cannot be empty. Please enter a valid name.");
                }

                //lists available rooms (limited to 3 rooms)
                while (assignedRoom == null) {
                    out.println("Available rooms:");
                    for (Room room : rooms) {
                        //displays full/open status of room
                        out.println(room.getRoomName() + (room.isFull() ? " (full)" : " (open)"));
                    }
                    out.println("END");
                    //prompts user to choose a room
                    out.println("Enter the room name to join:");
                    String chosenRoomName = in.readLine();
                    for (Room room : rooms) {
                        //ignore case and space for room name input
                        if (room.getRoomName().equalsIgnoreCase(chosenRoomName.trim()) && !room.isFull()) {
                            assignedRoom = room;
                            //adds player to the chosen room
                            room.addPlayer(this);
                            //confirmation msg to show player has joined
                            room.broadcastMessage(playerName + " has joined the room.");
                            //prints to the server where the player is
                            System.out.println(playerName + " joined " + room.getRoomName());
                            //checks if the room is full (2 players)
                            if (room.isFull()) {
                                //message confirming game will start
                                room.broadcastMessage("Both players have joined. Starting the game...");
                                //prints to server to update what is happening in the room
                                System.out.println("Starting game in " + room.getRoomName());
                                //starts the game in the room
                                startGame(room);
                            } else {
                                //if there is only 1 player, the message is shown to let the play know
                                out.println("Waiting for the other player to join...");
                            }
                            break;
                        }
                    }

                    //if the room capacity is maxed out or the room name is not on the list
                    if (assignedRoom == null) {
                        out.println("Invalid room name or room is full. Please try again.");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //method to start the game in the proper room
        public void startGame(Room room) {
            try {
                //asking players to set their 5 letter initial word that the other player will play to guess
                for (int i = 0; i < 2; i++) {
                    ClientHandler currentPlayer = room.getPlayers().get(i);
                    currentPlayer.out.println("It's your turn to set your secret 5 letter word:");
                    System.out.println("Prompting " + currentPlayer.playerName + " to set their word.");

                    //option for players to leave/exit the game mid-turn
                    while (true) {
                        String word = currentPlayer.in.readLine();
                        if (word == null || word.equalsIgnoreCase("exit")) {
                            currentPlayer.out.println("You have exited the game.");
                            room.broadcastMessage(currentPlayer.playerName + " has left the game.");
                            room.removePlayer(currentPlayer);
                            System.out.println(currentPlayer.playerName + " exited the game.");
                            return;
                        }

                        //checks length of the word to match 5 letter and sets it for hte other player to guess
                        if (word.length() == 5 && word.matches("[a-zA-Z]+")) {
                            room.getSecretWords()[i] = word.toLowerCase();
                            currentPlayer.out.println("Your secret word has been set.");
                            System.out.println(currentPlayer.playerName + " set their secret word.");
                            break;
                        } else {
                            //error if the length of the word is not 5
                            currentPlayer.out.println("Invalid word! Please enter a 5-letter word:");
                        }
                    }
                }

                //once both have chosen their 5-letter words, both are aware that the guessing game has begun
                room.broadcastMessage("Both words have been set. Let the guessing begin!");
                System.out.println("Game started in " + room.getRoomName());
                takeTurns(room);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //handles players taking turns
        public void takeTurns(Room room) {
            try {
                while (true) {
                    // identify the current player and the opponent based on the turn index
                    ClientHandler currentPlayer = room.getPlayers().get(room.getCurrentPlayerIndex());
                    ClientHandler otherPlayer = room.getPlayers().get(1 - room.getCurrentPlayerIndex());

                    // prompt the current player to enter their guess or type "exit" to leave
                    currentPlayer.out.println("Your turn to guess or type 'exit' to leave:");
                    String guess = currentPlayer.in.readLine();

                    // check if the player wants to exit the game
                    if (guess == null || guess.equalsIgnoreCase("exit")) {
                        // notify both players that the game has ended due to player exit
                        currentPlayer.out.println("You have exited the game.");
                        otherPlayer.out.println(currentPlayer.playerName + " has left the game.");

                        // remove the current player from the room and handle any remaining players
                        room.removePlayer(currentPlayer);
                        System.out.println(currentPlayer.playerName + " exited the game.");
                        break;
                    }

                    System.out.println(currentPlayer.getPlayerName() + " guessed: " + guess);

                    // check if the guess matches the opponent's secret word
                    if (guess.equals(room.getSecretWords()[1 - room.getCurrentPlayerIndex()])) {
                        room.broadcastMessage(currentPlayer.playerName + " guessed the word correctly! Game over.");

                        // remove both players from the room to reset the game state
                        room.removePlayer(currentPlayer);
                        room.removePlayer(otherPlayer);
                        System.out.println("Game over in " + room.getRoomName());
                        break;
                    } else {
                        // generate feedback on the player's guess compared to opponent's word
                        String feedback = generateFeedback(room.getSecretWords()[1 - room.getCurrentPlayerIndex()], guess);
                        // send feedback to the player
                        currentPlayer.out.println("Feedback: " + feedback);
                        // switch the turn to the other player
                        room.switchTurn();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //generates feedback using colours (similar to wordle)
        private String generateFeedback(String secretWord, String guess) {
            StringBuilder feedback = new StringBuilder();
            //using ANSI colours for the coloured feedback
            String green = "\033[32m";
            String yellow = "\033[33m";
            String grey = "\033[90m";
            String reset = "\033[0m";

            for (int i = 0; i < 5; i++) {
                if (guess.charAt(i) == secretWord.charAt(i)) {
                    //checking to see correct letter in correct position
                    feedback.append(green).append(guess.charAt(i)).append(reset).append(" ");
                } else if (secretWord.contains(String.valueOf(guess.charAt(i)))) {
                    //checking to see correct letter in wrong position
                    feedback.append(yellow).append(guess.charAt(i)).append(reset).append(" ");
                } else {
                    //checking to see wrong letter in wrong position
                    feedback.append(grey).append(guess.charAt(i)).append(reset).append(" ");
                }
            }
            return feedback.toString();
        }
    }
}
