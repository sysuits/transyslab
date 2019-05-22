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

import com.transyslab.commons.io.FileUtils;
import com.transyslab.simcore.AppSetup;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.time.LocalDateTime;


public class SubWindow {
    private JFrame windowFrame;
    private ProjectPanel projectPanel;
    private CasePanel casePanel;
    private CardLayout cardLayout;
    private static SubWindow theWindow;

    private SubWindow(){
        initComponets();
    }
    public static SubWindow getInstance(){
        if(theWindow == null)
            theWindow = new SubWindow();
        return theWindow;
    }

    public void showPanel(String panelName){
        this.cardLayout.show(this.windowFrame.getContentPane(),panelName);
        this.windowFrame.setVisible(true);
        switch (panelName){
            case "project":
                this.windowFrame.setTitle("\u65b0\u5efa\u9879\u76ee");
                break;
            case "case":
                this.windowFrame.setTitle("\u65b0\u5efa\u9879\u76ee");
                break;
            case "calibration":
                this.windowFrame.setTitle("\u6a21\u578b\u53c2\u6570\u8bbe\u7f6e");
                break;
            default:
                break;
        }


    }
    private void initComponets(){

        windowFrame = new JFrame();
        projectPanel = new ProjectPanel();
        casePanel = new CasePanel();
       // clbrtPanel = new ClbrtPanel();
        cardLayout = new CardLayout();
        //======== panel name ========
        //panels.put(panelNames[0],projectPanel);
        //panels.put(panelNames[1],casePanel);
        //======== windowFrame ========
        //标题字体
        windowFrame.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
        Container contentPane = windowFrame.getContentPane();
        contentPane.setLayout(cardLayout);
        contentPane.add(projectPanel,"project");
        contentPane.add(casePanel,"case");
       // contentPane.add(clbrtPanel,"calibration");
        windowFrame.pack();
        windowFrame.setLocationRelativeTo(windowFrame.getOwner());
    }
    public class ProjectPanel extends JPanel{
        private JTextField textField1; // 项目名称
        private JTextField textField2; // 保存路径
        private JTextField textField3; // 路网文件路径
        public ProjectPanel(){
            initComponent();
        }
        private void initComponent(){
            JLabel label1 = new JLabel();
            textField1 = new JTextField();
            JLabel label2 = new JLabel();
            textField2 = new JTextField();
            JButton button1 = new JButton();
            JLabel label3 = new JLabel();
            textField3 = new JTextField();
            JButton button2 = new JButton();
            JPanel panel2 = new JPanel();
            JButton button4 = new JButton();
            JButton button5 = new JButton();
            //======== 新建项目面板 ========
            setPreferredSize(new Dimension(306, 381));
            setMinimumSize(new Dimension(306, 381));
            setLayout(new GridBagLayout());
            ((GridBagLayout)getLayout()).columnWidths = new int[] {15, 91, 63, 56, 63, 25, 0};
            ((GridBagLayout)getLayout()).rowHeights = new int[] {45, 35, 32, 35, 32, 35, 32, 35, 35, 32, 30, 0};
            ((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};
            ((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};

            //---- 项目名称 ----
            label1.setText("\u9879\u76ee\u540d\u79f0\uff1a");
            label1.setHorizontalAlignment(SwingConstants.CENTER);
            label1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 16));
            add(label1, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
            add(textField1, new GridBagConstraints(2, 2, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- 保存路径 ----
            label2.setText("\u4fdd\u5b58\u8def\u5f84\uff1a");
            label2.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 16));
            label2.setHorizontalAlignment(SwingConstants.CENTER);
            add(label2, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
            add(textField2, new GridBagConstraints(2, 4, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- 按键：浏览 ----
            button1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            button1.setText("\u6d4f\u89c8");
            button1.setMargin(new Insets(2, 2, 2, 2));
            add(button1, new GridBagConstraints(4, 4, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- 路网文件路径 ----
            label3.setText("\u8def\u7f51\u6587\u4ef6\uff1a");
            label3.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 16));
            label3.setHorizontalAlignment(SwingConstants.CENTER);
            add(label3, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
            add(textField3, new GridBagConstraints(2, 6, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- 按键：浏览 ----
            button2.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            button2.setText("\u6d4f\u89c8");
            button2.setMargin(new Insets(2, 2, 2, 2));
            add(button2, new GridBagConstraints(4, 6, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //======== 底部按键面板 ========
            {
                panel2.setLayout(new GridBagLayout());
                ((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {15, 71, 31, 68, 0};
                ((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {27, 0};
                ((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
                ((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

                //---- 按键：下一步 ----
                button4.setText("\u4e0b\u4e00\u6b65");
                button4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
                button4.setMargin(new Insets(2, 8, 2, 8));
                panel2.add(button4, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));
                button4.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        /*		if(checkTextFields()){*/
                        cardLayout.show(windowFrame.getContentPane(),"case");
                        AppSetup.setupParameter.put("项目名称", textField1.getText());
                        AppSetup.setupParameter.put("项目路径", textField2.getText());
                        AppSetup.setupParameter.put("路网路径", textField3.getText());
                        //			}
			/*	else{
					JOptionPane.showMessageDialog(null, "请填写全部信息");
					return;
				}*/
                    }
                });
                //---- 按键：取消 ----
                button5.setText("\u53d6\u6d88");
                button5.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
                button5.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //隐藏窗口
                        windowFrame.setVisible(false);
                        //text内容置空
                        textField1.setText("");
                        textField2.setText("");
                        textField3.setText("");
                    }
                });
                panel2.add(button5, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
            }
            add(panel2, new GridBagConstraints(2, 9, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
        }
    }
    public class CasePanel extends JPanel{
        private JTextField textField1;//方案名称
        private JComboBox<String> comboBox1;//仿真模型
        private JTextField textField4;//开始时间
        private JTextField textField5;//结束时间
        private JTextField textField2;//仿真步长
        private JTextField textField3;//需求文件路径
        private JTextField[] textFields;
        public CasePanel() {
            initComponents();
        }

        private void initComponents() {

            JLabel label1 = new JLabel();
            textField1 = new JTextField("请输入方案名称");
            JLabel label2 = new JLabel();
            comboBox1 = new JComboBox<>();
            JLabel label3 = new JLabel();
            textField4 = new JTextField();
            JLabel label4 = new JLabel();
            textField5 = new JTextField();
            JLabel label5 = new JLabel();
            textField2 = new JTextField("0.2");
            JLabel label6 = new JLabel();
            textField3 = new JTextField("请指定需求文件路径");
            textFields = new JTextField[]{textField1, textField2, textField3,textField4,textField5};
            JButton button2 = new JButton();
            JPanel panel1 = new JPanel();
            JButton button5 = new JButton();
            JButton button3 = new JButton();
            JButton button4 = new JButton();

            //======== this ========
            setMinimumSize(new Dimension(306, 381));
            setPreferredSize(new Dimension(306, 381));

            setLayout(new GridBagLayout());
            ((GridBagLayout)getLayout()).columnWidths = new int[] {30, 0, 89, 35, 0, 29, 0};
            ((GridBagLayout)getLayout()).rowHeights = new int[] {25, 32, 15, 32, 15, 32, 15, 32, 15, 32, 15, 32, 25, 32, 0, 0};
            ((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};
            ((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};

            //---- 方案名称 ----
            label1.setText("\u65b9\u6848\u540d\u79f0\uff1a");
            label1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 16));
            label1.setHorizontalAlignment(SwingConstants.CENTER);
            add(label1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
            add(textField1, new GridBagConstraints(2, 1, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- 仿真模型 ----
            label2.setText("\u4eff\u771f\u6a21\u578b\uff1a");
            label2.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 16));
            label2.setHorizontalAlignment(SwingConstants.CENTER);
            add(label2, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- comboBox1 ----
            comboBox1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
            comboBox1.setModel(new DefaultComboBoxModel<>(new String[] {
                    "MLP",
                    "MesoTS"
            }));
            add(comboBox1, new GridBagConstraints(2, 3, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- 开始时间 ----
            label3.setText("\u5f00\u59cb\u65f6\u95f4\uff1a");
            label3.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 16));
            label3.setHorizontalAlignment(SwingConstants.CENTER);
            add(label3, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
            add(textField4, new GridBagConstraints(2, 5, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- 结束时间 ----
            label4.setText("\u7ed3\u675f\u65f6\u95f4\uff1a");
            label4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 16));
            label4.setHorizontalAlignment(SwingConstants.CENTER);
            add(label4, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
            add(textField5, new GridBagConstraints(2, 7, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- 仿真步长 ----
            label5.setText("\u4eff\u771f\u6b65\u957f\uff1a");
            label5.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 16));
            label5.setHorizontalAlignment(SwingConstants.CENTER);
            add(label5, new GridBagConstraints(1, 9, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
            add(textField2, new GridBagConstraints(2, 9, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- 需求文件路径 ----
            label6.setText("\u9700\u6c42\u6587\u4ef6\uff1a");
            label6.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 16));
            label6.setHorizontalAlignment(SwingConstants.CENTER);
            add(label6, new GridBagConstraints(1, 11, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
            add(textField3, new GridBagConstraints(2, 11, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- 按键：浏览 ----
            button2.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            button2.setText("\u6d4f\u89c8");
            button2.setMargin(new Insets(2, 2, 2, 2));
            button2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser("src/main/resources");
                    fileChooser.setDialogTitle("选择交通需求文件");
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    fileChooser.setFileFilter(new FileNameExtensionFilter("XML/CSV文件","xml","csv"));
                    int state = fileChooser.showOpenDialog(null);
                    if(state == JFileChooser.APPROVE_OPTION){
                        File file = fileChooser.getSelectedFile();
                        textField3.setText(file.getAbsolutePath());
                    }
                }
            });
            add(button2, new GridBagConstraints(4, 11, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //======== 底部按键面板 ========
            {
                panel1.setLayout(new GridBagLayout());
                ((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {15, 73, 15, 73, 15, 67, 0};
                ((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {27, 0};
                ((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
                ((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

                //---- 按键：上一步 ----
                button5.setText("\u4e0a\u4e00\u6b65");
                button5.setMargin(new Insets(2, 6, 2, 6));
                button5.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
                button5.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //保留Text_Field内容
                        cardLayout.show(windowFrame.getContentPane(),"project");
                    }
                });
                panel1.add(button5, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                //---- 按键：完成 ----
                button3.setText("\u5b8c\u6210");
                button3.setMargin(new Insets(2, 6, 2, 6));
                button3.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
                button3.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //				if(checkTextFields()){
                        //隐藏窗口
                        windowFrame.setVisible(false);

                        //读入面板设置的参数
                        Configurations configs = new Configurations();

                        AppSetup.setupParameter.put("方案名称", textField1.getText());
                        AppSetup.setupParameter.put("需求路径", textField3.getText());
                        // TODO 时间标准化
                        /*
                        AppSetup.startTime = Double.parseDouble(textFields[2].getText())*3600
                                + Double.parseDouble(textFields[3].getText())*60
                                + Double.parseDouble(textFields[4].getText());
                        AppSetup.endTime = Double.parseDouble(textFields[5].getText())*3600
                                + Double.parseDouble(textFields[6].getText())*60
                                + Double.parseDouble(textFields[7].getText());*/
                        AppSetup.timeStep = Double.parseDouble(textField2.getText());
                        String modelName = (String) comboBox1.getSelectedItem();
                        if(modelName.equals("MesoTS"))
                            AppSetup.modelType = 1;
                        else
                            AppSetup.modelType = 2;
                        StringBuilder projPath = new StringBuilder(AppSetup.setupParameter.get("项目路径"));
                        projPath.append("\\").append(AppSetup.setupParameter.get("项目名称")).append(".properties");
                        //新建文件
                        if(FileUtils.createFile(projPath.toString())){
                            //写入.properties文件
                            try{
                                FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                                        configs.propertiesBuilder(projPath.toString());
                                PropertiesConfiguration config = builder.getConfiguration();
                                config.addProperty("projectName", AppSetup.setupParameter.get("项目名称"));
                                config.addProperty("networkPath", AppSetup.setupParameter.get("路网路径"));
                                config.addProperty("caseName", textField1.getText());
                                LocalDateTime createDateTime = LocalDateTime.now();
                                config.addProperty("createTime", createDateTime.toString());
                                config.addProperty("demandPath", AppSetup.setupParameter.get("需求路径"));
                                config.addProperty("modelType", modelName);//
                                // TODO 标准时间
                                /*
                                //开始时间，时：分：秒
                                LocalTime stTime = LocalTime.of(Integer.parseInt(textFields[2].getText()),
                                        Integer.parseInt(textFields[3].getText()),
                                        Integer.parseInt(textFields[4].getText()));

                                config.addProperty("startTime", stTime.toString());
                                //结束时间，时：分：秒
                                LocalTime edTime = LocalTime.of(Integer.parseInt(textFields[5].getText()),
                                        Integer.parseInt(textFields[6].getText()),
                                        Integer.parseInt(textFields[7].getText()));
                                config.addProperty("endTime", edTime.toString());*/
                                config.addProperty("simStep", textField2.getText());
                                builder.save();
                            }catch(ConfigurationException cex){
                                cex.printStackTrace();
                            }
                        }

                        //text内容置空
                        clearTextFields();
                        //TODO 避免打开项目与新建项目按钮的冲突
                        MainWindow.getInstance().initSimEngines();
                    }
                });
                panel1.add(button3, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                //---- 按键：取消 ----
                button4.setText("\u53d6\u6d88");
                button4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
                button4.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        clearTextFields();
                        windowFrame.setVisible(false);
                    }
                });
                panel1.add(button4, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
            }
            add(panel1, new GridBagConstraints(1, 13, 4, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

        }
        private boolean checkTextFields(){
            for(JTextField text:textFields){
                if(text.getText().equals(""))
                    return false;
            }
            return true;
        }
        private void clearTextFields(){
            for(JTextField text:textFields){
                text.setText("");
            }
        }
    }

}
