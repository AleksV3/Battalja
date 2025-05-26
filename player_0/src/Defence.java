import java.util.ArrayList;
import java.util.Comparator;

public class Defence {
    ArrayList<Planet> defencePlanets;
    ArrayList<Planet> myTeamPlanets;
    ArrayList<Planet> enemyPlanets;

    ArrayList<Fleet> myFleets;
    ArrayList<Fleet> myTeamFleets;
    ArrayList<Fleet> enemyFleets;

    public Defence(ArrayList<Planet> defencePlanets, ArrayList<Planet> myTeamPlanets,
                   ArrayList<Planet> enemyPlanets, ArrayList<Fleet> myFleets,
                   ArrayList<Fleet> myTeamFleets, ArrayList<Fleet> enemyFleets)
    {
        this.defencePlanets = defencePlanets;
        this.myTeamPlanets = myTeamPlanets;
        this.enemyPlanets = enemyPlanets;
        this.myFleets = myFleets;
        this.myTeamFleets = myTeamFleets;
        this.enemyFleets = enemyFleets;
    }

    public int underAttack(Planet planet, int numberOfTurns) {
        int baseDefence = planet.getFleetSize();
        int incomingHelp = defendingFleet(planet, numberOfTurns);
        int totalDefence = baseDefence + incomingHelp;

        int incomingAttack = numbersOfAttacker(enemyFleets, planet, numberOfTurns);

        int netThreat = incomingAttack - totalDefence;

        if (netThreat > 0) {
            System.out.println("Planet " + planet.getName() + " is under attack! Threat level: " + netThreat);
        }

        return Math.max(netThreat, 0);
    }

    public int defendingFleet(Planet planet, int numberOfTurns) {
        int defence = 0;
        defence += numbersOfAttacker(myFleets, planet, numberOfTurns);
        defence += numbersOfAttacker(myTeamFleets, planet, numberOfTurns);
        return defence;
    }

    public int numbersOfAttacker(ArrayList<Fleet> fleets, Planet planet, int numberOfTurns) {
        int total = 0;
        for (Fleet f : fleets) {
            int turnsLeft = f.getNumberOfTurns() - f.getTurn();
            if (f.getDefendingPlanet().equals(planet.getName()) && turnsLeft <= numberOfTurns) {
                total += f.getSize();
            }
        }
        return total;
    }

    public ArrayList<Planet> defending(int numberOfTurns) {
        ArrayList<PlanetThreat> threatened = new ArrayList<>();

        for (Planet p : defencePlanets) {
            int threat = underAttack(p, numberOfTurns);
            if (threat > 0) {
                threatened.add(new PlanetThreat(p, threat));
            }
        }

        threatened.sort(Comparator.comparingInt(pt -> -pt.threat));

        ArrayList<Planet> result = new ArrayList<>();
        for (PlanetThreat pt : threatened) {
            result.add(pt.planet);
        }

        return result;
    }

    private static class PlanetThreat {
        Planet planet;
        int threat;
        PlanetThreat(Planet p, int t) {
            this.planet = p;
            this.threat = t;
        }
    }
}
