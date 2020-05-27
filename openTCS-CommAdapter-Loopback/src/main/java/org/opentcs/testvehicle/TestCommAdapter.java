/**
 * Title:TestCommAdapter.java
 * 功能；通信适配器
 * Author: star
 * Creation time: 
 * Modification time：2020-5-26 18:40
 * Version： V1.2
 */
package org.opentcs.testvehicle;

import com.google.inject.assistedinject.Assisted;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.UUID;
import javax.inject.Inject;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.Vehicle.Orientation;
import org.opentcs.data.order.Route.Step;
import org.opentcs.drivers.vehicle.BasicVehicleCommAdapter;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.testvehicle.bean.Car;
import org.opentcs.testvehicle.util.JsonUtil;
import org.opentcs.util.CyclicTask;
import org.opentcs.util.ExplainedBoolean;

public class TestCommAdapter extends BasicVehicleCommAdapter {
  
  private final MqttMessageUtil mqttUtil; //通信服务
  private static String dfString="";
  private TestAdapterComponentsFactory componentsFactory;
  private Vehicle vehicle;
  private boolean initialized;
  private CyclicTask testTask;
  private String name;
  private String key;     //订阅主题，redis key

  @Inject
  public TestCommAdapter(TestAdapterComponentsFactory componentsFactory, @Assisted Vehicle vehicle) {
    super(new TestVehicleModel(vehicle), 2, 1, "CHARGE");
    this.componentsFactory = componentsFactory;
    this.vehicle = vehicle;
    /*String url = vehicle.getProperty("url");
    String username = vehicle.getProperty("username");
    String password = vehicle.getProperty("password");
    System.out.println("***---***---username::::" + username);
    System.out.println("***---***---password::::" + password);*/
    key = vehicle.getProperty("key");
    System.out.println("***---***---key::::" + key);
    this.mqttUtil = new MqttMessageUtil();
    mqttUtil.connect();
    //this.mqttUtil.connect("admin", "public");
    //this.mqttUtil.subscribe("s", 0);
  }

  /**
   * （重新）在使用该组件之前将其初始化。
   */
  @Override
  public void initialize() {
  
    initialized = true;
    //网络通信,获取当前位置，电量，等信息
    //getProcessModel().setVehicleState(Vehicle.State.IDLE);
    //getProcessModel().setVehiclePosition("Point-0001");
  }

  /**
   * 启用此通讯适配器，即将其打开。
   */
  @Override
  public synchronized void enable() {
    if (isEnabled()) {
      return;
    }
    System.out.println("--***-*-*-*-kai qi xian cheng  ");
    //开启线程(略)
    testTask = new TestTask();
    
    //getName()返回此通信适配器的名称。
    Thread simThread = new Thread(testTask, getName() + "-Task");
    simThread.start();
    //开启订阅
    mqttUtil.subscribe(key, 0);
    System.out.println("--***-*-*-*-kai qi ding yue ");
    super.enable();
  }

  /**
   * 禁用此通讯适配器，即关闭它。
   */
  @Override
  public synchronized void disable() {
    if (!isEnabled()) {
      return;
    }
    //线程停止
    //在下一个执行周期之前终止此任务。此方法仅将任务标记为终止并立即返回。如果当前正在执行实际任务，则不会中断其执行，但完成后不会再次运行。
    testTask.terminate();
    testTask = null;
    //close
    mqttUtil.close();
    super.disable();
  }
  

  /**
   * 将给定命令转换为车辆可以理解的内容，并将结果数据发送至车辆。
   * @param cmd 
   *       MovementCommand运动指挥/移动命令： A command for moving a step.	用于移动步骤的命令。
   * @throws IllegalArgumentException 
   */
  @Override
  public void sendCommand(MovementCommand cmd)
      throws IllegalArgumentException {
    requireNonNull(cmd, "cmd");
  }

  /**
   * 考虑到车辆的当前状态，检查车辆是否能够处理给定的操作顺序。
   * @param operations 可能必须作为运输单的一部分处理的一系列操作。
   * @return 可处理性，告知车辆是否能够处理列表中的每个操作（以给定的顺序）。
   *        ExplainedBoolean：具有其值的解释/原因的布尔值。
   */
  @Override
  public ExplainedBoolean canProcess(List<String> operations) {
    requireNonNull(operations, "operations");

    final boolean canProcess = isEnabled();
    final String reason = canProcess ? "" : "adapter not enabled";
    return new ExplainedBoolean(canProcess, reason);
  }

