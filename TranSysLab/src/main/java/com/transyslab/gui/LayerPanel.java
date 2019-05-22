/*
 * Copyright 2019 The TranSysLab Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.transyslab.gui;
import com.transyslab.roadnetwork.*;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;


public class LayerPanel {

    private Map<String, JTabbedPane> layers;

    public LayerPanel() {
        layers = new HashMap<>();
        //panelAction = new HashMap<>();
        NodePanel nodeLayer = new NodePanel();
        LinkPanel linkLayer = new LinkPanel();
        SegmentPanel segmentLayer = new SegmentPanel();
        LanePanel laneLayer = new LanePanel();
        SensorPanel sensorLayer = new SensorPanel();
        VehiclePanel vehicleLayer = new VehiclePanel();
        layers.put("Node", nodeLayer);
        layers.put("Link", linkLayer);
        layers.put("Segment", segmentLayer);
        layers.put("Lane", laneLayer);
        layers.put("Vehicle", vehicleLayer);
        layers.put("Sensor", sensorLayer);
//        panelAction.put("Node", nodeLayer);
//        panelAction.put("Link", linkLayer);
//        panelAction.put("Segment", segmentLayer);
//        panelAction.put("Lane", laneLayer);
//        panelAction.put("Vehicle", vehicleLayer);
//        panelAction.put("Sensor", sensorLayer);
    }

    public JTabbedPane getLayer(String layerName) {
        return layers.get(layerName);
    }

    public class VehiclePanel extends JTabbedPane implements PanelAction {

        private JTextField textField1;//编号
        private JTextField textField4;//类型
        private JTextField textField2;//长度
        private JTextField textField5;//当前车速
        private JTextField textField3;//所在车道
        private JTextField textField6;//起点
        private JTextField textField7;//终点
        private JTextField textField8;//路径
        private JTextField textField9;
        private JTextField textField10;
        private JTextField textField11;
        private JTextField[] textFields;
        private JTextArea textArea4;//其它

        public VehiclePanel() {
            initComponents();
        }

        public void resetTxtComponents() {
            for (JTextField tmpText : textFields) {
                tmpText.setText("");
            }
            textArea4.setText("");
        }

        public void writeTxtComponents(NetworkObject object) {
            VehicleData vhcData = (VehicleData) object;
            textField1.setText(String.valueOf(vhcData.getId()));
            textField2.setText(String.format("%.2f", vhcData.getVhcLength()));
            textField3.setText(String.valueOf(vhcData.getCurLaneID()));
            textField4.setText(((vhcData.getSpecialFlag() & Constants.VIRTUAL_VEHICLE) != 0 ? "虚拟车" : "真实车") +
                    ((vhcData.getSpecialFlag() & Constants.FOLLOWING) != 0 ? " 队内" : " 队外"));
            textField5.setText(String.format("%.2f", vhcData.getCurSpeed()));
            textField6.setText(String.valueOf(vhcData.getOriNodeID()));
            textField7.setText(String.valueOf(vhcData.getDesNodeID()));
            textField8.setText(vhcData.getPathInfo());
            textArea4.setText(vhcData.getVhcInfo());
        }

        private void initComponents() {
            JPanel attrPanel = new JPanel();
            JPanel statPanel = new JPanel();
            JLabel label1 = new JLabel();
            textField1 = new JTextField();
            JLabel label5 = new JLabel();
            textField4 = new JTextField();
            JLabel label2 = new JLabel();
            textField2 = new JTextField();
            JLabel label8 = new JLabel();
            textField5 = new JTextField();
            JLabel label3 = new JLabel();
            textField3 = new JTextField();
            JPanel panel5 = new JPanel();
            JLabel label6 = new JLabel();
            textField6 = new JTextField();
            JLabel label7 = new JLabel();
            textField7 = new JTextField();
            JLabel label9 = new JLabel();
            textField8 = new JTextField();
            textFields = new JTextField[]{textField1, textField2, textField3, textField4,
                    textField5, textField6, textField7, textField8};

            JLabel label4 = new JLabel();
            JLabel label11 = new JLabel();
            textField9 = new JTextField();
            JLabel label12 = new JLabel();
            textField10 = new JTextField();
            JLabel label13 = new JLabel();
            textField11 = new JTextField();
            JScrollPane scrollPane4 = new JScrollPane();
            textArea4 = new JTextArea();

            //======== this ========
            this.setBorder(new EmptyBorder(3, 3, 3, 3));
            this.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            attrPanel.setLayout(new GridBagLayout());
            ((GridBagLayout) attrPanel.getLayout()).columnWidths = new int[]{0, 0, 0};
            ((GridBagLayout) attrPanel.getLayout()).rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 70, 0, 0};
            ((GridBagLayout) attrPanel.getLayout()).columnWeights = new double[]{0.0, 1.0, 1.0E-4};
            ((GridBagLayout) attrPanel.getLayout()).rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};

            //---- label1 ----
            label1.setText("\u7f16\u53f7\uff1a");
            label1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            attrPanel.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 0), 0, 0));
            attrPanel.add(textField1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //---- label5 ----
            label5.setText("\u7c7b\u578b\uff1a");
            label5.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            attrPanel.add(label5, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 0), 0, 0));
            attrPanel.add(textField4, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //---- label2 ----
            label2.setText("\u957f\u5ea6\uff1a");
            label2.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            attrPanel.add(label2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 0), 0, 0));
            attrPanel.add(textField2, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //---- label8 ----
            label8.setText("\u5f53\u524d\u8f66\u901f\uff1a");
            label8.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            attrPanel.add(label8, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 0), 0, 0));
            attrPanel.add(textField5, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //---- label3 ----
            label3.setText("\u6240\u5728\u8f66\u9053\uff1a");
            label3.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            attrPanel.add(label3, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 0), 0, 0));
            attrPanel.add(textField3, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //======== panel5 ========
            {
                panel5.setBorder(new CompoundBorder(
                        new TitledBorder(null, "\u8def\u5f84\u4fe1\u606f", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
                                new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 13)),
                        BorderFactory.createEmptyBorder()));
                panel5.setLayout(new GridBagLayout());
                ((GridBagLayout) panel5.getLayout()).columnWidths = new int[]{0, 0, 0};
                ((GridBagLayout) panel5.getLayout()).rowHeights = new int[]{0, 0, 0, 0};
                ((GridBagLayout) panel5.getLayout()).columnWeights = new double[]{0.0, 1.0, 1.0E-4};
                ((GridBagLayout) panel5.getLayout()).rowWeights = new double[]{0.0, 0.0, 0.0, 1.0E-4};

                //---- label6 ----
                label6.setText("\u8d77\u70b9\uff1a");
                label6.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(label6, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 5, 5, 5), 0, 0));
                panel5.add(textField6, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 14, 5, 0), 0, 0));

                //---- label7 ----
                label7.setText("\u7ec8\u70b9\uff1a");
                label7.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(label7, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 5, 5, 5), 0, 0));
                panel5.add(textField7, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 14, 5, 0), 0, 0));

                //---- label9 ----
                label9.setText("\u8def\u5f84\uff1a");
                label9.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(label9, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 5, 0, 5), 0, 0));
                panel5.add(textField8, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 14, 0, 0), 0, 0));
            }
            attrPanel.add(panel5, new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

            //---- label4 ----
            label4.setText("\u5176\u5b83\uff1a");
            label4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            attrPanel.add(label4, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 0), 0, 0));

            //======== scrollPane4 ========
            {
                scrollPane4.setViewportView(textArea4);
            }
            attrPanel.add(scrollPane4, new GridBagConstraints(1, 6, 1, 2, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 0, 3), 0, 0));
            this.addTab("属性",attrPanel);

            //======== panel2 ========
            {
                statPanel.setLayout(new GridBagLayout());
                ((GridBagLayout)statPanel.getLayout()).columnWidths = new int[] {0, 0, 0};
                ((GridBagLayout)statPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
                ((GridBagLayout)statPanel.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
                ((GridBagLayout)statPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

                //---- label11 ----
                label11.setText("停车次数");
                label11.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                statPanel.add(label11, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 5, 5, 8), 0, 0));
                statPanel.add(textField9, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 3, 5, 5), 0, 0));

                //---- label12 ----
                label12.setText("排队时间");
                label12.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                statPanel.add(label12, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 5, 5, 8), 0, 0));
                statPanel.add(textField10, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 3, 5, 5), 0, 0));

                //---- label13 ----
                label13.setText("旅行时间");
                label13.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                statPanel.add(label13, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 5, 5, 8), 0, 0));
                statPanel.add(textField11, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 3, 5, 5), 0, 0));
                /*
                //---- label10 ----
                label10.setText("平均旅行时间");
                label10.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel2.add(label10, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 5, 5, 8), 0, 0));
                panel2.add(textField7, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 3, 5, 5), 0, 0));*/
            }
            this.addTab("\u7edf\u8ba1", statPanel);

        }
    }

    public class SensorPanel extends JTabbedPane implements PanelAction {

        private JTextField textField1;//编号
        private JTextField textField2;//类型
        private JTextField textField3;//隶属于
        private JTextField textField5;//开始时间
        private JTextField textField4;//统计间隔
        private JTextField[] textFields;
        private JButton button1;//查看数据
        private JTextArea textArea4;//其它

        public SensorPanel() {
            initComponents();
        }

        public void resetTxtComponents() {
            for (JTextField tmpText : textFields) {
                tmpText.setText("");
            }
            textArea4.setText("");
        }

        // TODO 检测器未统一
        public void writeTxtComponents(NetworkObject object) {
            Sensor theSensor = (Sensor) object;
            textField1.setText(String.valueOf(theSensor.getId()));
            textField2.setText(String.valueOf(theSensor.getObjInfo()));
        }

        private void initComponents() {
            JPanel attrPanel = new JPanel();
            JLabel label1 = new JLabel();
            textField1 = new JTextField();
            JLabel label2 = new JLabel();
            textField2 = new JTextField();
            JLabel label3 = new JLabel();
            textField3 = new JTextField();
            JPanel panel5 = new JPanel();
            JLabel label5 = new JLabel();
            textField5 = new JTextField();
            JLabel label6 = new JLabel();
            textField4 = new JTextField();
            textFields = new JTextField[]{textField1, textField2, textField3, textField4, textField5};
            button1 = new JButton();
            JLabel label4 = new JLabel();
            JScrollPane scrollPane4 = new JScrollPane();
            textArea4 = new JTextArea();

            //======== this ========
            this.setBorder(new EmptyBorder(3, 3, 3, 3));
            this.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            attrPanel.setLayout(new GridBagLayout());
            ((GridBagLayout) attrPanel.getLayout()).columnWidths = new int[]{0, 0, 0};
            ((GridBagLayout) attrPanel.getLayout()).rowHeights = new int[]{0, 0, 0, 0, 0, 70, 0, 0};
            ((GridBagLayout) attrPanel.getLayout()).columnWeights = new double[]{0.0, 1.0, 1.0E-4};
            ((GridBagLayout) attrPanel.getLayout()).rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};

            //---- label1 ----
            label1.setText("\u7f16\u53f7\uff1a");
            label1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            attrPanel.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));
            attrPanel. add(textField1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //---- label2 ----
            label2.setText("\u7c7b\u578b\uff1a");
            label2.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            attrPanel.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));
            attrPanel.add(textField2, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //---- label3 ----
            label3.setText("\u96b6\u5c5e\u4e8e\uff1a");
            label3.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            attrPanel.add(label3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));
            attrPanel.add(textField3, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //======== panel5 ========
            {
                panel5.setBorder(new CompoundBorder(
                        new TitledBorder(null, "\u68c0\u6d4b\u4fe1\u606f", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
                                new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 13)),
                        BorderFactory.createEmptyBorder()));
                panel5.setLayout(new GridBagLayout());
                ((GridBagLayout) panel5.getLayout()).columnWidths = new int[]{38, 0, 0};
                ((GridBagLayout) panel5.getLayout()).rowHeights = new int[]{0, 0, 0, 0};
                ((GridBagLayout) panel5.getLayout()).columnWeights = new double[]{0.0, 1.0, 1.0E-4};
                ((GridBagLayout) panel5.getLayout()).rowWeights = new double[]{0.0, 0.0, 0.0, 1.0E-4};

                //---- label5 ----
                label5.setText("\u5f00\u59cb\u65f6\u95f4\uff1a");
                label5.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(label5, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 3, 5, 0), 0, 0));
                panel5.add(textField5, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 0), 0, 0));

                //---- label6 ----
                label6.setText("\u7edf\u8ba1\u95f4\u9694\uff1a");
                label6.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(label6, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 3, 5, 0), 0, 0));
                panel5.add(textField4, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 0), 0, 0));

                //---- button1 ----
                button1.setText("\u67e5\u770b\u6570\u636e");
                button1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(button1, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
            }
            attrPanel.add(panel5, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

            //---- label4 ----
            label4.setText("\u5176\u5b83\uff1a");
            label4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            attrPanel.add(label4, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));

            //======== scrollPane4 ========
            {
                scrollPane4.setViewportView(textArea4);
            }
            attrPanel.add(scrollPane4, new GridBagConstraints(1, 4, 1, 2, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 0, 3), 0, 0));
            this.addTab("属性",attrPanel);
        }

    }

    public class LanePanel extends JTabbedPane implements PanelAction {

        private JTextField textField1;//编号
        private JTextField textField2;//长度
        private JTextField textField3;//隶属于
        private JTextField[] textFields;
        private JComboBox<String> comboBox2;//横向规则
        private JComboBox<String> comboBox3;//纵向规则
        private JTextArea textArea4;//其它

        private JTextField textField4;//排队长度
        private JTextField textField5;//停车次数
        private JTextField textField6;//排队时间
        private JTextField textField7;//旅行时间
        private JTextField textField8;//畅行车速

        public LanePanel() {
            initComponents();
        }

        public void resetTxtComponents() {

            for (JTextField tmpText : textFields) {
                tmpText.setText("");
            }
            textArea4.setText("");
        }

        public void writeTxtComponents(NetworkObject object) {

            Lane theLane = (Lane) object;
            textField1.setText(String.valueOf(theLane.getId()));
            textField2.setText(String.valueOf(theLane.getLength()));
            String segmentID = "Segment" + String.valueOf(theLane.getSegment().getId());
            textField3.setText(segmentID);
            textArea4.setText(
                    "SegID " + theLane.getSegment().getId() + "\n" +
                            "LnkID " + theLane.getLink().getId() + "\n");
//                    ((MLPLane) theLane).getSDnLnInfo());*/

        }


        private void initComponents() {
            JPanel panel1 = new JPanel();
            JLabel label1 = new JLabel();
            textField1 = new JTextField();
            textField2 = new JTextField();
            JLabel label2 = new JLabel();
            JLabel label3 = new JLabel();
            textField3 = new JTextField();
            JPanel panel5 = new JPanel();
            JLabel label6 = new JLabel();
            comboBox2 = new JComboBox<>();
            JLabel label7 = new JLabel();
            comboBox3 = new JComboBox<>();
            JLabel label4 = new JLabel();
            JScrollPane scrollPane4 = new JScrollPane();
            textArea4 = new JTextArea();
            JPanel panel2 = new JPanel();
            JLabel label5 = new JLabel();
            textField4 = new JTextField();
            JLabel label8 = new JLabel();
            textField5 = new JTextField();
            JLabel label9 = new JLabel();
            textField6 = new JTextField();
            JLabel label10 = new JLabel();
            textField7 = new JTextField();
            JLabel label11 = new JLabel();
            textField8 = new JTextField();
            textFields = new JTextField[]{textField1,textField2,textField3,textField4,textField5,textField6,textField7, textField8};
            //======== tabbedPane1 ========
            {
                this.setBorder(new EmptyBorder(3, 3, 3, 3));
                this.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                //======== panel1属性 ========
                {
                    panel1.setLayout(new GridBagLayout());
                    ((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0};
                    ((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 75, 0, 0};
                    ((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
                    ((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};

                    //---- label1 ----
                    label1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                    label1.setText("\u7f16\u53f7\uff1a");
                    panel1.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 5, 5, 8), 0, 0));
                    panel1.add(textField1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 3, 5, 5), 0, 0));
                    panel1.add(textField2, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 3, 5, 5), 0, 0));

                    //---- label2 ----
                    label2.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                    label2.setText("\u957f\u5ea6\uff1a");
                    panel1.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 5, 5, 8), 0, 0));

                    //---- label3 ----
                    label3.setText("\u96b6\u5c5e\u4e8e\uff1a");
                    label3.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                    panel1.add(label3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 5, 5, 8), 0, 0));
                    panel1.add(textField3, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 3, 5, 5), 0, 0));

                    //======== panel5 ========
                    {
                        panel5.setBorder(new CompoundBorder(
                                new TitledBorder(null, "\u4ea4\u901a\u89c4\u5219", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
                                        new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 12)),
                                new EmptyBorder(2, 2, 2, 2)));
                        panel5.setFont(new Font("\u5b8b\u4f53", Font.PLAIN, 12));
                        panel5.setLayout(new GridBagLayout());
                        ((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {0, 0, 0};
                        ((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0, 0};
                        ((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
                        ((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

                        //---- label6 ----
                        label6.setText("\u6a2a\u5411\uff1a");
                        label6.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                        panel5.add(label6, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                new Insets(0, 6, 5, 5), 0, 0));

                        //---- comboBox2 ----
                        comboBox2.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                        comboBox2.setModel(new DefaultComboBoxModel<>(new String[] {
                                "\u5141\u8bb8\u5de6\u53f3\u6362\u9053",
                                "\u4ec5\u5141\u8bb8\u5de6\u6362\u9053",
                                "\u4ec5\u5141\u8bb8\u53f3\u6362\u9053",
                                "\u7981\u6b62\u6362\u9053"
                        }));
                        panel5.add(comboBox2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                new Insets(0, 6, 5, 0), 0, 0));

                        //---- label7 ----
                        label7.setText("\u7eb5\u5411\uff1a");
                        label7.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                        panel5.add(label7, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                new Insets(0, 6, 0, 5), 0, 0));

                        //---- comboBox3 ----
                        comboBox3.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                        comboBox3.setModel(new DefaultComboBoxModel<>(new String[] {
                                "\u76f4\u884c",
                                "\u4e13\u5de6",
                                "\u4e13\u53f3",
                                "\u76f4\u5de6",
                                "\u76f4\u53f3",
                                "\u76f4\u5de6\u53f3"
                        }));
                        panel5.add(comboBox3, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                new Insets(0, 6, 0, 0), 0, 0));
                    }
                    panel1.add(panel5, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 3, 5, 3), 0, 0));

                    //---- label4 ----
                    label4.setText("\u5176\u5b83\uff1a");
                    label4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                    panel1.add(label4, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 5, 5, 8), 0, 0));

                    //======== scrollPane4 ========
                    {
                        scrollPane4.setViewportView(textArea4);
                    }
                    panel1.add(scrollPane4, new GridBagConstraints(1, 4, 1, 2, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 3, 5, 5), 0, 0));
                }
                this.addTab("\u5c5e\u6027", panel1);

                //======== panel2 ========
                {
                    panel2.setLayout(new GridBagLayout());
                    ((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0, 0};
                    ((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
                    ((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
                    ((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

                    //---- label5 ----
                    label5.setText("最大排队长度");
                    label5.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                    panel2.add(label5, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 5, 5, 8), 0, 0));
                    panel2.add(textField4, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 3, 5, 5), 0, 0));

                    //---- label8 ----
                    label8.setText("平均停车次数");
                    label8.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                    panel2.add(label8, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 5, 5, 8), 0, 0));
                    panel2.add(textField5, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 3, 5, 5), 0, 0));

                    //---- label9 ----
                    label9.setText("平均排队时间");
                    label9.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                    panel2.add(label9, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 5, 5, 8), 0, 0));
                    panel2.add(textField6, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 3, 5, 5), 0, 0));

                    //---- label10 ----
                    label10.setText("平均旅行时间");
                    label10.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                    panel2.add(label10, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 5, 5, 8), 0, 0));
                    panel2.add(textField7, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 3, 5, 5), 0, 0));
                    //---- label11 ----
                    label11.setText("畅行车速");
                    label11.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                    panel2.add(label11, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 5, 5, 8), 0, 0));
                    panel2.add(textField8, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 3, 5, 5), 0, 0));
                }
                this.addTab("\u7edf\u8ba1", panel2);
            }
        }

    }

    public class SegmentPanel extends JTabbedPane implements PanelAction{

        private JTextField textField1;//编号
        private JTextField textField2;//长度
        private JTextField textField3;//隶属于
        private JTextField textField4;//限速
        private JTextField textField5;//控制
        private JTextField textField6;
        private JTextField textField7;
        private JTextField textField8;
        private JTextField textField9;
        private JTextField textField10;
        private JTextField[] textFields;
        private JTextArea textArea4;//其它


        public SegmentPanel() {
            initComponents();
        }
        public void resetTxtComponents(){
            for(JTextField tmpText:textFields){
                tmpText.setText("");
            }
            textArea4.setText("");
        }
        public void writeTxtComponents(NetworkObject object){
            Segment theSegment = (Segment) object;
            textField1.setText(String.valueOf(theSegment.getId()));
            textField2.setText(String.valueOf(theSegment.getLength()));
            String linkID = "Link"+ String.valueOf(theSegment.getLink().getId());
            textField3.setText(linkID);
            textField4.setText(String.valueOf(theSegment.getFreeSpeed()));
            // TODO 控制策略
            //textField5.setText(String.valueOf(theSegment));
        }
        private void initComponents() {
            JPanel attrPanel = new JPanel();
            JPanel statPanel = new JPanel();
            JLabel label1 = new JLabel();
            textField1 = new JTextField();
            JLabel label2 = new JLabel();
            textField2 = new JTextField();
            JLabel label3 = new JLabel();
            textField3 = new JTextField();
            JPanel panel5 = new JPanel();
            JLabel label6 = new JLabel();
            textField4 = new JTextField();
            JLabel label7 = new JLabel();
            textField5 = new JTextField();
            JLabel label11 = new JLabel();
            textField6 =  new JTextField();
            JLabel label12 = new JLabel();
            textField7 = new JTextField();
            JLabel label13 = new JLabel();
            textField8 = new JTextField();
            JLabel label14 = new JLabel();
            textField9 = new JTextField();
            JLabel label15 = new JLabel();
            textField10 = new JTextField();
            textFields = new JTextField[]{textField1, textField2, textField3, textField4, textField5,textField6
                    ,textField7,textField8,textField9,textField10};
            JLabel label4 = new JLabel();
            JScrollPane scrollPane4 = new JScrollPane();
            textArea4 = new JTextArea();

            //======== this ========

            this.setBorder(new EmptyBorder(3, 3, 3, 3));
            this.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            attrPanel.setLayout(new GridBagLayout());
            ((GridBagLayout)attrPanel.getLayout()).columnWidths = new int[] {0, 0, 0};
            ((GridBagLayout)attrPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 70, 0, 0};
            ((GridBagLayout)attrPanel.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
            ((GridBagLayout)attrPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};

            //---- label1 ----
            label1.setText("\u7f16\u53f7\uff1a");
            label1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            attrPanel.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));
            attrPanel.add(textField1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //---- label2 ----
            label2.setText("\u957f\u5ea6\uff1a");
            label2.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            attrPanel.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));
            attrPanel.add(textField2, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //---- label3 ----
            label3.setText("\u96b6\u5c5e\u4e8e\uff1a");
            label3.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            attrPanel.add(label3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));
            attrPanel.add(textField3, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //======== panel5 ========
            {
                panel5.setBorder(new CompoundBorder(
                        new TitledBorder(null, "\u4ea4\u901a\u89c4\u5219", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
                                new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 13)),
                        BorderFactory.createEmptyBorder()));
                panel5.setLayout(new GridBagLayout());
                ((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {0, 0, 0};
                ((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0, 0};
                ((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
                ((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

                //---- label6 ----
                label6.setText("\u9650\u901f\uff1a");
                label6.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(label6, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 5, 5, 5), 0, 0));
                panel5.add(textField4, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 7, 5, 0), 0, 0));

                //---- label7 ----
                label7.setText("\u63a7\u5236\uff1a");
                label7.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(label7, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 5, 0, 5), 0, 0));
                panel5.add(textField5, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 7, 0, 0), 0, 0));
            }
            attrPanel.add(panel5, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

            //---- label4 ----
            label4.setText("\u5176\u5b83\uff1a");
            label4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            attrPanel.add(label4, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));

            //======== scrollPane4 ========
            {

                //---- textArea4 ----
                textArea4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 13));
                scrollPane4.setViewportView(textArea4);
            }
            attrPanel.add(scrollPane4, new GridBagConstraints(1, 4, 1, 2, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 0, 3), 0, 0));
            this.addTab("属性",attrPanel);

            //======== panel2 ========
            {
                statPanel.setLayout(new GridBagLayout());
                ((GridBagLayout)statPanel.getLayout()).columnWidths = new int[] {0, 0, 0};
                ((GridBagLayout)statPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
                ((GridBagLayout)statPanel.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
                ((GridBagLayout)statPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

                //---- label11 ----
                label11.setText("最大排队长度");
                label11.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                statPanel.add(label11, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 5, 5, 8), 0, 0));
                statPanel.add(textField6, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 3, 5, 5), 0, 0));

                //---- label12 ----
                label12.setText("平均停车次数");
                label12.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                statPanel.add(label12, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 5, 5, 8), 0, 0));
                statPanel.add(textField7, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 3, 5, 5), 0, 0));

                //---- label13 ----
                label13.setText("平均排队时间");
                label13.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                statPanel.add(label13, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 5, 5, 8), 0, 0));
                statPanel.add(textField8, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 3, 5, 5), 0, 0));

                //---- label14 ----
                label14.setText("平均旅行时间");
                label14.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                statPanel.add(label14, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 5, 5, 8), 0, 0));
                statPanel.add(textField9, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 3, 5, 5), 0, 0));

                //---- label15 ----
                label15.setText("\u7545\u884c\u8f66\u901f\uff1a");
                label15.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                statPanel.add(label15, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 5, 5, 8), 0, 0));
                statPanel.add(textField10, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 3, 5, 5), 0, 0));
            }
            this.addTab("\u7edf\u8ba1", statPanel);

        }


    }

    public class LinkPanel extends JTabbedPane implements PanelAction{

        private JTextField textField1;//编号
        private JTextField textField3;//类型
        private JTextField textField2;//长度
        private JTextField textField4;//上游节点
        private JTextField textField5;//下游节点
        private JTextField[] textFields;
        private JTextArea textArea4;//其它

        public LinkPanel() {
            initComponents();
        }

        public void resetTxtComponents(){
            for(JTextField tmpText:textFields){
                tmpText.setText("");
            }
            textArea4.setText("");
        }
        public void writeTxtComponents(NetworkObject object){
            Link theLink = (Link) object;
            textField1.setText(String.valueOf(theLink.getId()));
            // TODO 类型&长度(删除)
            textField3.setText(String.valueOf(theLink.getName()));
            String upNodeID = "Node"+String.valueOf(theLink.getUpNode().getId());
            String dnNodeID = "Node"+String.valueOf(theLink.getDnNode().getId());
            textField4.setText(upNodeID);
            textField5.setText(dnNodeID);
        }
        private void initComponents() {
            JPanel attrPanel =  new JPanel();
            JLabel label1 = new JLabel();
            textField1 = new JTextField();
            JLabel label3 = new JLabel();
            textField3 = new JTextField();
            JLabel label2 = new JLabel();
            textField2 = new JTextField();
            JPanel panel5 = new JPanel();
            JLabel label6 = new JLabel();
            textField4 = new JTextField();
            JLabel label7 = new JLabel();
            textField5 = new JTextField();
            textFields = new JTextField[]{textField1, textField2, textField3, textField4, textField5};
            JLabel label4 = new JLabel();
            JScrollPane scrollPane4 = new JScrollPane();
            textArea4 = new JTextArea();

            //======== this ========
            this.setBorder(new EmptyBorder(3, 3, 3, 3));
            this.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            attrPanel.setLayout(new GridBagLayout());
            ((GridBagLayout)attrPanel.getLayout()).columnWidths = new int[] {0, 0, 0};
            ((GridBagLayout)attrPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 70, 0, 0};
            ((GridBagLayout)attrPanel.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
            ((GridBagLayout)attrPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};

            //---- label1 ----
            label1.setText("\u7f16\u53f7\uff1a");
            label1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            attrPanel.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));
            attrPanel.add(textField1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //---- label3 ----
            label3.setText("\u7c7b\u578b\uff1a");
            label3.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            attrPanel.add(label3, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));
            attrPanel.add(textField3, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //---- label2 ----
            label2.setText("\u957f\u5ea6\uff1a");
            label2.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            attrPanel.add(label2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));
            attrPanel.add(textField2, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //======== panel5 ========
            {
                panel5.setBorder(new CompoundBorder(
                        new TitledBorder(null, "\u8fde\u63a5\u8282\u70b9", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
                                new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 13)),
                        BorderFactory.createEmptyBorder()));
                panel5.setLayout(new GridBagLayout());
                ((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {28, 0, 0};
                ((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0, 0};
                ((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
                ((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

                //---- label6 ----
                label6.setText("\u4e0a\u6e38\uff1a");
                label6.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(label6, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 3, 5, 0), 0, 0));
                panel5.add(textField4, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 2, 5, 0), 0, 0));

                //---- label7 ----
                label7.setText("\u4e0b\u6e38\uff1a");
                label7.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(label7, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 3, 0, 0), 0, 0));
                panel5.add(textField5, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 2, 0, 0), 0, 0));
            }
            attrPanel.add(panel5, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

            //---- label4 ----
            label4.setText("\u5176\u5b83\uff1a");
            label4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            attrPanel.add(label4, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));

            //======== scrollPane4 ========
            {

                //---- textArea4 ----
                textArea4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 13));
                scrollPane4.setViewportView(textArea4);
            }
            attrPanel.add(scrollPane4, new GridBagConstraints(1, 4, 1, 2, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));
            this.addTab("属性",attrPanel);
        }
    }
    public class NodePanel extends JTabbedPane implements PanelAction{

        private JTextField textField1; //编号
        private JTextField textField2; //类型
        private JTextField textField3; //x
        private JTextField textField4; //y
        private JTextField textField5; //z
        private JTextField[] textFields;
        private JTextArea textArea4; //其它信息
        public NodePanel() {
            initComponents();
        }
        public void resetTxtComponents(){
            for(JTextField tmpText:textFields){
                tmpText.setText("");
            }
            textArea4.setText("");
        }
        public void writeTxtComponents(NetworkObject object){
            Node theNode = (Node) object;
            textField1.setText(String.valueOf(theNode.getId()));
            textField2.setText(String.valueOf(theNode.getType()));
            textField3.setText(String.valueOf(theNode.getPosPoint().getLocationX()));
            textField4.setText(String.valueOf(theNode.getPosPoint().getLocationY()));
            textField5.setText(String.valueOf(theNode.getPosPoint().getLocationZ()));

        }
        private void initComponents() {
            JPanel attrPanel = new JPanel();
            JLabel label1 = new JLabel();
            textField1 = new JTextField();
            JLabel label2 = new JLabel();
            textField2 = new JTextField();
            JPanel panel5 = new JPanel();  //坐标信息
            JLabel label6 = new JLabel();
            textField3 = new JTextField();
            JLabel label7 = new JLabel();
            textField4 = new JTextField();
            JLabel label3 = new JLabel();
            textField5 = new JTextField();
            textFields = new JTextField[]{textField1, textField2, textField3, textField4, textField5};
            JLabel label4 = new JLabel();
            JScrollPane scrollPane4 = new JScrollPane();//其它
            textArea4 = new JTextArea();

            //======== this ========
            this.setBorder(new EmptyBorder(3, 3, 3, 3));
            this.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            attrPanel.setLayout(new GridBagLayout());
            ((GridBagLayout)attrPanel.getLayout()).columnWidths = new int[] {0, 0, 0};
            ((GridBagLayout)attrPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 70, 0, 0};
            ((GridBagLayout)attrPanel.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
            ((GridBagLayout)attrPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};

            //---- label1 ----
            label1.setText("\u7f16\u53f7\uff1a");
            label1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            attrPanel.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));
            attrPanel.add(textField1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //---- label2 ----
            label2.setText("\u7c7b\u578b\uff1a");
            label2.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            attrPanel.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));
            attrPanel.add(textField2, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //======== panel5 ========
            {
                panel5.setBorder(new CompoundBorder(
                        new TitledBorder(null, "\u5750\u6807\u4fe1\u606f", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
                                new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 13)),
                        BorderFactory.createEmptyBorder()));
                panel5.setLayout(new GridBagLayout());
                ((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {0, 0, 0};
                ((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
                ((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
                ((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

                //---- label6 ----
                label6.setText("x\uff1a");
                label6.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(label6, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 8, 5, 5), 0, 0));
                panel5.add(textField3, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 10, 5, 0), 0, 0));

                //---- label7 ----
                label7.setText("y\uff1a");
                label7.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(label7, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 8, 5, 5), 0, 0));
                panel5.add(textField4, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 10, 5, 0), 0, 0));

                //---- label3 ----
                label3.setText("z\uff1a");
                label3.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(label3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 8, 0, 5), 0, 0));
                panel5.add(textField5, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 10, 0, 0), 0, 0));
            }
            attrPanel.add(panel5, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

            //---- label4 ----
            label4.setText("\u5176\u5b83\uff1a");
            label4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            attrPanel.add(label4, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));

            //======== scrollPane4 ========
            {

                //---- textArea4 ----
                textArea4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 13));
                scrollPane4.setViewportView(textArea4);
            }
            attrPanel.add(scrollPane4, new GridBagConstraints(1, 3, 1, 2, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));
            this.addTab("属性",attrPanel);
        }

    }
}
