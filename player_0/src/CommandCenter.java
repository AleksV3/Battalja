import java.util.ArrayList;
import java.util.Random;

public class CommandCenter {
    //lastnosti
    public int x; //pozicija
    public int y;
    public String myColor; //barva mojega igralca

    //saving planets
    ArrayList<Planet> myPlanets;
    ArrayList<Planet> neutralPlanets;
    ArrayList<Planet> enemyPlanets;
    ArrayList<Planet> myTemmatePlanets;
    //saving fleets
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
        myTemmateFleets = new ArrayList<>();
    }
    public String attack(){
        if (!(myPlanets.isEmpty() || enemyPlanets.isEmpty())) {
            if ((neutralPlanets.size() > 0)) {
                return NapadNajblizjega(neutralPlanets);
            } else {
                return NapadNajblizjega(enemyPlanets);
            }
        }
        return "";
    }
    public String fastAttack(ArrayList<Planet> planeti) {
        String napad = ""; //ustvarimo prazen napad
        for (int i = 0; i < myPlanets.size(); i++) {
            Planet a = myPlanets.get(i); //vrne planet na indeksu i
            Planet b = distenc(planeti, i);
            if (myPlanets.get(i).getFleetSize() >= 30) {
                napad += a.attackPlanet(b,0);
            }
        }
        return napad;
    }
    public String NapadNajblizjega(ArrayList<Planet> planeti) {
        String napad = ""; //ustvarimo prazen napad
        for (int i = 0; i < myPlanets.size(); i++) {
            Planet a = myPlanets.get(i); //vrne planet na indeksu i
            Planet b = distenc(planeti, i);
            if (neutralPlanets.size() > 1) {
                    b = distenc(planeti,i);
                    napad += a.attackPlanet(b,0);
            } else {
                napad += a.attackPlanet(b,0);
                }
            }
        System.out.println(napad);
        return napad;
    }
    public Planet distenc(ArrayList<Planet> planeti, int m) {

        Planet a = myPlanets.get(m);
        Planet c = null;
        double max = Double.MAX_VALUE;
        double raz = 0;
        double j = a.getX();
        double h = a.getY();
        for (int i = 0; i < planeti.size(); i++) {
            Planet b = planeti.get(i);
            if (a != b) {
                double s = b.getX();
                double d = b.getY();
                raz = (Math.pow(j - s, 2) + Math.pow(h - d, 2));
                if (raz < max && raz > 0) {
                    max = raz;
                    c = planeti.get(i);
                }
            }
        }
        return c;
    }
    public boolean whoIsCloser(ArrayList<Planet> planeti, ArrayList<Planet> planeti2, int i) {
    Planet a = distenc(planeti, i);
    Planet b = distenc(planeti2, i);
    double x1 = a.getX();
    double y1 = a.getY();
    double x2 = b.getX();
    double y2 = b.getY();
    Planet c = myPlanets.get(i);
    double x3 = c.getX();
    double y3 = c.getY();

    double sum1 = (Math.pow(x3 - x2, 2) + Math.pow(y3 - y2, 2));
    double sum2 = (Math.pow(x3 - x1, 2) + Math.pow(y3 - y1, 2));
    if (sum1 > sum2) {
        return true;
    } else {
        return false;
    }
}
    //planet size, army size, planet color (blue, cyan, green, yellow or null for neutral)
    public void addPlanet(String[] token) {
        Planet p = new Planet(token);
        String planetColor = p.getColor();

        if (planetColor != null && planetColor.equals(myColor)) {
            myPlanets.add(p);
        } else if (
                (myColor.equals("green") && "yellow".equals(planetColor)) ||
                        (myColor.equals("yellow") && "green".equals(planetColor)) ||
                        (myColor.equals("cyan") && "blue".equals(planetColor)) ||
                        (myColor.equals("blue") && "cyan".equals(planetColor))
        ) {
            myTemmatePlanets.add(p);
        } else if (planetColor.equals("gray")) {
            neutralPlanets.add(p);
        } else {
            enemyPlanets.add(p);
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
    public void resetState() {
        myPlanets.clear();
        neutralPlanets.clear();
        enemyPlanets.clear();
        myTemmatePlanets.clear();

        myFleets.clear();
        enemyFleets.clear();
        myTemmateFleets.clear();
    }


}