  /**
   * 处理到通信适配器的通用消息。此方法为通信适配器提供了通用的单向通信通道。该消息可以是任何内容，包括null，
   * 并且由于 Kernel.sendCommAdapterMessage(org.opentcs.data.TCSObjectReference, java.lang.Object) 
   * 提供了一种从内核外部发送消息的方式，因此它基本上可以源自任何来源。因此，该消息不一定对具体的通信适配器实现完全有意义。
   * @param message 要处理的消息
   */
  @Override
  public void processMessage(Object message) {
    throw new UnsupportedOperationException("Not supported yet."); 
  }

  /**
   * 启动与车辆的通信通道。该方法不应阻塞，即，它不应等待建立实际的连接，因为车辆可能暂时不存在或根本没有响应。
   * 在这种情况下，通信适配器应继续尝试建立连接，直到成功或被disconnectVehicle调用为止 。
   */
  @Override
  protected void connectVehicle() {
  //  throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    System.out.println("connnnnnnnnnnnnnnnnnnnnnnnnn");
  }

  /**
   * 关闭与车辆的通讯通道。
   */
  @Override
  protected void disconnectVehicle() {
    
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  /**
   * 检查与车辆的通讯通道是否打开。
      注意，该方法的返回值并不指示与车辆的通信当前是否处于活动状态和/或车辆是否被认为在正确地工作/响应。
   * @return true 当且仅当与车辆的通信通道打开时。
   */
  @Override
  protected boolean isVehicleConnected() {
    return true;
  }
  
  
  /**
   * 内部类，用于处理运行步骤
   * CyclicTask:循环任务的模板。A template for cyclic tasks.
   */
  private class TestTask extends CyclicTask {

    private TestTask() {
      super(0);
    }

    
    //线程执行
    /**
     * 定义此任务在每个周期中应执行的实际工作。
     */
    @Override
    protected void runActualTask() {
      try {
        //获取状态  位置  速度  方向等
        Car car = mqttUtil.getPaylod(key);
        System.out.println("------car chenggong ");
        if (car == null) {
          System.out.println("------car null");
          Thread.sleep(1000);
          return;
        }System.out.println("------car chenggong hou == " + car.toString());
        /******String str = mqttUtil.getPaylod();
        if (str == null  ||  str.equals("")) {
          Thread.sleep(1000);
          return;
        }*/
        //此方法返回true，如果此字符串包含，否则返回false。
        /*if (!str.contains(";")) {
          Thread.sleep(1000);
          return;
        }*/
    
        /*****Car car = new Car();
        car.setPoint("Point-0020");
        car.setStatus("free");*/
        //if (car.getState().equals("") || car.getState() == null)
        //  car.setState("free");
        String currentPoint = car.getPoint();
        String currentStatus = car.getState();
        //更新车辆的当前位置。
        getProcessModel().setVehiclePosition(currentPoint);
        System.out.println("********set vehicle point++++" + currentPoint);
        //设置车辆的当前状态。
        if (currentStatus.equals("free")) {
          getProcessModel().setVehicleState(Vehicle.State.IDLE);
        }
        else if (currentStatus.equals("executing")) {
          getProcessModel().setVehicleState(Vehicle.State.EXECUTING);
        }
        System.out.println("********set vehicle state++++" + currentStatus);
        
        final MovementCommand curCommand;
        synchronized (TestCommAdapter.this) {
          curCommand = getSentQueue().peek();
        }
        if (curCommand == null) {
         Thread.sleep(1000);
          return;
        }
        final Step curStep = curCommand.getStep();
        simulateMovement(curStep);
        //检查是否除了移动之外还执行操作。true 当且仅当不执行任何操作。
        if (!curCommand.isWithoutOperation()) {
          simulateOperation(curCommand.getOperation());
        }
        if (isTerminated()) {
          Thread.sleep(1000);
          return;
        }
        if (getSentQueue().size() <= 1 && getCommandQueue().isEmpty()) {
          getProcessModel().setVehicleState(Vehicle.State.IDLE);
        }
        synchronized (TestCommAdapter.this) {
          MovementCommand sentCmd = getSentQueue().poll();
          if (sentCmd != null && sentCmd.equals(curCommand)) {
            getProcessModel().commandExecuted(curCommand);
            TestCommAdapter.this.notify();
          }
        }
        Thread.sleep(1000);
      }
      catch (Exception ex) {

      }
        
        
        
        //获取状态  位置  速度  反向
        /*final MovementCommand curCommand;
        synchronized (TestCommAdapter.this) {
          //	getSentQueue()返回一个队列Queue，其中包含此适配器已发送到车辆但尚未处理的命令。
          //  Queue.peek()取队首元素但不删除
          curCommand = getSentQueue().peek();
        }
        //.getStep()返回描述运动的步骤。
        final Step curStep = curCommand.getStep();
        //运行Step，略
        if (!curCommand.isWithoutOperation()) {
          //运行操作（上料或者下料，略）
        }
        //getCommandQueue()返回此适配器的命令队列。
        if (getSentQueue().size() <= 1 && getCommandQueue().isEmpty()) {
          //getProcessModel()返回车辆及其通讯适配器属性的可观察模型。
          getProcessModel().setVehicleState(Vehicle.State.IDLE);
        }
        //更新UI
        synchronized (TestCommAdapter.this) {
          MovementCommand sentCmd = getSentQueue().poll();
          if (sentCmd != null && sentCmd.equals(curCommand)) {
            getProcessModel().commandExecuted(curCommand);
            TestCommAdapter.this.notify();
          }
        }
      }
      catch (Exception ex) {

      }*/
    }
  
  
    /**
     * MovementCommand
     * 
     * 
     * 
     * isWithoutOperation（）检查是否除了移动之外还执行操作。
                          返回值：true 当且仅当不执行任何操作。
     *
     */
    
    
    private void simulateMovement(Step step) throws Exception{
      if (step.getPath() == null) {
        return;
      }
      //返回车辆应该行驶的方向。
      Orientation orientation = step.getVehicleOrientation();
      //返回行驶路径。返回此路径的长度（以毫米为单位）。
      long pathLength = step.getPath().getLength();
      int maxVelocity;
      switch (orientation) {
        case BACKWARD:
          //返回此路径的最大允许反向速度（以毫米/秒为单位）。
          maxVelocity = step.getPath().getMaxReverseVelocity();
          break;
        default:
          //返回此路径的最大允许前进速度（单位为mm / s）。
          maxVelocity = step.getPath().getMaxVelocity();
          break;
      }
      //返回对此路径的终点的引用。
      String pointName = step.getDestinationPoint().getName();
      System.out.println("********vehicle point=====" + pointName);
      //返回车辆及其通讯适配器属性的可观察模型。设置：EXECUTING：车辆正在处理移动命令。
      getProcessModel().setVehicleState(Vehicle.State.EXECUTING);
      String currentPoint = "";
      String currentStatus = "";
      boolean flag = false;
      while (!flag) {
        /*String str = mqttUtil.getPaylod();
        if (str.equals("OK")) {
          flag = true;
        }*/
        Car car = mqttUtil.getPaylod(key);
        if (car.getState().equals("ok")) {
          flag = true;
        }
        Thread.sleep(1000);
      }
      
      //isTerminated指示此任务是否已终止。
      while (!currentPoint.equals(pointName) && !isTerminated()) {
        Car car = mqttUtil.getPaylod(key);
        if (car == null) {
          Thread.sleep(1000);
          continue;
        }
        /*String str = mqttUtil.getPaylod();
        if (str == null ) {
          Thread.sleep(1000);
          continue;
        }
        if (!str.contains(";")) {
          Thread.sleep(1000);
          continue;
        }*/
        ///currentPoint = str.split(";")[0];
       // currentStatus = str.split(";")[1];
       /**/
        /***Car car = new Car();
        car.setPoint("Point-0020");
        car.setStatus("free");*/
        //if (car.getState().equals("") || car.getState() == null)
        //  car.setState("free");
        currentPoint = car.getPoint();
        currentStatus = car.getState();
        /**/
        getProcessModel().setVehiclePosition(currentPoint);
        System.out.println("********set vehicle point++++" + currentPoint);
        if (currentStatus.equals("free")) {
          getProcessModel().setVehicleState(Vehicle.State.IDLE);
        }
        else if (currentStatus.equals("executing")) {
          getProcessModel().setVehicleState(Vehicle.State.EXECUTING);
        }
      }
    }

    private void simulateOperation(String operation) throws Exception{
      requireNonNull(operation, "operation");
      //指示此任务是否已终止。true 终止。
      if (isTerminated()) {
        return;
      }
//      getProcessModel().setVehicleState(Vehicle.State.EXECUTING);
//      if (operation.equals(getProcessModel().getLoadOperation())) {
//        //getProcessModel().setVehicleLoadHandlingDevices(Arrays.asList(new LoadHandlingDevice(LHD_NAME, true)));
//      }
//      else if (operation.equals(getProcessModel().getUnloadOperation())) {
//        //getProcessModel().setVehicleLoadHandlingDevices(Arrays.asList(new LoadHandlingDevice(LHD_NAME, false)));
//      }
    }
  }
  
}
