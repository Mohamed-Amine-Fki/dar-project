import java.io.*;
import java.net.*;
import java.util.*;

public class NumberGuessServer {
    private static final int PORT = 12345;
    private static Set<PrintWriter> clients = new HashSet<>();
    private static int secretNumber;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Number Guess Game Server is running...");
            secretNumber = generateSecretNumber();

            Thread consoleThread = new Thread(() -> {
                Scanner consoleScanner = new Scanner(System.in);
                while (true) {
                    String command = consoleScanner.nextLine();
                    if (command.equalsIgnoreCase("/newgame")) {
                        secretNumber = generateSecretNumber();
                        broadcastToClients("New game started! Guess a number between 1 and 100.");
                    } else {
                        System.out.println("Invalid command.");
                    }
                }
            });
            consoleThread.start();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected.");

                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                clients.add(writer);

                Thread clientThread = new Thread(new ClientHandler(clientSocket, writer));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter writer;

        public ClientHandler(Socket socket, PrintWriter writer) {
            this.clientSocket = socket;
            this.writer = writer;
        }

        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                writer.println("Welcome to Guess the Number! Guess a number between 1 and 100.");

                String message;
                while ((message = reader.readLine()) != null) {
                    int guess = Integer.parseInt(message.trim());
                    handleGuess(guess, writer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void handleGuess(int guess, PrintWriter writer) {
        if (guess == secretNumber) {
            writer.println("Congratulations! You guessed the number " + secretNumber + " correctly!");
            broadcastToClients("Game Over! The number was " + secretNumber + ". A new game will start soon.");
            secretNumber = generateSecretNumber();
        } else if (guess < secretNumber) {
            writer.println("Try a higher number!");
        } else {
            writer.println("Try a lower number!");
        }
    }

    private static int generateSecretNumber() {
        return new Random().nextInt(100) + 1;
    }

    private static void broadcastToClients(String message) {
        for (PrintWriter client : clients) {
            client.println(message);
            client.flush();
        }
    }
}
