import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class Strategy {
    private static final int MIN_DEFENSE = 5;
    private static final double GROWTH_THRESHOLD = 2;
    private static final double FEED_THRESHOLD = 0.7;

    private List<Planet> myPlanets;
    private List<Planet> enemyPlanets;
    private List<Planet> neutralPlanets;
    private List<Planet> teammatesPlanets;

    private List<Fleet> myFleets;
    private List<Fleet> teammatesFleets;
    private List<Fleet> enemyFleets;

    private List<List<Point>> clusters;
    Cluster cluster;

    private int x;
    private int y;

    private int turn = 0;

    public Strategy(List<Planet> myPlanets, List<Planet> enemyPlanets,
                    List<Planet> neutralPlanets, List<Planet> teammatesPlanets,
                    List<Fleet> myFleets, List<Fleet> teammatesFleets,
                    List<Fleet> enemyFleets,int x,int y) {
        this.myPlanets = myPlanets;
        this.enemyPlanets = enemyPlanets;
        this.neutralPlanets = neutralPlanets;
        this.teammatesPlanets = teammatesPlanets;
        this.myFleets = myFleets;
        this.teammatesFleets = teammatesFleets;
        this.enemyFleets = enemyFleets;
        this.x = x;
        this.y = y;
    }

    public String executeStrategy(int turn) {
        this.turn = turn;
        classifyPlanets();
        cluster = new Cluster(myPlanets,enemyPlanets,teammatesPlanets,neutralPlanets,x,y);
        clusters = cluster.findTop2ClustersDBSCAN(4,2);

        int stage = determineGameStage(turn);
        StringBuilder commands = new StringBuilder();
        commands.append(defendUnderAttack());
        if(!teammatesPlanets.isEmpty()) {
            commands.append(defendTeammate());
        }
        switch (stage) {
            case 0:
                commands.append(earlyGameExpansion());
                break;
            case 1:
                commands.append(midGameStrategy());
                break;
            case 2:
                commands.append(lateGameStrategy());
                break;
        }

        return commands.toString();
    }

    private int determineGameStage(int turn) {
        if (turn < 60 || neutralPlanets.size() > 3 ) return 0;
        if (turn < 120) return 1;
        return 2;
    }

    private void classifyPlanets() {
        Comparator<Planet> byFleet = Comparator.comparingInt(Planet::getFleetSize);
        myPlanets.sort(byFleet);
        enemyPlanets.sort(byFleet);
        neutralPlanets.sort(byFleet);
    }

    private Planet closestClusterPlanetToMe() {
        Planet closest = null;
        int closestDistance = Integer.MAX_VALUE;
        for (List<Point> cluster : clusters) {
            for (Point p : cluster) {
                for (Planet planet : myPlanets) {
                    int dx = planet.getX() - p.x;
                    int dy = planet.getY() - p.y;
                    int distance = dx * dx + dy * dy;
                    if (distance < closestDistance) {
                        closest = planet;
                        closestDistance = distance;
                    }
                }
            }
        }
        return closest;
    }

    private boolean isTeammateClosestToCluster() {
        int closestDistanceMe = Integer.MAX_VALUE;
        for (List<Point> cluster: clusters) {
            for (Point p : cluster) {
                for (Planet planet : myPlanets) {
                    int dx = planet.getX() - p.x;
                    int dy = planet.getY() - p.y;
                    int distance = dx * dx + dy * dy;
                    if (distance < closestDistanceMe) {
                        closestDistanceMe = distance;
                    }
                }
                for (Planet planet : teammatesPlanets) {
                    int dx = planet.getX() - p.x;
                    int dy = planet.getY() - p.y;
                    int distance = dx * dx + dy * dy;
                    if (distance < closestDistanceMe) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private List<Point> findClosestCluster() {
        List<Point> closestCluster = new ArrayList<>();
        int closestDistance = Integer.MAX_VALUE;
        for (List<Point> cluster: clusters) {
            for (Point p : cluster) {
                for (Planet planet : myPlanets) {
                    int dx = planet.getX() - p.x;
                    int dy = planet.getY() - p.y;
                    int distance = dx * dx + dy * dy;
                    if (distance < closestDistance) {
                        closestCluster = cluster;
                        closestDistance = distance;
                    }
                }
            }
        }
        return closestCluster;
    }

    private boolean isPlanetInCluster(Planet planet,List<Point> cluster) {
        for (Point P : cluster) {
            if (P.x == planet.getX() && P.y == planet.getY()) {
                return true;
            }
        }
        return false;
    }

    private boolean isClusterMine(List<Point> cluster){
        int owned = 0;
        for (Point p : cluster) {
            owned = 0;
            for (Planet planet : myPlanets) {
                if (p.x == planet.getX() && p.y == planet.getY()) {
                    owned++;
                    break;
                }
            }
        }
        return owned == cluster.size();
    }

    private String earlyGameExpansion() {
        StringBuilder cmd = new StringBuilder();

        List<Point> closestCluster = findClosestCluster();
        List<Planet> clusterPlanets = cluster.planetsInTheCluster(closestCluster);

        List<Planet> targetPlanetsInCluster = clusterPlanets.stream()
                .filter(p -> !myPlanets.contains(p))
                .sorted(Comparator.comparingInt(Planet::getFleetSize)) // go for easier ones
                .toList();

        for (Planet src : myPlanets) {
            if (myPlanets.size() > 3) {
                if ((src.getPlanetSize() >= 8) || src.isUnderAttack()
                        || src.isAttacking()) continue;
            }

            Planet target = null;

            for (Planet potential : targetPlanetsInCluster) {
                if (isCloserThanTeammate(src, potential) &&
                        !weAreWinningBattleFor(potential) &&
                        !isTeammateClosestToCluster()) {
                    target = potential;
                    System.out.println(target.getName());
                    break;
                }
            }

            if (target == null) {
                target = Stream.concat(neutralPlanets.stream(), enemyPlanets.stream())
                        .filter(p -> isCloserThanTeammate(src, p))
                        .filter(p -> !weAreWinningBattleFor(p))
                        .max(Comparator.comparingDouble(p -> p.getPlanetSize() / (getDistance(src, p) + p.getFleetSize() + 5)))
                        .orElse(null);
            }

            if (target != null) {
                int required = target.getFleetSize() + 1;
                int maxSend = optimalFleetSize(src);
                if (canAffordToSend(src, required)) {
                    cmd.append(src.attackPlanet(target, required));
                } else if (canAffordToSend(src, maxSend) && maxSend > 0) {
                    cmd.append(src.attackPlanet(target, maxSend));
                }
            }
        }

        return cmd.toString();
    }

    private String midGameStrategy() {
        StringBuilder cmd = new StringBuilder();
        cmd.append(feedPlanets());
        ArrayList <Planet> enemySmallPlanets = new ArrayList<>();
        ArrayList <Planet> enemyBigPlanets = new ArrayList<>();
        for(Planet src : myPlanets) {
            if(src.getPlanetSize() >= 8){
                enemyBigPlanets.add(src);
            }else{
                enemySmallPlanets.add(src);
            }
        }
        for (Planet src : myPlanets) {
            if (!shouldSendFleet(src) || src.isUnderAttack() ||src.isAttacking() ) continue;
            Planet target = Stream.concat(neutralPlanets.stream(), enemySmallPlanets.stream())
                    .filter(p -> !weAreWinningBattleFor(p))
                    .max(Comparator.comparingDouble(p -> fleetGrowthRate(p) / (getDistance(src, p) + p.getFleetSize() + 5)))
                    .orElse(null);

            if (target != null) {
                int required = target.getFleetSize() + 1;
                int maxSend = optimalFleetSize(src);
                if(required <= maxSend) continue;
                if (canAffordToSend(src, required) && maxSend != 0) {
                    cmd.append(src.attackPlanet(target, required));
                }else {
                    required = required/4;
                    cmd.append(src.attackPlanet(target, required));
                }
            }
        }
        return cmd.toString();
    }

    private String lateGameStrategy() {
        StringBuilder cmd = new StringBuilder();
        for (Planet src : myPlanets) {
            if(src.isUnderAttack() || src.isAttacking()) continue;
                Planet target = enemyPlanets.stream()
                    .filter(p -> !weAreWinningBattleFor(p))
                    .max(Comparator.comparingDouble(p -> fleetGrowthRate(p) / (getDistance(src, p) + p.getFleetSize())))
                    .orElse(null);
            if (target != null) {
                int maxSend = optimalFleetSize(src);
                int required = target.getFleetSize() + 1;
                if(required < maxSend) continue;
                if (canAffordToSend(src, required)) {
                    cmd.append(src.attackPlanet(target, required));
                }
            }
        }

        return cmd.toString();
    }

    private String feedPlanets() {
        StringBuilder cmd = new StringBuilder();
        List<Planet> smallPlanets = new ArrayList<>();
        List<Planet> midPlanets = new ArrayList<>();
        List<Planet> bigPlanets = new ArrayList<>();

        for(Planet src : myPlanets) {
            if(src.getPlanetSize() >= 8){
                bigPlanets.add(src);
            }else if(src.getPlanetSize() > 5){
                midPlanets.add(src);
            }else{
                smallPlanets.add(src);
            }
        }
        for(Planet src : teammatesPlanets) {
            if(src.getPlanetSize() >= 8) {
                bigPlanets.add(src);
            }
        }
        for (Planet src : smallPlanets) {
            Planet feedTarget = null;
            if(!bigPlanets.isEmpty()) {
                feedTarget = getClosestPlanet(src,bigPlanets);
            }else{
                feedTarget = getClosestPlanet(src,midPlanets);
            }
            int amount = optimalFleetSize(src);
            if (feedTarget != null && !src.isAttacking() && !src.isUnderAttack()) {
                    if(amount == 0){
                        amount = 5;
                    }
                    cmd.append(src.attackPlanet(feedTarget, amount));
                    feedTarget.setUnderAttack(true);
                    src.setAttacking(true);
            }
        }
        return cmd.toString();
    }

    private Planet getClosestPlanet(Planet planet, List<Planet> planets) {
        Planet closest = null;
        double minDistance = Double.MAX_VALUE;

        for (Planet p : planets) {
            if(!planet.equals(p)) {
                double distance = getDistance(p, planet);
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = p;
                }
            }
        }

        return closest;
    }

    private String defendUnderAttack() {
        StringBuilder cmd = new StringBuilder();
        for (Planet underAttack : myPlanets) {
            int incoming = getIncomingEnemyFleet(underAttack);
            if (incoming > 0 && underAttack.getFleetSize() <= incoming) {
                underAttack.setUnderAttack(true);
                int needed = incoming - underAttack.getFleetSize();

                Planet defender = myPlanets.stream()
                        .filter(p -> p != underAttack && p.getFleetSize() > needed + MIN_DEFENSE)
                        .filter(p -> !willTeammateDefend(underAttack, needed))
                        .min(Comparator.comparingDouble(p -> getDistance(p, underAttack) - 0.1 * p.getPlanetSize()))
                        .orElse(null);

                if (defender != null) {
                    cmd.append(defender.attackPlanet(underAttack, needed));
                    defender.setAttacking(true);
                }
            }
        }
        return cmd.toString();
    }

    private String defendTeammate() {
        StringBuilder cmd = new StringBuilder();
        for (Planet teammate : teammatesPlanets) {
            int incoming = getIncomingEnemyFleet(teammate);
            teammate.setUnderAttack(true);
            if (incoming > 0 && teammate.getFleetSize() <= incoming) {
                int needed = incoming - teammate.getFleetSize();

                Planet defender = myPlanets.stream()
                        .filter(p ->!p.isAttacking() && !p.isUnderAttack() &&
                                p.getFleetSize() > needed + MIN_DEFENSE)
                        .filter(p -> !willTeammateDefend(teammate, needed))
                        .min(Comparator.comparingDouble(p -> getDistance(p, teammate) - 0.1 * p.getPlanetSize()))
                        .orElse(null);

                if (defender != null) {
                    cmd.append(defender.attackPlanet(teammate, needed));
                    defender.setAttacking(true);
                }
            }
        }
        return cmd.toString();
    }
    private boolean willTeammateDefend(Planet underAttack, int defend) {
        Planet teammate = getClosestPlanet(underAttack, teammatesPlanets);
        Planet myClosest = getClosestPlanet(underAttack, myPlanets);

        double teammateDis = getDistance(underAttack, teammate);
        double myDis = getDistance(underAttack, myClosest);

        boolean teammateIsCloser = teammateDis < myDis * 0.95;

        return teammateIsCloser && canAffordToSend(teammate, defend);
    }

    private boolean weAreWinningBattleFor(Planet planet) {
        return getIncomingEnemyFleet(planet) * 0.8 < getIncomingAllyFleet(planet);
    }

    private boolean isCloserThanTeammate(Planet myPlanet, Planet target) {
        double myDist = getDistance(myPlanet, target);
        int stage = determineGameStage(turn);
        double teamDist = teammatesPlanets.stream()
                .filter(p -> stage == 2 || p.getPlanetSize() >= 8)
                .mapToDouble(p -> getDistance(p, target))
                .min()
                .orElse(Double.MAX_VALUE);
        return myDist <= teamDist;
    }

    private boolean willBeUnderAttack(Planet myPlanet, int amount) {
        return myPlanet.getFleetSize() - amount < getIncomingEnemyFleet(myPlanet);
    }

    private boolean canAffordToSend(Planet planet, int amount) {
        return planet.getFleetSize() - amount >= MIN_DEFENSE &&
                !willBeUnderAttack(planet, amount);
    }

    private int optimalFleetSize(Planet planet) {
        int stage = determineGameStage(turn);
        float percent = switch (stage) {
            case 0 -> 0.10f;
            case 1 -> 0.25f;
            case 2 -> 0.40f;
            default -> 0.10f;
        };
        if(!shouldSendFleet(planet)){
            return 0;
        }
        int fleetsToSend = (int) Math.floor(fleetGrowthRate(planet) * percent);
        int F = planet.getFleetSize() - fleetsToSend;
        double size = planet.getPlanetSize();
        double F_max = getFMax(planet);
        double F_capacity = (F < F_max) ? 1 - (F / F_max) : 1e-8 * size;
        double fleetsNextTurnGenerate = ((1.0 / 10.0) * (size / 10.0) * F * F_capacity) * percent;
        if ((fleetsNextTurnGenerate - fleetsToSend) >= 0) {
            return fleetsToSend;
        }
        return 0;
    }

    public boolean shouldSendFleet(Planet planet) {
        double fMax = getFMax(planet);
        int size = planet.getFleetSize();
        double numShips = planet.getFleetSize();

        double sendThreshold;
        if (size <= 3) sendThreshold = 0.4;
        else if (size <= 6) sendThreshold = 0.65;
        else sendThreshold = 0.85;

        return numShips >= sendThreshold * fMax;
    }

    private double fleetGrowthRate(Planet p) {
        double F = p.getFleetSize();
        double size = p.getPlanetSize();
        double F_max = getFMax(p);

        double F_capacity = (F < F_max) ? 1 - (F / F_max) : 1e-8 * size;
        return (1.0 / 10.0) * (size / 10.0) * F * F_capacity;
    }

    private double getFMax(Planet p) {
        return 500 * p.getPlanetSize() + 5000;
    }

    private double getDistance(Planet a, Planet b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private int getIncomingEnemyFleet(Planet p) {
        return enemyFleets.stream()
                .filter(f -> f.getDefendingPlanet().equals(p.getName()))
                .mapToInt(Fleet::getSize).sum();
    }

    private int getIncomingAllyFleet(Planet p) {
        return Stream.concat(myFleets.stream(), teammatesFleets.stream())
                .filter(f -> f.getDefendingPlanet().equals(p.getName()))
                .mapToInt(Fleet::getSize).sum();
    }
}
