package org.opentcs.testvehicle.bean;

public class Position {

    private String x;
    private String y;

    public Position() {

    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "Position{" +
                "x='" + x + '\'' +
                ", y='" + y + '\'' +
                '}';
    }
}
