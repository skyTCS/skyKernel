package org.opentcs.skyvehicle.bean;

/**
 * 车辆基本信息
 * @author eternal
 */
public class Car {
  
    private String name;
    private String energy;
    private String state;
    private String point;

  
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }


    @Override
    public String toString() {
        return "Car{" +
                "name='" + name + '\'' +
                ", energy='" + energy + '\'' +
                ", state='" + state + '\'' +
                ", point='" + point + '\'' +
                '}';
    }
  
}
