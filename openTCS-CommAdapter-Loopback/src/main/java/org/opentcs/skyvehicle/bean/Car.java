package org.opentcs.skyvehicle.bean;

/**
 * 车辆基本信息
 * @author eternal
 */
public class Car {
  
    private String name;    //名称
    private String energy;  //电量
    private String state;   //状态
    private String point;   //位置

  
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
