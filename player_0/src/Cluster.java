import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Cluster {

    public List<Planet> myPlanets;
    public List<Planet> enemyPlanets;
    public List<Planet> teammatesPlanets;
    public List<Planet> neutralPlanets;
    public int height;
    public int width;

    public Cluster(List<Planet> myPlanets, List<Planet> enemyPlanets,
                   List<Planet> teammatesPlanets, List<Planet> neutralPlanets,
                   int height, int width) {
        this.myPlanets = myPlanets;
        this.enemyPlanets = enemyPlanets;
        this.teammatesPlanets = teammatesPlanets;
        this.neutralPlanets = neutralPlanets;
        this.height = height;
        this.width = width;
    }

    public List<List<Point>> findTop2ClustersDBSCAN(double eps, int minPts) {
        List<Point> points = new ArrayList<>(clusterMap().values());
        Map<Point, Boolean> visited = new HashMap<>();
        List<List<Point>> clusters = new ArrayList<>();

        for (Point p : points) {
            if (visited.getOrDefault(p, false)) continue;

            visited.put(p, true);
            List<Point> neighbors = regionQuery(points, p, eps);

            if (neighbors.size() < minPts) {
                // noise - ignored
            } else {
                List<Point> cluster = new ArrayList<>();
                expandCluster(p, neighbors, cluster, visited, points, eps, minPts);
                clusters.add(cluster);
            }
        }
        clusters.sort((a, b) -> Integer.compare(b.size(), a.size()));
/**
        System.out.println("=== Top Clusters Found ===");
        for (int i = 0; i < Math.min(2, clusters.size()); i++) {
            List<Point> cluster = clusters.get(i);
            Point centroid = calculateCentroid(cluster);
            System.out.println("Cluster #" + (i + 1) + ": " + cluster.size() + " points, approx. center at " + centroid);
        }
        System.out.println("==========================");
**/
        return clusters.subList(0, Math.min(2, clusters.size()));
    }

    public List<Planet> planetsInTheCluster(List<Point> cluster) {
    ArrayList<Planet> planets = combainAllPlanets();
    List<Planet> planetsInTheCluster = new ArrayList<>();
        for (Point p : cluster) {
            for (Planet planet : planets) {
                if (p.x == planet.x && p.y == planet.y) {
                    planetsInTheCluster.add(planet);
                }
            }
        }
        return planetsInTheCluster;
    }

    private ArrayList<Planet> combainAllPlanets() {
        ArrayList<Planet> planets = new ArrayList<>();
        planets.addAll(myPlanets);
        planets.addAll(enemyPlanets);
        planets.addAll(teammatesPlanets);
        planets.addAll(neutralPlanets);
        return planets;
    }

    public Point calculateCentroid(List<Point> cluster) {
        int sumX = 0, sumY = 0;
        for (Point p : cluster) {
            sumX += p.x;
            sumY += p.y;
        }
        int centerX = sumX / cluster.size();
        int centerY = sumY / cluster.size();
        return new Point(centerX, centerY);
    }

    private void expandCluster(Point p, List<Point> neighbors, List<Point> cluster,
                               Map<Point, Boolean> visited, List<Point> points,
                               double eps, int minPts) {
        cluster.add(p);
        Queue<Point> queue = new LinkedList<>(neighbors);

        while (!queue.isEmpty()) {
            Point q = queue.poll();
            if (!visited.getOrDefault(q, false)) {
                visited.put(q, true);
                List<Point> qNeighbors = regionQuery(points, q, eps);
                if (qNeighbors.size() >= minPts) {
                    queue.addAll(qNeighbors);
                }
            }
            if (!isInAnyCluster(q, cluster)) {
                cluster.add(q);
            }
        }
    }

    private boolean isInAnyCluster(Point p, List<Point> cluster) {
        for (Point other : cluster) {
            if (p.equals(other)) return true;
        }
        return false;
    }

    private List<Point> regionQuery(List<Point> all, Point center, double eps) {
        List<Point> neighbors = new ArrayList<>();
        for (Point other : all) {
            if (center.distance(other) <= eps) {
                neighbors.add(other);
            }
        }
        return neighbors;
    }

    private HashMap<Planet, Point> clusterMap() {
        HashMap<Planet, Point> planetMap = new HashMap<>();
        for (Planet planet : myPlanets) planetMap.put(planet, new Point(planet.getX(), planet.getY()));
        for (Planet planet : enemyPlanets) planetMap.put(planet, new Point(planet.getX(), planet.getY()));
        for (Planet planet : teammatesPlanets) planetMap.put(planet, new Point(planet.getX(), planet.getY()));
        for (Planet planet : neutralPlanets) planetMap.put(planet, new Point(planet.getX(), planet.getY()));
        return planetMap;
    }
}
