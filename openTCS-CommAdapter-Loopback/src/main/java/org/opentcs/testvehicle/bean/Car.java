/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.testvehicle.bean;

/**
 *
 * @author eternal
 */
public class Car {
  
    private String name;
    private String energy;
    //private String location;
    private String state;
    private String point;
    //private Position position;

  
    public Car() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnergy() {
      return energy;
    }

    public void setEnergy(String energy) {
      this.energy = energy;
    }

    public String getPoint() {
      return point;
    }

    public void setPoint(String point) {
      this.point = point;
    }


    /*public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }*/

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /*public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }*/

    @Override
    public String toString() {
        return "Car{" +
                "name='" + name + '\'' +
                ", energy='" + energy + '\'' +
                //", location='" + location + '\'' +
                ", state='" + state + '\'' +
                //", position='" + position.toString() + '\'' +
                '}';
    }
  
}
