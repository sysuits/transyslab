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

import com.jogamp.opengl.util.FPSAnimator;
import com.transyslab.commons.renderer.*;
import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.simcore.AppSetup;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mesots.MesoEngine;
import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.rts.RTEngine;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Hashtable;


public class MainWindow {
    private JFrame windowFrame;
    private JTextArea textArea2;// ����̨��Ϣ
    private JTextArea textArea3;// ������Ϣ
    private JProgressBar progressBar1;
    private JLabel label5;//����״̬��Ϣ
    private JLabel label8;//����״̬��Ϣ
    private JLabel label9;//����������ֵ
    private JSlider slider1;
    private JPanel panel9;
    private final String[] layerNames = {"Node","Link","Segment","Lane","Sensor","Vehicle"};
    private String curLayerName = "Node";
    private LayerPanel layerPanel;
    private JOGLCanvas canvas;
    private FPSAnimator animator;
    private SimulationEngine engine;
    private ClbrtForm clbrtForm;
    private static MainWindow theWindow;
    private MainWindow(){
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        initComponents();
    }
    public static MainWindow getInstance(){
        if(theWindow == null)
            theWindow = new MainWindow();
        return theWindow;
    }
    private void initComponents(){
        windowFrame = new JFrame();
        progressBar1 = new JProgressBar();
        progressBar1.setOrientation(JProgressBar.HORIZONTAL);
        progressBar1.setMinimum(0);
        progressBar1.setMaximum(100);
        progressBar1.setValue(0);
        progressBar1.setStringPainted(true);
        label5 = new JLabel();
        label8 = new JLabel();
        label9 = new JLabel();
        canvas = new JOGLCanvas();
        Camera cam = new OrbitCamera();
        canvas.setCamera(cam);
        // Create a animator that drives canvas' display() at the specified FPS.
        animator = new FPSAnimator(canvas, Constants.FPS, true);
        slider1 = new JSlider(0,120,0);
        JMenuBar menuBar1 = new JMenuBar();
        JMenu menu1 = new JMenu();
        JMenu menu2 = new JMenu();
        JMenu menu3 = new JMenu();
        JMenu menu4 = new JMenu();
        JMenu menu5 = new JMenu();
        JMenu menu6 = new JMenu();
        JMenu menu7 = new JMenu();
        JMenu menu8 = new JMenu();
        JToolBar toolBar1 = new JToolBar();
        JButton button1 = new JButton();
        JButton button2 = new JButton();
        JButton button3 = new JButton();
        JButton button4 = new JButton();
        JButton button5 = new JButton();
        JButton button7 = new JButton();
        JButton button8 = new JButton();
        JButton button9 = new JButton();
        JButton button10 = new JButton();
        JButton button11 = new JButton();
        JPanel panel7 = new JPanel();
        JPanel panel2 = new JPanel();
        JPanel panel8 = new JPanel();
        JPanel panel9 = SignalStagePanel.getInstance(); // ��λͼ
        JComboBox<String> comboBox1 = new JComboBox<>();
        JTabbedPane tabbedPane1 = new JTabbedPane();
        JPanel panel1 = new JPanel();
        JScrollPane scrollPane2 = new JScrollPane();
        textArea2 = new JTextArea();
        JPanel panel3 = new JPanel();
        JScrollPane scrollPane3 = new JScrollPane();
        textArea3 = new JTextArea();
        layerPanel = new LayerPanel();
        //======== windowFrame ========
        windowFrame.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
        windowFrame.setTitle("TranSysLab");
        Container contentPane = windowFrame.getContentPane();
        contentPane.setLayout(new GridBagLayout());
        ((GridBagLayout)contentPane.getLayout()).columnWidths = new int[] {407, 206, 0};
        ((GridBagLayout)contentPane.getLayout()).rowHeights = new int[] {0, 431, 43, 0, 132, 20, 0};
        ((GridBagLayout)contentPane.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
        ((GridBagLayout)contentPane.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 0.0, 0.0, 0.0,1.0E-4};
        windowFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Use a dedicate thread to run the stop() to ensure that the
                // animator stops before program exits.
                new Thread() {
                    @Override
                    public void run() {
                        if (animator.isStarted())
                            animator.stop();
                        System.exit(0);
                    }
                }.start();
            }
        });

        //======== �˵��� ========
        {
            menuBar1.setBorder(new CompoundBorder(UIManager.getBorder("Menu.border"),
                    null));

            //======== menu1 ========
            {
                menu1.setText("\u6587\u4ef6");
                menu1.setFont(new Font("Dialog", Font.PLAIN, 12));
                menu1.setBorder(new EmptyBorder(0, 3, 0, 3));
            }
            menuBar1.add(menu1);

            //======== menu2 ========
            {
                menu2.setText("\u7f16\u8f91");
                menu2.setFont(new Font("Dialog", Font.PLAIN, 12));
                menu2.setBorder(new EmptyBorder(0, 3, 0, 3));
            }
            menuBar1.add(menu2);

            //======== menu3 ========
            {
                menu3.setText("\u8fd0\u884c");
                menu3.setFont(new Font("Dialog", Font.PLAIN, 12));
                menu3.setBorder(new EmptyBorder(0, 3, 0, 3));
            }
            menuBar1.add(menu3);

            //======== menu4 ========
            {
                menu4.setText("\u7a97\u53e3");
                menu4.setFont(new Font("Dialog", Font.PLAIN, 12));
                menu4.setBorder(new EmptyBorder(0, 3, 0, 3));
            }
            menuBar1.add(menu4);

            //======== menu5 ========
            {
                menu5.setText("\u67e5\u770b");
                menu5.setFont(new Font("Dialog", Font.PLAIN, 12));
                menu5.setBorder(new EmptyBorder(0, 3, 0, 3));
            }
            menuBar1.add(menu5);

            //======== menu6 ========
            {
                menu6.setText("\u65b9\u6848");
                menu6.setFont(new Font("Dialog", Font.PLAIN, 12));
                menu6.setBorder(new EmptyBorder(0, 3, 0, 3));
            }
            menuBar1.add(menu6);

            //======== menu7 ========
            {
                menu7.setText("\u5de5\u5177");
                menu7.setFont(new Font("Dialog", Font.PLAIN, 12));
                menu7.setBorder(new EmptyBorder(0, 3, 0, 3));
            }
            menuBar1.add(menu7);

            //======== menu8 ========
            {
                menu8.setText("\u5e2e\u52a9");
                menu8.setFont(new Font("Dialog", Font.PLAIN, 12));
                menu8.setBorder(new EmptyBorder(0, 3, 0, 3));
            }
            menuBar1.add(menu8);
        }
        windowFrame.setJMenuBar(menuBar1);

        //======== ������ ========
        {

            toolBar1.setBorder(UIManager.getBorder("ToolBar.border"));
            //---- button1 ----

            button1.setIcon(new ImageIcon(loadImage("icon/new/file.png")
                            .getImage().getScaledInstance(20,20,java.awt.Image.SCALE_SMOOTH)));
            //button1.setFont(new Font("\u534e\u6587\u7ec6\u9ed1", Font.PLAIN, 12));
            button1.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SubWindow.getInstance().showPanel("project");
                }
            });
            toolBar1.add(button1);

            //---- button2 ----
            button2.setIcon(new ImageIcon(loadImage("icon/new/openfile.png")
                    .getImage().getScaledInstance(20,20,java.awt.Image.SCALE_SMOOTH)));
            button2.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("ѡ����Ŀ�ļ�");
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    fileChooser.setFileFilter(new FileNameExtensionFilter("�����ļ�","properties"));
                    int state = fileChooser.showOpenDialog(null);
                    if(state == JFileChooser.APPROVE_OPTION){
                        File file = fileChooser.getSelectedFile();
                        Configurations configs = new Configurations();
                        AppSetup.masterFileName = file.getAbsolutePath();

                        try{
                            Configuration config = configs.properties(file);

                            String modelType = config.getString("modelType");

                            String root = file.getParent() + "/";
//                            textArea3.append("�Ż����⣺" + config.getString("problemName") + "\n");
//                            textArea3.append("�����̣߳�" + config.getString("numOfEngines") + " ��\n");
//                            textArea3.append("����ģ�ͣ�" + config.getString("modelType") + "\n\n");
//
//                            textArea3.append("����״̬������" + (Boolean.parseBoolean(config.getString("engineBroadcast"))?"��":"��") + "\n");
//                            textArea3.append("���ӻ����У�" + (Boolean.parseBoolean(config.getString("displayOn"))?"��":"��") + "\n\n");
//
//                            textArea3.append("���·��" + root + config.getString("outputPath") + "\n");
//                            textArea3.append("��������¼�����" + (Boolean.parseBoolean(config.getString("rawRecOn"))?"��":"��") + "\n");
//                            textArea3.append("�켣��¼�����" + (Boolean.parseBoolean(config.getString("trackOn"))?"��":"��") + "\n");
//                            textArea3.append("��Ȧ��¼�����" + (Boolean.parseBoolean(config.getString("statRecordOn"))?"��":"��") + "\n");
//                            textArea3.append("ͳ�Ʒ�ʽ��" + config.getString("avgMode") + "\n");
//                            textArea3.append("ͳ�Ƽ����" + config.getString("statTimeStep") + "��\n");
//
//                            textArea3.append("·�����룺" + root + config.getString("roadNetworkPath") + "\n");
//                            textArea3.append("ʵ���������룺" + root + config.getString("empDataPath") + "\n\n");
//
//                            textArea3.append("����ʱ��(��ʼ:����:����)��" +
//                                    config.getString("timeStart") + " : " +
//                                    config.getString("timeStep") + " : " +
//                                    config.getString("timeEnd") + "\n");

                            double sTime = LocalTime.parse(config.getString("timeStart"),DateTimeFormatter.ofPattern("YYYY-MM-DD HH:mm:ss")).toSecondOfDay();
                            updateSlider((long)sTime);
                            canvas.setSliderFTime(sTime);
                            if(modelType.equals("MesoTS")) {
                                //TODO: ����properties���͹��̲�ͳһ�����ⱨ����meso properties�Ľ��ͷ��ڴ˷�֧֮�� wym
                                String projectName = config.getString("projectName");
                                String networkPath = config.getString("networkPath");
                                String caseName = config.getString("caseName");
                                String createTime = config.getString("createTime");
                                String startTime =  config.getString("startTime");
                                String endTime = config.getString("endTime");

                                String demandPath = config.getString("demandPath");
                                Float simStep = config.getFloat("simStep");
                                LocalTime stTime = LocalTime.parse(startTime);
                                AppSetup.startTime = stTime.getHour()*3600+stTime.getMinute()*60+stTime.getSecond();
                                LocalTime edTime = LocalTime.parse(endTime);
                                AppSetup.endTime = edTime.getHour()*3600+edTime.getMinute()*60+edTime.getSecond();
                                AppSetup.setupParameter.put("��Ŀ����", projectName);
                                AppSetup.setupParameter.put("·��·��",networkPath);
                                AppSetup.setupParameter.put("��������", caseName);
                                AppSetup.setupParameter.put("����·��", demandPath);
                                AppSetup.timeStep = simStep;
                                AppSetup.modelType = Constants.MODEL_TYPE_MESO;
                            }
                            else if(modelType.equals("MLP")){
                                AppSetup.setupParameter.put("�����ļ�·��", fileChooser.getSelectedFile().getPath());//���͹��̷���MLPEngine��
                                AppSetup.modelType = Constants.MODEL_TYPE_MLP;
                                AppSetup.displayOn = config.getBoolean("displayOn");
                            }
                            else if(modelType.equals("RT")){
                                AppSetup.setupParameter.put("�����ļ�·��", fileChooser.getSelectedFile().getPath());
                                AppSetup.modelType = Constants.MODEL_TYPE_RT;
                            }
                            initSimEngines();

                            engine.addActionLisener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    if (e.getSource() == engine) {
                                        switch (e.getID()) {
                                            case EngineEvent.UPDATE: {
                                                SimulationClock clock = engine.getNetwork().getSimClock();
                                                double progress = (clock.getCurrentTime() - clock.getStartTime()) / clock.getDuration();
                                                progressBar1.setValue((int)(progress*100));
                                                //int minutes = (int)Math.floor((clock.getCurrentTime() - clock.getStartTime())/60);
                                                int seconds = (int)Math.floor(clock.getCurrentTime() - clock.getStartTime());
                                                slider1.setValue(seconds);
                                                if(slider1.getValue()>=120) {// ����120����(����ʱ��������)
                                                    updateSlider((long)clock.getCurrentTime());
                                                    updateSignalPanel();
                                                }
                                            }
                                                break;
                                            case EngineEvent.BROADCAST: {
                                                textArea2.setText(((EngineEvent)e).getMsg());
                                            }
                                                break;
                                            default:
                                                System.out.println("Unknown information from engine.");
                                        }
                                    }
                                }
                            });
                        }
                        catch(ConfigurationException cex)
                        {
                            // loading of the configuration file failed
                        }

                    }

                }
            });
            toolBar1.add(button2);
            toolBar1.addSeparator();

            //---- button3 ----
            button3.setIcon(new ImageIcon(loadImage("icon/new/save.png")
                   .getImage().getScaledInstance(20,20,java.awt.Image.SCALE_SMOOTH)));
            toolBar1.add(button3);
            toolBar1.addSeparator();

            //---- ��������ʼ���� ----
            //button4.setMargin(new Insets(0, 8, 0, ));
            button4.setIcon(new ImageIcon(loadImage("icon/new/play.png")
                    .getImage().getScaledInstance(20,20,java.awt.Image.SCALE_SMOOTH)));
            button4.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(!canvas.isNetworkReady()){
                        JOptionPane.showMessageDialog(null, "���ȼ���·��");
                        return;
                    }
                    else{
                        switch (canvas.getStatus()){
                            case JOGLCanvas.ANIMATOR_PAUSE:
                                canvas.setStatus(JOGLCanvas.ANIMATOR_PLAYING);
                                break;
                            case JOGLCanvas.ANIMATOR_STOP:
                                canvas.setStatus(JOGLCanvas.ANIMATOR_PLAYING);
                                new Thread(()->engine.run()).start();
                                break;
                            default:
                                break;
                        }
                    }
                }
            });
            toolBar1.add(button4);

            //---- ��������ͣ���� ----
            button5.setIcon(new ImageIcon(loadImage("icon/new/pause.png")
                    .getImage().getScaledInstance(20,20, Image.SCALE_SMOOTH)));
            button5.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if(canvas.getStatus()==JOGLCanvas.ANIMATOR_PLAYING)
                        canvas.setStatus(JOGLCanvas.ANIMATOR_PAUSE);
                }
            });
            toolBar1.add(button5);

            //---- ������ֹͣ���� ----
            button7.setIcon(new ImageIcon(loadImage("icon/new/stop.png")
                    .getImage().getScaledInstance(20,20,java.awt.Image.SCALE_SMOOTH)));
            button7.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(canvas.getStatus() == JOGLCanvas.ANIMATOR_PAUSE || canvas.getStatus() == JOGLCanvas.ANIMATOR_PLAYING){
                        canvas.setStatus(JOGLCanvas.ANIMATOR_STOP);
                        engine.stop();
                        AnimationFrame.resetCounter();
                        FrameQueue.getInstance().clear();
                    }
                }
            });
            toolBar1.add(button7);
            toolBar1.addSeparator();

            //---- ���������ݿ����� ----
            button8.setIcon(new ImageIcon(loadImage("icon/new/database.png")
                    .getImage().getScaledInstance(20,20,java.awt.Image.SCALE_SMOOTH)));
            button8.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(canvas.getStatus()==JOGLCanvas.ANIMATOR_PAUSE){
                        // TODO �ݶ�Ϊ����֡����
                        // ·��ѡ����
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setDialogTitle("ѡ����ձ���·��");
                        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        int state = fileChooser.showOpenDialog(null);
                        if(state == JFileChooser.APPROVE_OPTION){
                            String filePath = fileChooser.getSelectedFile().getPath();
                            AnimationFrame frame = canvas.getCurFrame();
                            if(frame!=null){
                                frame.toCSV(filePath);
                            }
                        }
                    }
                    else if(canvas.getStatus()==JOGLCanvas.ANIMATOR_STOP){
                        JOptionPane.showMessageDialog(null, "���ȿ�ʼ���Ŷ�����");
                        return;
                    }
                    else{
                        JOptionPane.showMessageDialog(null, "������ͣ������");
                        return;
                    }
                }
            });
            toolBar1.add(button8);

            //---- ����������У׼ ----
            button9.setIcon(new ImageIcon(loadImage("icon/new/chart.png")
                    .getImage().getScaledInstance(20,20,java.awt.Image.SCALE_SMOOTH)));
            button9.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(!canvas.isNetworkReady()){
                        JOptionPane.showMessageDialog(null, "���ȼ���·��");
                        return;
                    }
                    else{

                        if(clbrtForm == null) {
                            clbrtForm = new ClbrtForm();
                        }
                        else if(!clbrtForm.isShowing()){
                            clbrtForm.clear();
                            clbrtForm.setVisible(true);
                            clbrtForm.requestFocusInWindow();
                        }

                    }

                }
            });
            toolBar1.add(button9);

            //---- ��������һ֡ ----
            button10.setIcon(new ImageIcon(loadImage("icon/new/next.png")
                    .getImage().getScaledInstance(20,20,java.awt.Image.SCALE_SMOOTH)));
            button10.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(!canvas.isNetworkReady()){
                        JOptionPane.showMessageDialog(null, "���ȼ���·��");
                        return;
                    }
                    else{
                        if (canvas.getStatus()==JOGLCanvas.ANIMATOR_PAUSE) {
                            canvas.setMode(JOGLCanvas.ANIMATOR_FRAME_ADVANCE);
                        }
                    }

                }
            });
            toolBar1.add(button10);

            //---- ��������΢����ʾ�л� ----
            button11.setIcon(new ImageIcon(loadImage("icon/new/switch.png")
                    .getImage().getScaledInstance(20,20,java.awt.Image.SCALE_SMOOTH)));
            button11.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(!canvas.isNetworkReady()){
                        JOptionPane.showMessageDialog(null, "���ȼ���·��");
                        return;
                    }
                    else{
                        RTEngine.isState = !RTEngine.isState;
                    }
                }
            });
            toolBar1.add(button11);
        }
        contentPane.add(toolBar1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 1, 5), 0, 0));

        //======== canvasչʾ�� ========
        contentPane.add(canvas, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 1, 5), 0, 0));

        //======== ��ͨҪ��������� ========
        {
            panel2.setBorder(new CompoundBorder(
                    /*new TitledBorder(null, "\u5c5e\u6027", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
                            new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 13))*/
                    BorderFactory.createEmptyBorder(),null));

            panel2.setLayout(new GridBagLayout());
            ((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0};
            ((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 0};
            ((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
            ((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {1.0, 0.0, 1.0E-4};
            //======== panel7 ========
            //======== Ĭ����ʾNode���� ========
            {
                panel7.setLayout(new CardLayout());

                for (int i = 0; i < layerNames.length; i++) {
                    panel7.add(layerPanel.getLayer(layerNames[i]), layerNames[i]);
                }
                //panel7.add(layerPanel.getLayer("Lane"),"Lane");
            }
            panel2.add(panel7, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));
            //---- ��ͨҪ��ѡ���� ----
            comboBox1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
            comboBox1.setModel(new DefaultComboBoxModel<>(layerNames));
            comboBox1.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    String selectedItem = (String)comboBox1.getSelectedItem();
                    if(curLayerName != selectedItem){
                        //���Ҫ���������
                        ((PanelAction)layerPanel.getLayer(curLayerName)).resetTxtComponents();
                        //�����ѡ����
                        canvas.deselect();
                    }

                    curLayerName = selectedItem;
                    ((CardLayout)panel7.getLayout()).show(panel7,curLayerName);
                }
            });
            panel2.add(comboBox1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                    new Insets(0, 3, 3, 3), 0, 0));
        }
        contentPane.add(panel2, new GridBagConstraints(1, 1, 1, 3, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 1, 0), 0, 0));
        //======== ��λ��ʾ ========
        {
            //panel9.setLayout(new CardLayout());


        }
        contentPane.add(panel9, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 1, 5), 0, 0));

        //======== ʱ������� ========
        {
            //slider1.setUI(new colo)
            slider1.setMajorTickSpacing(30);
            slider1.setMinorTickSpacing(5);
            slider1.setPaintTicks(true);
            slider1.setPaintLabels(true);
            slider1.setPaintTrack(true);
            LocalTime now = LocalTime.now();
            updateSlider(now.toSecondOfDay());


        }
        contentPane.add(slider1, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 1, 5), 0, 0));
        //======== �ײ���Ϣ�� ========
        {
            tabbedPane1.setFont(new Font("\u534e\u6587\u7ec6\u9ed1", Font.PLAIN, 13));

            //======== ����̨ ========
            {
                panel1.setLayout(new GridBagLayout());
                ((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0};
                ((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
                ((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
                ((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

                //======== scrollPane2 ========
                {
                    scrollPane2.setViewportView(textArea2);
                    textArea2.setLineWrap(true);
                }
                panel1.add(scrollPane2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
            }
            tabbedPane1.addTab("\u63a7\u5236\u53f0", panel1);

            //======== ���� ========
            {
                panel3.setLayout(new GridBagLayout());
                ((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {0, 0};
                ((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0};
                ((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
                ((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

                //======== scrollPane3 ========
                {
                    scrollPane3.setViewportView(textArea3);
                    textArea3.setLineWrap(true);
                }
                panel3.add(scrollPane3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
            }
            tabbedPane1.addTab("\u65b9\u6848", panel3);
        }
        contentPane.add(tabbedPane1, new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 1, 0), 0, 0));
        //======== ״̬�� ========
        {
            panel8.setLayout(new GridBagLayout());
            ((GridBagLayout)panel8.getLayout()).columnWidths = new int[] {12, 42, 0, 0, 0, 0};
            ((GridBagLayout)panel8.getLayout()).rowHeights = new int[] {0, 0};
            ((GridBagLayout)panel8.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 0.0, 0.0, 1.0E-4};
            ((GridBagLayout)panel8.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

            //---- label5 ----
            label5.setText("\u72b6\u6001");
            label5.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
            panel8.add(label5, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(2, 5, 2, 2), 0, 0));

            //---- label8 ----
            label8.setText("\u4efb\u52a1\u6267\u884c\u4e2d");
            label8.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
            label9.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
            panel8.add(label8, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 2), 0, 0));

            panel8.add(label9, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 2), 0, 0));
            panel8.add(progressBar1, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
        }
        contentPane.add(panel8, new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        windowFrame.setSize(1042, 787);
        windowFrame.setLocationRelativeTo(windowFrame.getOwner());
        windowFrame.setVisible(true);
        windowFrame.requestFocusInWindow();
        animator.start();

    }
    public void initSimEngines(){
        //FrameQueue.getInstance().initFrameQueue();
        switch (AppSetup.modelType) {
            case Constants.MODEL_TYPE_MESO:
                engine = new MesoEngine(0,"E:\\test\\");
                break;
            case Constants.MODEL_TYPE_MLP:
                engine = new MLPEngine(AppSetup.setupParameter.get("�����ļ�·��"));
                ((MLPEngine)engine).displayOn = AppSetup.displayOn;
                break;
            case Constants.MODEL_TYPE_RT:
                engine = new RTEngine(AppSetup.setupParameter.get("�����ļ�·��"));
                break;
            default:
                break;
        }
        engine.loadFiles();

        // Network is ready for simulation
        canvas.setFirstRender(true);
        canvas.setDrawableNetwork(engine.getNetwork());
        canvas.requestFocusInWindow();
    }
    public void updateSlider(long secondOfDay){
        LocalTime time = LocalTime.ofSecondOfDay(secondOfDay);
        time = LocalTime.of(time.getHour(),time.getMinute(),time.getSecond());
        Hashtable position = new Hashtable();
        JLabel tick1 = new JLabel(time.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        tick1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 15));
        //time = time.plusMinutes(30);
        time = time.plusSeconds(30);
        JLabel tick2 = new JLabel(time.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        tick2.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 15));
        time = time.plusSeconds(30);
        JLabel tick3 = new JLabel(time.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        tick3.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 15));
        time = time.plusSeconds(30);
        JLabel tick4 = new JLabel(time.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        tick4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 15));
        time = time.plusSeconds(30);
        JLabel tick5 = new JLabel(time.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        tick5.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 15));

        slider1.setValue(0);
        position.put(0,tick1);
        position.put(30,tick2);
        position.put(60, tick3);
        position.put(90, tick4);
        position.put(120, tick5);
        slider1.setLabelTable(position);

    }
    /*public void launchEngineWithParas(double[] paras, long seed){
        switch (AppSetup.modelType) {
            case Constants.MODEL_TYPE_MESO:
                //TODO ����ƴ����������
                ((MesoEngine)engine).run();
                break;
            case Constants.MODEL_TYPE_MLP:
                if (seed>=0) {
                    ((MLPEngine)engine).seedFixed = true;
                    ((MLPEngine)engine).runningSeed = seed;
                }
                else
                    ((MLPEngine)engine).seedFixed = false;
                    new Thread(() -> ((MLPEngine)engine).runWithPara(paras==null ? MLPParameter.DEFAULT_PARAMETERS : paras)).start();
                break;

            default:
                break;
        }
    }*/
    public String getCurLayerName(){
        return curLayerName;
    }
    public JSlider getSlider(){
        return this.slider1;
    }
    public void updateSignalPanel(){
        double curTime = engine.getNetwork().getSimClock().getCurrentTime();
        SignalStagePanel.getInstance().updateTime(curTime,curTime+120);
    }

    public LayerPanel getLayerPanel(){
        return layerPanel;
    }
    private ImageIcon loadImage(String relativePath) {
        return new ImageIcon(ClassLoader.getSystemResource(relativePath));
    }

}
