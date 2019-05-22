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

import com.transyslab.commons.tools.optimizer.DifferentialEvolution;
import com.transyslab.experiments.OptToolBox;
import com.transyslab.simcore.AppSetup;
import org.apache.commons.configuration2.Configuration;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import javax.swing.*;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class ClbrtForm extends JFrame{

	private JPanel panel1;//优化问题设置
	private JLabel label1;
	private JPanel panel3;//优化目标
	private JLabel label4;
	private JComboBox<String> comboBox1;//评价参量
	private JLabel label5;
	private JComboBox<String> comboBox2;//评价函数
	private JLabel label6;
	private JComboBox<String> comboBox3;//优化方向
	private JPanel panel4;//求解算法
	private JLabel label7;
	private JComboBox<String> comboBox4;//算法选择
	private JLabel label8;
	private JLabel label9;
	private JTextField textField1;//变异步长F
	private JLabel label10;
	private JTextField textField2;//交叉概率Cr
	private JLabel label11;//收敛条件
	private JLabel label12;
	private JComboBox<String> comboBox5;//收敛条件：小于/小于或等于/大于/大于或等于
	private JTextField textField3;//收敛条件：目标函数阈值
	private JLabel label13;
	private JTextField textField4;// 最大迭代次数
	private JPanel panel5;// 算法运行控制
	private JButton button1;// 开始
	private JButton button2;// 暂停
	private JButton button3;// 结束
	private JPanel panel6;// 优化结果
	private JLabel label2;
	private JLabel label3;
	private JTextField textField5;//当前迭代次数，不可编辑，只用于显示
	private JButton button4;//清除结果
	private JScrollPane scrollPane1;
	private JTextArea textArea1;// 显示迭代过程的中间结果和最优结果
	private MainWindow mainWindow;
	private DifferentialEvolution algorithm;
	public ClbrtForm() {
		initComponents();
	}
	private void initComponents() {
		panel1 = new JPanel();
		label1 = new JLabel();
		panel3 = new JPanel();
		label4 = new JLabel();
		comboBox1 = new JComboBox<>();
		label5 = new JLabel();
		comboBox2 = new JComboBox<>();
		label6 = new JLabel();
		comboBox3 = new JComboBox<>();
		panel4 = new JPanel();
		label7 = new JLabel();
		comboBox4 = new JComboBox<>();
		label8 = new JLabel();
		label9 = new JLabel();
		textField1 = new JTextField();
		// TODO 默认取值
		textField1.setText("0.5");
		label10 = new JLabel();
		// TODO 默认取值
		textField2 = new JTextField();
		textField2.setText("0.5");
		label11 = new JLabel();
		label12 = new JLabel();
		comboBox5 = new JComboBox<>();
		textField3 = new JTextField();
		// TODO 默认取值
		textField3.setText("0.35");
		label13 = new JLabel();
		// TODO 默认取值
		textField4 = new JTextField();
		textField4.setText("1000");
		panel5 = new JPanel();
		button1 = new JButton();
		button2 = new JButton();
		button3 = new JButton();
		panel6 = new JPanel();
		label2 = new JLabel();
		label3 = new JLabel();
		textField5 = new JTextField();
		button4 = new JButton();
		scrollPane1 = new JScrollPane();
		textArea1 = new JTextArea();

		//======== this ========
		setTitle("\u4eff\u771f\u6a21\u578b\u53c2\u6570\u6821\u51c6");
		Container contentPane = getContentPane();
		contentPane.setLayout(new GridBagLayout());
		((GridBagLayout)contentPane.getLayout()).columnWidths = new int[] {0, 0, 20, 0};
		((GridBagLayout)contentPane.getLayout()).rowHeights = new int[] {0, 10, 0};
		((GridBagLayout)contentPane.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
		((GridBagLayout)contentPane.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				e.getWindow().setVisible(false);
			}
		});

		//======== panel1 ========
		{
			panel1.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED, Color.gray, null, null, null));

			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};

			//---- label1 ----
			label1.setText("\u4f18\u5316\u95ee\u9898\u8bbe\u7f6e");
			label1.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 16));
			panel1.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(5, 5, 8, 0), 0, 0));

			//======== panel3 ========
			{
				panel3.setBorder(new TitledBorder(new TitledBorder("text"), "\u4f18\u5316\u76ee\u6807", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
						new Font("\u5fae\u8f6f\u96c5\u9ed1 Light", Font.PLAIN, 14)));
				panel3.setLayout(new GridBagLayout());
				((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {0, 0, 0};
				((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
				((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
				((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

				//---- label4 ----
				label4.setText("\u8bc4\u4ef7\u53c2\u91cf\uff1a");
				label4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
				panel3.add(label4, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 5, 8, 5), 0, 0));

				//---- comboBox1 ----
				comboBox1.setModel(new DefaultComboBoxModel<>(new String[] {
						"\u901f\u5ea6\uff08m/s\uff09"
				}));
				comboBox1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
				panel3.add(comboBox1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 0, 8, 5), 0, 0));

				//---- label5 ----
				label5.setText("\u8bc4\u4ef7\u51fd\u6570\uff1a");
				label5.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
				panel3.add(label5, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 5, 8, 5), 0, 0));

				//---- comboBox2 ----
				comboBox2.setModel(new DefaultComboBoxModel<>(new String[] {
						"RMSE"
				}));
				comboBox2.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
				panel3.add(comboBox2, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 0, 8, 5), 0, 0));

				//---- label6 ----
				label6.setText("\u4f18\u5316\u65b9\u5411\uff1a");
				label6.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
				panel3.add(label6, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 5, 5, 5), 0, 0));

				//---- comboBox3 ----
				comboBox3.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
				comboBox3.setModel(new DefaultComboBoxModel<>(new String[] {
						"\u6700\u5c0f\u5316",
						"\u6700\u5927\u5316"
				}));
				panel3.add(comboBox3, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 0, 5, 5), 0, 0));
			}
			panel1.add(panel3, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 5, 8, 5), 0, 0));

			//======== panel4 ========
			{
				panel4.setBorder(new TitledBorder(new TitledBorder("text"), "\u6c42\u89e3\u7b97\u6cd5", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
						new Font("\u5fae\u8f6f\u96c5\u9ed1 Light", Font.PLAIN, 14)));
				panel4.setLayout(new GridBagLayout());
				((GridBagLayout)panel4.getLayout()).columnWidths = new int[] {108, 0, 105, 0, 100, 0};
				((GridBagLayout)panel4.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
				((GridBagLayout)panel4.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};
				((GridBagLayout)panel4.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};

				//---- label7 ----
				label7.setText("\u7b97\u6cd5\u9009\u62e9\uff1a");
				label7.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
				panel4.add(label7, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 5, 10, 5), 0, 0));

				//---- comboBox4 ----
				comboBox4.setModel(new DefaultComboBoxModel<>(new String[] {
						"\u5dee\u5206\u8fdb\u5316\u7b97\u6cd5"
				}));
				comboBox4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
				panel4.add(comboBox4, new GridBagConstraints(1, 0, 4, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 0, 10, 5), 0, 0));

				//---- label8 ----
				label8.setText("\u7b97\u6cd5\u53c2\u6570\uff1a");
				label8.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
				panel4.add(label8, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 5, 10, 5), 0, 0));

				//---- label9 ----
				label9.setText("\u53d8\u5f02\u6b65\u957f");
				label9.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
				panel4.add(label9, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 0, 10, 10), 0, 0));
				panel4.add(textField1, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 0, 10, 10), 0, 0));

				//---- label10 ----
				label10.setText("\u4ea4\u53c9\u6982\u7387");
				label10.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
				panel4.add(label10, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 0, 10, 10), 0, 0));
				panel4.add(textField2, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 0, 10, 5), 0, 0));

				//---- label11 ----
				label11.setText("\u6536\u655b\u6761\u4ef6\uff1a");
				label11.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
				panel4.add(label11, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 5, 10, 5), 0, 0));

				//---- label12 ----
				label12.setText("\u8bc4\u4ef7\u51fd\u6570");
				label12.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
				panel4.add(label12, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 0, 10, 10), 0, 0));

				//---- comboBox5 ----
				comboBox5.setModel(new DefaultComboBoxModel<>(new String[] {
						"\u5c0f\u4e8e",
						"\u5c0f\u4e8e\u6216\u7b49\u4e8e",
						"\u5927\u4e8e",
						"\u5927\u4e8e\u6216\u7b49\u4e8e"
				}));
				comboBox5.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
				panel4.add(comboBox5, new GridBagConstraints(2, 2, 2, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 0, 10, 10), 0, 0));
				panel4.add(textField3, new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 0, 10, 5), 0, 0));

				//---- label13 ----
				label13.setText("\u6700\u5927\u8fed\u4ee3\u6b21\u6570\uff1a");
				label13.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
				panel4.add(label13, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 5, 5, 5), 0, 0));

				//---- textField4 ----
				textField4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
				panel4.add(textField4, new GridBagConstraints(1, 3, 3, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 0, 5, 10), 0, 0));
			}
			panel1.add(panel4, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 5, 8, 5), 0, 0));

			//======== panel5 ========
			{
				panel5.setBorder(new TitledBorder(null, "\u7b97\u6cd5\u8fd0\u884c", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
						new Font("\u5fae\u8f6f\u96c5\u9ed1 Light", Font.PLAIN, 14)));
				panel5.setLayout(new GridBagLayout());
				((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
				((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0};
				((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
				((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

				//---- button1 开始 ----
				button1.setText("\u5f00\u59cb");
				button1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
				panel5.add(button1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 5, 5, 10), 0, 0));
				button1.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						textArea1.setText("");//clear
						textField5.setText("");//clear
						algorithm = OptToolBox.createAlgorithm(new String[] {AppSetup.masterFileName});
						JMetalRandom.getInstance().setSeed(1528339627088L);//测试，固定算法种子
						new Thread(algorithm).start();
						algorithm.addAlgListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								if (e.getSource()==algorithm) {
									if (e.getID()==DifferentialEvolution.BROADCAST) {
										textArea1.append(e.getActionCommand());
										textField5.setText(e.getActionCommand().split(",")[0]);
									}
									if (e.getID()==DifferentialEvolution.END)
										textArea1.append(((DifferentialEvolution)e.getSource()).getStopInfo());
								}
							}
						});
					}
				});

				//---- button2 暂停 ----
				button2.setText("\u6682\u505c");
				button2.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
				panel5.add(button2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 5, 5, 10), 0, 0));
				// TODO 暂时取消
				button2.setVisible(false);

				//---- button3 终止 ----
				button3.setText("\u7ed3\u675f");
				button3.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
				panel5.add(button3, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 5, 5, 0), 0, 0));
				button3.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (e.getSource()==button3) {
							textArea1.append("Stopping...\n");
							algorithm.shutdown();
						}
					}
				});
			}
			panel1.add(panel5, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 5, 13, 5), 0, 0));
		}
		contentPane.add(panel1, new GridBagConstraints(0, 0, 1, 2, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
				new Insets(10, 10, 10, 7), 0, 0));

		//======== panel6 ========
		{
			panel6.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED, Color.gray, null, null, null));
			panel6.setLayout(new GridBagLayout());
			((GridBagLayout)panel6.getLayout()).columnWidths = new int[] {0, 155, 0, 0};
			((GridBagLayout)panel6.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
			((GridBagLayout)panel6.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
			((GridBagLayout)panel6.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0, 1.0E-4};

			//---- label2 ----
			label2.setText("\u4f18\u5316\u7ed3\u679c");
			label2.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 16));
			panel6.add(label2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(5, 5, 10, 10), 0, 0));

			//---- label3 ----
			label3.setText("\u5f53\u524d\u8fed\u4ee3\u6b21\u6570\uff1a");
			label3.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
			panel6.add(label3, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(5, 5, 10, 5), 0, 0));

			//---- textField5 ----
			textField5.setEditable(false);
			panel6.add(textField5, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(5, 0, 10, 10), 0, 0));

			//---- button4 ----
			button4.setText("\u6e05\u9664\u7ed3\u679c");
			button4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
			panel6.add(button4, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(5, 0, 10, 5), 0, 0));
			button4.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (e.getSource()==button4)
						textArea1.setText("");
				}
			});
			//======== scrollPane1 ========
			{

				//---- textArea1 ----
				textArea1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
				scrollPane1.setViewportView(textArea1);
			}
			panel6.add(scrollPane1, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(5, 5, 5, 5), 0, 0));
		}
		contentPane.add(panel6, new GridBagConstraints(1, 0, 2, 2, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(10, 2, 10, 10), 0, 0));

		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	public void clear(){
		// TODO 优化
		textField1.setText(null);
		textField2.setText(null);
		textField3.setText(null);
		textField4.setText(null);
		textField5.setText(null);
		textArea1.setText(null);
	}
}
