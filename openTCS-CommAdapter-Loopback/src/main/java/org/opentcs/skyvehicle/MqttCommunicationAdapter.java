package org.opentcs.skyvehicle;

import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.assistedinject.Assisted;
import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.inject.Inject;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;
import org.opentcs.drivers.vehicle.BasicVehicleCommAdapter;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.SimVehicleCommAdapter;
//import org.opentcs.drivers.vehicle.VehicleCommAdapterPanel;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
import org.opentcs.drivers.vehicle.messages.SetSpeedMultiplier;
import org.opentcs.skyvehicle.bean.Car;
import org.opentcs.skyvehicle.service.VehicleService;
import org.opentcs.util.CyclicTask;
import org.opentcs.util.ExplainedBoolean;
import org.opentcs.virtualvehicle.LoopbackVehicleModel;
import org.opentcs.virtualvehicle.VelocityController;
import org.opentcs.virtualvehicle.VirtualVehicleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 与实体车辆通信
 * @author eternal
 */
public class MqttCommunicationAdapter 
    extends BasicVehicleCommAdapter {
  
  /**
   * 该适配器设置的负载处理设备的名称。
   */
  public static final String LHD_NAME = "default";
  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(MqttCommunicationAdapter.class);
  /**
   * 错误代码，指示加载操作与车辆当前的加载状态之间存在冲突。
   * 加载时无法加载
   */
  private static final String LOAD_OPERATION_CONFLICT = "cannotLoadWhenLoaded";
  /**
   * 错误代码，指示卸载操作与车辆的当前负载状态之间存在冲突。
   * 未加载时无法卸载
   */
  private static final String UNLOAD_OPERATION_CONFLICT = "cannotUnloadWhenNotLoaded";
  /**
   * 每步前进速度控制器的时间（以毫秒为单位）。
   */
  private static final int ADVANCE_TIME = 100;
  /**
   * 该实例的配置。
   */
  private final VirtualVehicleConfiguration configuration;
  /**
   * 适配器组件工厂。
   */
  private final MqttAdapterComponentsFactory componentsFactory;
  /**
   * 内核的执行程序。
   */
  private final ExecutorService kernelExecutor;
  /**
   * 模拟虚拟车辆行为的任务。
   */
  private CyclicTask vehicleSimulationTask;
  /**
   * 布尔标志，用于检查是否允许执行下一个命令。
   */
  private boolean singleStepExecutionAllowed;
  /**
   * 车辆到此comm适配器实例。
   */
  private final Vehicle vehicle;
  /**
   * 车辆的负载状态。
   */
  private LoadState loadState = LoadState.EMPTY;
  /**
   * 回送适配器是否已初始化。
   */
  private boolean initialized;
  
  private String topic;
  
  private VehicleService vs;

  /**
   * 
   * @param componentsFactory
   * @param configuration
   * @param vehicle
   * @param kernelExecutor 
   */
  @Inject
  public MqttCommunicationAdapter(MqttAdapterComponentsFactory componentsFactory, 
                                  VirtualVehicleConfiguration configuration,
                                  @Assisted Vehicle vehicle,
                                  @KernelExecutor ExecutorService kernelExecutor) {
    /**
     * 车辆及其通讯适配器属性的可观察模型。
     * 此通讯适配器的命令队列接受的命令数。必须至少为1。
     * 发送给车辆的最大订单数。
     * 识别为充电操作的字符串。
     */
    //super(new MqttVehicleModel(vehicle),configuration.commandQueueCapacity(), 1, configuration.rechargeOperation());
    super(new MqttVehicleModel(vehicle), 3, 2, "Charge");
    //判空，打异常
    this.vehicle = requireNonNull(vehicle, "vehicle");
    this.configuration = requireNonNull(configuration, "configuration");
    this.componentsFactory = requireNonNull(componentsFactory, "componentsFactory");
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
  }
  
  

  /**
   * 将给定的命令转换为车辆可以理解的内容，并将结果数据发送至车辆。
   * @param mc
   * @throws IllegalArgumentException 
   */
  @Override
  public void sendCommand(MovementCommand mc){
    requireNonNull(mc, "mc");
    System.out.println("***mc***final point::" + mc.getFinalDestination().getName());
    System.out.println("***mc***final location::" + mc.getFinalDestinationLocation().getName());
    System.out.println("***mc***operation::" + mc.getFinalOperation());
    //System.out.println("***mc***operation location::" + mc.getOpLocation().getName());
    // 重置单步模式的执行标志。
    singleStepExecutionAllowed = false;
    // 什么都不做-该命令将被放入sendQueue
    // 自动，模拟任务将在哪里拾取它。
  }

  /**
   * 考虑到车辆的当前状态，检查车辆是否能够处理给定的操作顺序。
   * @param list
   * @return 
   */
  @Override
  public ExplainedBoolean canProcess(List<String> operations) {
    requireNonNull(operations, "operations");

    LOG.debug("{}: Checking processability of {}...", getName(), operations);
    boolean canProcess = true;
    String reason = "";

    // Do NOT require the vehicle to be IDLE or CHARGING here!
    // That would mean a vehicle moving to a parking position or recharging location would always
    // have to finish that order first, which would render a transport order's dispensable flag
    // useless.
    //请勿在此处要求车辆闲置或充电！
    //这意味着要移动到停车位置或充电位置的车辆始终必须先完成该订单，这将使运输订单的可分配标志失效。
    boolean loaded = loadState == LoadState.FULL;
    Iterator<String> opIter = operations.iterator();
    //hasNext()如果迭代器中还有元素，则返回true。
    while (canProcess && opIter.hasNext()) {
      //.next()返回迭代器中的下一个元素
      final String nextOp = opIter.next();
      // If we're loaded, we cannot load another piece, but could unload.
      //如果加载，则无法加载另一块，但可以卸载。
      if (loaded) {
        //startsWith()如果此字符串的方法测试用指定的前缀开始.
        if (nextOp.startsWith(getProcessModel().getLoadOperation())) {
          canProcess = false;
          reason = LOAD_OPERATION_CONFLICT;//
        }
        else if (nextOp.startsWith(getProcessModel().getUnloadOperation())) {
          loaded = false;
        }
      } // If we're not loaded, we could load, but not unload.如果未加载，则可以加载，但不能卸载。
      else if (nextOp.startsWith(getProcessModel().getLoadOperation())) {
        loaded = true;
      }
      else if (nextOp.startsWith(getProcessModel().getUnloadOperation())) {
        canProcess = false;
        reason = UNLOAD_OPERATION_CONFLICT;
      }
    }
    if (!canProcess) {
      LOG.debug("{}: Cannot process {}, reason: '{}'", getName(), operations, reason);
    }
    return new ExplainedBoolean(canProcess, reason);
  }

  /**
   * 检查是否可以将新命令发送到车辆。
   * @return 
   */
  @Override
  protected synchronized boolean canSendNextCommand() {
    return super.canSendNextCommand() && (!getProcessModel().isSingleStepModeEnabled() || singleStepExecutionAllowed);
  }
  
  

  /**
   * 处理到通信适配器的通用消息。
   * @param o 
   */
  @Override
  public void processMessage(Object message) {
     // Process LimitSpeeed message which might pause the vehicle.
     //Process LimitSpeeed消息，可能会暂停车辆。
     //instanceof左边是对象，右边是类；当对象是右边类或子类所创建对象时，返回true；否则，返回false。
    if (message instanceof SetSpeedMultiplier) {
      SetSpeedMultiplier lsMessage = (SetSpeedMultiplier) message;
      int multiplier = lsMessage.getMultiplier();//返回速度乘数（以百分比为单位）。
      getProcessModel().setVehiclePaused(multiplier == 0);
    }
  }

  /**
   * 启动与车辆的通信通道。
   */
  @Override
  protected void connectVehicle() {
    //throw new UnsupportedOperationException("Not supported yet."); 
  }

  /**
   * 关闭与车辆的通讯通道。
   */
  @Override
  protected void disconnectVehicle() {
    //throw new UnsupportedOperationException("Not supported yet."); 
  }

  /**
   * 检查与车辆的通讯通道是否打开。
   * 
   */
  @Override
  protected boolean isVehicleConnected() {
    return true;
  }

/********************************************************/
  /**
   * 
   * @return 
   */
  @Override
  public MqttVehicleModel getProcessModel() {
    return (MqttVehicleModel) super.getProcessModel(); //To change body of generated methods, choose Tools | Templates.
  }

  /**
   * 创建具有此comm适配器的过程模型集的特定属性的可转移过程模型。
   * @return 
   */
  @Override
  protected VehicleProcessModelTO createCustomTransferableProcessModel() {
    return new MqttVehicleModelTO()
        .setLoadOperation(getProcessModel().getLoadOperation())
        .setMaxAcceleration(getProcessModel().getMaxAcceleration())
        .setMaxDeceleration(getProcessModel().getMaxDecceleration())
        .setMaxFwdVelocity(getProcessModel().getMaxFwdVelocity())
        .setMaxRevVelocity(getProcessModel().getMaxRevVelocity())
        .setOperatingTime(getProcessModel().getOperatingTime())
        .setSingleStepModeEnabled(getProcessModel().isSingleStepModeEnabled())
        .setUnloadOperation(getProcessModel().getUnloadOperation())
        .setVehiclePaused(getProcessModel().isVehiclePaused());
  }
  
  /**
   * Triggers a step in single step mode.
   * 在单步模式下触发一步。
   */
  public synchronized void trigger() {
    singleStepExecutionAllowed = true;
  }
  
  /**
   * 设置初始车辆位置。
   * @param newPos 
   */
 /* @Override
  public synchronized void initVehiclePosition(String newPos) {
    kernelExecutor.submit(() -> {
      getProcessModel().setVehiclePosition(newPos);
    });
  }

  @Override
  protected List<org.opentcs.drivers.vehicle.VehicleCommAdapterPanel> createAdapterPanels() {
    return Arrays.asList(componentsFactory.createPanel(this));
  }*/
  
  /**
   * 启用此通讯适配器，即将其打开。
   */
  @Override
  public synchronized void enable() {
    if (isEnabled()) {
      return;
    }
    //h
    vs = new VehicleService();
    vs.vehicleConnect(getProcessModel().getTopic(), 0);
    topic = getProcessModel().getTopic();
    getProcessModel().getVelocityController().addVelocityListener(getProcessModel());
    // Create task for vehicle simulation.创建用于车辆仿真的任务。
    vehicleSimulationTask = new VehicleMqttTask();
    Thread simThread = new Thread(vehicleSimulationTask, getName() + "-simulationTask");
    simThread.start();
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
    vs = null;
    // Disable vehicle simulation.禁用车辆模拟。
    vehicleSimulationTask.terminate();
    vehicleSimulationTask = null;
    //removeVelocityListener从此车辆控制器的侦听器集中删除一个速度侦听器。
    getProcessModel().getVelocityController().removeVelocityListener(getProcessModel());
    super.disable();
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    super.propertyChange(evt);

    if (!((evt.getSource()) instanceof LoopbackVehicleModel)) {
      return;
    }
    if (Objects.equals(evt.getPropertyName(),
                       VehicleProcessModel.Attribute.LOAD_HANDLING_DEVICES.name())) {
      if (!getProcessModel().getVehicleLoadHandlingDevices().isEmpty()
          && getProcessModel().getVehicleLoadHandlingDevices().get(0).isFull()) {
        loadState = LoadState.FULL;
      }
      else {
        loadState = LoadState.EMPTY;
      }
    }
  }
  
  /**
   * 终止实例并释放资源。
   */
  @Override
  public void terminate() {
    super.terminate();
  }
  
  /**
   * （重新）在使用该组件之前将其初始化。
   */
  @Override
  public void initialize() {
    super.initialize();

  }


  
/********************************************************/  
  
  
  
  
/***********************************/ 
  //模拟车辆行为的任务。定义此任务在每个周期中应执行的实际工作。

  private class VehicleMqttTask
      extends CyclicTask {

    /**
     * The time that has passed for the velocity controller whenever   每当速度控制器经过的时间
     * <em>advanceTime</em> has passed for real.    <em> advanceTime </ em>已经过去了。
     */
    private int simAdvanceTime;
    
    public VehicleMqttTask() {
      //两次执行实际任务之间的睡眠时间（以毫秒为单位）。
      super(500);
    }

    @Override
    protected void runActualTask() {
       //获取状态  位置  速度  方向等
        try {
          //获取状态  位置  速度  方向等
          Car carInfo = vs.getCar(topic);
          System.out.println("-----car String = " + carInfo.toString());
          if (carInfo == null) {
          Thread.sleep(200);
          return;
          }
          
          String currentPoint = carInfo.getPoint();
          String currentStatus = carInfo.getState();
          getProcessModel().setVehiclePosition(currentPoint);
          if (currentStatus.equals("0")) {
              getProcessModel().setVehicleState(Vehicle.State.IDLE);
          } else if (currentStatus.equals("1")) {
              getProcessModel().setVehicleState(Vehicle.State.EXECUTING);
          }
          /**//**/
          final MovementCommand curCommand;
          synchronized (MqttCommunicationAdapter.this) {
            curCommand = getSentQueue().peek();
          }
          //simAdvanceTime = (int) (ADVANCE_TIME * configuration.simulationTimeFactor());
          simAdvanceTime = (int) (ADVANCE_TIME * 1.0);
          if (curCommand == null) {
            //System.out.println("++++curCommand == null le ");
            Uninterruptibles.sleepUninterruptibly(ADVANCE_TIME, TimeUnit.MILLISECONDS);
            getProcessModel().getVelocityController().advanceTime(simAdvanceTime);
          }
          else {
            // If we were told to move somewhere, simulate the journey.
            //如果被告知要搬到某个地方，请模拟旅程。
            LOG.debug("Processing MovementCommand...");
            //System.out.println("++++kai shi mo ni ma");
            final Route.Step curStep = curCommand.getStep();
            // Simulate the movement.模拟运动。
            //System.out.println("++++da yin yi xia lu jing ::" + curStep);
            simulateMovement(curStep);
            // Simulate processing of an operation.模拟操作的处理。
            if (!curCommand.isWithoutOperation()) {
              System.out.println("gan le gan le ----");
              simulateOperation(curCommand.getOperation());
            }
            LOG.debug("Processed MovementCommand.");
            if (!isTerminated()) {
              // Set the vehicle's state back to IDLE, but only if there aren't 
              // any more movements to be processed.
              //将车辆的状态重新设置为“ IDLE”，但前提是没有更多要处理的动作。
              if (getSentQueue().size() <= 1 && getCommandQueue().isEmpty()) {
                getProcessModel().setVehicleState(Vehicle.State.IDLE);
              }
              // Update GUI.更新GUI。
              synchronized (MqttCommunicationAdapter.this) {
                MovementCommand sentCmd = getSentQueue().poll();
                // If the command queue was cleared in the meantime, the kernel
                // might be surprised to hear we executed a command we shouldn't
                // have, so we only peek() at the beginning of this method and
                // poll() here. If sentCmd is null, the queue was probably cleared
                // and we shouldn't report anything back.
                //如果在此期间清除了命令队列，则内核可能会惊讶地听到我们执行了本不应该执行的命令，
                //因此我们仅在此方法的开头偷看（），然后在此处轮询（）。 如果sendCmd为null，
                //则队列可能已清除，我们不应该向后报告任何内容。
                if (sentCmd != null && sentCmd.equals(curCommand)) {
                  // Let the vehicle manager know we've finished this command.
                  //让车辆管理员知道我们已经完成了此命令。
                  getProcessModel().commandExecuted(curCommand);
                  MqttCommunicationAdapter.this.notify();
                }
              }
            }
          }

        }
        catch (Exception e) {
          LOG.error(e.getMessage());
        }
    }
      

    /**
     * Simulates the vehicle's movement. If the method parameter is null,
     * then the vehicle's state is failure and some false movement
     * must be simulated. In the other case normal step
     * movement will be simulated.
     * 模拟车辆的运动。 如果方法参数为空，则车辆的状态为故障，必须模拟一些错误的运动。 
     * 在其他情况下，将模拟正常的脚步运动。
     *
     * @param step A step   一步
     * @throws InterruptedException If an exception occured while sumulating
     */
    private void simulateMovement(Route.Step step) throws InterruptedException {
      if (step.getPath() == null) {
        return;
      }

      Vehicle.Orientation orientation = step.getVehicleOrientation();
      long pathLength = step.getPath().getLength();
      int maxVelocity;
      switch (orientation) {
        case BACKWARD:
          maxVelocity = step.getPath().getMaxReverseVelocity();
          break;
        default:
          maxVelocity = step.getPath().getMaxVelocity();
          break;
      }
      String pointName = step.getDestinationPoint().getName();
      //System.out.println("-------point" + pointName);
      getProcessModel().setVehicleState(Vehicle.State.EXECUTING);
      String currentPoint = "";
      String currentStatus = "";
      vs.sendPath(pointName,topic);
      
      
      //
      while (!currentPoint.equals(pointName) && !isTerminated()) {   
        Car carInfo = vs.getCar(topic);
        //System.out.println("222-----car String = " + carInfo.toString());
        if (carInfo == null) {
          Thread.sleep(200);
          continue;
        }
        currentPoint = carInfo.getPoint();
        currentStatus = carInfo.getState();
        getProcessModel().setVehiclePosition(currentPoint);
        if (currentStatus.equals("0")) {
          getProcessModel().setVehicleState(Vehicle.State.IDLE);
        } else if (currentStatus.equals("1")) {
          getProcessModel().setVehicleState(Vehicle.State.EXECUTING);
        }
        
      }
      
      //逐步推进速度控制器，直到控制器处理完所有通道条目。isTerminated指示此任务是否已终止。
      
    }
    
    /**
     * Simulates an operation.模拟操作。
     *
     * @param operation A operation  一项操作。
     * @throws InterruptedException If an exception occured while simulating
     */
    private void simulateOperation(String operation) {
      requireNonNull(operation, "operation");
      System.out.println("---zai zhe li ting le:::" + operation);
      if (isTerminated()) {
        return;
      }

      LOG.debug("Operating...");
      vs.sendWork(operation,topic);
    }
  }
  
  /**
   * The vehicle's possible load states.
   */
  private enum LoadState {
    EMPTY,
    FULL;
  }
}
