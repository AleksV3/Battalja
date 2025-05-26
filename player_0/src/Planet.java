public class Planet {

    //P <int> <int> <int> <float> <int> <string>
    //- Planet: Name (number), position x, position y,
    //planet size, army size, planet color (blue, cyan, green, yellow or null for neutral)

    public String name;
    public int x;
    public int y;
    public float planetSize;
    public int fleetSize;
    public String color;
    public boolean underAttack;
    public boolean attacking;

    //konstruktor
    public Planet(String[] t){
        this.name = t[1];
        this.x = Integer.parseInt(t[2]);
        this.y = Integer.parseInt(t[3]);
        this.planetSize = Float.parseFloat(t[4]);
        this.fleetSize = Integer.parseInt(t[5]);
        this.color = t[6];
        this.underAttack = false;
        this.attacking = false;
    }

    public boolean isAttacking() {
        return attacking;
    }

    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public float getPlanetSize() {
        return planetSize;
    }

    public int getFleetSize() {
        return fleetSize;
    }

    public String getColor() {
        return color;
    }

    public boolean isUnderAttack() {
        return underAttack;
    }

    public void setUnderAttack(boolean underAttack) {
        this.underAttack = underAttack;
    }



    //zapisi potezo kot string
    // this planet napade planet b
    public String attackPlanet(Planet b, int fleetSize){
        String poteza =  "/A ";
        poteza += this.name + " "; //planet A ki napada
        poteza += b.getName()+ " ";//napadan planet
        if(fleetSize > 0){
            poteza += fleetSize + " ";
        }else{
            poteza = " ";
        }
        poteza += "\n";
        return poteza;
    }

}
