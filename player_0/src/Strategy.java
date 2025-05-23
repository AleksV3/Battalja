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

    private int turn = 0;

    public Strategy(List<Planet> myPlanets, List<Planet> enemyPlanets, List<Planet> neutralPlanets,
                    List<Planet> teammatesPlanets, List<Fleet> myFleets, List<Fleet> teammatesFleets, List<Fleet> enemyFleets) {
        this.myPlanets = myPlanets;
        this.enemyPlanets = enemyPlanets;
        this.neutralPlanets = neutralPlanets;
        this.teammatesPlanets = teammatesPlanets;
        this.myFleets = myFleets;
        this.teammatesFleets = teammatesFleets;
        this.enemyFleets = enemyFleets;
    }

    public String executeStrategy(int turn) {
        this.turn = turn;
        classifyPlanets();

        int stage = determineGameStage(turn);
        StringBuilder commands = new StringBuilder();
        commands.append(defendUnderAttack());

        switch (stage) {
            case 0 -> commands.append(earlyGameExpansion());
            case 1 -> commands.append(midGameStrategy());
            case 2 -> commands.append(lateGameStrategy());
        }

        return commands.toString();
    }

    private int determineGameStage(int turn) {
        if (turn < 60 || neutralPlanets.size() > 3) return 0;
        if (turn < 120) return 1;
        return 2;
    }

    private void classifyPlanets() {
        Comparator<Planet> byFleet = Comparator.comparingInt(Planet::getFleetSize);
        myPlanets.sort(byFleet);
        enemyPlanets.sort(byFleet);
        neutralPlanets.sort(byFleet);
    }

    private String earlyGameExpansion() {
        StringBuilder cmd = new StringBuilder();
        for (Planet src : myPlanets) {
            if (src.getPlanetSize() >= 8 || src.isUnderAttack() ||src.isAttacking() ) continue;
            Planet target = Stream.concat(neutralPlanets.stream(), enemyPlanets.stream())
                    .filter(p -> isCloserThanTeammate(src, p))
                    .filter(p -> !weAreWinningBattleFor(p))
                    .max(Comparator.comparingDouble(p -> p.getPlanetSize() / (getDistance(src, p) + p.getFleetSize() + 5)))
                    .orElse(null);
            if (target != null) {
                int required = target.getFleetSize() + 1;
                int maxSend = optimalFleetSize(src);
                if (canAffordToSend(src, required)) {
                    cmd.append(src.attackPlanet(target, required));
                } else if (canAffordToSend(src, maxSend) && maxSend != 0) {
                    cmd.append(src.attackPlanet(target, maxSend));
                }
            }
        }
        return cmd.toString();
    }

    private String midGameStrategy() {
        StringBuilder cmd = new StringBuilder();
        for (Planet src : myPlanets) {
            if (src.getPlanetSize() >= 8 || src.isUnderAttack() ||src.isAttacking() ) continue;
            Planet target = Stream.concat(neutralPlanets.stream(), enemyPlanets.stream())
                    .filter(p -> !weAreWinningBattleFor(p))
                    .max(Comparator.comparingDouble(p -> fleetGrowthRate(p) / (getDistance(src, p) + p.getFleetSize() + 5)))
                    .orElse(null);

            if (target != null) {
                int required = target.getFleetSize() + 1;
                int maxSend = optimalFleetSize(src);
                if(required < maxSend) continue;
                if (canAffordToSend(src, required)  && maxSend != 0) {
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

        for (Planet src : myPlanets) {
            if (src.getFleetSize() < 20) continue;

            Planet feedTarget = myPlanets.stream()
                    .filter(p -> p != src)
                    .filter(p -> {
                        int futureFleet = p.getFleetSize() + getIncomingAllyFleet(p);
                        double maxGrowth = fleetGrowthRate(p);
                        return futureFleet < maxGrowth * FEED_THRESHOLD;
                    })
                    .max(Comparator.comparingDouble(this::fleetGrowthRate))
                    .orElse(null);

            if (feedTarget != null) {
                int futureFleet = feedTarget.getFleetSize() + getIncomingAllyFleet(feedTarget);
                double maxGrowth = fleetGrowthRate(feedTarget);
                if (maxGrowth == 0)continue;
                int amount = Math.min(src.getFleetSize() - MIN_DEFENSE, (int)(maxGrowth * FEED_THRESHOLD - futureFleet));

                if (amount > 0 && !willBeUnderAttack(src, amount)) {
                    cmd.append(src.attackPlanet(feedTarget, amount));
                    myPlanets.remove(src);
                }
            }
        }
        return cmd.toString();
    }

    private String defendUnderAttack() {
        StringBuilder cmd = new StringBuilder();
        for (Planet underAttack : myPlanets) {
            int incoming = getIncomingEnemyFleet(underAttack);
            if (incoming > 0 && underAttack.getFleetSize() < incoming) {
                underAttack.setUnderAttack(true);
                int needed = incoming - underAttack.getFleetSize();
                Planet defender = myPlanets.stream()
                        .filter(p -> p != underAttack && p.getFleetSize() > needed + MIN_DEFENSE)
                        .min(Comparator.comparingDouble(p -> getDistance(p, underAttack)))
                        .orElse(null);
                if (defender != null && defender != underAttack) {
                    cmd.append(defender.attackPlanet(underAttack, needed));
                    defender.setAttacking(true);
                }
            }
        }
        return cmd.toString();
    }
    private boolean isEnemy(Planet p) {
        return enemyPlanets.contains(p);
    }
    private boolean weAreWinningBattleFor(Planet planet) {
        return getIncomingEnemyFleet(planet) * 0.8 < getIncomingAllyFleet(planet);
    }

    private boolean isCloserThanTeammate(Planet myPlanet, Planet target) {
        double myDist = getDistance(myPlanet, target);
        double teamDist = teammatesPlanets.stream()
                .mapToDouble(p -> getDistance(p, target)).min().orElse(Double.MAX_VALUE);
        return myDist <= teamDist;
    }

    private boolean willBeUnderAttack(Planet myPlanet, int amount) {
        return myPlanet.getFleetSize() - amount < getIncomingEnemyFleet(myPlanet);
    }

    private boolean canAffordToSend(Planet planet, int amount) {
        return planet.getFleetSize() - amount >= MIN_DEFENSE && !willBeUnderAttack(planet, amount);
    }

    private int optimalFleetSize(Planet planet) {
        int stage = determineGameStage(turn);
        float percent = switch (stage) {
            case 0 -> 0.10f;
            case 1 -> 0.25f;
            case 2 -> 0.40f;
            default -> 0.10f;
        };
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
