import java.io.*;
import java.net.*;

//client class to connect clients to the server, join game rooms, ad interact with the game
//allows players to set, guess nad receive feedback on words
public class Client {
    public static void main(String[] args) {
        //server conenction + port
        String serverAddress = "localhost";
        int port = 12345;

        try (Socket socket = new Socket(serverAddress, port)) {
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));


            //ensures the messages sent by the server is in blue for readability
            //helps sepearate input and output
            String blue = "\033[34m";
            String reset = "\033[0m";
            System.out.println("Connected to server.");

            displayHelp(blue, reset);

            while (true) {
                String serverResponse = input.readLine();
                if (serverResponse == null) {
                    System.out.println("Server disconnected. Exiting...");
                    break;
                }

                System.out.println(blue + serverResponse + reset);

                //  prompts user for input when necessary (following cases)
                if (serverResponse.contains("Please enter your name") ||
                        serverResponse.contains("Enter the room name to join:") ||
                        serverResponse.contains("It's your turn to set your secret 5 letter word:") ||
                        serverResponse.contains("Your turn to guess or type 'exit' to leave:") ||
                        serverResponse.contains("Type 'wait' to wait for a new player or 'exit' to leave:")) {

                    String inputText = console.readLine();
                    out.println(inputText);

                    if (inputText.equalsIgnoreCase("exit")) {
                        System.out.println("Exiting the game...");
                        break;
                    }
                }
            }

            System.out.println("Closing connection...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //game menu, game instructions to help new players understand how to play the game
    public static void displayHelp(String blue, String reset) {
        System.out.println(blue + "Welcome!");
        System.out.println("Game Instructions:");
        System.out.println("1. Enter your name when prompted.");
        System.out.println("2. Join a room by entering the room name.");
        System.out.println("3. Each player sets a 5-letter secret word.");
        System.out.println("4. Take turns guessing the other player's word.");
        System.out.println("5. Color feedback for your guesses:");
        System.out.println("   - Green: Correct letter in the correct position.");
        System.out.println("   - Yellow: Correct letter, but in the wrong position.");
        System.out.println("   - Grey: Letter is not in the word.");
        System.out.println("6. Type 'exit' at any time to leave the game." + reset);
    }
}
