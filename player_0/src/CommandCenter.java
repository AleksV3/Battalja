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
    ArrayList<Planet> attackingPlanets;
    ArrayList<Planet> feedingPlanets;
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
        attackingPlanets = new ArrayList<>();
        feedingPlanets = new ArrayList<>();

        myFleets = new ArrayList<>();
        enemyFleets = new ArrayList<>();
        myTemmateFleets = new ArrayList<>();

        defendingPlanets = new ArrayList<>();

        //d = new Defence(myPlanets,myTemmatePlanets,enemyPlanets,myFleets,myTemmateFleets,enemyFleets);

    }
    public String defence() {
        String defence = "";
        // Game phase parameters
        getTurnCounter();
        int gameStage = gameStage(turnCounter);
        boolean earlyGame =false;
        if(gameStage == 0){
            earlyGame = true;
        }
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
                Planet defender = planetDistanc(potentialDefenders, def);

                // Only send fleets if we can make a difference
                if (defender.getFleetSize() > 1) {  // Always keep at least 1 ship
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
                Planet lastResort =planetDistanc(myPlanets, def);
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

    public boolean isTeammateCloser(Planet enemy) {
        Planet me = planetDistanc(myPlanets,enemy);
        Planet teammate = planetDistanc(myTemmatePlanets,enemy);
        return getDistance(me,enemy) >= getDistance(teammate,enemy);
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
    public int gameStage(int turn){
        int stage = 0;
        if(turn < 30 || neutralPlanets.size() > 3){
            return stage;
        }else if(turn < 60){
            return stage+1;
        }else {
            return stage+2;
        }
    }

    public String attack(int turn) {
        String attackCommand = "";
        /**
         // Early game: focus on expansion and securing neutral planets
        if (stage == 0) {
            attackCommand += earlyGameAttack();
        }
        // Mid-game: consolidate forces and prepare for late game
        else if (stage == 1) {
            attackCommand += midGameAttack();
        }
        // Late game: full-scale attacks and reinforcement
        else {
            attackCommand += lateGameAttack();
        }

        return attackCommand;**/
    Strategy strategy = new Strategy(myPlanets,enemyPlanets,neutralPlanets,myTemmatePlanets,myFleets,myTemmateFleets,enemyFleets);
    return strategy.executeStrategy(turn);
    }

    private String earlyGameAttack() {
        StringBuilder commands = new StringBuilder();

        if (!neutralPlanets.isEmpty()) {
            for (Planet planet : myPlanets) {
                if (planet.getFleetSize() <= 1) continue; // Don't leave undefended

                boolean isBigPlanet = planet.getPlanetSize() > 0.3;
                boolean hasEnoughFleet = planet.getFleetSize() > (isBigPlanet ? 80 : 0); // apply threshold for big planets

                if (hasEnoughFleet) {
                    Planet target = findBestNeutralTarget(planet);
                    if (target != null) {
                        int requiredShips = target.getFleetSize() + 1;
                        if (planet.getFleetSize() > requiredShips) {
                            int shipsToSend = Math.min(planet.getFleetSize() - 1, requiredShips);
                            commands.append(planet.attackPlanet(target, shipsToSend));
                        }
                    }
                }
            }
        } else if (!enemyPlanets.isEmpty()) {
            for (Planet planet : myPlanets) {
                if (planet.getFleetSize() <= 10) continue;

                Planet target = findBestEnemyTarget(planet, 4, 6); // small enemy planets
                if (target != null && !isTeammateCloser(target)) {
                    int shipsToSend = Math.min(planet.getFleetSize() - 5, 15); // Conservative attacks
                    commands.append(planet.attackPlanet(target, shipsToSend));
                }
            }
        }

        return commands.toString();
    }


    private String midGameAttack() {
        StringBuilder commands = new StringBuilder();

        if(myPlanets.size() > 1) {
            for (Planet source : myPlanets) {
                if (source.getFleetSize() <= 1) continue;

                Planet feedTarget = findBestFeedPlanet(source);
                if (feedTarget != null && feedTarget != source) {
                    // Send excess ships, but keep some defense
                    int excessShips = source.getFleetSize() - 5;
                    if (excessShips > 0) {
                        commands.append(source.attackPlanet(feedTarget, excessShips));
                        myPlanets.remove(feedTarget);
                    }
                }
            }
        }

        // 2. Opportunistic attacks on weak enemy planets
        for (Planet source : myPlanets) {
            if (source.getFleetSize() <= 15) continue;

            Planet target = findBestEnemyTarget(source, 4, 8);
            if (target != null && !isTeammateCloser(target)) {
                int enemyStrength = target.getFleetSize() +
                        attackingFleets(target, enemyFleets) -
                        attackingFleets(target, myFleets) -
                        attackingFleets(target, myTemmateFleets);

                if (enemyStrength < 0) enemyStrength = 0;

                int shipsToSend = Math.min(source.getFleetSize() - 5, enemyStrength + 5);
                if (shipsToSend > 0) {
                    commands.append(source.attackPlanet(target, shipsToSend));
                }
            }
        }

        return commands.toString();
    }

    private String lateGameAttack() {
        StringBuilder commands = new StringBuilder();

        // Classify our planets into roles
        classifyPlanets();

        // 1. Attack with dedicated attack planets
        for (Planet attacker : attackingPlanets) {
            if (attacker.getFleetSize() <= 10) continue;

            Planet target = findBestEnemyTarget(attacker, 0, 15); // Any enemy planet
            if (target != null) {
                int enemyStrength = target.getFleetSize() +
                        attackingFleets(target, enemyFleets) -
                        attackingFleets(target, myFleets) -
                        attackingFleets(target, myTemmateFleets);

                if (enemyStrength < 0) enemyStrength = 0;

                int shipsToSend = Math.min(attacker.getFleetSize() - 5, enemyStrength + 10);
                if (shipsToSend > 0) {
                    commands.append(attacker.attackPlanet(target, shipsToSend));
                }
            }
        }

        // 2. Continue feeding our large planets
        for (Planet feeder : feedingPlanets) {
            if (feeder.getFleetSize() <= 1) continue;

            Planet feedTarget = findBestFeedPlanet(feeder);
            if (feedTarget != null && feedTarget != feeder) {
                int excessShips = feeder.getFleetSize() - 5;
                if (excessShips > 0) {
                    commands.append(feeder.attackPlanet(feedTarget, excessShips));
                }
            }
        }

        return commands.toString();
    }

    private void classifyPlanets() {
        attackingPlanets.clear();
        feedingPlanets.clear();

        for (Planet planet : myPlanets) {
            double maxFleet = 500 * planet.getPlanetSize() + 5000;

            // Large planets that are nearly full become attackers
            if (planet.getPlanetSize() >= 6 && planet.getFleetSize() >= 0.8 * maxFleet) {
                attackingPlanets.add(planet);
            }
            // Medium planets become feeders
            else if (planet.getPlanetSize() >= 4) {
                feedingPlanets.add(planet);
            }
        }
    }

    private Planet findBestNeutralTarget(Planet source) {
        if (neutralPlanets.isEmpty()) return null;

        Planet bestTarget = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Planet target : neutralPlanets) {
            if (!isPlanetMine(target)) {
                double distance = getDistance(source, target);
                double value = target.getPlanetSize() * 100;
                double cost = target.getFleetSize() + 1;

                // Score favors high-value, low-cost, nearby targets
                double score = value / (distance);

                if (score > bestScore) {
                    bestScore = score;
                    bestTarget = target;
                }
            }else {
               return findBestEnemyTarget(source,0,1);
            }
        }

        return bestTarget;
    }

    private Planet findBestEnemyTarget(Planet source, int minSize, int maxSize) {
        if (enemyPlanets.isEmpty()) return null;

        Planet bestTarget = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Planet target : enemyPlanets) {
            // Filter by size if specified
            if ((minSize > 0 && target.getPlanetSize() < minSize) ||
                    (maxSize > 0 && target.getPlanetSize() > maxSize)) {
                continue;
            }

            if (!isPlanetMine(target)) {
                double distance = getDistance(source, target);
                double value = target.getPlanetSize() * 100;

                // Consider incoming fleets
                int enemyStrength = target.getFleetSize() +
                        attackingFleets(target, enemyFleets) -
                        attackingFleets(target, myFleets) -
                        attackingFleets(target, myTemmateFleets);
                if (enemyStrength < 0) enemyStrength = 0;

                double cost = enemyStrength + 5; // Add buffer

                // Score favors high-value, low-cost, nearby targets
                double score = value / (distance);

                if (score > bestScore) {
                    bestScore = score;
                    bestTarget = target;
                }
             }
        }
        return bestTarget;
    }
    public boolean isPlanetMine(Planet planet){
        int attack = attackingFleets(planet,myTemmateFleets) + attackingFleets(planet,myFleets) -
                attackingFleets(planet,enemyFleets);
        return attack > 0;
    }

    private int fleetsNeeded(Planet planet) {
        int fleetSize = (planet.getFleetSize() + attackingFleets(planet,enemyFleets)) -
                (attackingFleets(planet,myFleets) + attackingFleets(planet, myTemmateFleets));
        if (fleetSize <= 0){
            return 0;
        }
        return fleetSize;
    }
    public int attackingFleets(Planet planet, ArrayList<Fleet> fleets){
        int sum = 0;
        for(Fleet fleet : fleets){
            if(fleet.getDefendingPlanet().equals(planet.getName())){
                sum += fleet.getSize();
            }
        }
        return sum;
    }

    private Planet findBestFeedPlanet(Planet fromPlanet) {
        Planet bestFeed = null;
        double bestScore = -1;

        for (Planet planet : myPlanets) {
            if (planet == null || planet == fromPlanet) continue; // skip self
            double maxFleet = 500 * planet.getPlanetSize() + 5000;

            if (planet.getFleetSize() >= 0.9 * maxFleet) {
                attackingPlanets.add(planet); // ready to attack, not to feed
                continue;
            }

            double growth = fleetGrowthRate(planet);
            double distanceSq = Math.pow(planet.getX() - fromPlanet.getX(), 2) + Math.pow(planet.getY() - fromPlanet.getY(), 2);

            if (distanceSq == 0) continue; // skip self or broken case

            double efficiency = growth / distanceSq;

            if (efficiency > bestScore) {
                bestScore = efficiency;
                bestFeed = planet;
            }
        }

        if (bestFeed != null) attackingPlanets.add(bestFeed);
        return bestFeed;
    }

    private Planet findBestTarget(ArrayList<Planet> targets, Planet source) {
        if (targets == null || targets.isEmpty()) return null;
        Planet bestTarget = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (Planet target : targets) {
            if(!isPlanetMine(target)){
                double distance = getDistance(source, target);
                double score = 0;
                score = (target.getPlanetSize() * 100) /
                         (distance / (target.getFleetSize() + 1));
                if (score > bestScore) {
                    bestScore = score;
                    bestTarget = target;
                }
            }
        }
        if(bestTarget == null){
            return myPlanets.get(0);
        }
        if(!myTemmatePlanets.isEmpty() && !myPlanets.isEmpty()) {
            if ((!isTeammateCloser(bestTarget) && targets.size() != 1)) {
                return bestTarget;
            } else {
                if (targets.size() == 1) {
                    return bestTarget;
                } else {
                    targets.remove(bestTarget);
                    return findBestTarget(targets, source);
                }
            }
        }else {
            if (targets.size() == 1) {
                return bestTarget;
            } else {
                targets.remove(bestTarget);
                return findBestTarget(targets, source);
            }
        }
   }

    private static double getDistance(Planet source, Planet target) {
        return Math.hypot(
                source.getX() - target.getX(),
                source.getY() - target.getY());
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

            Planet b = planetDistanc(planeti, a);
            if (b != null) {
                double growth = fleetGrowthRate(a);
                double F_max = 500 * a.getPlanetSize() + 5000;


            }
        }
        return napad;
    }

    public int predictNextFleetSize(Planet p) {
        double growth = fleetGrowthRate(p);
        return (int) Math.floor(p.getFleetSize() + growth);
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

    public Planet planetDistanc(ArrayList<Planet> planeti, Planet p) {
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

        Planet a = planetDistanc(planeti, p);
        Planet b = planetDistanc(planeti2, p);
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
        if (attackingPlanets != null) attackingPlanets.clear();
        if (feedingPlanets != null) feedingPlanets.clear();
    }
    public int getTurnCounter(){
      return turnCounter;
    }
    public void incrementTurnCounter(){
        turnCounter++;
    }
}