package org.opentcs.skyvehicle;

import com.google.inject.assistedinject.Assisted;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapterEvent;
import org.opentcs.drivers.vehicle.VehicleCommAdapterPanel;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.util.Comparators;
import org.opentcs.util.gui.StringListCellRenderer;
import static org.opentcs.virtualvehicle.I18nLoopbackCommAdapter.BUNDLE_PATH;
import org.opentcs.virtualvehicle.inputcomponents.DropdownListInputPanel;
import org.opentcs.virtualvehicle.inputcomponents.InputDialog;
import org.opentcs.virtualvehicle.inputcomponents.InputPanel;
import org.opentcs.virtualvehicle.inputcomponents.SingleTextInputPanel;
import org.opentcs.virtualvehicle.inputcomponents.TextInputPanel;
import org.opentcs.virtualvehicle.inputcomponents.TripleTextInputPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *与MqttCommunicationAdapter对应的MqttCommunicationAdapterPanel。
 * @author eternal
 */
@Deprecated
public class MqttCommunicationAdapterPanel extends VehicleCommAdapterPanel{

  /**
   * A resource bundle for internationalization.国际化的资源包。
   */
  private static final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PATH);
  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(MqttCommunicationAdapterPanel.class);
  /**
   * The object service.对象服务。
   */
  private final TCSObjectService objectService;
  /**
   * This panel's communication adapter.该面板的通信适配器。
   */
  private final MqttCommunicationAdapter commAdapter;
  /**
   * The comm adapter's model.通讯适配器的型号。
   */
  private final MqttVehicleModel vehicleModel;

  @Inject
  public MqttCommunicationAdapterPanel(TCSObjectService objectService,
                                       @Assisted MqttCommunicationAdapter adapter) {
    this.objectService = requireNonNull(objectService, "objectService");;
    this.commAdapter = requireNonNull(adapter, "adapter");
    this.vehicleModel = adapter.getProcessModel();
    
    initComponents();
    maxFwdVeloTxt.setText(String.valueOf(vehicleModel.getMaxFwdVelocity()));
    maxRevVeloTxt.setText(String.valueOf(vehicleModel.getMaxRevVelocity()));
    opTimeTxt.setText(String.valueOf(vehicleModel.getOperatingTime()));
    maxAccelTxt.setText(String.valueOf(vehicleModel.getMaxAcceleration()));
    maxDecelTxt.setText(String.valueOf(vehicleModel.getMaxDecceleration()));
    updateCommAdapterEnabled(vehicleModel.isCommAdapterEnabled());
  }
  
  
  
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getSource() instanceof MqttVehicleModel) {
      updateLoopbackVehicleModelData(evt);
      updateVehicleProcessModelData(evt);
    }
  }

  private void updateLoopbackVehicleModelData(PropertyChangeEvent evt) {
    MqttVehicleModel vm = (MqttVehicleModel) evt.getSource();
    if (Objects.equals(evt.getPropertyName(),
                       MqttVehicleModel.Attribute.OPERATING_TIME.name())) {
      updateOperatingTime(vm.getOperatingTime());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            MqttVehicleModel.Attribute.ACCELERATION.name())) {
      updateMaxAcceleration(vm.getMaxAcceleration());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            MqttVehicleModel.Attribute.DECELERATION.name())) {
      updateMaxDeceleration(vm.getMaxDecceleration());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            MqttVehicleModel.Attribute.MAX_FORWARD_VELOCITY.name())) {
      updateMaxForwardVelocity(vm.getMaxFwdVelocity());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            MqttVehicleModel.Attribute.MAX_REVERSE_VELOCITY.name())) {
      updateMaxReverseVelocity(vm.getMaxRevVelocity());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            MqttVehicleModel.Attribute.SINGLE_STEP_MODE.name())) {
      updateSingleStepMode(vm.isSingleStepModeEnabled());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            MqttVehicleModel.Attribute.VEHICLE_PAUSED.name())) {
      updateVehiclePaused(vm.isVehiclePaused());
    }
  }

  private void updateVehicleProcessModelData(PropertyChangeEvent evt) {
    VehicleProcessModel vpm = (VehicleProcessModel) evt.getSource();
    if (Objects.equals(evt.getPropertyName(),
                       VehicleProcessModel.Attribute.COMM_ADAPTER_ENABLED.name())) {
      updateCommAdapterEnabled(vpm.isCommAdapterEnabled());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            VehicleProcessModel.Attribute.POSITION.name())) {
      updatePosition(vpm.getVehiclePosition());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            VehicleProcessModel.Attribute.STATE.name())) {
      updateVehicleState(vpm.getVehicleState());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            VehicleProcessModel.Attribute.PRECISE_POSITION.name())) {
      updatePrecisePosition(vpm.getVehiclePrecisePosition());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            VehicleProcessModel.Attribute.ORIENTATION_ANGLE.name())) {
      updateOrientationAngle(vpm.getVehicleOrientationAngle());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            VehicleProcessModel.Attribute.ENERGY_LEVEL.name())) {
      updateEnergyLevel(vpm.getVehicleEnergyLevel());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            VehicleProcessModel.Attribute.LOAD_HANDLING_DEVICES.name())) {
      updateVehicleLoadHandlingDevice(vpm.getVehicleLoadHandlingDevices());
    }
  }

  private void updateVehicleLoadHandlingDevice(List<LoadHandlingDevice> devices) {
    if (devices.size() > 1) {
      LOG.warn("size of load handling devices greater than 1 ({})", devices.size());
    }
    Iterator<LoadHandlingDevice> deviceIterator = devices.iterator();
    boolean loaded = deviceIterator.hasNext() ? deviceIterator.next().isFull() : false;
    SwingUtilities.invokeLater(() -> lHDCheckbox.setSelected(loaded));
  }

  private void updateEnergyLevel(int energy) {
    SwingUtilities.invokeLater(() -> energyLevelTxt.setText(Integer.toString(energy)));
  }

  private void updateCommAdapterEnabled(boolean isEnabled) {
    SwingUtilities.invokeLater(() -> {
      setStatePanelEnabled(isEnabled);
      chkBoxEnable.setSelected(isEnabled);
    });
  }

  private void updatePosition(String vehiclePosition) {
    SwingUtilities.invokeLater(() -> {
      if (vehiclePosition == null) {
        positionTxt.setText("");
      }
      else {
        for (Point curPoint : objectService.fetchObjects(Point.class)) {
          if (curPoint.getName().equals(vehiclePosition)) {
            positionTxt.setText(curPoint.getName());
            break;
          }
        }
      }
    });
  }

  private void updateVehicleState(Vehicle.State vehicleState) {
    SwingUtilities.invokeLater(() -> stateTxt.setText(vehicleState.toString()));
  }

  private void updatePrecisePosition(Triple precisePos) {
    SwingUtilities.invokeLater(() -> {
      if (precisePos == null) {
        setPrecisePosText(null, null, null);
      }
      else {
        setPrecisePosText(precisePos.getX(), precisePos.getY(), precisePos.getZ());
      }
    });
  }

  private void updateOrientationAngle(Double orientation) {
    SwingUtilities.invokeLater(() -> {
      if (Double.isNaN(orientation)) {
        orientationAngleTxt.setText(bundle.getString("loopbackCommAdapterPanel.textField_orientationAngle.angleNotSetPlaceholder"));
      }
      else {
        orientationAngleTxt.setText(Double.toString(orientation));
      }
    });
  }

  private void updateOperatingTime(int defaultOperatingTime) {
    SwingUtilities.invokeLater(() -> opTimeTxt.setText(Integer.toString(defaultOperatingTime)));
  }

  private void updateMaxAcceleration(int maxAcceleration) {
    SwingUtilities.invokeLater(() -> maxAccelTxt.setText(Integer.toString(maxAcceleration)));
  }

  private void updateMaxDeceleration(int maxDeceleration) {
    SwingUtilities.invokeLater(() -> maxDecelTxt.setText(Integer.toString(maxDeceleration)));
  }

  private void updateMaxForwardVelocity(int maxFwdVelocity) {
    SwingUtilities.invokeLater(() -> maxFwdVeloTxt.setText(Integer.toString(maxFwdVelocity)));
  }

  private void updateMaxReverseVelocity(int maxRevVelocity) {
    SwingUtilities.invokeLater(() -> maxRevVeloTxt.setText(Integer.toString(maxRevVelocity)));
  }

  private void updateSingleStepMode(boolean singleStepMode) {
    SwingUtilities.invokeLater(() -> {
      triggerButton.setEnabled(singleStepMode);
      singleModeRadioButton.setSelected(singleStepMode);
      flowModeRadioButton.setSelected(!singleStepMode);
    });
  }

  private void updateVehiclePaused(boolean isVehiclePaused) {
    SwingUtilities.invokeLater(() -> pauseVehicleCheckBox.setSelected(isVehiclePaused));
  }

  /**
   * Enable/disable the input fields and buttons in the "Current position/state"
   * panel. If disabled the user can not change any values or modify the
   * vehicles state.
   *
   * @param enabled boolean indicating if the panel should be enabled
   */
  private void setStatePanelEnabled(boolean enabled) {
    SwingUtilities.invokeLater(() -> positionTxt.setEnabled(enabled));
    SwingUtilities.invokeLater(() -> stateTxt.setEnabled(enabled));
    SwingUtilities.invokeLater(() -> energyLevelTxt.setEnabled(enabled));
    SwingUtilities.invokeLater(() -> precisePosTextArea.setEnabled(enabled));
    SwingUtilities.invokeLater(() -> orientationAngleTxt.setEnabled(enabled));
    SwingUtilities.invokeLater(() -> pauseVehicleCheckBox.setEnabled(enabled));
  }

  // CHECKSTYLE:OFF检查：关闭
  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * 从构造函数内部调用此方法以初始化表单。
   * regenerated by the Form Editor.由表单编辑器重新生成。
   */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        modeButtonGroup = new javax.swing.ButtonGroup();
        propertyEditorGroup = new javax.swing.ButtonGroup();
        vehicleBahaviourPanel = new javax.swing.JPanel();
        PropsPowerOuterContainerPanel = new javax.swing.JPanel();
        PropsPowerInnerContainerPanel = new javax.swing.JPanel();
        vehiclePropsPanel = new javax.swing.JPanel();
        maxFwdVeloLbl = new javax.swing.JLabel();
        maxFwdVeloTxt = new javax.swing.JTextField();
        maxFwdVeloUnitLbl = new javax.swing.JLabel();
        maxRevVeloLbl = new javax.swing.JLabel();
        maxRevVeloTxt = new javax.swing.JTextField();
        maxRevVeloUnitLbl = new javax.swing.JLabel();
        maxAccelLbl = new javax.swing.JLabel();
        maxAccelTxt = new javax.swing.JTextField();
        maxAccelUnitLbl = new javax.swing.JLabel();
        maxDecelTxt = new javax.swing.JTextField();
        maxDecelLbl = new javax.swing.JLabel();
        maxDecelUnitLbl = new javax.swing.JLabel();
        defaultOpTimeLbl = new javax.swing.JLabel();
        defaultOpTimeUntiLbl = new javax.swing.JLabel();
        opTimeTxt = new javax.swing.JTextField();
        profilesContainerPanel = new javax.swing.JPanel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        vehicleStatePanel = new javax.swing.JPanel();
        stateContainerPanel = new javax.swing.JPanel();
        connectionPanel = new javax.swing.JPanel();
        chkBoxEnable = new javax.swing.JCheckBox();
        curPosPanel = new javax.swing.JPanel();
        energyLevelTxt = new javax.swing.JTextField();
        energyLevelLbl = new javax.swing.JLabel();
        pauseVehicleCheckBox = new javax.swing.JCheckBox();
        orientationAngleLbl = new javax.swing.JLabel();
        precisePosUnitLabel = new javax.swing.JLabel();
        orientationAngleTxt = new javax.swing.JTextField();
        energyLevelLabel = new javax.swing.JLabel();
        orientationLabel = new javax.swing.JLabel();
        positionTxt = new javax.swing.JTextField();
        positionLabel = new javax.swing.JLabel();
        pauseVehicleLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        stateTxt = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        precisePosTextArea = new javax.swing.JTextArea();
        propertySetterPanel = new javax.swing.JPanel();
        keyLabel = new javax.swing.JLabel();
        valueTextField = new javax.swing.JTextField();
        propSetButton = new javax.swing.JButton();
        removePropRadioBtn = new javax.swing.JRadioButton();
        setPropValueRadioBtn = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        keyTextField = new javax.swing.JTextField();
        eventPanel = new javax.swing.JPanel();
        includeAppendixCheckBox = new javax.swing.JCheckBox();
        appendixTxt = new javax.swing.JTextField();
        dispatchEventButton = new javax.swing.JButton();
        dispatchCommandFailedButton = new javax.swing.JButton();
        controlTabPanel = new javax.swing.JPanel();
        singleModeRadioButton = new javax.swing.JRadioButton();
        flowModeRadioButton = new javax.swing.JRadioButton();
        triggerButton = new javax.swing.JButton();
        loadDevicePanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        lHDCheckbox = new javax.swing.JCheckBox();

        setName("LoopbackCommunicationAdapterPanel"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        vehicleBahaviourPanel.setLayout(new java.awt.BorderLayout());

        PropsPowerOuterContainerPanel.setLayout(new java.awt.BorderLayout());

        PropsPowerInnerContainerPanel.setLayout(new javax.swing.BoxLayout(PropsPowerInnerContainerPanel, javax.swing.BoxLayout.X_AXIS));

        vehiclePropsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("loopbackCommAdapterPanel.panel_vehicleProperties.border.title"))); // NOI18N
        vehiclePropsPanel.setLayout(new java.awt.GridBagLayout());

        maxFwdVeloLbl.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        maxFwdVeloLbl.setText(bundle.getString("loopbackCommAdapterPanel.label_maximumForwardVelocity.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        vehiclePropsPanel.add(maxFwdVeloLbl, gridBagConstraints);

        maxFwdVeloTxt.setEditable(false);
        maxFwdVeloTxt.setColumns(5);
        maxFwdVeloTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        maxFwdVeloTxt.setText("0");
        maxFwdVeloTxt.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        maxFwdVeloTxt.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        vehiclePropsPanel.add(maxFwdVeloTxt, gridBagConstraints);

        maxFwdVeloUnitLbl.setText("mm/s");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        vehiclePropsPanel.add(maxFwdVeloUnitLbl, gridBagConstraints);

        maxRevVeloLbl.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        maxRevVeloLbl.setText(bundle.getString("loopbackCommAdapterPanel.label_maximumReverseVelocity.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        vehiclePropsPanel.add(maxRevVeloLbl, gridBagConstraints);

        maxRevVeloTxt.setEditable(false);
        maxRevVeloTxt.setColumns(5);
        maxRevVeloTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        maxRevVeloTxt.setText("0");
        maxRevVeloTxt.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        maxRevVeloTxt.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        vehiclePropsPanel.add(maxRevVeloTxt, gridBagConstraints);

        maxRevVeloUnitLbl.setText("mm/s");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        vehiclePropsPanel.add(maxRevVeloUnitLbl, gridBagConstraints);

        maxAccelLbl.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        maxAccelLbl.setText(bundle.getString("loopbackCommAdapterPanel.label_maximumAcceleration.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        vehiclePropsPanel.add(maxAccelLbl, gridBagConstraints);

        maxAccelTxt.setEditable(false);
        maxAccelTxt.setColumns(5);
        maxAccelTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        maxAccelTxt.setText("1000");
        maxAccelTxt.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        maxAccelTxt.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        vehiclePropsPanel.add(maxAccelTxt, gridBagConstraints);

        maxAccelUnitLbl.setText("<html>mm/s<sup>2</sup>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        vehiclePropsPanel.add(maxAccelUnitLbl, gridBagConstraints);

        maxDecelTxt.setEditable(false);
        maxDecelTxt.setColumns(5);
        maxDecelTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        maxDecelTxt.setText("1000");
        maxDecelTxt.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        maxDecelTxt.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        vehiclePropsPanel.add(maxDecelTxt, gridBagConstraints);

        maxDecelLbl.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        maxDecelLbl.setText(bundle.getString("loopbackCommAdapterPanel.label_maximumDeceleration.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        vehiclePropsPanel.add(maxDecelLbl, gridBagConstraints);

        maxDecelUnitLbl.setText("<html>mm/s<sup>2</sup>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        vehiclePropsPanel.add(maxDecelUnitLbl, gridBagConstraints);

        defaultOpTimeLbl.setText(bundle.getString("loopbackCommAdapterPanel.label_operatingTime.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        vehiclePropsPanel.add(defaultOpTimeLbl, gridBagConstraints);

        defaultOpTimeUntiLbl.setText("ms");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        vehiclePropsPanel.add(defaultOpTimeUntiLbl, gridBagConstraints);

        opTimeTxt.setEditable(false);
        opTimeTxt.setColumns(5);
        opTimeTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        opTimeTxt.setText("1000");
        opTimeTxt.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        opTimeTxt.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        vehiclePropsPanel.add(opTimeTxt, gridBagConstraints);

        PropsPowerInnerContainerPanel.add(vehiclePropsPanel);

        PropsPowerOuterContainerPanel.add(PropsPowerInnerContainerPanel, java.awt.BorderLayout.WEST);

        vehicleBahaviourPanel.add(PropsPowerOuterContainerPanel, java.awt.BorderLayout.NORTH);

        profilesContainerPanel.setLayout(new java.awt.BorderLayout());
        profilesContainerPanel.add(filler1, java.awt.BorderLayout.CENTER);

        vehicleBahaviourPanel.add(profilesContainerPanel, java.awt.BorderLayout.SOUTH);

        add(vehicleBahaviourPanel, java.awt.BorderLayout.CENTER);

        vehicleStatePanel.setLayout(new java.awt.BorderLayout());

        stateContainerPanel.setLayout(new javax.swing.BoxLayout(stateContainerPanel, javax.swing.BoxLayout.Y_AXIS));

        connectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("loopbackCommAdapterPanel.panel_adapterStatus.border.title"))); // NOI18N
        connectionPanel.setName("connectionPanel"); // NOI18N
        connectionPanel.setLayout(new java.awt.GridBagLayout());

        chkBoxEnable.setText(bundle.getString("loopbackCommAdapterPanel.checkBox_enableAdapter.text")); // NOI18N
        chkBoxEnable.setName("chkBoxEnable"); // NOI18N
        chkBoxEnable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkBoxEnableActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        connectionPanel.add(chkBoxEnable, gridBagConstraints);

        stateContainerPanel.add(connectionPanel);

        curPosPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("loopbackCommAdapterPanel.panel_vehicleStatus.border.title"))); // NOI18N
        curPosPanel.setName("curPosPanel"); // NOI18N
        curPosPanel.setLayout(new java.awt.GridBagLayout());

        energyLevelTxt.setEditable(false);
        energyLevelTxt.setBackground(new java.awt.Color(255, 255, 255));
        energyLevelTxt.setText("100");
        energyLevelTxt.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        energyLevelTxt.setName("energyLevelTxt"); // NOI18N
        energyLevelTxt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                energyLevelTxtMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        curPosPanel.add(energyLevelTxt, gridBagConstraints);

        energyLevelLbl.setText("%");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        curPosPanel.add(energyLevelLbl, gridBagConstraints);

        pauseVehicleCheckBox.setEnabled(false);
        pauseVehicleCheckBox.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        pauseVehicleCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        pauseVehicleCheckBox.setName("pauseVehicleCheckBox"); // NOI18N
        pauseVehicleCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                pauseVehicleCheckBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        curPosPanel.add(pauseVehicleCheckBox, gridBagConstraints);

        orientationAngleLbl.setText("<html>&#186;");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        curPosPanel.add(orientationAngleLbl, gridBagConstraints);

        precisePosUnitLabel.setText("mm");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        curPosPanel.add(precisePosUnitLabel, gridBagConstraints);

        orientationAngleTxt.setEditable(false);
        orientationAngleTxt.setBackground(new java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/commadapter/loopback/Bundle"); // NOI18N
        orientationAngleTxt.setText(bundle.getString("loopbackCommAdapterPanel.textField_orientationAngle.angleNotSetPlaceholder")); // NOI18N
        orientationAngleTxt.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        orientationAngleTxt.setName("orientationAngleTxt"); // NOI18N
        orientationAngleTxt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                orientationAngleTxtMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        curPosPanel.add(orientationAngleTxt, gridBagConstraints);

        energyLevelLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        energyLevelLabel.setText(bundle.getString("loopbackCommAdapterPanel.label_energyLevel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
        curPosPanel.add(energyLevelLabel, gridBagConstraints);

        orientationLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        orientationLabel.setText(bundle.getString("loopbackCommAdapterPanel.label_orientationAngle.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
        curPosPanel.add(orientationLabel, gridBagConstraints);

        positionTxt.setEditable(false);
        positionTxt.setBackground(new java.awt.Color(255, 255, 255));
        positionTxt.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        positionTxt.setName("positionTxt"); // NOI18N
        positionTxt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                positionTxtMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        curPosPanel.add(positionTxt, gridBagConstraints);

        positionLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        positionLabel.setText(bundle.getString("loopbackCommAdapterPanel.label_position.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
        curPosPanel.add(positionLabel, gridBagConstraints);

        pauseVehicleLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        pauseVehicleLabel.setText(bundle.getString("loopbackCommAdapterPanel.label_pauseVehicle.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
        curPosPanel.add(pauseVehicleLabel, gridBagConstraints);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel2.setText(bundle.getString("loopbackCommAdapterPanel.label_state.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
        curPosPanel.add(jLabel2, gridBagConstraints);

        stateTxt.setEditable(false);
        stateTxt.setBackground(new java.awt.Color(255, 255, 255));
        stateTxt.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        stateTxt.setName("stateTxt"); // NOI18N
        stateTxt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                stateTxtMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        curPosPanel.add(stateTxt, gridBagConstraints);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel3.setText(bundle.getString("loopbackCommAdapterPanel.label_precisePosition.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
        curPosPanel.add(jLabel3, gridBagConstraints);

        precisePosTextArea.setEditable(false);
        precisePosTextArea.setFont(positionTxt.getFont());
        precisePosTextArea.setRows(3);
        precisePosTextArea.setText("X:\nY:\nZ:");
        precisePosTextArea.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        precisePosTextArea.setName("precisePosTextArea"); // NOI18N
        precisePosTextArea.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                precisePosTextAreaMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        curPosPanel.add(precisePosTextArea, gridBagConstraints);

        stateContainerPanel.add(curPosPanel);
        curPosPanel.getAccessibleContext().setAccessibleName("Change");

        propertySetterPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("loopbackCommAdapterPanel.panel_vehicleProperty.border.title"))); // NOI18N
        propertySetterPanel.setLayout(new java.awt.GridBagLayout());

        keyLabel.setText(bundle.getString("loopbackCommAdapterPanel.label_propertyKey.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        propertySetterPanel.add(keyLabel, gridBagConstraints);

        valueTextField.setMaximumSize(new java.awt.Dimension(4, 18));
        valueTextField.setMinimumSize(new java.awt.Dimension(4, 18));
        valueTextField.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        propertySetterPanel.add(valueTextField, gridBagConstraints);

        propSetButton.setText(bundle.getString("loopbackCommAdapterPanel.button_setProperty.text")); // NOI18N
        propSetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                propSetButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        propertySetterPanel.add(propSetButton, gridBagConstraints);

        propertyEditorGroup.add(removePropRadioBtn);
        removePropRadioBtn.setText(bundle.getString("loopbackCommAdapterPanel.radioButton_removeProperty.text")); // NOI18N
        removePropRadioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removePropRadioBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        propertySetterPanel.add(removePropRadioBtn, gridBagConstraints);

        propertyEditorGroup.add(setPropValueRadioBtn);
        setPropValueRadioBtn.setSelected(true);
        setPropValueRadioBtn.setText(bundle.getString("loopbackCommAdapterPanel.radioButton_setProperty.text")); // NOI18N
        setPropValueRadioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setPropValueRadioBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        propertySetterPanel.add(setPropValueRadioBtn, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        keyTextField.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel3.add(keyTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        propertySetterPanel.add(jPanel3, gridBagConstraints);

        stateContainerPanel.add(propertySetterPanel);

        eventPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("loopbackCommAdapterPanel.panel_eventDispatching.title"))); // NOI18N
        eventPanel.setLayout(new java.awt.GridBagLayout());

        includeAppendixCheckBox.setText(bundle.getString("loopbackCommAdapterPanel.checkBox_includeAppendix.text")); // NOI18N
        includeAppendixCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                includeAppendixCheckBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weightx = 1.0;
        eventPanel.add(includeAppendixCheckBox, gridBagConstraints);

        appendixTxt.setEditable(false);
        appendixTxt.setColumns(10);
        appendixTxt.setText("XYZ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        eventPanel.add(appendixTxt, gridBagConstraints);

        dispatchEventButton.setText(bundle.getString("loopbackCommAdapterPanel.button_dispatchEvent.text")); // NOI18N
        dispatchEventButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dispatchEventButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        eventPanel.add(dispatchEventButton, gridBagConstraints);

        dispatchCommandFailedButton.setText(bundle.getString("loopbackCommAdapterPanel.button_failCurrentCommand.text")); // NOI18N
        dispatchCommandFailedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dispatchCommandFailedButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        eventPanel.add(dispatchCommandFailedButton, gridBagConstraints);

        stateContainerPanel.add(eventPanel);

        controlTabPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("loopbackCommAdapterPanel.panel_commandProcessing.border.title"))); // NOI18N
        controlTabPanel.setLayout(new java.awt.GridBagLayout());

        modeButtonGroup.add(singleModeRadioButton);
        singleModeRadioButton.setText(bundle.getString("loopbackCommAdapterPanel.checkBox_commandProcessingManual.text")); // NOI18N
        singleModeRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        singleModeRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        singleModeRadioButton.setName("singleModeRadioButton"); // NOI18N
        singleModeRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                singleModeRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        controlTabPanel.add(singleModeRadioButton, gridBagConstraints);

        modeButtonGroup.add(flowModeRadioButton);
        flowModeRadioButton.setSelected(true);
        flowModeRadioButton.setText(bundle.getString("loopbackCommAdapterPanel.checkBox_commandProcessingAutomatic.text")); // NOI18N
        flowModeRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        flowModeRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        flowModeRadioButton.setName("flowModeRadioButton"); // NOI18N
        flowModeRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flowModeRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        controlTabPanel.add(flowModeRadioButton, gridBagConstraints);

        triggerButton.setText(bundle.getString("loopbackCommAdapterPanel.button_nextStep.text")); // NOI18N
        triggerButton.setEnabled(false);
        triggerButton.setName("triggerButton"); // NOI18N
        triggerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                triggerButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        controlTabPanel.add(triggerButton, gridBagConstraints);

        stateContainerPanel.add(controlTabPanel);

        vehicleStatePanel.add(stateContainerPanel, java.awt.BorderLayout.NORTH);

        loadDevicePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("loopbackCommAdapterPanel.panel_loadHandlingDevice.border.title"))); // NOI18N
        loadDevicePanel.setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());
        loadDevicePanel.add(jPanel1, java.awt.BorderLayout.SOUTH);

        lHDCheckbox.setText("Device loaded");
        lHDCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lHDCheckboxClicked(evt);
            }
        });
        jPanel2.add(lHDCheckbox);

        loadDevicePanel.add(jPanel2, java.awt.BorderLayout.WEST);

        vehicleStatePanel.add(loadDevicePanel, java.awt.BorderLayout.CENTER);

        add(vehicleStatePanel, java.awt.BorderLayout.WEST);

        getAccessibleContext().setAccessibleName(bundle.getString("loopbackCommAdapterPanel.accessibleName")); // NOI18N
    }// </editor-fold>                        

  private void singleModeRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                      
    if (singleModeRadioButton.isSelected()) {
      triggerButton.setEnabled(true);
      vehicleModel.setSingleStepModeEnabled(true);
    }
  }                                                     

  private void flowModeRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                    
    if (flowModeRadioButton.isSelected()) {
      triggerButton.setEnabled(false);
      vehicleModel.setSingleStepModeEnabled(false);
      commAdapter.trigger();
    }
  }                                                   

  private void triggerButtonActionPerformed(java.awt.event.ActionEvent evt) {                                              
    commAdapter.trigger();
  }                                             

private void chkBoxEnableActionPerformed(java.awt.event.ActionEvent evt) {                                             
  if (chkBoxEnable.isSelected()) {
    commAdapter.enable();
    setStatePanelEnabled(true);
  }
  else {
    commAdapter.disable();
    setStatePanelEnabled(false);
  }
}                                            


  private void precisePosTextAreaMouseClicked(java.awt.event.MouseEvent evt) {                                                
    if (precisePosTextArea.isEnabled()) {
      Triple pos = vehicleModel.getVehiclePrecisePosition();
      // Create panel and dialog
      TripleTextInputPanel.Builder builder
          = new TripleTextInputPanel.Builder(bundle.getString("loopbackCommAdapterPanel.dialog_setPrecisePosition.title"));
      builder.setUnitLabels("mm");
      builder.setLabels("X:", "Y:", "Z:");
      builder.enableResetButton(null);
      builder.enableValidation(TextInputPanel.TextInputValidator.REGEX_INT);
      if (pos != null) {
        builder.setInitialValues(Long.toString(pos.getX()),
                                 Long.toString(pos.getY()),
                                 Long.toString(pos.getZ()));
      }
      InputPanel panel = builder.build();
      InputDialog dialog = new InputDialog(panel);
      dialog.setVisible(true);
      // Get dialog result and set vehicle precise position
      if (dialog.getReturnStatus() == InputDialog.ReturnStatus.ACCEPTED) {
        if (dialog.getInput() == null) {
          // Clear precise position
          vehicleModel.setVehiclePrecisePosition(null);
        }
        else {
          // Set new precise position
          long x, y, z;
          String[] newPos = (String[]) dialog.getInput();
          try {
            x = Long.parseLong(newPos[0]);
            y = Long.parseLong(newPos[1]);
            z = Long.parseLong(newPos[2]);
          }
          catch (NumberFormatException | NullPointerException e) {
            return;
          }
          vehicleModel.setVehiclePrecisePosition(new Triple(x, y, z));
        }
      }
    }
  }                                               

  private void stateTxtMouseClicked(java.awt.event.MouseEvent evt) {                                      
    if (stateTxt.isEnabled()) {
      List<Vehicle.State> states
          = new ArrayList<>(Arrays.asList(Vehicle.State.values()));
      Vehicle.State currentState = vehicleModel.getVehicleState();
      // Create panel and dialog
      InputPanel panel = new DropdownListInputPanel.Builder<>(
          bundle.getString("loopbackCommAdapterPanel.dialog_setState.title"),
          states)
          .setLabel(bundle.getString("loopbackCommAdapterPanel.label_state.text"))
          .setInitialSelection(currentState)
          .build();
      InputDialog dialog = new InputDialog(panel);
      dialog.setVisible(true);
      // Get dialog results and set vahicle stare
      if (dialog.getReturnStatus() == InputDialog.ReturnStatus.ACCEPTED) {
        Vehicle.State newState = (Vehicle.State) dialog.getInput();
        if (newState != currentState) {
          vehicleModel.setVehicleState(newState);
        }
      }
    }
  }                                     

  private void positionTxtMouseClicked(java.awt.event.MouseEvent evt) {                                         
    if (positionTxt.isEnabled()) {
      // Prepare list of model points
      Set<Point> pointSet = objectService.fetchObjects(Point.class);
      List<Point> pointList = new ArrayList<>(pointSet);
      Collections.sort(pointList, Comparators.objectsByName());
      pointList.add(0, null);
      // Get currently selected point
      // TODO is there a better way to do this?
      Point currentPoint = null;
      String currentPointName = vehicleModel.getVehiclePosition();
      for (Point p : pointList) {
        if (p != null && p.getName().equals(currentPointName)) {
          currentPoint = p;
          break;
        }
      }
      // Create panel and dialog
      InputPanel panel = new DropdownListInputPanel.Builder<>(
          bundle.getString("loopbackCommAdapterPanel.dialog_setPosition.title"),
          pointList)
          .setLabel(bundle.getString("loopbackCommAdapterPanel.label_position.text"))
          .setEditable(true)
          .setInitialSelection(currentPoint)
          .setRenderer(new StringListCellRenderer<>(x -> x == null ? "" : x.getName()))
          .build();
      InputDialog dialog = new InputDialog(panel);
      dialog.setVisible(true);
      // Get result from dialog and set vehicle position
      if (dialog.getReturnStatus() == InputDialog.ReturnStatus.ACCEPTED) {
        Object item = dialog.getInput();
        if (item == null) {
          vehicleModel.setVehiclePosition(null);
        }
        else {
          vehicleModel.setVehiclePosition(((Point) item).getName());
        }

      }
    }
  }                                        

  private void orientationAngleTxtMouseClicked(java.awt.event.MouseEvent evt) {                                                 
    if (orientationAngleTxt.isEnabled()) {
      double currentAngle = vehicleModel.getVehicleOrientationAngle();
      String initialValue
          = (Double.isNaN(currentAngle) ? "" : Double.toString(currentAngle));
      // Create dialog and panel
      InputPanel panel = new SingleTextInputPanel.Builder(
          bundle.getString("loopbackCommAdapterPanel.dialog_setOrientationAngle.title"))
          .setLabel(bundle.getString("loopbackCommAdapterPanel.label_orientationAngle.text"))
          .setUnitLabel("<html>&#186;")
          .setInitialValue(initialValue)
          .enableResetButton(null)
          .enableValidation(TextInputPanel.TextInputValidator.REGEX_FLOAT)
          .build();
      InputDialog dialog = new InputDialog(panel);
      dialog.setVisible(true);
      // Get input from dialog
      InputDialog.ReturnStatus returnStatus = dialog.getReturnStatus();
      if (returnStatus == InputDialog.ReturnStatus.ACCEPTED) {
        String input = (String) dialog.getInput();
        if (input == null) { // The reset button was pressed
          if (!Double.isNaN(vehicleModel.getVehicleOrientationAngle())) {
            vehicleModel.setVehicleOrientationAngle(Double.NaN);
          }
        }
        else {
          // Set orientation provided by the user
          double angle;
          try {
            angle = Double.parseDouble(input);
          }
          catch (NumberFormatException e) {
            LOG.warn("Exception parsing orientation angle value '{}'", input, e);
            return;
          }
          vehicleModel.setVehicleOrientationAngle(angle);
        }
      }
    }
  }                                                

  private void pauseVehicleCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {                                                      
    if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
      vehicleModel.setVehiclePaused(true);
    }
    else if (evt.getStateChange() == java.awt.event.ItemEvent.DESELECTED) {
      vehicleModel.setVehiclePaused(false);
    }
  }                                                     

  private void energyLevelTxtMouseClicked(java.awt.event.MouseEvent evt) {                                            
    if (energyLevelTxt.isEnabled()) {
      // Create panel and dialog
      InputPanel panel = new SingleTextInputPanel.Builder(
          bundle.getString("loopbackCommAdapterPanel.dialog_setEnergyLevel.title"))
          .setLabel(bundle.getString("loopbackCommAdapterPanel.label_energyLevel.text"))
          .setUnitLabel("%")
          .setInitialValue(energyLevelTxt.getText())
          .enableValidation(TextInputPanel.TextInputValidator.REGEX_INT_RANGE_0_100)
          .build();
      InputDialog dialog = new InputDialog(panel);
      dialog.setVisible(true);
      // Get result from dialog and set energy level
      if (dialog.getReturnStatus() == InputDialog.ReturnStatus.ACCEPTED) {
        String input = (String) dialog.getInput();
        int energy;
        try {
          energy = Integer.parseInt(input);
        }
        catch (NumberFormatException e) {
          return;
        }
        vehicleModel.setVehicleEnergyLevel(energy);
      }
    }
  }                                           

  private void includeAppendixCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {                                                         
    appendixTxt.setEditable(includeAppendixCheckBox.isSelected());
  }                                                        

  private void dispatchEventButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                    
    String appendix = includeAppendixCheckBox.isSelected() ? appendixTxt.getText() : null;
    vehicleModel.publishEvent(new VehicleCommAdapterEvent(commAdapter.getName(), appendix));
  }                                                   

  private void dispatchCommandFailedButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                            
    MovementCommand failedCommand = commAdapter.getSentQueue().peek();
    if (failedCommand != null) {
      vehicleModel.commandFailed(commAdapter.getSentQueue().peek());
    }
  }                                                           

  private void propSetButtonActionPerformed(java.awt.event.ActionEvent evt) {                                              
    this.vehicleModel.setVehicleProperty(
        keyTextField.getText(),
        setPropValueRadioBtn.isSelected() ? valueTextField.getText() : null);
  }                                             

  private void removePropRadioBtnActionPerformed(java.awt.event.ActionEvent evt) {                                                   
    valueTextField.setEnabled(false);
  }                                                  

  private void setPropValueRadioBtnActionPerformed(java.awt.event.ActionEvent evt) {                                                     
    valueTextField.setEnabled(true);
  }                                                    

  private void lHDCheckboxClicked(java.awt.event.ActionEvent evt) {                                    
    this.vehicleModel.setVehicleLoadHandlingDevices(Arrays.asList(
        new LoadHandlingDevice(MqttCommunicationAdapter.LHD_NAME, lHDCheckbox.isSelected())));
  }                                   

  /**
   * Set the specified precise position to the text area. The method takes care
   * of the formatting. If any of the parameters is null all values will be set
   * to the "clear"-value.
   *将指定的精确位置设置到文本区域。 该方法负责格式化。 如果任何参数为null，则所有值都将设置为“清除”值。
   * @param x x-position
   * @param y y-position
   * @param z z-poition
   */
  private void setPrecisePosText(Long x, Long y, Long z) {
    // Convert values to srings
    String xS, yS, zS;
    try {
      xS = x.toString();
      yS = y.toString();
      zS = z.toString();
    }
    catch (NullPointerException e) {
      xS = yS = zS = bundle.getString("loopbackCommAdapterPanel.textArea_precisePosition.positionNotSetPlaceholder");
    }
    // Clip extremely long string values
    xS = (xS.length() > 20) ? (xS.substring(0, 20) + "...") : xS;
    yS = (yS.length() > 20) ? (yS.substring(0, 20) + "...") : yS;
    zS = (zS.length() > 20) ? (zS.substring(0, 20) + "...") : zS;
    // Build formatted text
    StringBuilder text = new StringBuilder("");
    text.append("X: ").append(xS).append("\n")
        .append("Y: ").append(yS).append("\n")
        .append("Z: ").append(zS);
    precisePosTextArea.setText(text.toString());
  }
    // Variables declaration - do not modify                     
    private javax.swing.JPanel PropsPowerInnerContainerPanel;
    private javax.swing.JPanel PropsPowerOuterContainerPanel;
    private javax.swing.JTextField appendixTxt;
    private javax.swing.JCheckBox chkBoxEnable;
    private javax.swing.JPanel connectionPanel;
    private javax.swing.JPanel controlTabPanel;
    private javax.swing.JPanel curPosPanel;
    private javax.swing.JLabel defaultOpTimeLbl;
    private javax.swing.JLabel defaultOpTimeUntiLbl;
    private javax.swing.JButton dispatchCommandFailedButton;
    private javax.swing.JButton dispatchEventButton;
    private javax.swing.JLabel energyLevelLabel;
    private javax.swing.JLabel energyLevelLbl;
    private javax.swing.JTextField energyLevelTxt;
    private javax.swing.JPanel eventPanel;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JRadioButton flowModeRadioButton;
    private javax.swing.JCheckBox includeAppendixCheckBox;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel keyLabel;
    private javax.swing.JTextField keyTextField;
    private javax.swing.JCheckBox lHDCheckbox;
    private javax.swing.JPanel loadDevicePanel;
    private javax.swing.JLabel maxAccelLbl;
    private javax.swing.JTextField maxAccelTxt;
    private javax.swing.JLabel maxAccelUnitLbl;
    private javax.swing.JLabel maxDecelLbl;
    private javax.swing.JTextField maxDecelTxt;
    private javax.swing.JLabel maxDecelUnitLbl;
    private javax.swing.JLabel maxFwdVeloLbl;
    private javax.swing.JTextField maxFwdVeloTxt;
    private javax.swing.JLabel maxFwdVeloUnitLbl;
    private javax.swing.JLabel maxRevVeloLbl;
    private javax.swing.JTextField maxRevVeloTxt;
    private javax.swing.JLabel maxRevVeloUnitLbl;
    private javax.swing.ButtonGroup modeButtonGroup;
    private javax.swing.JTextField opTimeTxt;
    private javax.swing.JLabel orientationAngleLbl;
    private javax.swing.JTextField orientationAngleTxt;
    private javax.swing.JLabel orientationLabel;
    private javax.swing.JCheckBox pauseVehicleCheckBox;
    private javax.swing.JLabel pauseVehicleLabel;
    private javax.swing.JLabel positionLabel;
    private javax.swing.JTextField positionTxt;
    private javax.swing.JTextArea precisePosTextArea;
    private javax.swing.JLabel precisePosUnitLabel;
    private javax.swing.JPanel profilesContainerPanel;
    private javax.swing.JButton propSetButton;
    private javax.swing.ButtonGroup propertyEditorGroup;
    private javax.swing.JPanel propertySetterPanel;
    private javax.swing.JRadioButton removePropRadioBtn;
    private javax.swing.JRadioButton setPropValueRadioBtn;
    private javax.swing.JRadioButton singleModeRadioButton;
    private javax.swing.JPanel stateContainerPanel;
    private javax.swing.JTextField stateTxt;
    private javax.swing.JButton triggerButton;
    private javax.swing.JTextField valueTextField;
    private javax.swing.JPanel vehicleBahaviourPanel;
    private javax.swing.JPanel vehiclePropsPanel;
    private javax.swing.JPanel vehicleStatePanel;
    // End of variables declaration                   
  // CHECKSTYLE:ON

}
