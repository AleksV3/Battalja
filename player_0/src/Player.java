import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Player {

    public static CommandCenter commandCenter;
    public static int universeWidth;
    public static int universeHeight;
    public static String myColor;
    public static ArrayList<String> otherPlayersPlanets = new ArrayList<>();
    public static ArrayList<String> myTemmatePlanets = new ArrayList<>();
    public static ArrayList<String> myPlanets = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        // Wait for initial /U line to set up universe and player color
        String line;
        while (!(line = stdin.readLine()).startsWith("/U")) {}
        String[] tokens = line.split(" ");
        universeWidth = Integer.parseInt(tokens[1]);
        universeHeight = Integer.parseInt(tokens[2]);
        myColor = tokens[3];

        // Initialize command center once
        commandCenter = new CommandCenter(universeWidth, universeHeight, myColor);

        // Main game loop
        while (true) {
            getGameState(stdin);

            String attack = commandCenter.attack();
            System.out.println(attack);
            System.out.println("/M Hello");

            for (String myPlanet : myPlanets) {
                System.out.println("/P " + myPlanet + " My planet " + myPlanet);
            }

            System.out.println("/E");
        }
    }

    public static void getGameState(BufferedReader stdin) throws IOException {
        // Clear lists
        myPlanets.clear();
        myTemmatePlanets.clear();
        otherPlayersPlanets.clear();

        // Reset CommandCenter internal lists
        commandCenter.resetState();

        String line;
        while (!(line = stdin.readLine()).equals("/S")) {
            String[] tokens = line.split(" ");
            char command = line.charAt(1);

            switch (command) {
                case 'P':
                    commandCenter.addPlanet(tokens);
                    break;
                case 'F':
                    commandCenter.addFleets(tokens);
                    break;
            }
        }
    }
}
