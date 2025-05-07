import java.util.ArrayList;

public class CommandCenter {
    //lastnosti
    public int x;
    public int y;
    public String myColor;
    private int turnCounter = 0;


    //saving planets
    ArrayList<Planet> myPlanets;
    ArrayList<Planet> neutralPlanets;
    ArrayList<Planet> enemyPlanets;
    ArrayList<Planet> myTemmatePlanets;
    //saving fleets
    ArrayList<Fleet> myFleets;
    ArrayList<Fleet> enemyFleets;
    ArrayList<Fleet> myTemmateFleets;

    ArrayList<Planet> defendingPlanets;

    Defence d;

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

        defendingPlanets = new ArrayList<>();

        d = new Defence(myPlanets,myTemmatePlanets,enemyPlanets,myFleets,myTemmateFleets,enemyFleets);

    }
    public String defence() {
        String defence = "";
        // Game phase parameters
        boolean earlyGame = isEarlyGame();
        int defenseRange = earlyGame ? 30 : 50;

        // Get planets that need defending
        defendingPlanets = d.defending(defenseRange);
        myPlanets.removeAll(defendingPlanets);

        for (Planet def : defendingPlanets) {
            ArrayList<Planet> potentialDefenders = new ArrayList<>(myPlanets);
            int threatLevel = d.underAttack(def, defenseRange);
            int remainingThreat = threatLevel;

            // Early game: try harder to defend
            int maxAttempts = earlyGame ? 3 : 1;
            int attempts = 0;

            while (remainingThreat > 0 && attempts < maxAttempts && !potentialDefenders.isEmpty()) {
                Planet defender = distenc(potentialDefenders, def);

                // Only send fleets if we can make a difference
                if (defender.getFleetSize() > 10) {  // Always keep at least 10 ship
                    int shipsToSend = Math.min(defender.getFleetSize() - 1, remainingThreat);
                    if (shipsToSend > 0) {
                        defence += defender.attackPlanet(def, shipsToSend);
                        remainingThreat -= shipsToSend;
                        System.out.println((earlyGame ? "EARLY " : "LATE ") +
                                "DEFENCE: " + def.getName() +
                                " from " + defender.getName() +
                                " (" + shipsToSend + " ships)");
                    }
                }

                potentialDefenders.remove(defender);
                attempts++;
            }

            // If still threatened in early game, send emergency help from closest planet
            if (remainingThreat > 0 && earlyGame && !myPlanets.isEmpty()) {
                Planet lastResort = distenc(myPlanets, def);
                if (lastResort.getFleetSize() > 1) {
                    int emergencyShips = Math.min(lastResort.getFleetSize() - 1, remainingThreat);
                    defence += lastResort.attackPlanet(def, emergencyShips);
                    System.out.println("EMERG DEF: " + emergencyShips +
                            " ships from " + lastResort.getName());
                }
            }
        }
        return defence;
    }

    private boolean isEarlyGame() {
        return turnCounter < 30;
    }

    public ArrayList<Planet> closestPlanetBasedOnSize(ArrayList<Planet> otherPlanets, int minSize, int maxSize) {
        ArrayList<Planet> closestMidPlanets = new ArrayList<>();
        if (otherPlanets == null) return closestMidPlanets;

        for (Planet planet : otherPlanets) {
            if (planet != null &&
                    planet.getPlanetSize() >= minSize &&
                    planet.getPlanetSize() <= maxSize) {
                closestMidPlanets.add(planet);
            }
        }
        return closestMidPlanets;
    }

   /** public String attack(){
        if ((myPlanets == null || myPlanets.isEmpty()) ||
                (enemyPlanets == null && neutralPlanets == null)) {
            return "";
        }

        if(myPlanets.size() < 4 && neutralPlanets != null && !neutralPlanets.isEmpty()) {
            ArrayList<Planet> tempPlanets = closestPlanetBasedOnSize(neutralPlanets,4,8);
            if(tempPlanets == null || tempPlanets.isEmpty()) {
                return NapadNajblizjega(neutralPlanets);
            } else {
                return NapadNajblizjega(tempPlanets);
            }
        } else if ((myPlanets.size() > 4) && (neutralPlanets != null && neutralPlanets.size() > 3)) {
            ArrayList<Planet> tempPlanets = closestPlanetBasedOnSize(neutralPlanets,4,10);
            if(tempPlanets == null || tempPlanets.isEmpty()) {
                return NapadNajblizjega(neutralPlanets);
            } else {
                return NapadNajblizjega(tempPlanets);
            }
        } else if(enemyPlanets != null && enemyPlanets.size() < 6){
            ArrayList<Planet> tempPlanets = closestPlanetBasedOnSize(enemyPlanets,4,8);
            if(tempPlanets == null || tempPlanets.isEmpty()) {
                return NapadNajblizjega(enemyPlanets);
            } else {
                return NapadNajblizjega(tempPlanets);
            }
        } else if (enemyPlanets != null) {
            return NapadNajblizjega(enemyPlanets);
        }
        return "";
    }
**/
   public String attack() {
       if (myPlanets == null || myPlanets.isEmpty()) return "";
       String attackCommands = "";
       ArrayList<Planet> potentialTargets = new ArrayList<>();
       if (neutralPlanets != null) potentialTargets.addAll(neutralPlanets);
       if (enemyPlanets != null) potentialTargets.addAll(enemyPlanets);
       if(!enemyFleets.isEmpty()){
           turnCounter = enemyFleets.get(0).getTurn();
       }else if(!myFleets.isEmpty()){
           turnCounter = myFleets.get(0).getTurn();
       }else if (!myTemmateFleets.isEmpty()){
           turnCounter = myTemmateFleets.get(0).getTurn();
       }else {
           turnCounter = 0;
       }

       if (potentialTargets.isEmpty()) return "";

       // Early game strategy (first 50 turns)
       boolean earlyGame = myPlanets.size() < 3 || turnCounter < 30;

       for (Planet myPlanet : myPlanets) {
           int availableForces = (int)(myPlanet.getFleetSize() * (earlyGame ? 0.7 : 0.5));
           if (availableForces < 10) continue;

           Planet target = findBestTarget(potentialTargets, myPlanet, earlyGame);
           if (target != null) {
               int forcesToSend = Math.min(availableForces,
                       calculateRequiredForces(target, earlyGame));
               attackCommands += myPlanet.attackPlanet(target, forcesToSend);
           }
       }

       return attackCommands;
   }

    private Planet findBestTarget(ArrayList<Planet> targets, Planet source, boolean earlyGame) {
        if (targets == null || targets.isEmpty()) return null;

        Planet bestTarget = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Planet target : targets) {
            double distance = Math.hypot(
                    source.getX() - target.getX(),
                    source.getY() - target.getY());

            double score = 0;
            if (earlyGame) {
                score = (target.getPlanetSize() * 100) /
                        (distance * (target.getFleetSize() + 1));
            } else {
                score = (target.getPlanetSize() * 50 +
                        (target.getColor().equals("gray") ? 100 : 0)) /
                        (distance * (target.getFleetSize() + 1));
            }

            if (score > bestScore) {
                bestScore = score;
                bestTarget = target;
            }
        }

        return bestTarget;
    }

    private int calculateRequiredForces(Planet target, boolean earlyGame) {
        int baseForces = target.getFleetSize() + 10;
        if (earlyGame) {
            return (int)(baseForces * 1.2);
        }
        return baseForces;
    }

    public String NapadNajblizjega(ArrayList<Planet> planeti) {
        if (planeti == null || myPlanets == null) return "";

        String napad = "";
        for (Planet a : myPlanets) {
            if (a == null) continue;

            Planet b = distenc(planeti, a);
            if (b != null) {
                double growth = fleetGrowthRate(a);
                double F_max = 500 * a.getPlanetSize() + 5000;


            }
        }
        return napad;
    }

    public double fleetGrowthRate(Planet p) {
        double F = p.getFleetSize();
        double size = p.getPlanetSize();
        double F_max = 500 * size + 5000;

        double F_capacity;
        if (F < F_max) {
            F_capacity = 1 - (F / F_max);
        } else {
            F_capacity = 1e-8 * size;
        }
        return (1.0 / 10.0) * (size / 10.0) * F * F_capacity;
    }



    public Planet distenc(ArrayList<Planet> planeti, Planet p) {
        if (planeti == null || p == null) return null;

        Planet closest = null;
        double minDistance = Double.MAX_VALUE;

        for (Planet b : planeti) {
            if (b == null || p.equals(b)) continue;

            double distance = Math.pow(p.getX() - b.getX(), 2) + Math.pow(p.getY() - b.getY(), 2);
            if (distance < minDistance) {
                minDistance = distance;
                closest = b;
            }
        }
        return closest;
    }

    public boolean whoIsCloser(ArrayList<Planet> planeti, ArrayList<Planet> planeti2, Planet p) {
        if (p == null || planeti == null || planeti2 == null) return false;

        Planet a = distenc(planeti, p);
        Planet b = distenc(planeti2, p);
        if (a == null || b == null) return false;

        double dist1 = Math.pow(p.getX() - a.getX(), 2) + Math.pow(p.getY() - a.getY(), 2);
        double dist2 = Math.pow(p.getX() - b.getX(), 2) + Math.pow(p.getY() - b.getY(), 2);
        return dist1 > dist2;
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