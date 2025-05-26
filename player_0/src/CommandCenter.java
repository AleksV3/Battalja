import java.util.ArrayList;

public class CommandCenter {
    //lastnosti
    public int x;
    public int y;
    public String myColor;

    ArrayList<Planet> myPlanets;
    ArrayList<Planet> neutralPlanets;
    ArrayList<Planet> enemyPlanets;
    ArrayList<Planet> myTemmatePlanets;

    ArrayList<Fleet> myFleets;
    ArrayList<Fleet> enemyFleets;
    ArrayList<Fleet> myTemmateFleets;

    public CommandCenter(int x, int y, String color) {
        this.x = x;
        this.y = y;
        this.myColor = color;

        myPlanets = new ArrayList<>();
        neutralPlanets = new ArrayList<>();
        enemyPlanets = new ArrayList<>();
        myTemmatePlanets = new ArrayList<>();

        myFleets = new ArrayList<>();
        enemyFleets = new ArrayList<>();
        myTemmateFleets = new ArrayList<>();;

    }

    public String attack(int turn) {
        Strategy strategy = new Strategy(myPlanets,enemyPlanets,
                neutralPlanets,myTemmatePlanets,
                myFleets,myTemmateFleets,enemyFleets,x,y);
        return strategy.executeStrategy(turn);
    }
    public void addPlanet(String[] token) {
        if (token == null) return;

        try {
            Planet p = new Planet(token);
            String planetColor = p.getColor();

            if (planetColor != null && planetColor.equals(myColor)) {
                myPlanets.add(p);
            } else if (planetColor != null && (
                    (myColor.equals("green") && "yellow".equals(planetColor)) ||
                            (myColor.equals("yellow") && "green".equals(planetColor)) ||
                            (myColor.equals("cyan") && "blue".equals(planetColor)) ||
                            (myColor.equals("blue") && "cyan".equals(planetColor)))){
                myTemmatePlanets.add(p);
            } else if ("gray".equals(planetColor)) {
                neutralPlanets.add(p);
            } else if (planetColor != null) {
                enemyPlanets.add(p);
            }
        } catch (Exception e) {
            System.err.println("Error adding planet: " + e.getMessage());
        }
    }

    public void addFleets(String[] token) {
        Fleet f = new Fleet(token);
        String fleetColor = f.getColor();

        if (fleetColor != null && fleetColor.equals(myColor)) {
            myFleets.add(f);
        } else if (
                (myColor.equals("green") && "yellow".equals(fleetColor)) ||
                        (myColor.equals("yellow") && "green".equals(fleetColor)) ||
                        (myColor.equals("cyan") && "blue".equals(fleetColor)) ||
                        (myColor.equals("blue") && "cyan".equals(fleetColor))
        ) {
            myTemmateFleets.add(f);
        } else {
            enemyFleets.add(f);
        }
    }

    public synchronized void resetState() {
        if (myPlanets != null) myPlanets.clear();
        if (neutralPlanets != null) neutralPlanets.clear();
        if (enemyPlanets != null) enemyPlanets.clear();
        if (myTemmatePlanets != null) myTemmatePlanets.clear();
        if (myFleets != null) myFleets.clear();
        if (enemyFleets != null) enemyFleets.clear();
        if (myTemmateFleets != null) myTemmateFleets.clear();
     }
}